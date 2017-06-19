package core;

import java.util.Collection;
import java.util.Vector;

public class HoareConditionNeg extends HoareCondition {
	private HoareCondition _base = null;
	
	@Override
	public String toString() {
		return "not " + _base.toString();
	}
	
	@Override
	public HoareCondition copy() {
		return new HoareConditionNeg(_base.copy());
	}
	
	@Override
	public void replace(LexerRule lexerRule, String var, SyntaxTreeNode exp) {
		_base.replace(lexerRule, var, exp);
	}
	
	public HoareConditionNeg(HoareCondition base) {
		super();		
	}
}