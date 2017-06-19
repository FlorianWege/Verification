package core;

import java.util.function.Predicate;

import core.Lexer.LexerException;
import core.Parser.NoRuleException;
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
	
	public static HoareCondition fromString(String s) throws NoRuleException, LexerException {
		BoolExpGrammar boolExpGrammar = new BoolExpGrammar();
		
		Parser parser = new Parser(boolExpGrammar);
		
		SyntaxTree tree = parser.parse(new Lexer(boolExpGrammar).tokenize(s).getTokens());
		
		return new HoareConditionBoolExpr(tree.getRoot());
	}
}