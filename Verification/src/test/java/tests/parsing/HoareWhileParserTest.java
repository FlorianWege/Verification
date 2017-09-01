package tests.parsing;

import java.io.IOException;
import java.net.URISyntaxException;

import org.testng.annotations.Test;

import core.Grammar;
import core.Lexer;
import core.Parser;
import core.Parser.ParserException;
import core.Lexer.LexerException;
import core.Lexer.LexerResult;
import grammars.HoareWhileGrammar;
import grammars.WhileGrammar;
import util.IOUtil;

public class HoareWhileParserTest {
	@Test()
	public void test() throws IOException, URISyntaxException, LexerException, ParserException {
		String s = IOUtil.getResourceAsString("Factorial2.txt");
		
		Grammar grammar = new HoareWhileGrammar();
		
		LexerResult lexerResult = new Lexer(grammar).tokenize(s);
		
		lexerResult.print();

		new Parser(grammar).parse(lexerResult.getTokens());
	}
}
