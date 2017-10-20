package core.structures.semantics.exp;

import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import grammars.ExpGrammar;

import java.util.ArrayList;
import java.util.List;

public abstract class Exp extends SemanticNode {
    public Exp makeNeg() {
        Prod ret = new Prod(this);

        ret.neg();

        return ret;
    }

    public Exp makeInv() {
        Pow ret = new Pow(this, new ExpLit(-1));

        return ret;
    }

    public abstract Exp reduce();
    public abstract void order();

    public abstract int comp(Exp b);

    public int compPrecedence(Exp b) {
        List<Class<? extends Exp>> types = new ArrayList<>();

        types.add(ExpMu.class);
        types.add(ExpLit.class);
        types.add(Id.class);
        types.add(Sum.class);
        types.add(Prod.class);
        types.add(Pow.class);
        types.add(Fact.class);

        if (types.indexOf(getClass()) < types.indexOf(b.getClass())) return -1;
        if (types.indexOf(getClass()) > types.indexOf(b.getClass())) return 1;

        return comp(b);
    }

    public String parenthesize(String s) {
        return _grammar.TERMINAL_PAREN_OPEN.getPrimRule() + s + _grammar.TERMINAL_PAREN_CLOSE.getPrimRule();
    }

    public static Exp fromString(String s) throws Lexer.LexerException, Parser.ParserException {
        return (Exp) SemanticNode.fromString(s, ExpGrammar.getInstance());
    }
}