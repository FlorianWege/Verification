package core.structures;

import java.util.Vector;

import core.Rule;
import core.RuleKey;

/**
 * rule for the lexer (terminals, their key starting with an uppercase letter)
 */
public class LexerRule extends Rule {
	public final static LexerRule EPSILON = new LexerRule("eps");
	
	public Vector<LexerRulePattern> _rulePatterns = new Vector<>();
	public boolean _skip;
	
	public Vector<LexerRulePattern> getRulePatterns() {
		return _rulePatterns;
	}
	
	public LexerRule(RuleKey key, boolean skip) {
		super(key);

		_skip = skip;
	}
	
	public LexerRule(String keyS) {
		this(new RuleKey(keyS), false);
	}
	
	@Override
	public String toString() {
		return _key.toString();
	}
	
	public void addRule(LexerRulePattern pattern) {
		_rulePatterns.add(pattern);
	}

	public void addRuleRegEx(String patternS) {
		addRule(new LexerRulePattern(patternS, true));
	}
	
	public void addRule(String patternS) {
		addRule(new LexerRulePattern(patternS, false));
	}
}