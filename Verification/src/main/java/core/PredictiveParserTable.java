package core;

import java.util.HashMap;
import java.util.Map;

public class PredictiveParserTable {
	private Map<ParserRule, Map<LexerRule, ParserRulePattern>> _map = new HashMap<>();
	
	public ParserRulePattern get(ParserRule parserRule, LexerRule lexerRule) {
		if (!_map.containsKey(parserRule)) return null;
		
		Map<LexerRule, ParserRulePattern> sourceRuleMap = _map.get(parserRule);
		
		return sourceRuleMap.get(lexerRule);
	}
	
	public void set(ParserRule parserRule, LexerRule lexerRule, ParserRulePattern targetRulePattern) {
		if (!_map.containsKey(parserRule)) _map.put(parserRule, new HashMap<>());
		
		_map.get(parserRule).put(lexerRule, targetRulePattern);
	}
	
	public PredictiveParserTable() {
		
	}
}