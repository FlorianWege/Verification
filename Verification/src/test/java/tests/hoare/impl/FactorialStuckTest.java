package tests.hoare.impl;

import core.Lexer;
import core.Parser;
import core.structures.semantics.boolExp.BoolAnd;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.BoolImpl;
import org.testng.annotations.Test;

public class FactorialStuckTest {
    @Test()
    public void test() throws Lexer.LexerException, Parser.ParserException {
        System.out.println(new BoolImpl(new BoolAnd(BoolExp.fromString("f=k! & 0<=k & k<=n"), BoolExp.fromString("k<n")), BoolExp.fromString("f=k! & 0<=k & k<=n")).reduce());
    }
}
