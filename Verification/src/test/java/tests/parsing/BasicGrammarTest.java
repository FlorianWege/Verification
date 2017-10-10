package tests.parsing;

import grammars.HoareWhileGrammar;
import org.testng.annotations.Test;

import core.Grammar;
import core.Lexer;
import core.Lexer.LexerException;
import core.Lexer.LexerResult;
import core.Parser;
import core.Parser.NoRuleException;
import core.Parser.ParserException;
import grammars.WhileGrammar;
import util.IOUtil;

import java.io.IOException;
import java.net.URISyntaxException;

public class BasicGrammarTest {
	@Test()
	public void whileGrammar() throws LexerException, ParserException {
		new Parser(WhileGrammar.getInstance()).parse("a=1;IF abc THEN var=1 FI;WHILE def DO var=2 OD;b=2");
	}

	@Test()
	public void hoareWhileGrammar_factorial() throws IOException, URISyntaxException, LexerException, ParserException {
		String s = IOUtil.getResourceAsString("resources/src/res/factorial2.c");

		new Parser(HoareWhileGrammar.getInstance()).parse(s);
	}
}