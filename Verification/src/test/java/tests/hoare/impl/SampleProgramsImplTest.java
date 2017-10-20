package tests.hoare.impl;

import core.Lexer;
import core.Parser;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.BoolImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SampleProgramsImplTest {
    private void checkImpl(BoolExp source, BoolExp target) throws Lexer.LexerException, Parser.ParserException {
        Assert.assertEquals(new BoolImpl(source, target).reduce(), BoolExp.fromString("true").reduce());
    }

    private void checkImpl(String sourceS, String targetS) throws Lexer.LexerException, Parser.ParserException {
        checkImpl(BoolExp.fromString(sourceS), BoolExp.fromString(targetS));
    }

    @Test
    public void swap() throws Lexer.LexerException, Parser.ParserException {
        Assert.assertEquals(BoolExp.fromString("X<>x|X=x").reduce(), BoolExp.fromString("true").reduce());

        Assert.assertEquals(BoolExp.fromString("X<>x|Y<>y|Y=y&X=x").reduce(), BoolExp.fromString("X<>x|Y<>y").reduce());

        Assert.assertEquals(BoolExp.fromString("X<>x|Y<>y|Y=y&X=x").reduce(), BoolExp.fromString("true"));

        checkImpl("X=x&Y=y", "Y=y&X=x");
    }

    @Test
    public void power() throws Lexer.LexerException, Parser.ParserException {
        checkImpl("erg=2^(y-x)&~[x<>0]", "erg=2^y");
    }

    @Test
    public void complement() throws Lexer.LexerException, Parser.ParserException {
        Assert.assertEquals(new BoolImpl("x=1", "x=1").reduce(), BoolExp.fromString("true"));
        Assert.assertEquals(new BoolImpl("x=0-1", "x=1").reduce(), BoolExp.fromString("x<>0-1|x=1").reduce());
        Assert.assertEquals(new BoolImpl("x=1", "x=0-1").reduce(), BoolExp.fromString("x<>1|x=0-1").reduce());
        Assert.assertEquals(new BoolImpl("x=0-1", "x=0-1").reduce(), BoolExp.fromString("true"));

        checkImpl("x=1|x=0-1", "x=1|x=0-1");
    }

    @Test
    public void sample() throws Lexer.LexerException, Parser.ParserException {
        Assert.assertEquals(new BoolImpl("x=3", "x=0-3").reduce(), BoolExp.fromString("x<>3|x=0-3").reduce());
    }
}