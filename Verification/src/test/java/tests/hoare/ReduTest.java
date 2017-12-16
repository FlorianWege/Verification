package tests.hoare;

import core.Lexer;
import core.Parser;
import core.structures.semantics.boolExp.*;
import core.structures.semantics.exp.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ReduTest {
    @Test()
    public void test() throws Lexer.LexerException, Parser.ParserException {
        BoolExp exp = BoolExp.fromString("x^2+4*x-5=0");

        exp = exp.reduce();
        exp = exp.order();

        BoolExp compExp = BoolExp.fromString("x=0-5|x=1");

        compExp = compExp.reduce();
        compExp = compExp.order();

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

        BoolExp exp3 = BoolExp.fromString("x<2|x<2").reduceEx().getRet();

        Assert.assertEquals(exp3, BoolExp.fromString("x<2").reduce());
    }

    @Test()
    public void implFalse() throws Lexer.LexerException, Parser.ParserException {
        BoolExp exp = new BoolImpl(BoolExp.fromString("A=1"), new BoolLit(false)).reduce();

        Assert.assertEquals(exp, BoolExp.fromString("~A=1").reduce());
    }

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
    public void a() throws Lexer.LexerException, Parser.ParserException {
        BoolExp boolExp = new BoolImpl(BoolExp.fromString("B-1<0"), BoolExp.fromString("A-1<0")).reduce();
    }

    @Test()
    public void doubleNeg() throws Lexer.LexerException, Parser.ParserException {
        BoolExp exp = BoolExp.fromString("~[~B=1]").reduce();

        Assert.assertEquals(exp, BoolExp.fromString("B=1"));
    }

    @Test()
    public void contraposition() throws Lexer.LexerException, Parser.ParserException {
        BoolExp exp = new BoolImpl(BoolExp.fromString("A=1"), BoolExp.fromString("B=1")).reduce();

        Assert.assertEquals(exp, new BoolImpl(BoolExp.fromString("~B=1"), BoolExp.fromString("~A=1")).reduce());
    }

    @Test()
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
    }

    @Test()
    public void mu() {
        Assert.assertEquals(new ExpComp(new ExpMu(), new ExpCompOp(ExpCompOp.Type.EQUAL), new ExpLit(0)).reduce(), new BoolLit(false));
        Assert.assertEquals(new ExpComp(new Prod(new ExpMu(), new ExpLit(4)), new ExpCompOp(ExpCompOp.Type.EQUAL), new ExpLit(0)).reduce(), new BoolLit(false));
        Assert.assertNotEquals(new ExpComp(new Prod(new ExpMu(), new Id("a")), new ExpCompOp(ExpCompOp.Type.EQUAL), new ExpLit(0)).reduce(), new BoolLit(false));
    }

    @Test()
    public void substitution() throws Lexer.LexerException, Parser.ParserException {
        Assert.assertEquals(BoolExp.fromString("n=k & n=0").reduce(), BoolExp.fromString("n=0 & k=0").reduce());
        Assert.assertEquals(BoolExp.fromString("n=k & k=0").reduce(), BoolExp.fromString("n=0 & k=0").reduce());
    }

    @Test()
    public void factorial() throws Lexer.LexerException, Parser.ParserException {
        Exp exp = Exp.fromString("(k+1)!").reduce();
        //Assert.assertEquals(BoolExp.fromString("k+1=(k+1)!").reduce(), BoolExp.fromString("1=k!").reduce());
        Assert.assertEquals(Exp.fromString("fact(a)").reduce(), Exp.fromString("a!").reduce());
    }
}