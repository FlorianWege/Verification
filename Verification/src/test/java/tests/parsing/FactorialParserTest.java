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
import grammars.WhileGrammar;
import util.IOUtil;

public class FactorialParserTest {
	@Test()
	public FactorialParserTest() throws NoRuleException, LexerException, IOException, URISyntaxException {
		Grammar grammar = new WhileGrammar();
		
		//LexerResult lexerResult = new Lexer(grammar).tokenize("a=1;IF abc THEN var=1; FI");
		LexerResult lexerResult = new Lexer(grammar).tokenize(IOUtil.getResourceAsString("Factorial.txt"));
		
		lexerResult.print();

		new Parser(grammar, grammar.getPredictiveParserTable()).parse(lexerResult.getTokens(), grammar.getStartParserRule());
	}
}
