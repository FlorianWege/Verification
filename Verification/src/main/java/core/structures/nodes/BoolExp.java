package core.structures.nodes;

import core.Lexer;
import core.Lexer.LexerException;
import core.Parser;
import core.Parser.ParserException;
import core.SyntaxTree;
import core.SyntaxNode;
import grammars.BoolExpGrammar;
import grammars.HoareWhileGrammar;

public class BoolExp extends SyntaxNodeSpec {
	private SyntaxNode _base;
	
	public SyntaxNode getBase() {
		return _base;
	}
	
	public BoolExp copy() {
		return new BoolExp(_base.copy());
	}
	
	public BoolExp(SyntaxNode base) {
		if (!base.getSymbol().equals(HoareWhileGrammar.getInstance().NON_TERMINAL_BOOL_EXP)) throw new RuntimeException("wrong node " + base);

		_base = base;
	}
	
	public static BoolExp fromString(String s) throws LexerException, ParserException {
		BoolExpGrammar boolExpGrammar = new BoolExpGrammar();
		
		Parser parser = new Parser(boolExpGrammar);
		
		SyntaxTree tree = parser.parse(new Lexer(boolExpGrammar).tokenize(s).getTokens());
		
		return new BoolExp(tree.getRoot());
	}
}