package core;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.structures.NonTerminal;
import core.structures.ParserRule;
import core.structures.Terminal;
import grammars.BoolExpGrammar;
import util.StringUtil;

public class PredictiveParserTable {
	private Map<NonTerminal, Map<Terminal, ParserRule>> _map = new HashMap<>();
	
	public Map<Terminal, ParserRule> getSourceRuleMap(NonTerminal parserRule) {
		return _map.get(parserRule);
	}
	
	public ParserRule get(NonTerminal parserRule, Terminal lexerRule) {
		if (!_map.containsKey(parserRule)) return null;
		
		Map<Terminal, ParserRule> sourceRuleMap = _map.get(parserRule);
		
		return sourceRuleMap.get(lexerRule);
	}
	
	public void set(NonTerminal parserRule, Terminal lexerRule, ParserRule targetRule) {
		if (!_map.containsKey(parserRule)) _map.put(parserRule, new HashMap<>());
		
		Map<Terminal, ParserRule> subMap = _map.get(parserRule);
		
		if (subMap.containsKey(lexerRule) && !subMap.get(lexerRule).equals(targetRule)) throw new RuntimeException("try to reassign " + parserRule + "/" + lexerRule + " to " + targetRule + " (currentVal=" + subMap.get(lexerRule) + ")");
		
		subMap.put(lexerRule, targetRule);
	}
	
	public void merge(PredictiveParserTable other) {
		_map.putAll(other._map);
	}
	
	public void print(PrintStream out) {
		for (Map.Entry<NonTerminal, Map<Terminal, ParserRule>> lexerMapEntry : _map.entrySet()) {
			Map<Terminal, ParserRule> lexerMap = lexerMapEntry.getValue();
			
			out.println(lexerMapEntry.getKey());

			for (Map.Entry<Terminal, ParserRule> entry : lexerMap.entrySet()) {
				out.println("\t" + entry.getKey() + "->" + entry.getValue());
			}
		}
	}
	
	private Map<NonTerminal, Set<Terminal>> _followMap = new LinkedHashMap<>();
	
	public Set<Terminal> getFollow(NonTerminal nonTerminal, Grammar grammar, Set<NonTerminal> recursiveSet) {		
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
							Set<Terminal> subSet = getFirst(restSymbols, new LinkedHashSet<NonTerminal>());

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
	
	public Set<Terminal> getFollow(NonTerminal nonTerminal, Grammar grammar) {
		return getFollow(nonTerminal, grammar, new LinkedHashSet<NonTerminal>());
	}
	
	public Set<Terminal> getFirst(List<Symbol> symbols, Set<NonTerminal> recursiveSet) {
		if (symbols.contains(Terminal.EPSILON)) return new LinkedHashSet<>(Arrays.asList(new Terminal[]{Terminal.EPSILON}));

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
	
	private Map<NonTerminal, Set<Terminal>> _firstMap = new LinkedHashMap<>();
	
	public Set<Terminal> getFirst(NonTerminal nonTerminal, Set<NonTerminal> recursiveSet) {
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
	
	public Set<Terminal> getFirst(NonTerminal nonTerminal) {
		return getFirst(nonTerminal, new LinkedHashSet<>());
	}
	
	public static void firstTest() {
		/*System.out.println("sampleA");
		SampleGrammarA g = new SampleGrammarA();
		
		for (NonTerminal p : g.getParserRules()) {
			System.out.println(p + "->" + getFirst(p) + "->" + getFollow(p, g));
		}
		
		System.out.println("sampleB");
		SampleGrammarB h = new SampleGrammarB();
		
		for (NonTerminal p : h.getParserRules()) {
			System.out.println(p + "->" + getFirst(p) + "->" + getFollow(p, h));
		}
		
		System.out.println("sampleC");
		SampleGrammarC i = new SampleGrammarC();
		
		for (NonTerminal p : i.getParserRules()) {
			System.out.println(p + "->" + getFirst(p) + "->" + getFollow(p, i));
		}
		
		System.out.println("sampleD");
		SampleGrammarD j = new SampleGrammarD();
		
		for (NonTerminal p : j.getParserRules()) {
			System.out.println(p + "->" + getFirst(p) + "->" + getFollow(p, j));
		}
		
		new PredictiveParserTable(j).print(System.out);
		
		System.out.println("sampleE");
		SampleGrammarE k = new SampleGrammarE();
		
		for (NonTerminal p : k.getParserRules()) {
			System.out.println(p + "->" + getFirst(p) + "->" + getFollow(p, k));
		}
		
		System.out.println("sampleF");
		SampleGrammarF l = new SampleGrammarF();
		
		for (NonTerminal p : l.getParserRules()) {
			System.out.println(p + "->" + getFirst(p) + "->" + getFollow(p, l));
		}
		
		new PredictiveParserTable(i).print(System.out);*/
		
		_exp = new BoolExpGrammar();
		
		System.out.println(new PredictiveParserTable().getFollow(_exp.NON_TERMINAL_BOOL_AND, _exp));
		
		//HoareWhileGrammar all = new HoareWhileGrammar();
		
		//getFirst(all.NON_TERMINAL_PROG);
		
		/*for (NonTerminal p : all.getParserRules()) {
			System.out.println(p + "->" + getFirst(p) + "->" + getFollow(p, all));
		}
		
		new PredictiveParserTable(all);//.print(System.out);*/
	}
	
	static BoolExpGrammar _exp;
	
	public PredictiveParserTable(Grammar g) {
		Map<NonTerminal, Set<Terminal>> firstMap = new LinkedHashMap<>();
		Map<NonTerminal, Set<Terminal>> followMap = new LinkedHashMap<>();
		
		for (NonTerminal p : g.getNonTerminals()) {
			firstMap.put(p, getFirst(p));
			followMap.put(p, getFollow(p, g));
		}
		
		for (NonTerminal p : g.getNonTerminals()) {
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
						for (Terminal terminal : firstMap.get((NonTerminal) symbol)) {
							set(p, terminal, r);
						}
					}
				}
			}
		}
	}
	
	private PredictiveParserTable() {
		
	}
}