package core.structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import core.Symbol;

public class ParserRule implements Serializable {
	private final NonTerminal _nonTerminal;
	private final List<Symbol> _symbols;

	public ParserRule(NonTerminal nonTerminal, List<Symbol> symbols) {
		if (symbols.isEmpty()) throw new RuntimeException("no symbols");

		_nonTerminal = nonTerminal;
		_symbols = new ArrayList<>(symbols);
	}

	public ParserRule(NonTerminal nonTerminal, Symbol... symbols) {
		this(nonTerminal, Arrays.asList(symbols));
	}
	
	public NonTerminal getNonTerminal() {
		return _nonTerminal;
	}
	
	public Vector<Symbol> getSymbols() {
		return new Vector<>(_symbols);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof ParserRule) {
			ParserRule otherRule = (ParserRule) other;

			if (getSymbols().size() == otherRule.getSymbols().size()) {
				for (int i = 0; i < getSymbols().size(); i++) {
					if (!getSymbols().get(i).equals(otherRule.getSymbols().get(i))) return false;
				}

				return true;
			}
		}
		
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