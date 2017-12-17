package tests.parsing;

import core.Lexer;
import core.Parser;
import core.structures.semantics.boolExp.BoolAnd;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.ExpComp;
import core.structures.semantics.boolExp.ExpCompOp;
import core.structures.semantics.exp.ExpLit;
import core.structures.semantics.exp.Id;
import grammars.BoolExpGrammar;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BoolExpTest {
    protected final static BoolExpGrammar g = new BoolExpGrammar();

    @Test()
    public void test() throws Lexer.LexerException, Parser.ParserException {
        BoolExp boolExp = BoolExp.fromString("B=0&B=1");

        Assert.assertEquals(new BoolAnd(new ExpComp(new Id("B"), new ExpCompOp(ExpCompOp.Type.EQUAL), new ExpLit(0)), new ExpComp(new Id("B"), new ExpCompOp(ExpCompOp.Type.EQUAL), new ExpLit(1))), boolExp);
    }
}