package core.structures;

import core.SyntaxTreeNode;

public class HoareConditionBoolExpr extends HoareCondition {
	private SyntaxTreeNode _base = null;
	
	@Override
	public String toString() {
		//return _base.toString();
		return _base.synthesize();
	}
	
	@Override
	public HoareCondition copy() {
		return new HoareConditionBoolExpr(_base.copy());
	}
	
	@Override
	public void replace(LexerRule lexerRule, String var, SyntaxTreeNode exp) {
		_base.replace(lexerRule, var, exp);
	}
	
	public HoareConditionBoolExpr(SyntaxTreeNode base) {
		super();
		
		_base = base;
	}
}