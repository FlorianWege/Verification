package core.structures;

import core.Rule;
import core.RuleKey;

/**
 * rule for the parser (non-terminals, their key starting with a lowercase letter)
 */
public class ParserRule extends Rule {
	public ParserRule(RuleKey key) {
		super(key);
	}
	
	@Override
	public String toString() {
		return _key.toString();
	}
	
	private ParserRulePatternOr _rulePattern = new ParserRulePatternOr();
	
	public ParserRulePattern getRulePattern(int index) {
		return _rulePattern.getChild(index);
	}
	
	public void addRule(ParserRulePattern pattern) {
		_rulePattern.addPattern(pattern);
	}
	
	public void addRule(Rule rule) {
		addRule(new ParserRulePatternAnd(new ParserRulePattern(rule)));
	}
	
	/*public void addRule(String patternS) {
		addRule(new ParserRulePattern(patternS));
	}*/
}