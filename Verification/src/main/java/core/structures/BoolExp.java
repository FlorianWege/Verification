package core.structures;

import core.Lexer;
import core.Lexer.LexerException;
import core.Parser;
import core.Parser.ParserException;
import core.SyntaxTree;
import core.SyntaxTreeNode;
import grammars.BoolExpGrammar;

public class BoolExp {
	private SyntaxTreeNode _base;
	
	public SyntaxTreeNode getBase() {
		return _base;
	}
	
	public BoolExp copy() {
		return new BoolExp(_base.copy());
	}
	
	private BoolExp(SyntaxTreeNode base) {
		_base = base;
	}
	
	public static BoolExp fromString(String s) throws LexerException, ParserException {
		BoolExpGrammar boolExpGrammar = new BoolExpGrammar();
		
		Parser parser = new Parser(boolExpGrammar);
		
		SyntaxTree tree = parser.parse(new Lexer(boolExpGrammar).tokenize(s).getTokens());
		
		return new BoolExp(tree.getRoot());
	}
}
