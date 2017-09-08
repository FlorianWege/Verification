package core;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import core.structures.LexerRule;
import core.structures.NonTerminal;
import core.structures.ParserRule;
import core.structures.Terminal;
import util.StringUtil;

public class Grammar {
	public Grammar() {
	}
	
	private ParserTable _parserTable;
	
	ParserTable getParserTable() {
		return _parserTable;
	}
	
	protected void updateParserTable() {
		_parserTable = new ParserTable(this);
	}
	
	private NonTerminal _startSymbol;
	
	NonTerminal getStartSymbol() {
		return _startSymbol;
	}
	
	public void setStartSymbol(NonTerminal val) {
		_startSymbol = val;
	}
	
	private Map<SymbolKey, Symbol> _symbols = new LinkedHashMap<>();
	
	public Map<SymbolKey, Symbol> getSymbols() {
		return _symbols;
	}
	
	private Set<Terminal> _terminals = new LinkedHashSet<>();
	
	public Set<Terminal> getTerminals() {
		return _terminals;
	}
	
	public Terminal createTerminal(SymbolKey key) {
		assert(!_symbols.containsKey(key)) : "key " + key + " already exists";

		Terminal terminal = new Terminal(key);

		_symbols.put(key, terminal);
		_terminals.add(terminal);
		
		return terminal;
	}
	
	public Terminal createTerminal(String keyS) {
		return createTerminal(new SymbolKey(keyS));
	}
	
	private Set<NonTerminal> _nonTerminals = new LinkedHashSet<>();
	
	public Set<NonTerminal> getNonTerminals() {
		return _nonTerminals;
	}
	
	public NonTerminal createNonTerminal(SymbolKey key) {
		assert(!_symbols.containsKey(key)) : "key " + key + " already exists";
		
		NonTerminal nonTerminal = new NonTerminal(key);
		
		_symbols.put(key, nonTerminal);
		_nonTerminals.add(nonTerminal);
		
		return nonTerminal;
	}
	
	public NonTerminal createNonTerminal(String keyS) {
		return createNonTerminal(new SymbolKey(keyS));
	}

	public ParserRule createRule(NonTerminal nonTerminal, Symbol... symbols) {
		return nonTerminal.createRule(symbols);
	}
	
	public ParserRule createRule(NonTerminal nonTerminal, String s) {
		String[] sArr = s.split("\\s+");
		
		Vector<Symbol> symbols = new Vector<>();
		
		for (String el : sArr) {
			Symbol rule = _symbols.get(new SymbolKey(el));

			if (rule == null) throw new RuntimeException("unknown rule " + el);
			
			symbols.add(rule);
		}
		
		return nonTerminal.createRule(symbols);
	}
	
	public void printLatex(PrintStream outStream) {
		for (Symbol sym : _symbols.values()) {
			if (sym instanceof NonTerminal) {
				NonTerminal nonTerminal = (NonTerminal) sym;
				
				Set<ParserRule> rules = nonTerminal.getRules();
				
				outStream.print("<" + (nonTerminal.equals(getStartSymbol()) ? "*" : "") + StringUtil.latexify(nonTerminal.toString()) + ">");
				
				outStream.print(" ::= ");
				
				int c = 0;
				
				for (ParserRule rule : rules) {
					if (c > 0) outStream.print("\\alt ");
					
					for (Symbol symbol : rule.getSymbols()) {
						if (symbol instanceof Terminal) {
							if (symbol.equals(Terminal.EPSILON)) {
								outStream.print("\\straightepsilon{} ");
							} else {
								outStream.print("\\lit{" + StringUtil.latexify(symbol.toLatexString()) + "} ");
							}
						} else if (symbol instanceof NonTerminal) {
							outStream.print("<" + StringUtil.latexify(symbol.toString()) + "> ");
						}
					}
					
					outStream.println();
					c++;
				}
				
				outStream.println();
			} else if (sym instanceof Terminal) {
				Terminal terminal = (Terminal) sym;
				
				if (!terminal.hasRegexRule()) continue;
				
				outStream.print("<" + StringUtil.latexify(terminal.toString()) + ">");
				
				outStream.print(" ::= ");
				
				int c = 0;
				
				for (LexerRule rule : terminal.getRules()) {
					if (c > 0) outStream.print("\\alt ");
					
					if (rule.isRegEx()) outStream.print(StringUtil.latexify(rule.toString())); else outStream.print("\\lit{" + StringUtil.latexify(rule.toString()) + "}");
					
					outStream.println();
					c++;
				}
				
				outStream.println();
			}
		}
	}
	
	protected void merge(Grammar other) {
		_parserTable.merge(other.getParserTable());
		_terminals.addAll(other.getTerminals());
		_nonTerminals.addAll(other.getNonTerminals());
		_symbols.putAll(other.getSymbols());
	}
}