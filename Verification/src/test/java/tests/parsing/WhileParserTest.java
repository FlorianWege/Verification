package tests.parsing;

import org.testng.annotations.Test;

import core.Grammar;
import core.Lexer;
import core.Lexer.LexerException;
import core.Lexer.LexerResult;
import core.Parser;
import core.Parser.NoRuleException;
import core.Parser.ParserException;
import grammars.WhileGrammar;

public class WhileParserTest {
	@Test()
	public void test() throws LexerException, ParserException {
		Grammar grammar = new WhileGrammar();
		
		LexerResult lexerResult = new Lexer(grammar).tokenize("a=1;IF abc THEN var=1 FI;WHILE def DO var=2 OD;b=2");
		
		lexerResult.print();

		new Parser(grammar).parse(lexerResult.getTokens());
	}
}