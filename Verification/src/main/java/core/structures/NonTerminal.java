package core.structures;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import core.Symbol;
import core.SymbolKey;

/**
 * rule for the parser (non-terminals, their key starting with a lowercase letter)
 */
public class NonTerminal extends Symbol {
	public NonTerminal(SymbolKey key) {
		super(key);
	}
	
	@Override
	public String toLatexString() {
		return "<" + toString() + ">";
	}
	
	private Set<ParserRule> _rules = new LinkedHashSet<>();
	
	public Set<ParserRule> getRules() {
		return new LinkedHashSet<>(_rules);
	}
	
	public ParserRule createRule(Vector<Symbol> symbols) {
		if (symbols.isEmpty()) throw new RuntimeException("no symbols");
		
		ParserRule rule = new ParserRule(this, symbols);
		
		_rules.add(rule);
		
		return rule;
	}
	
	public ParserRule createRule(Symbol... symbols) {
		return createRule(new Vector<>(Arrays.asList(symbols)));
	}
}