package tests.hoare;

import core.Grammar;
import core.Lexer;
import core.Parser;
import core.Token;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.*;
import core.structures.semantics.exp.*;
import core.structures.syntax.SyntaxNode;
import grammars.BoolExpGrammar;
import grammars.HoareWhileGrammar;
import org.junit.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class ImplTest {
    private Grammar g = new BoolExpGrammar();

    private BoolOr reduce(BoolExp boolExp) {
        boolExp = boolExp.reduce();

        BoolOr dnf = boolExp.makeDNF();
        BoolOr newOr = new BoolOr();

        for (BoolExp part : dnf.getBoolExps()) {
            //each part is an And at best

            BoolAnd partAnd = new BoolAnd(part);

            part = partAnd.reduce_CNF();

            newOr.addBoolExp(part);
        }

        return newOr;
    }

    private boolean checkInclusion(BoolExp left, BoolExp right) {
        BoolAnd leftAnd = new BoolAnd(left);
        BoolAnd rightAnd = new BoolAnd(right);

        for (BoolExp rightPart : rightAnd.getBoolExps()) {
            if (!leftAnd.getBoolExps().contains(rightPart)) return false;
        }

        return true;
    }

    private void checkTrue(BoolExp left, BoolExp right) {
        BoolOr leftOr = reduce(left);
        BoolOr rightOr = reduce(right);

        for (BoolExp leftOrPart : leftOr.getBoolExps()) {
            boolean found = false;

            for (BoolExp rightOrPart : rightOr.getBoolExps()) {
                boolean check = checkInclusion(leftOrPart, rightOrPart);

                System.out.println("check " + leftOrPart + " against " + rightOrPart + "->" + check);
                if (check) {
                    found = true;

                    break;
                }
            }

            Assert.assertTrue(found);
        }
    }

    private void checkTrue(String leftS, String rightS) throws Lexer.LexerException, Parser.ParserException {
        BoolExp left = (BoolExp) SemanticNode.fromString(leftS, g);
        BoolExp right = (BoolExp) SemanticNode.fromString(rightS, g);

        checkTrue(left, right);
    }

    @Test()
    public void a() throws Lexer.LexerException, Parser.ParserException {
        checkTrue("erg=2^(y-x)&x>=0", "erg=2^y");
    }

    /*@Test()
    public void b() {
        checkTrue("x=y&y=0", "x=0");
    }

    @Test()
    public void c() {
        checkTrue("x<0&y=1", "x+y<1");
    }

    @Test()
    public void d() {
        checkTrue("a=b-c", "1=2^(a-b+c)");
    }

    @Test()
    public void e() {
        checkTrue("x^2=9", "x=3|x=-3");
    }

    @Test()
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