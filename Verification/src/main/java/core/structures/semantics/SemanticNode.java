package core.structures.semantics;

import core.*;
import core.structures.ParserRule;
import core.structures.TNode;
import core.structures.semantics.boolExp.*;
import core.structures.semantics.exp.*;
import core.structures.semantics.prog.*;
import core.structures.syntax.SyntaxNode;
import core.structures.syntax.SyntaxNodeTerminal;
import grammars.HoareWhileGrammar;
import util.ErrorUtil;
import util.IOUtil;
import util.StringUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SemanticNode extends TNode<SemanticNode> implements Serializable {
    protected static ErrorUtil.NestedPrinter _printer = ErrorUtil.PRINTER;

    @Override
    public int hashCode() {
        return getContentString().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SemanticNode) return getContentString().equals(((SemanticNode) other).getContentString());

        if (other instanceof Integer) return equals(new ExpLit((Integer) other));

        return super.equals(other);
    }

    public <T extends SemanticNode> Set<T> findType(Class<? extends SemanticNode> type) {
        Set<T> ret = new HashSet<>();

        if (type.isInstance(this)) ret.add((T) this);

        for (SemanticNode child : getChildren()) {
            ret.addAll(child.findType(type));
        }

        return ret;
    }

    protected final static HoareWhileGrammar _grammar = HoareWhileGrammar.getInstance();

    public String getTypeName() {
        String s = getClass().getSimpleName();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            Character c = s.charAt(i);

            if (i > 0 && Character.isUpperCase(c)) sb.append("_");

            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    public abstract String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper);
    public String getContentString() {
        return getContentString(new IOUtil.BiFunc<SemanticNode, String, String>() {
            @Override
            public String apply(SemanticNode semanticNode, String s) {
                return s;
            }
        });
    }

    @Nonnull
    @Override
    public String getTreeText() {
        return !getChildren().isEmpty() ? getTypeName() : getChildName() + ": " + getContentString();
    }

    @Override
    public String toString() {
        return getContentString();
    }

    protected final List<SemanticNode> _children = new ArrayList<>();

    @Nonnull
    @Override
    public List<SemanticNode> getChildren() {
        return _children;
    }

    private String _childName;

    public String getChildName() {
        return _childName;
    }

    protected void addChild(@Nonnull SemanticNode child) {
        addChild(child, null);
    }
    protected void addChild(@Nonnull SemanticNode child, String label) {
        addChild(_children.size(), child, label);
    }
    protected void addChild(int pos, @Nonnull SemanticNode child) {
        addChild(pos, child, null);
    }
    protected void addChild(int pos, @Nonnull SemanticNode child, String label) {
        _children.add(pos, child);
        child._childName = label;
    }

    @CheckReturnValue
    @Nonnull
    public abstract SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc);

    private SyntaxNode _syntax = null;

    @Nonnull
    public List<Token> tokenize() {
        return (_syntax != null) ? _syntax.tokenize(false) : new ArrayList<>();
    }

    public String synthesize() {
        return (_syntax != null) ? _syntax.synthesize() : "";
    }

    public String synthesize(boolean includeWrappingSeps, boolean flatten, IOUtil.Func<Token, String> tokenMapper) {
        return (_syntax != null) ? _syntax.synthesize(includeWrappingSeps, flatten, tokenMapper) : "";
    }

    private void print(PrintStream stream, int nestDepth) {
        stream.println(StringUtil.repeat("\t", nestDepth) + this);

        for (SemanticNode child : getChildren()) {
            child.print(stream, nestDepth + 1);
        }
    }

    public void print(PrintStream stream) {
        print(stream, 0);
    }

    private static SemanticNode transform(@Nonnull SyntaxNode syntaxNode) {
        SemanticNode newNode = null;

        Symbol symbol = syntaxNode.getSymbol();
        ParserRule subRule = syntaxNode.getSubRule();

        if (symbol.equals(_grammar.NON_TERMINAL_BOOL_EXP) && subRule.equals(_grammar.RULE_BOOL_OR)) {
            SyntaxNode orSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_BOOL_OR);

            if (orSyntaxNode != null) newNode = transform(orSyntaxNode);
        } else if (symbol.equals(_grammar.NON_TERMINAL_BOOL_OR) || symbol.equals(_grammar.NON_TERMINAL_BOOL_OR_)) {
            if (subRule.equals(_grammar.RULE_BOOL_AND_BOOL_OR_) || subRule.equals(_grammar.RULE_OP_OR_BOOL_AND_BOOL_OR_)) {
                BoolOr boolOrNode = new BoolOr();

                SyntaxNode andSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_BOOL_AND);

                if ((andSyntaxNode != null)) {
                    SemanticNode andNode = transform(andSyntaxNode);

                    if (andNode != null) boolOrNode.addBoolExp((BoolExp) andNode);
                }

                SyntaxNode or_SyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_BOOL_OR_);

                if (or_SyntaxNode != null) {
                    SemanticNode or_Node = transform(or_SyntaxNode);

                    if (or_Node != null) boolOrNode.addBoolExp((BoolExp) or_Node);
                }

                if (boolOrNode.getChildren().size() > 1) {
                    newNode = boolOrNode;
                } else if (!boolOrNode.getChildren().isEmpty()) {
                    newNode = boolOrNode.getChildren().get(0);
                }
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_BOOL_AND) || symbol.equals(_grammar.NON_TERMINAL_BOOL_AND_)) {
            if (subRule.equals(_grammar.RULE_BOOL_NEG_BOOL_AND_) || subRule.equals(_grammar.RULE_OP_AND_BOOL_NEG_BOOL_AND_)) {
                BoolAnd boolAndNode = new BoolAnd();

                SyntaxNode negSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_BOOL_NEG);

                if (negSyntaxNode != null) {
                    SemanticNode negNode = transform(negSyntaxNode);

                    if (negNode != null) boolAndNode.addBoolExp((BoolExp) negNode);
                }

                SyntaxNode and_SyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_BOOL_AND_);

                if (and_SyntaxNode != null) {
                    SemanticNode and_Node = transform(and_SyntaxNode);

                    if (and_Node != null) boolAndNode.addBoolExp((BoolExp) and_Node);
                }

                if (boolAndNode.getChildren().size() > 1) {
                    newNode = boolAndNode;
                } else if (!boolAndNode.getChildren().isEmpty()) {
                    newNode = boolAndNode.getChildren().get(0);
                }
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_BOOL_NEG)) {
            if (subRule.equals(_grammar.RULE_NEG_BOOL_ELEM)) {
                SyntaxNode elemSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_BOOL_ELEM);

                if (elemSyntaxNode != null) newNode = new BoolNeg((BoolExp) transform(elemSyntaxNode));
            } else if (subRule.equals(_grammar.RULE_BOOL_ELEM)) {
                SyntaxNode elemSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_BOOL_ELEM);

                if (elemSyntaxNode != null) newNode = transform(elemSyntaxNode);
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_BOOL_ELEM)) {
            if (subRule.equals(_grammar.RULE_BOOL_LIT)) {
                SyntaxNode boolLitSyntaxNode = syntaxNode.findChild(_grammar.TERMINAL_BOOL_LIT);

                if (boolLitSyntaxNode != null) newNode = transform(boolLitSyntaxNode);
            } else if (subRule.equals(_grammar.RULE_EXP_OP_COMP_EXP)) {
                SyntaxNode leftExpSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_EXP, 0);
                SyntaxNode expCompOpSyntaxNode = syntaxNode.findChild(_grammar.TERMINAL_OP_COMP);
                SyntaxNode rightExpSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_EXP, 1);

                if ((leftExpSyntaxNode != null) && (expCompOpSyntaxNode != null) && (rightExpSyntaxNode != null)) {
                    Exp leftExpNode = (Exp) transform(leftExpSyntaxNode);
                    ExpCompOp expCompOpNode = (ExpCompOp) transform(expCompOpSyntaxNode);
                    Exp rightExpNode = (Exp) transform(rightExpSyntaxNode);

                    newNode = new ExpComp(leftExpNode, expCompOpNode, rightExpNode);
                }
            } else if (subRule.equals(_grammar.RULE_PAREN_BOOL_EXP)) {
                SyntaxNode boolExpSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_BOOL_EXP);

                if (boolExpSyntaxNode != null) newNode = transform(boolExpSyntaxNode);
            }
        } else if (symbol.equals(_grammar.TERMINAL_BOOL_LIT)) {
            newNode = new BoolLit((SyntaxNodeTerminal) syntaxNode);
        } else if (symbol.equals(_grammar.TERMINAL_OP_COMP)) {
            newNode = new ExpCompOp((SyntaxNodeTerminal) syntaxNode);
        }

        if (symbol.equals(_grammar.NON_TERMINAL_EXP)) {
            newNode = transform(syntaxNode.findChild(_grammar.NON_TERMINAL_SUM));
        } else if (symbol.equals(_grammar.NON_TERMINAL_SUM) || symbol.equals(_grammar.NON_TERMINAL_SUM_)) {
            if (subRule.equals(_grammar.RULE_PROD_SUM_) || subRule.equals(_grammar.RULE_OP_PLUS_PROD_SUM_) || subRule.equals(_grammar.RULE_OP_MINUS_PROD_SUM_)) {
                boolean positive = subRule.equals(_grammar.RULE_PROD_SUM_) || subRule.equals(_grammar.RULE_OP_PLUS_PROD_SUM_);

                Sum sumNode = new Sum();

                SyntaxNode prodSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_PROD);

                if (prodSyntaxNode != null) {
                    Exp prodNode = (Exp) transform(prodSyntaxNode);

                    if (prodNode != null) {
                        Exp exp = prodNode;

                        if (!positive) exp = exp.makeNeg();

                        sumNode.addExp(exp);
                    }
                }

                SyntaxNode sum_SyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_SUM_);

                if (sum_SyntaxNode != null) {
                    Exp sum_Node = (Exp) transform(sum_SyntaxNode);

                    if (sum_Node != null) sumNode.addExp(sum_Node);
                }

                if (sumNode.getExps().size() > 1) {
                    newNode = sumNode;
                } else if (!sumNode.getExps().isEmpty()) {
                    newNode = sumNode.getExps().get(0);
                }
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_PROD) || symbol.equals(_grammar.NON_TERMINAL_PROD_)) {
            if (subRule.equals(_grammar.RULE_POW_PROD_) || subRule.equals(_grammar.RULE_OP_MULT_POW_PROD_) || subRule.equals(_grammar.RULE_OP_DIV_POW_PROD_)) {
                boolean positive = subRule.equals(_grammar.RULE_POW_PROD_) || subRule.equals(_grammar.RULE_OP_MULT_POW_PROD_);

                Prod prodNode = new Prod();

                SyntaxNode powSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_POW);

                if (powSyntaxNode != null) {
                    Exp powNode = (Exp) transform(powSyntaxNode);

                    if (powNode != null) {
                        Exp exp = powNode;

                        if (!positive) exp = powNode.makeInv();

                        prodNode.addExp(exp);
                    }
                }

                SyntaxNode prod_SyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_PROD_);

                if (prod_SyntaxNode != null) {
                    Exp prod_Node = (Exp) transform(prod_SyntaxNode);

                    if (prod_Node != null) prodNode.addExp(prod_Node);
                }

                if (prodNode.getExps().size() > 1) {
                    newNode = prodNode;
                } else if (!prodNode.getExps().isEmpty()) {
                    newNode = prodNode.getExps().get(0);
                }
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_POW) && subRule.equals(_grammar.RULE_FACT_POW_)) {
            SyntaxNode factSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_FACT);

            if (factSyntaxNode != null) {
                Exp factNode = (Exp) transform(factSyntaxNode);

                SyntaxNode pow_SyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_POW_);

                if (pow_SyntaxNode != null) {
                    Exp pow_Node = (Exp) transform(pow_SyntaxNode);

                    if (pow_Node != null) newNode = new Pow(factNode, pow_Node); else newNode = factNode;
                } else {
                    newNode = factNode;
                }
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_POW_) && subRule.equals(_grammar.RULE_OP_POW_POW)) {
            SyntaxNode powSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_POW);

            if (powSyntaxNode != null) newNode = transform(powSyntaxNode);
        } else if (symbol.equals(_grammar.NON_TERMINAL_FACT) && subRule.equals(_grammar.RULE_EXP_ELEM_FACT_)) {
            SyntaxNode expElemSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_EXP_ELEM);

            if (expElemSyntaxNode != null) {
                SyntaxNode fact_SyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_FACT_);

                Exp exp = (Exp) transform(expElemSyntaxNode);

                while (fact_SyntaxNode != null && fact_SyntaxNode.getSubRule().equals(_grammar.RULE_OP_FACT_FACT_)) {
                    fact_SyntaxNode = fact_SyntaxNode.findChild(_grammar.NON_TERMINAL_FACT_);

                    exp = new Fact(exp);
                }

                newNode = exp;
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_EXP_ELEM)) {
            if (subRule.equals(_grammar.RULE_ID_PARAM_LIST)) {
                SyntaxNode idSyntaxNode = syntaxNode.findChild(_grammar.TERMINAL_ID);

                SyntaxNode paramListSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_PARAM_LIST);

                Id id = (Id) transform(idSyntaxNode);

                if (id != null) {
                    ParamList paramList = (ParamList) transform(paramListSyntaxNode);

                    if (paramList != null) {
                        if (id.getName().equals("fact")) id = new FactId(paramList);
                        if (id.getName().equals("gcd")) id = new GcdId(paramList);
                    }

                    newNode = id;
                }
            } else if (subRule.equals(_grammar.RULE_EXP_LIT)) {
                SyntaxNode numSyntaxNode = syntaxNode.findChild(_grammar.TERMINAL_EXP_LIT);

                newNode = transform(numSyntaxNode);
            } else if (subRule.equals(_grammar.RULE_PARENS_EXP)) {
                SyntaxNode expSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_EXP);

                newNode = transform(expSyntaxNode);
            }
        } else if (symbol.equals(_grammar.TERMINAL_ID)) {
            newNode = new Id(((SyntaxNodeTerminal) syntaxNode).getToken().getText());
        } else if (symbol.equals(_grammar.TERMINAL_EXP_LIT)) {
            newNode = new ExpLit(new BigInteger(((SyntaxNodeTerminal) syntaxNode).getToken().getText()), BigInteger.ONE);
        } else if (symbol.equals(_grammar.NON_TERMINAL_PARAM_LIST) && subRule.equals(_grammar.RULE_PARENS_PARAM_PARAM_LIST_)) {
            SyntaxNode paramSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_PARAM);

            if (paramSyntaxNode != null) {
                ParamList paramListNode = new ParamList();

                SemanticNode paramNode = transform(paramSyntaxNode);

                if (paramNode != null) paramListNode.addParam((Exp) paramNode);

                SemanticNode paramList_Node = transform(syntaxNode.findChild(_grammar.NON_TERMINAL_PARAM_LIST_));

                if (paramList_Node != null) paramListNode.addParamList((ParamList) paramList_Node);

                newNode = paramListNode;
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_PARAM_LIST_) && subRule.equals(_grammar.RULE_PARAM_SEP_PARAM_PARAM_LIST_)) {
            SyntaxNode paramSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_PARAM);

            if (paramSyntaxNode != null) {
                ParamList paramListNode = new ParamList();

                SemanticNode paramNode = transform(paramSyntaxNode);

                if (paramNode != null) paramListNode.addParam((Exp) paramNode);

                SyntaxNode paramList_SyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_PARAM_LIST_);

                if (paramList_SyntaxNode != null) {
                    SemanticNode paramList_Node = transform(paramList_SyntaxNode);

                    if (paramList_Node != null) paramListNode.addParamList((ParamList) paramList_Node);
                }

                newNode = paramListNode;
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_PARAM) && subRule.equals(_grammar.RULE_EXP)) {
            newNode = transform(syntaxNode.findChild(_grammar.NON_TERMINAL_EXP));
        }

        if (symbol.equals(_grammar.NON_TERMINAL_PROG) || symbol.equals(_grammar.NON_TERMINAL_PROG_)) {
            if (subRule.equals(_grammar.RULE_PROG_CMD_PROG_) || subRule.equals(_grammar.RULE_PROG__SEP_CMD_PROG_)) {
                SyntaxNode cmdSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_CMD);

                Comp comp = new Comp();

                if (cmdSyntaxNode != null) {
                    SemanticNode cmdNode = transform(cmdSyntaxNode);

                    if (cmdNode != null) comp.addProg((Prog) cmdNode);
                }

                SyntaxNode prog_SyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_PROG_);

                if (prog_SyntaxNode != null) {
                    SemanticNode prog_Node = transform(prog_SyntaxNode);

                    if (prog_Node != null) comp.addProg((Prog) prog_Node);
                }

                if (comp.getChildren().size() > 1) {
                    newNode = comp;
                } else if (!comp.getChildren().isEmpty()) {
                    newNode = comp.getChildren().get(0);
                }
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_CMD)) {
            if (subRule.equals(_grammar.RULE_CMD_SKIP)) {
                SyntaxNode skipNode = syntaxNode.findChild(_grammar.NON_TERMINAL_SKIP);

                if (skipNode != null) newNode = transform(skipNode);
            } else if (subRule.equals(_grammar.RULE_CMD_ASSIGN)) {
                SyntaxNode assignNode = syntaxNode.findChild(_grammar.NON_TERMINAL_ASSIGN);

                if (assignNode != null) newNode = transform(assignNode);
            } else if (subRule.equals(_grammar.RULE_CMD_ALT)) {
                SyntaxNode altNode = syntaxNode.findChild(_grammar.NON_TERMINAL_ALT);

                if (altNode != null) newNode = transform(altNode);
            } else if (subRule.equals(_grammar.RULE_CMD_WHILE)) {
                SyntaxNode whileNode = syntaxNode.findChild(_grammar.NON_TERMINAL_WHILE);

                if (whileNode != null) newNode = transform(whileNode);
            } else if (subRule.equals(_grammar.RULE_HOARE_BLOCK)) {
                SyntaxNode hoare_blockSyntayNode = syntaxNode.findChild(_grammar.NON_TERMINAL_HOARE_BLOCK);

                if (hoare_blockSyntayNode != null) newNode = transform(hoare_blockSyntayNode);
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_SKIP) && subRule.equals(_grammar.RULE_SKIP)) {
            newNode = new Skip();
        } else if (symbol.equals(_grammar.NON_TERMINAL_ASSIGN) && subRule.equals(_grammar.RULE_ASSIGN)) {
            SyntaxNode idSyntaxNode = syntaxNode.findChild(_grammar.TERMINAL_ID);
            SyntaxNode expSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_EXP);

            if ((idSyntaxNode != null) && (expSyntaxNode != null)) {
                SemanticNode idNode = transform(idSyntaxNode);
                SemanticNode expNode = transform(expSyntaxNode);

                newNode = new Assign((Id) idNode, (Exp) expNode);
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_ALT) && subRule.equals(_grammar.RULE_ALT)) {
            SyntaxNode boolExpSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_BOOL_EXP);
            SyntaxNode thenProgSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_PROG);
            SyntaxNode alt_elseSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_ALT_ELSE);

            if ((boolExpSyntaxNode != null) && (thenProgSyntaxNode != null) && (alt_elseSyntaxNode != null)) {
                SemanticNode boolExpNode = transform(boolExpSyntaxNode);
                SemanticNode thenProgNode = transform(thenProgSyntaxNode);
                SemanticNode alt_elseNode = transform(alt_elseSyntaxNode);

                newNode = new Alt((BoolExp) boolExpNode, (Prog) thenProgNode, (Prog) alt_elseNode);
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_ALT_ELSE) && subRule.equals(_grammar.RULE_ALT_ELSE)) {
            SyntaxNode progSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_PROG);

            if (progSyntaxNode != null) {
                SemanticNode progNode = transform(progSyntaxNode);

                if (progNode != null) newNode = progNode;
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_WHILE) && subRule.equals(_grammar.RULE_WHILE)) {
            SyntaxNode boolExpSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_BOOL_EXP);
            SyntaxNode progSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_PROG);

            if ((boolExpSyntaxNode != null) && (progSyntaxNode != null)) {
                SemanticNode boolExpNode = transform(boolExpSyntaxNode);
                SemanticNode progNode = transform(progSyntaxNode);

                newNode = new While((BoolExp) boolExpNode, (Prog) progNode);
            }
        }

        if (symbol.equals(_grammar.NON_TERMINAL_HOARE_BLOCK) && subRule.equals(_grammar.RULE_HOARE_PRE_PROG_HOARE_POST)) {
            SyntaxNode hoare_preSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_HOARE_PRE);
            SyntaxNode progSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_PROG);
            SyntaxNode hoare_postSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_HOARE_POST);

            if ((hoare_preSyntaxNode != null) && (progSyntaxNode != null) && (hoare_postSyntaxNode != null)) {
                SemanticNode hoare_preNode = transform(hoare_preSyntaxNode);
                SemanticNode progNode = transform(progSyntaxNode);
                SemanticNode hoare_postNode = transform(hoare_postSyntaxNode);

                newNode = new HoareBlock((HoareCond) hoare_preNode, (Prog) progNode, (HoareCond) hoare_postNode);
            }
        } else if (symbol.equals(_grammar.NON_TERMINAL_HOARE_PRE) || symbol.equals(_grammar.NON_TERMINAL_HOARE_POST)) {
            if (subRule.equals(_grammar.RULE_HOARE_PRE_CURLIES_BOOL_EXP_CURLY_CLOSE) || subRule.equals(_grammar.RULE_HOARE_POST_CURLY_OPEN_BOOL_EXP_CURLY_CLOSE)) {
                SyntaxNode boolExpSyntaxNode = syntaxNode.findChild(_grammar.NON_TERMINAL_BOOL_EXP);

                if (boolExpSyntaxNode != null) {
                    SemanticNode boolExpNode = transform(boolExpSyntaxNode);

                    newNode = new HoareCond((BoolExp) boolExpNode);
                }
            }
        }

        if (newNode != null) newNode._syntax = syntaxNode;

        return newNode;
    }

    @Nullable
    public SyntaxNode getSyntax() {
        return _syntax;
    }

    public class CopyException extends RuntimeException {
        public CopyException(@Nonnull Exception e) {
            super(e);
        }
    }

    public final @Nonnull SemanticNode copy() {
        try {
            return (SemanticNode) IOUtil.deepCopy(this);
        } catch (Exception e) {
            throw new CopyException(e);
        }
    }

    public static @Nonnull SemanticNode fromSyntax(@Nonnull SyntaxNode node) {
        return transform(node);
    }

    public static @Nonnull SemanticNode fromString(@Nonnull String s, @Nonnull Grammar grammar) throws Lexer.LexerException, Parser.ParserException {
        Lexer lexer = new Lexer(grammar);

        Lexer.LexerResult lexerResult = lexer.tokenize(s);

        Parser parser = new Parser(grammar);

        SyntaxNode syntaxTree = parser.parse(lexerResult.getTokens());

        return fromSyntax(syntaxTree);
    }
}