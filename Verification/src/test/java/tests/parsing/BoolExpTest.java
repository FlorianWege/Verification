package tests.parsing;

import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.syntax.SyntaxNode;
import grammars.BoolExpGrammar;
import org.testng.annotations.Test;

public class BoolExpTest {
    protected final static BoolExpGrammar g = new BoolExpGrammar();

    @Test()
    public void test() throws Lexer.LexerException, Parser.ParserException {
        SyntaxNode tree = new Parser(g).parse("B=0&B=1");

        SemanticNode node = SemanticNode.fromSyntax(tree);

        BoolExp red = ((BoolExp) node).reduce();

        red = red.order();

        System.out.println(red.getContentString());
    }
}