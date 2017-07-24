package tests.parsing;

import java.io.IOException;
import java.net.URISyntaxException;

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

public class FactorialParserTest {
	@Test()
	public void test() throws LexerException, IOException, URISyntaxException, ParserException {
		Grammar grammar = new WhileGrammar();
		
		//LexerResult lexerResult = new Lexer(grammar).tokenize("a=1;IF abc THEN var=1; FI");
		
		String inputS = IOUtil.getResourceAsString("Factorial.txt");
		
		System.out.println(inputS);
		
		LexerResult lexerResult = new Lexer(grammar).tokenize(inputS);
		
		lexerResult.print();

		new Parser(grammar).parse(lexerResult.getTokens());
	}
}