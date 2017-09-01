package core.structures.hoareCond;

import java.util.function.Predicate;

import core.Lexer;
import core.Parser;
import core.SyntaxTree;
import core.SyntaxTreeNode;
import core.Lexer.LexerException;
import core.Parser.NoRuleException;
import core.Parser.ParserException;
import core.structures.Terminal;
import grammars.BoolExpGrammar;

public abstract class HoareCond {
	private Predicate<Object> _predicate = new Predicate<Object>() {
		@Override
		public boolean test(Object arg0) {
			return true;
		}
		
	};
	
	public String toStringEx(String replacement) {
		return (replacement == null) ? "{" + this + "}" : "{" + this + "[" + replacement + "]" + "}";
	}
	
	public String toStringEx() {
		return toStringEx(null);
	}
	
	public abstract HoareCond copy();
	public abstract void replace(Terminal lexerRule, String var, SyntaxTreeNode exp);
	
	public HoareCond() {
		
	}
	
	public static HoareCond fromString(String s) throws LexerException, ParserException {
		BoolExpGrammar boolExpGrammar = new BoolExpGrammar();
		
		Parser parser = new Parser(boolExpGrammar);
		
		SyntaxTree tree = parser.parse(new Lexer(boolExpGrammar).tokenize(s).getTokens());
		
		return new HoareCondBoolExpr(tree.getRoot());
	}
}