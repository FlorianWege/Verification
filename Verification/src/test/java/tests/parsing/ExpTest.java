package tests.parsing;

import core.Lexer;
import core.Parser;
import core.structures.semantics.exp.Exp;
import core.structures.semantics.exp.Id;
import core.structures.semantics.exp.Sum;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExpTest {
	@Test()
	public void test() throws Lexer.LexerException, Parser.ParserException {
		Exp exp = Exp.fromString("A+B");

		Assert.assertEquals(new Sum(new Id("A"), new Id("B")), exp);
	}
}