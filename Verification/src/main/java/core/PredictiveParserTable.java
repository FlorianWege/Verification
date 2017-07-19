package core;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import core.structures.LexerRule;
import core.structures.ParserRule;
import core.structures.ParserRulePattern;

public class PredictiveParserTable {
	private Map<ParserRule, Map<LexerRule, ParserRulePattern>> _map = new HashMap<>();
	
	public Map<LexerRule, ParserRulePattern> getSourceRuleMap(ParserRule parserRule) {
		return _map.get(parserRule);
	}
	
	public ParserRulePattern get(ParserRule parserRule, LexerRule lexerRule) {
		if (!_map.containsKey(parserRule)) return null;
		
		Map<LexerRule, ParserRulePattern> sourceRuleMap = _map.get(parserRule);
		
		return sourceRuleMap.get(lexerRule);
	}
	
	public void set(ParserRule parserRule, LexerRule lexerRule, ParserRulePattern targetRulePattern) {
		if (!_map.containsKey(parserRule)) _map.put(parserRule, new HashMap<>());
		
		_map.get(parserRule).put(lexerRule, targetRulePattern);
	}
	
	public void merge(PredictiveParserTable other) {
		_map.putAll(other._map);
	}
	
	public void print(PrintStream out) {
		for (Map.Entry<ParserRule, Map<LexerRule, ParserRulePattern>> lexerMapEntry : _map.entrySet()) {
			Map<LexerRule, ParserRulePattern> lexerMap = lexerMapEntry.getValue();
			
			out.println(lexerMapEntry.getKey());

			for (Map.Entry<LexerRule, ParserRulePattern> entry : lexerMap.entrySet()) {
				out.println("\t" + entry.getKey() + "->" + entry.getValue());
			}
		}
	}
	
	public PredictiveParserTable() {
		
	}
}