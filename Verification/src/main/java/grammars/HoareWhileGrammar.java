package grammars;

import core.Grammar;
import core.PredictiveParserTable;
import core.structures.LexerRule;
import core.structures.ParserRule;
import core.structures.ParserRulePattern;

public class HoareWhileGrammar extends WhileGrammar {
	public final LexerRule preLexRule;
	public final LexerRule postLexRule;
	
	public final ParserRule hoareBlockRule;
	public final ParserRule preRule;
	public final ParserRule postRule;
	
	public final HoareExpGrammar _hoareExpGrammar;
	
	public HoareWhileGrammar() {
		super();
		
		_hoareExpGrammar = new HoareExpGrammar();
		
		merge(_hoareExpGrammar);
		
		//lexer rules
		preLexRule = createTokenInfo("PRE");

		preLexRule.addRule("PRE");

		postLexRule = createTokenInfo("POST");

		postLexRule.addRule("POST");

		//parser rules
		preRule = createParserRule("pre");
		
		preRule.addRule(createRulePattern("PRE hoareExp"));
		
		postRule = createParserRule("post");
		
		postRule.addRule(createRulePattern("POST hoareExp"));
		
		hoareBlockRule = createParserRule("hoareBlock");

		hoareBlockRule.addRule(createRulePattern("pre prog post"));
		
		//extend while
		ParserRulePattern progHoareBlockPattern = createRulePattern("hoareBlock prest");
		
		progRule.addRule(progHoareBlockPattern);
		
		//predictive parser table
		PredictiveParserTable ruleMap = getPredictiveParserTable();
		
		//extend while
		ruleMap.set(progRule, preLexRule, progHoareBlockPattern);
		ruleMap.set(hoareBlockRule, preLexRule, hoareBlockRule.getRulePattern(0));
		ruleMap.set(preRule, preLexRule, preRule.getRulePattern(0));
		ruleMap.set(postRule, postLexRule, postRule.getRulePattern(0));
	}
}