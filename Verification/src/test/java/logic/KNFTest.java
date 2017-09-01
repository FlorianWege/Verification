package logic;

import java.io.IOException;

import org.testng.annotations.Test;

import core.Grammar;
import core.Lexer;
import core.Lexer.LexerResult;
import core.Parser;
import core.Parser.ParserException;
import core.SyntaxTree;
import grammars.BoolExpGrammar;

public class KNFTest {
	@Test()
	public void test() throws IOException, Lexer.LexerException, ParserException {
		Grammar grammar = new BoolExpGrammar();
		
		LexerResult lexerResult = new Lexer(grammar).tokenize("~[~a>1]  && b<1 || c>1");
		
		SyntaxTree tree = new Parser(grammar).parse(lexerResult.getTokens());
		
		System.out.println(tree.getRoot().synthesize());
	}
}
