package grammars;

import java.util.HashMap;
import java.util.Map;

import core.Grammar;
import core.LexerRule;
import core.ParserRule;
import core.ParserRulePattern;
import core.PredictiveParserTable;

public class ExpGrammar extends Grammar {
	public final LexerRule zahlRule;
	public final LexerRule parenOpenRule;
	public final LexerRule parenCloseRule;
	public final LexerRule opPlusRule;
	public final LexerRule opMinusRule;
	public final LexerRule opMultRule;
	public final LexerRule opDivRule;
	public final LexerRule idRule;
	
	private final ParserRule expRule;
	private final ParserRule erestRule;
	private final ParserRule termRule;
	private final ParserRule trestRule;
	private final ParserRule faktorRule;
	
	public ExpGrammar() {
		super();
		
		//lexer rules
		zahlRule = createTokenInfo("ZAHL");
		
		zahlRule.addRuleRegEx("[1-9][0-9]*");
		
		parenOpenRule = createTokenInfo("parenOpen");
		
		parenOpenRule.addRule("(");
		
		parenCloseRule = createTokenInfo("parenClose");
		
		parenCloseRule.addRule(")");
		
		opPlusRule = createTokenInfo("opPlus");
		
		opPlusRule.addRule("+");
		
		opMinusRule = createTokenInfo("opMinus");
		
		opMinusRule.addRule("-");

		opMultRule = createTokenInfo("opMult");
		
		opMultRule.addRule("*");
		
		opDivRule = createTokenInfo("opDiv");
		
		opDivRule.addRule("/");
		
		idRule = createTokenInfo("ID");
		
		idRule.addRuleRegEx("[a-zA-Z][a-zA-Z0-9]*");
		
		//parser rules
		expRule = createParserRule("exp");
		erestRule = createParserRule("erest");
		termRule = createParserRule("term");
		trestRule = createParserRule("trest");
		faktorRule = createParserRule("faktor");
		
		expRule.addRule(createRulePattern("term erest"));
		
		erestRule.addRule(createRulePattern("opPlus term erest"));
		erestRule.addRule(createRulePattern("opMinus term erest"));
		erestRule.addRule(LexerRule.EPSILON);
		
		termRule.addRule(createRulePattern("faktor trest"));
		
		trestRule.addRule(createRulePattern("opMult faktor trest"));
		trestRule.addRule(createRulePattern("opDiv faktor trest"));
		trestRule.addRule(LexerRule.EPSILON);
		
		faktorRule.addRule(createRulePattern("ID"));
		faktorRule.addRule(createRulePattern("ZAHL"));
		faktorRule.addRule(createRulePattern("parenOpen exp parenClose"));
		
		setStartParserRule(expRule);
		
		//predictive parser table
		PredictiveParserTable ruleMap = getPredictiveParserTable();
		
		ruleMap.set(expRule, zahlRule, expRule.getRulePattern(0));
		ruleMap.set(expRule, idRule, expRule.getRulePattern(0));
		ruleMap.set(expRule, parenOpenRule, expRule.getRulePattern(0));
		
		ruleMap.set(erestRule, opPlusRule, erestRule.getRulePattern(0));
		ruleMap.set(erestRule, opMinusRule, erestRule.getRulePattern(1));
		ruleMap.set(erestRule, parenCloseRule, erestRule.getRulePattern(2));
		ruleMap.set(erestRule, LexerRule.EPSILON, erestRule.getRulePattern(2));
		
		ruleMap.set(termRule, zahlRule, termRule.getRulePattern(0));
		ruleMap.set(termRule, idRule, termRule.getRulePattern(0));
		ruleMap.set(termRule, parenOpenRule, termRule.getRulePattern(0));
		
		ruleMap.set(trestRule, opPlusRule, trestRule.getRulePattern(2));
		ruleMap.set(trestRule, opMinusRule, trestRule.getRulePattern(2));
		ruleMap.set(trestRule, opMultRule, trestRule.getRulePattern(0));
		ruleMap.set(trestRule, opDivRule, trestRule.getRulePattern(1));
		ruleMap.set(trestRule, parenCloseRule, trestRule.getRulePattern(2));
		ruleMap.set(trestRule, LexerRule.EPSILON, erestRule.getRulePattern(2));
		
		ruleMap.set(faktorRule, zahlRule, faktorRule.getRulePattern(1));
		ruleMap.set(faktorRule, idRule, faktorRule.getRulePattern(0));
		ruleMap.set(faktorRule, parenOpenRule, faktorRule.getRulePattern(2));
	}
}