package core.structures;

import java.util.Collection;
import java.util.Vector;

import core.SyntaxTreeNode;

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