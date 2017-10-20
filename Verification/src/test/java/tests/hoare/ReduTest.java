package tests.hoare;

import core.Lexer;
import core.Parser;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.BoolImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ReduTest {
    /*@Test()
    public void test() throws Lexer.LexerException, Parser.ParserException {
        BoolExp exp = (BoolExp) SemanticNode.fromString("x^2+4*x-5=0", BoolExpGrammar.getInstance());

        exp = exp.reduce();
        exp.order();

        System.out.println("exp " + exp);

        BoolExp compExp = (BoolExp) SemanticNode.fromString("x=0-5|x=1", BoolExpGrammar.getInstance());

        compExp = compExp.reduce();
        compExp.order();

        System.out.println("compExp " + exp);

        Assert.assertEquals(exp, compExp);
    }

    @Test()
    public void complement() throws Lexer.LexerException, Parser.ParserException {
        BoolExp exp = BoolExp.fromString("x=1|x<>1").reduce();

        Assert.assertEquals(exp, BoolExp.fromString("true"));

        BoolExp exp2 = BoolExp.fromString("~[x=1&~x=1]").reduce();

        Assert.assertEquals(exp2, BoolExp.fromString("true"));
    }

    @Test()
    public void idempotency() throws Lexer.LexerException, Parser.ParserException {
        BoolExp exp = BoolExp.fromString("x=1|x=1").reduce();

        Assert.assertEquals(exp, BoolExp.fromString("x=1").reduce());

        BoolExp exp2 = BoolExp.fromString("x=1&x=1").reduce();

        Assert.assertEquals(exp2, BoolExp.fromString("x=1").reduce());
    }

    @Test()
    public void implFalse() throws Lexer.LexerException, Parser.ParserException {
        BoolExp exp = new BoolImpl(BoolExp.fromString("A=1"), new BoolLit(false)).reduce();

        Assert.assertEquals(exp, BoolExp.fromString("~A=1").reduce());
    }*/

    @Test()
    public void absorption() throws Lexer.LexerException, Parser.ParserException {
        Assert.assertEquals(BoolExp.fromString("x<1|x>1|x<>1").reduce(), BoolExp.fromString("x<>1").reduce());
        Assert.assertEquals(BoolExp.fromString("x>1|x=1").reduce(), BoolExp.fromString("x>=1").reduce());
        Assert.assertEquals(BoolExp.fromString("x<1|x=1").reduce(), BoolExp.fromString("x<=1").reduce());
    }

    @Test()
    public void substitute() throws Lexer.LexerException, Parser.ParserException {
        Assert.assertEquals(BoolExp.fromString("~[x<>0]").reduce(), BoolExp.fromString("x=0").reduce());
        Assert.assertEquals(BoolExp.fromString("erg=2^(y-x)&~[x<>0]").reduce(), BoolExp.fromString("erg=2^y&x=0").reduce());
        Assert.assertEquals(BoolExp.fromString("x=1&x=y").reduce(), BoolExp.fromString("x=1&y=1").reduce());
    }

    @Test()
    public void contraposition() throws Lexer.LexerException, Parser.ParserException {
        BoolExp exp = new BoolImpl(BoolExp.fromString("A=1"), BoolExp.fromString("B=1")).reduce();

        Assert.assertEquals(exp, new BoolImpl(BoolExp.fromString("~A=1"), BoolExp.fromString("~B=1")).reduce());
    }

    /*@Test()
    public void deMorgan() throws Lexer.LexerException, Parser.ParserException {
        Assert.assertEquals(BoolExp.fromString("~[A=1&B=1]").reduce(), BoolExp.fromString("~A=1|~B=1").reduce());
        Assert.assertEquals(BoolExp.fromString("~[A=1&B=1]").reduce(), BoolExp.fromString("~A=1|~B=1").reduce());
    }

    @Test()
    public void misc() throws Lexer.LexerException, Parser.ParserException {
        Assert.assertEquals(BoolExp.fromString("A=1&true").reduce(), BoolExp.fromString("A=1").reduce());
        Assert.assertEquals(BoolExp.fromString("A=1|false").reduce(), BoolExp.fromString("A=1").reduce());
        Assert.assertEquals(BoolExp.fromString("A=1|true").reduce(), BoolExp.fromString("true").reduce());
        Assert.assertEquals(BoolExp.fromString("~[A=1&false]").reduce(), BoolExp.fromString("true").reduce());
        Assert.assertEquals(BoolExp.fromString("~[~A=1]").reduce(), BoolExp.fromString("A=1").reduce());
    }*/
}