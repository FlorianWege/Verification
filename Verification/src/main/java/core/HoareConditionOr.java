package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

public class HoareConditionOr extends HoareCondition {
	private Collection<HoareCondition> _children = null;
	
	public Collection<HoareCondition> getChildren() {
		return _children;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (HoareCondition child : _children) {
			if (sb.length() > 0) sb.append(" ");

			sb.append(child.toString());
		}
		
		return sb.toString();
	}
	
	@Override
	public HoareCondition copy() {
		Collection<HoareCondition> newChildren = new Vector<>();
		
		for (HoareCondition condition : getChildren()) {
			newChildren.add(condition.copy());
		}
		
		return new HoareConditionOr((HoareCondition[]) newChildren.toArray());
	}
	
	@Override
	public void replace(LexerRule lexerRule, String var, SyntaxTreeNode exp) {
		for (HoareCondition child : getChildren()) {
			child.replace(lexerRule, var, exp);
		}
	}
	
	public void addCondition(HoareCondition condition) {
		_children.add(condition);
	}
	
	public HoareConditionOr(HoareCondition... conditions) {
		super();
		
		_children = new ArrayList<>(Arrays.asList(conditions));		
	}
}
