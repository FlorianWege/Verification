package tests.hoare.impl;

import core.Grammar;
import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.*;
import grammars.BoolExpGrammar;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ImplTest {
    private Grammar g = new BoolExpGrammar();

    private BoolExp checkInclusion(BoolExp left, BoolExp right) {
        BoolAnd leftAnd = new BoolAnd(left);
        BoolAnd rightAnd = new BoolAnd(right);

        BoolAnd newRightAnd = new BoolAnd();

        for (BoolExp rightPart : rightAnd.getBoolExps()) {
            if (!leftAnd.getBoolExps().contains(rightPart)) {
                newRightAnd.addBoolExp(rightPart);
            }
        }

        BoolExp newRight = newRightAnd.reduce();

        System.out.println("reduced inclusion " + newRight);

        return new BoolImpl(leftAnd, newRight);
    }

    private void checkTrue(BoolImpl impl) {
        BoolExp source = impl.getSource();
        BoolExp target = impl.getTarget();

        System.out.println("---reduced");
        source = source.reduce();
        target = target.reduce();

        System.out.println("left " + source);
        System.out.println("right " + target);

        System.out.println("---DNF");
        source = source.makeDNF();
        target = target.makeDNF();

        System.out.println("left " + source);
        System.out.println("right " + target);

        System.out.println("---impl");
        impl = new BoolImpl(source, target);

        System.out.println("impl " + impl);

        System.out.println("---split");
        BoolExp split = impl.split(true, false);

        System.out.println("split " + split);

        BoolOr splitOr = new BoolOr(split);

        BoolOr newOr = new BoolOr();

        for (BoolExp splitOrPart : splitOr.getBoolExps()) {
            BoolAnd partAnd = new BoolAnd(splitOrPart);

            BoolAnd newAnd = new BoolAnd();

            for (BoolExp part : partAnd.getBoolExps()) {
                System.out.println("before reduce " + part);

                part = part.reduce();

                /*assert (part instanceof BoolImpl);

                System.out.println("check " + ((BoolImpl) part).getSource() + " against " + ((BoolImpl) part).getTarget());

                BoolExp newImpl = checkInclusion(((BoolImpl) part).getSource(), ((BoolImpl) part).getTarget());

                System.out.println("left over " + newImpl);

                newImpl = newImpl.reduce();

                System.out.println("left over reduced " + newImpl);

                newAnd.addBoolExp(newImpl);*/
                newAnd.addBoolExp(part);
            }

            newOr.addBoolExp(newAnd);
        }

        BoolExp finalBoolExp = newOr.reduce();

        System.out.println("end " + finalBoolExp);

        Assert.assertTrue(finalBoolExp.equals(new BoolLit(true)));
    }

    private void checkTrue(BoolExp left, BoolExp right) {
        checkTrue(new BoolImpl(left, right));
    }

    private void checkTrue(String leftS, String rightS) throws Lexer.LexerException, Parser.ParserException {
        BoolExp left = (BoolExp) SemanticNode.fromString(leftS, g);
        BoolExp right = (BoolExp) SemanticNode.fromString(rightS, g);

        checkTrue(left, right);
    }

    @Test()
    public void a() throws Lexer.LexerException, Parser.ParserException {
        checkTrue("erg=2^(y-x)&x=0", "erg=2^y&x=0");
    }

    @Test()
    public void b() throws Lexer.LexerException, Parser.ParserException {
        checkTrue("x=y&y=0", "x=0");
    }

    @Test()
    public void c() throws Lexer.LexerException, Parser.ParserException {
        checkTrue("x<0&y=1", "x+y<1");
    }

    @Test()
    public void d() throws Lexer.LexerException, Parser.ParserException {
        checkTrue("a=b-c", "1=2^(a-b+c)");
    }

    @Test()
    public void e() throws Lexer.LexerException, Parser.ParserException {
        //checkTrue("A=1|B=2", "C=3|D=4");
        checkTrue("x=3|x=0-3", "x=3|x=0-3");
        //checkTrue("x^2=9", "x=3|x=0-3");
    }

    /*@Test()
    public void f() {
        checkTrue("x=3|x=-3", "x^2=9");
    }

    @Test()
    public void g() {
        checkTrue("x=2|x=4", "x=4");
    }

    @Test()
    public void test() throws Lexer.LexerException, Parser.ParserException {
        String leftS = "erg=2^y";
        String rightS = "erg=2^(y-x)&x=0";

        //parse
        BoolExp left = (BoolExp) SemanticNode.fromString(leftS, g);
        BoolExp right = (BoolExp) SemanticNode.fromString(rightS, g);

        BoolOr rightDNF = right.makeDNF();

        System.out.println(findIds(rightDNF));

        for (BoolExp rightPart : rightDNF.getBoolExps()) {
            check(left, rightPart);
        }

//        BoolImpl._reduceStrat = BoolImpl.ReduceStrat.REDUCE_MORPH;
//
//        BoolExp ret = new BoolImpl(left, right).reduce();
//
//        System.out.println("split " + ret.getContentString());
//
//        BoolImpl._reduceStrat = BoolImpl.ReduceStrat.REDUCE_MORPH;
//
//        ret = ret.reduce();
//
//        System.out.println("morph " + ret.getContentString());
//
//        System.out.println(((BoolExp) SemanticNode.fromString("~[B=2&C=3]", g)).reduce().getContentString());
    }*/
}