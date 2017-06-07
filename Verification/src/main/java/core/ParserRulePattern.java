package core;

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

	public ParserRulePattern(Rule rule) {
		_rule = rule;
	}
	
	protected ParserRulePattern() {
		
	}
}