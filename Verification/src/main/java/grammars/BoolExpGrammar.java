package grammars;

import core.PredictiveParserTable;
import core.structures.LexerRule;
import core.structures.ParserRule;

public class BoolExpGrammar extends ExpGrammar {
	public final LexerRule boolLiteralRule;
	public final LexerRule opNegateRule;
	public final LexerRule opAndRule;
	public final LexerRule opOrRule;
	public final LexerRule opCompareRule;
	
	public final ParserRule boolExpRule;
	public final ParserRule boolExpRestRule;
	public final ParserRule boolOrRule;
	public final ParserRule boolAndRule;
	public final ParserRule boolElementaryRule;
	
	public BoolExpGrammar() {
		//lexer rules
		boolLiteralRule = createTokenInfo("BOOL_LITERAL");
		
		boolLiteralRule.addRule("true");
		boolLiteralRule.addRule("false");
		
		opNegateRule = createTokenInfo("OP_NEGATE");
		
		opNegateRule.addRule("!");
		
		opAndRule = createTokenInfo("OP_AND");
		
		opAndRule.addRule("&&");

		opOrRule = createTokenInfo("OP_OR");
		
		opOrRule.addRule("||");

		opCompareRule = createTokenInfo("OP_COMPARE");
		
		opCompareRule.addRule("<");
		opCompareRule.addRule("<=");
		opCompareRule.addRule(">");
		opCompareRule.addRule(">=");
		opCompareRule.addRule("==");
		opCompareRule.addRule("!=");
		
		//parser rules
		boolExpRule = createParserRule("boolExp");
		boolExpRestRule = createParserRule("boolExp_rest");
		boolElementaryRule = createParserRule("boolElementary");
		boolOrRule = createParserRule("boolOr");
		boolAndRule = createParserRule("boolAnd");

		boolExpRule.addRule(createRulePattern("OP_NEGATE boolExp"));
		boolExpRule.addRule(createRulePattern("boolElementary boolExp_rest"));

		boolExpRestRule.addRule(LexerRule.EPSILON);
		boolExpRestRule.addRule(createRulePattern("OP_OR boolOr"));

		boolOrRule.addRule(createRulePattern("OP_AND boolAnd"));
		boolOrRule.addRule(createRulePattern("boolExp"));

		boolAndRule.addRule(createRulePattern("boolExp"));

		//boolExpRule.addRule(createRulePattern("PAREN_OPEN boolExp PAREN_CLOSE"));

		boolElementaryRule.addRule(createRulePattern("exp OP_COMPARE exp"));
		boolElementaryRule.addRule(createRulePattern("BOOL_LITERAL"));

		setStartParserRule(boolExpRule);

		//predictive parser table
		PredictiveParserTable ruleMap = getPredictiveParserTable();

		ruleMap.set(boolExpRule, opNegateRule, boolExpRule.getRulePattern(0));
		ruleMap.set(boolExpRule, zahlRule, boolExpRule.getRulePattern(1));
		ruleMap.set(boolExpRule, idRule, boolExpRule.getRulePattern(1));
		//ruleMap.set(boolExpRule, parenOpenRule, boolExpRule.getRulePattern(3));
		
		ruleMap.set(boolExpRestRule, LexerRule.EPSILON, boolExpRestRule.getRulePattern(0));
		ruleMap.set(boolExpRestRule, opOrRule, boolExpRestRule.getRulePattern(1));
		
		ruleMap.set(boolOrRule, opAndRule, boolOrRule.getRulePattern(0));
		ruleMap.set(boolOrRule, opNegateRule, boolOrRule.getRulePattern(1));
		ruleMap.set(boolOrRule, zahlRule, boolOrRule.getRulePattern(1));
		ruleMap.set(boolOrRule, idRule, boolOrRule.getRulePattern(1));
		
		ruleMap.set(boolAndRule, opNegateRule, boolAndRule.getRulePattern(0));
		ruleMap.set(boolAndRule, zahlRule, boolAndRule.getRulePattern(0));
		ruleMap.set(boolAndRule, idRule, boolAndRule.getRulePattern(0));
		
		ruleMap.set(boolElementaryRule, zahlRule, boolElementaryRule.getRulePattern(0));
		ruleMap.set(boolElementaryRule, idRule, boolElementaryRule.getRulePattern(0));
		ruleMap.set(boolElementaryRule, boolLiteralRule, boolElementaryRule.getRulePattern(1));
	}
}