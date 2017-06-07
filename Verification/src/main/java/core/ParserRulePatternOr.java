package core;

import java.util.Arrays;
import java.util.Vector;

public class ParserRulePatternOr extends ParserRulePattern {
	private Vector<ParserRulePattern> _children = null;
	
	public ParserRulePattern getChild(int index) {
		return _children.get(index);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("[");
		
		for (ParserRulePattern child : _children) {
			if (sb.length() > 0) sb.append("|");
			
			sb.append(child.toString());
		}
		
		sb.append("]");
		
		return sb.toString();
	}
	
	public void addPattern(ParserRulePattern rulePattern) {
		_children.add(rulePattern);
	}
	
	public ParserRulePatternOr(ParserRulePattern... rulePatterns) {
		super();
		
		_children = new Vector<>(Arrays.asList(rulePatterns));		
	}
}