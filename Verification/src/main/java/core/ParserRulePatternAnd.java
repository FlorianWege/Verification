package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ParserRulePatternAnd extends ParserRulePattern {
	private Collection<ParserRulePattern> _children = null;
	
	public Collection<ParserRulePattern> getChildren() {
		return _children;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (ParserRulePattern child : _children) {
			if (sb.length() > 0) sb.append(" ");

			sb.append(child.toString());
		}
		
		return sb.toString();
	}
	
	public void addPattern(ParserRulePattern rulePattern) {
		_children.add(rulePattern);
	}
	
	public ParserRulePatternAnd(ParserRulePattern... rulePatterns) {
		super();
		
		_children = new ArrayList<>(Arrays.asList(rulePatterns));		
	}
}