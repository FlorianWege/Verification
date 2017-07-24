package core.structures;

import core.Rule;

/**
 * right side (one option) of a parser rule
 */
public class ParserRulePattern {
	private Rule _rule;
	
	public Rule getRule() {
		return _rule;
	}
	
	@Override
	public String toString() {
		return _rule.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Rule) {
			if (_rule != null) {
				return _rule.equals(other);
			}
		}
		
		return super.equals(other);
	}

	public ParserRulePattern(Rule rule) {
		_rule = rule;
	}
	
	protected ParserRulePattern() {
		
	}
}