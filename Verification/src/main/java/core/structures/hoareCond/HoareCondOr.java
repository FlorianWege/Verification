package core.structures.hoareCond;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import core.SyntaxNode;
import core.structures.Terminal;

public class HoareCondOr extends HoareCond {
	private Collection<HoareCond> _children = null;
	
	public Collection<HoareCond> getChildren() {
		return _children;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (HoareCond child : _children) {
			if (sb.length() > 0) sb.append(" || ");

			sb.append(child.toString());
		}
		
		return sb.toString();
	}
	
	@Override
	public HoareCond copy() {
		Collection<HoareCond> newChildren = new Vector<>();
		
		for (HoareCond condition : getChildren()) {
			newChildren.add(condition.copy());
		}
		
		return new HoareCondOr(newChildren.toArray(new HoareCond[newChildren.size()]));
	}
	
	@Override
	public void replace(Terminal lexerRule, String var, SyntaxNode exp) {
		for (HoareCond child : getChildren()) {
			child.replace(lexerRule, var, exp);
		}
	}
	
	public void addCondition(HoareCond condition) {
		_children.add(condition);
	}
	
	public HoareCondOr(HoareCond... conditions) {
		super();
		
		_children = new ArrayList<>(Arrays.asList(conditions));		
	}
}
