package logic;

import java.io.IOException;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.syntax.SyntaxNode;
import org.testng.annotations.Test;

import core.Grammar;
import core.Lexer;
import core.Lexer.LexerResult;
import core.Parser;
import core.Parser.ParserException;
import grammars.BoolExpGrammar;

public class CNFTest {
	private Grammar grammar = BoolExpGrammar.getInstance();

	@Test()
	public void test() throws IOException, Lexer.LexerException, ParserException {
		SyntaxNode tree = new Parser(grammar).parse("F=F&G=G&H=H|F=F&G=G&I=I");

		SemanticNode node = SemanticNode.fromSyntax(tree);

		BoolExp cnf = ((BoolExp) node).makeCNF();

		cnf.print(System.out);
		System.out.println(cnf.getContentString());
	}
}