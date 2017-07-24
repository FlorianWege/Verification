package tests.parsing;

import java.io.IOException;

import org.testng.annotations.Test;

import core.Grammar;
import core.Lexer;
import core.Lexer.LexerResult;
import core.Parser;
import core.Parser.NoRuleException;
import core.Parser.ParserException;
import grammars.ExpGrammar;

public class ExpParserTest {
	@Test()
	public void test() throws IOException, Lexer.LexerException, ParserException {
		Grammar expGrammar = new ExpGrammar();
		
		LexerResult lexerResult = new Lexer(expGrammar).tokenize("4+4");
		
		lexerResult.print();
		
		new Parser(expGrammar).parse(lexerResult.getTokens());
	}
}