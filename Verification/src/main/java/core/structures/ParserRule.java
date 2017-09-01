package core.structures;

import java.util.Arrays;
import java.util.Vector;

import core.Symbol;

public class ParserRule {
	private NonTerminal _nonTerminal;
	private Vector<Symbol> _symbols = new Vector<>();
	
	public ParserRule(NonTerminal nonTerminal) {
		_nonTerminal = nonTerminal;
	}

	public ParserRule(NonTerminal nonTerminal, Vector<Symbol> symbols) {
		this(nonTerminal);
		
		if (symbols.isEmpty()) throw new RuntimeException("no symbols");
		
		_symbols = new Vector<>(symbols);
	}
	
	public ParserRule(NonTerminal nonTerminal, Symbol... symbols) {
		this(nonTerminal, new Vector<Symbol>(Arrays.asList(symbols)));
		
		_symbols = new Vector<>(Arrays.asList(symbols));		
	}
	
	/*	@Override
	public String toString() {
		return _rule.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Symbol) {
			if (_rule != null) {
				return _rule.equals(other);
			}
		}
		
		return super.equals(other);
	}*/
	
	public NonTerminal getNonTerminal() {
		return _nonTerminal;
	}
	
	public Vector<Symbol> getSymbols() {
		return new Vector<>(_symbols);
	}
	
	@Override
	public boolean equals(Object other) {
		if (_symbols.size() == 1) return _symbols.get(0).equals(other);
		
		return super.equals(other);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Symbol child : _symbols) {
			if (sb.length() > 0) sb.append(" ");

			sb.append(child.toString());
		}
		
		return sb.toString();
	}
	
	public void addSymbol(Symbol symbol) {
		_symbols.add(symbol);
	}
}