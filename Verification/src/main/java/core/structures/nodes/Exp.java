package core.structures.nodes;

import core.Lexer;
import core.Lexer.LexerException;
import core.Parser;
import core.Parser.ParserException;
import core.SyntaxNode;
import core.SyntaxTree;
import grammars.ExpGrammar;
import grammars.HoareWhileGrammar;

public class Exp extends SyntaxNodeSpec {
	private SyntaxNode _base;
	
	public SyntaxNode getBaseEx() {
		try {
			return fromString("(" + _base.synthesize() + ")")._base;
		} catch (ParserException | LexerException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String synthesize() {
		return _base.synthesize();
	}
	
	public static Exp fromString(String s) throws ParserException, LexerException {
		ExpGrammar expGrammar = new ExpGrammar();
		
		Parser parser = new Parser(expGrammar);
		
		SyntaxTree tree = parser.parse(new Lexer(expGrammar).tokenize(s).getTokens());
	
		return new Exp(tree.getRoot());
	}
	
	public Exp(SyntaxNode base) {
		if (!base.getSymbol().equals(HoareWhileGrammar.getInstance().NON_TERMINAL_EXP)) throw new RuntimeException("wrong node " + base);

		_base = base;
	}
}