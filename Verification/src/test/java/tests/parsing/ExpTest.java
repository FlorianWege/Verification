package tests.parsing;

import core.*;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.exp.*;
import core.structures.syntax.SyntaxNode;
import grammars.ExpGrammar;
import org.testng.annotations.Test;

import java.util.List;

public class ExpTest {
	protected final static ExpGrammar g = new ExpGrammar();

	@Test()
	public void test() throws Lexer.LexerException, Parser.ParserException {
		String s = "A+B";

		//parse
		List<Token> tokens = new Lexer(g).tokenize(s).getTokens();

		SyntaxNode tree = new Parser(g).parse(tokens);

		SemanticNode node = SemanticNode.fromSyntax(tree);

		Exp red = ((Exp) node).reduce();

		red = red.order();

		System.out.println(red.getContentString());
	}
}