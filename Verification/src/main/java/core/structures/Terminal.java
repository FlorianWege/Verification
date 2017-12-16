package core.structures;

import core.Symbol;
import core.SymbolKey;

import javax.annotation.Nonnull;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * rule for the lexer (terminals, their key starting with an uppercase letter)
 */
public class Terminal extends Symbol {
	public final static Terminal EPSILON = new Terminal("\u03F5");
	public final static Terminal TERMINATOR = new Terminal("$");

	public Terminal(SymbolKey key) {
		super(key);
	}

	public Terminal(String keyS) {
		this(new SymbolKey(keyS));
	}

	public boolean hasRegexRule() {
		for (LexerRule rule : _rules) if (rule.isRegEx()) return true;
		
		return false;
	}

	private boolean _isKeyword = false;

	public boolean isKeyword() {
		return _isKeyword;
	}

	public Terminal setKeyword() {
		_isKeyword = true;

		return this;
	}

	@Override
	public String toLatexString() {
		if (hasRegexRule()) return toString();

		return getPrimRule().toString();
	}
	
	private final Set<LexerRule> _rules = new LinkedHashSet<>();
	
	public Set<LexerRule> getRules() {
		return _rules;
	}

	public LexerRule getPrimRule() {
		return getRules().iterator().next();
	}

	private boolean _isSkipped = false;
	
	public boolean isSkipped() {
		return _isSkipped;
	}

	public Terminal setSkipped() {
		_isSkipped = true;

		return this;
	}

	private boolean _isSep = false;

	public boolean isSep() {
		return _isSep;
	}

	public Terminal setSep() {
		_isSep = true;

		return this;
	}
	
	public void addRule(LexerRule rule) {
		_rules.add(rule);
	}

	public LexerRule addRuleRegEx(@Nonnull String ruleS) {
		LexerRule lexerRule = new LexerRule(ruleS, true);

		addRule(lexerRule);

		return lexerRule;
	}
	
	public LexerRule addRule(@Nonnull String ruleS) {
		LexerRule lexerRule = new LexerRule(ruleS, false);

		addRule(lexerRule);

		return lexerRule;
	}
}