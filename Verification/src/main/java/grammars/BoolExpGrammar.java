package grammars;

import core.LexerRule;
import core.ParserRule;
import core.PredictiveParserTable;

public class BoolExpGrammar extends ExpGrammar {
	public final LexerRule opNegateRule;
	public final LexerRule opCompareRule;
	
	public final ParserRule boolExpRule;
	public final ParserRule boolExpRestRule;
	
	public BoolExpGrammar() {
		opNegateRule = createTokenInfo("OP_NEGATE");
		
		opNegateRule.addRule("!");
		
		opCompareRule = createTokenInfo("OP_COMPARE");
		
		opCompareRule.addRule("<");
		opCompareRule.addRule("<=");
		opCompareRule.addRule(">");
		opCompareRule.addRule(">=");
		opCompareRule.addRule("==");
		opCompareRule.addRule("!=");
		
		boolExpRule = createParserRule("boolExp");
		boolExpRestRule = createParserRule("boolExp_rest");

		boolExpRule.addRule(createRulePattern("ZAHL"));
		boolExpRule.addRule(createRulePattern("OP_NEGATE boolExp"));
		boolExpRule.addRule(createRulePattern("exp OP_COMPARE exp"));
		//boolExpRule.addRule(createRulePattern("PAREN_OPEN boolExp PAREN_CLOSE"));

		setStartParserRule(boolExpRule);

		//predictive parser table
		PredictiveParserTable ruleMap = getPredictiveParserTable();

		ruleMap.set(boolExpRule, zahlRule, boolExpRule.getRulePattern(0));
		ruleMap.set(boolExpRule, opNegateRule, boolExpRule.getRulePattern(1));
		ruleMap.set(boolExpRule, idRule, boolExpRule.getRulePattern(2));
		//ruleMap.set(boolExpRule, parenOpenRule, boolExpRule.getRulePattern(3));
	}
}