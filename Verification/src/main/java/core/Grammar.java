package core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Grammar {
	public Grammar() {
		
	}
	
	private PredictiveParserTable _predictiveParserTable = new PredictiveParserTable();
	
	public PredictiveParserTable getPredictiveParserTable() {
		return _predictiveParserTable;
	}
	
	private ParserRule _startParserRule;
	
	public ParserRule getStartParserRule() {
		return _startParserRule;
	}
	
	public void setStartParserRule(ParserRule val) {
		_startParserRule = val;
	}
	
	private Map<RuleKey, Rule> _rules = new HashMap<>();
	
	private Vector<LexerRule> _lexerRules = new Vector<>();
	
	public Vector<LexerRule> getTokenInfos() {
		return _lexerRules;
	}
	
	public LexerRule createTokenAssign(RuleKey key, boolean skip) {
		assert(!_rules.containsKey(key)) : "key " + key + " already exists";
		
		LexerRule rule = new LexerRule(key, skip);
		
		_rules.put(key, rule);
		_lexerRules.add(rule);
		
		return rule;
	}
	
	public LexerRule createTokenInfo(RuleKey key) {
		return createTokenAssign(key, false);
	}
	
	public LexerRule createTokenInfo(String keyS) {
		return createTokenInfo(new RuleKey(keyS));
	}
	
	private Vector<ParserRule> _parserRules = new Vector<>();
	
	public ParserRule createParserRule(RuleKey key) {
		assert(!_rules.containsKey(key)) : "key " + key + " already exists";
		
		ParserRule rule = new ParserRule(key);
		
		_rules.put(key, rule);
		_parserRules.add(rule);
		
		return rule;
	}
	
	public ParserRule createParserRule(String keyS) {
		return createParserRule(new RuleKey(keyS));
	}
	
	public ParserRulePattern createRulePattern(String s) {
		String[] sArr = s.split("\\s+");
		
		ParserRulePatternAnd andPattern = new ParserRulePatternAnd();
		
		for (String el : sArr) {
			Rule rule = _rules.get(new RuleKey(el));
			//System.out.println(el + ";" + rule);
			if (rule == null) throw new RuntimeException("unknown rule " + el);
			
			if (rule instanceof LexerRule) {
				andPattern.addPattern(new ParserRulePattern((LexerRule) rule));
			}
			if (rule instanceof ParserRule) {
				andPattern.addPattern(new ParserRulePattern((ParserRule) rule));
			}
		}
		
		return andPattern;
	}
}