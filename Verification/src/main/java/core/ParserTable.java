package core;

import core.structures.NonTerminal;
import core.structures.ParserRule;
import core.structures.Terminal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;

public class ParserTable implements Serializable {
	private final Map<NonTerminal, Map<Terminal, ParserRule>> _map = new HashMap<>();

	@Nullable
	public ParserRule get(@Nonnull NonTerminal parserRule, @Nonnull Terminal lexerRule) {
		if (!_map.containsKey(parserRule)) return null;
		
		Map<Terminal, ParserRule> sourceRuleMap = _map.get(parserRule);
		
		return sourceRuleMap.get(lexerRule);
	}
	
	public void set(@Nonnull NonTerminal parserRule, @Nonnull Terminal lexerRule, @Nonnull ParserRule targetRule) {
		if (!_map.containsKey(parserRule)) _map.put(parserRule, new HashMap<>());
		
		Map<Terminal, ParserRule> subMap = _map.get(parserRule);
		
		if (subMap.containsKey(lexerRule) && !subMap.get(lexerRule).equals(targetRule)) throw new RuntimeException("try to reassign " + parserRule + "/" + lexerRule + " to " + targetRule + " (currentVal=" + subMap.get(lexerRule) + ")");
		
		subMap.put(lexerRule, targetRule);
	}
	
	public void merge(@Nonnull ParserTable other) {
		_map.putAll(other._map);
	}
	
	public void print(@Nonnull PrintStream out) {
		for (Map.Entry<NonTerminal, Map<Terminal, ParserRule>> lexerMapEntry : _map.entrySet()) {
			Map<Terminal, ParserRule> lexerMap = lexerMapEntry.getValue();
			
			out.println(lexerMapEntry.getKey());

			for (Map.Entry<Terminal, ParserRule> entry : lexerMap.entrySet()) {
				out.println("\t" + entry.getKey() + "->" + entry.getValue());
			}
		}
	}
	
	//private final Map<NonTerminal, Set<Terminal>> _followMap = new LinkedHashMap<>();

	@Nonnull
	private Set<Terminal> getFollow(@Nonnull NonTerminal nonTerminal, @Nonnull Grammar grammar, @Nonnull Set<NonTerminal> recursiveSet) {
		assert(grammar.getStartSymbol() != null) : "no start symbol";

		//if (_followMap.containsKey(nonTerminal)) return _followMap.get(nonTerminal);

		Set<Terminal> ret = new LinkedHashSet<>();

		if (recursiveSet.contains(nonTerminal)) return ret;

		recursiveSet.add(nonTerminal);

		if (grammar.getStartSymbol().equals(nonTerminal)) ret.add(Terminal.TERMINATOR);

		for (NonTerminal p : grammar.getNonTerminals()) {
			for (ParserRule rule : p.getRules()) {
				List<Symbol> symbols = rule.getSymbols();
				
				for (int i = 0; i < symbols.size(); i++) {
					Symbol symbol = rule.getSymbols().get(i);
					
					if (symbol.equals(nonTerminal)) {
						List<Symbol> restSymbols = (i < symbols.size() - 1) ? symbols.subList(i + 1, symbols.size()) : new ArrayList<>();
						
						if (restSymbols.isEmpty()) {
							ret.addAll(getFollow(p, grammar, recursiveSet));
						} else {
							Set<Terminal> subSet = getFirst(restSymbols, new LinkedHashSet<>());

							if (subSet.contains(Terminal.EPSILON)) {
								subSet.remove(Terminal.EPSILON);
								
								ret.addAll(subSet);

								ret.addAll(getFollow(p, grammar, recursiveSet));
							} else {
								ret.addAll(subSet);
							}
						}
					}
				}
			}
		}

		//_followMap.put(nonTerminal, ret);

		return ret;
	}

	@Nonnull
	private Set<Terminal> getFollow(@Nonnull NonTerminal nonTerminal, @Nonnull Grammar grammar) {
		return getFollow(nonTerminal, grammar, new LinkedHashSet<>());
	}

	@Nonnull
	private Set<Terminal> getFirst(@Nonnull List<Symbol> symbols, @Nonnull Set<NonTerminal> recursiveSet) {
		if (symbols.contains(Terminal.EPSILON)) return new LinkedHashSet<>(Collections.singletonList(Terminal.EPSILON));

		Set<Terminal> ret = new LinkedHashSet<>();

		for (int i = 0; i < symbols.size(); i++) {
			Symbol symbol = symbols.get(i);
 
			if (symbol instanceof Terminal) {
				ret.add((Terminal) symbol); break;
			} else if (symbol instanceof NonTerminal) {
				Set<Terminal> setSub = getFirst((NonTerminal) symbol, recursiveSet);
				
				if (i < symbols.size() - 1 && setSub.contains(Terminal.EPSILON)) {
					setSub.remove(Terminal.EPSILON); ret.addAll(setSub);
				} else {
					ret.addAll(setSub); break;
				}
			}
		}
		
		return ret;
	}
	
	//private final Map<NonTerminal, Set<Terminal>> _firstMap = new LinkedHashMap<>();

	@Nonnull
	private Set<Terminal> getFirst(@Nonnull NonTerminal nonTerminal, @Nonnull Set<NonTerminal> recursiveSet) {
		//if (_firstMap.containsKey(nonTerminal)) return _firstMap.get(nonTerminal);
		
		Set<Terminal> ret = new LinkedHashSet<>();

		if (recursiveSet.contains(nonTerminal)) return ret;

		recursiveSet.add(nonTerminal);
		
		for (ParserRule p : nonTerminal.getRules()) {
			ret.addAll(getFirst(p.getSymbols(), recursiveSet));
		}
		
		//_firstMap.put(nonTerminal, ret);
		
		return ret;
	}

	@Nonnull
	private Set<Terminal> getFirst(@Nonnull NonTerminal nonTerminal) {
		return getFirst(nonTerminal, new LinkedHashSet<>());
	}
	
	public ParserTable(@Nonnull Grammar grammar) {
		Map<NonTerminal, Set<Terminal>> firstMap = new LinkedHashMap<>();
		Map<NonTerminal, Set<Terminal>> followMap = new LinkedHashMap<>();
		
		for (NonTerminal p : grammar.getNonTerminals()) {
			firstMap.put(p, getFirst(p));
			followMap.put(p, getFollow(p, grammar));
		}
		
		for (NonTerminal p : grammar.getNonTerminals()) {
			for (ParserRule r : p.getRules()) {
				List<Symbol> symbols = r.getSymbols();
				
				if (symbols.contains(Terminal.EPSILON))
					for (Terminal terminal : followMap.get(p)) {	
						set(p, terminal, r);
					}
				else {
					Symbol symbol = r.getSymbols().get(0);
					
					if (symbol instanceof Terminal) {
						set(p, (Terminal) symbol, r);
					} else if (symbol instanceof NonTerminal) {
						for (Terminal terminal : firstMap.get(symbol)) {
							set(p, terminal, r);
						}
					}
				}
			}
		}
	}
}