package core.structures;

import java.util.LinkedHashSet;
import java.util.Set;

import core.Symbol;
import core.SymbolKey;

/**
 * rule for the lexer (terminals, their key starting with an uppercase letter)
 */
public class Terminal extends Symbol {
	public final static Terminal EPSILON = new Terminal("\u03F5");
	public final static Terminal TERMINATOR = new Terminal("$");
	
	public boolean hasRegexRule() {
		for (LexerRule rule : _rules) {
			if (rule.isRegEx()) return true;
		}
		
		return false;
	}
	
	@Override
	public String toLatexString() {
		if (hasRegexRule()) return toString();
		
		for (LexerRule rule : _rules) {
			return rule.toString();
		}
		
		return toString();
	}
	
	private Set<LexerRule> _rules = new LinkedHashSet<>();
	
	public Set<LexerRule> getRules() {
		return _rules;
	}
	
	private boolean _skip;
	
	public boolean isSkipped() {
		return _skip;
	}

	private boolean _isSep = false;

	public boolean isSep() {
		return _isSep;
	}

	public void setSep() {
		_isSep = true;
	}

	public Terminal(SymbolKey key, boolean skip) {
		super(key);

		_skip = skip;
	}
	
	public Terminal(SymbolKey key) {
		this(key, false);
	}
	
	public Terminal(String keyS) {
		this(new SymbolKey(keyS));
	}
	
	public void addRule(LexerRule rule) {
		_rules.add(rule);
	}

	public void addRuleRegEx(String ruleS) {
		addRule(new LexerRule(ruleS, true));
	}
	
	public void addRule(String ruleS) {
		addRule(new LexerRule(ruleS, false));
	}
}