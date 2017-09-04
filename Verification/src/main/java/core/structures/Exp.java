package core.structures;

import core.Lexer;
import core.Lexer.LexerException;
import core.Parser;
import core.Parser.ParserException;
import core.SyntaxTree;
import core.SyntaxTreeNode;
import grammars.ExpGrammar;

public class Exp {
	private SyntaxTreeNode _base;
	
	public SyntaxTreeNode getBaseEx() {
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
	
	public Exp(SyntaxTreeNode base) {
		_base = base;
	}
}
