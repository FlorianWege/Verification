package core.structures;

import java.util.function.Predicate;

import core.Lexer;
import core.Parser;
import core.SyntaxTree;
import core.SyntaxTreeNode;
import core.Lexer.LexerException;
import core.Parser.NoRuleException;
import core.Parser.ParserException;
import grammars.BoolExpGrammar;

public abstract class HoareCondition {
	private Predicate<Object> _predicate = new Predicate<Object>() {
		@Override
		public boolean test(Object arg0) {
			return true;
		}
		
	};
	
	public abstract HoareCondition copy();
	public abstract void replace(LexerRule lexerRule, String var, SyntaxTreeNode exp);
	
	public HoareCondition() {
		
	}
	
	public static HoareCondition fromString(String s) throws LexerException, ParserException {
		BoolExpGrammar boolExpGrammar = new BoolExpGrammar();
		
		Parser parser = new Parser(boolExpGrammar);
		
		SyntaxTree tree = parser.parse(new Lexer(boolExpGrammar).tokenize(s).getTokens());
		
		return new HoareConditionBoolExpr(tree.getRoot());
	}
}