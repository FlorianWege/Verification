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
			System.out.println("ABC " + this + ";" + this.getClass() + ";" + _rule + ";" + other);
			if (_rule != null) {
				System.out.println("ABC " + _rule + ";" + other + ";" + _rule.equals(other));
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