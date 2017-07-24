package grammars;

import core.PredictiveParserTable;
import core.structures.LexerRule;
import core.structures.ParserRule;
import core.structures.ParserRulePattern;

public class BoolExpGrammar extends ExpGrammar {
	public final LexerRule boolLiteralRule;
	public final LexerRule opNegateRule;
	public final LexerRule opAndRule;
	public final LexerRule opOrRule;
	public final LexerRule opCompareRule;
	
	public final ParserRule boolExpRule;
	public final ParserRule boolOrRestRule;
	public final ParserRule boolOrRule;
	public final ParserRule boolAndRule;
	public final ParserRule boolAndRestRule;
	public final ParserRule boolElementaryRule;
	
	public final ParserRulePattern PATTERN_BOOL_AND_BOOL_OR_REST;
	public final ParserRulePattern PATTERN_BOOL_OR;
	public final ParserRulePattern PATTERN_OP_OR_BOOL_OR;
	public final ParserRulePattern PATTERN_BOOL_ELEMENTARY_BOOL_AND_REST;
	public final ParserRulePattern PATTERN_OP_AND_BOOL_AND;
	public final ParserRulePattern PATTERN_EXP_OP_COMPARE_EXP;
	public final ParserRulePattern PATTERN_BOOL_LITERAL;
	
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
		boolOrRule = createParserRule("boolOr");
		boolOrRestRule = createParserRule("boolOrRest");
		boolAndRule = createParserRule("boolAnd");
		boolAndRestRule = createParserRule("boolAndRest");
		boolElementaryRule = createParserRule("boolElementary");

		PATTERN_BOOL_OR = createRulePattern("boolOr");

		boolExpRule.addRule(PATTERN_BOOL_OR);

		PATTERN_BOOL_AND_BOOL_OR_REST = createRulePattern("boolAnd boolOrRest");

		boolOrRule.addRule(PATTERN_BOOL_AND_BOOL_OR_REST);

		PATTERN_OP_OR_BOOL_OR = createRulePattern("OP_OR boolOr");
		
		boolOrRestRule.addRule(PATTERN_OP_OR_BOOL_OR);
		boolOrRestRule.addRule(LexerRule.EPSILON);
		
		PATTERN_BOOL_ELEMENTARY_BOOL_AND_REST = createRulePattern("boolElementary boolAndRest");

		boolAndRule.addRule(PATTERN_BOOL_ELEMENTARY_BOOL_AND_REST);

		PATTERN_OP_AND_BOOL_AND = createRulePattern("OP_AND boolAnd");

		boolAndRestRule.addRule(PATTERN_OP_AND_BOOL_AND);
		boolAndRestRule.addRule(LexerRule.EPSILON);

		PATTERN_EXP_OP_COMPARE_EXP = createRulePattern("exp OP_COMPARE exp");
		PATTERN_BOOL_LITERAL = createRulePattern("BOOL_LITERAL");

		boolElementaryRule.addRule(PATTERN_EXP_OP_COMPARE_EXP);
		boolElementaryRule.addRule(PATTERN_BOOL_LITERAL);

		setStartParserRule(boolExpRule);

		//predictive parser table
		PredictiveParserTable ruleMap = getPredictiveParserTable();

		ruleMap.set(boolExpRule, opNegateRule, PATTERN_BOOL_OR);
		ruleMap.set(boolExpRule, zahlRule, PATTERN_BOOL_OR);
		ruleMap.set(boolExpRule, idRule, PATTERN_BOOL_OR);

		ruleMap.set(boolOrRule, opNegateRule, PATTERN_BOOL_AND_BOOL_OR_REST);
		ruleMap.set(boolOrRule, zahlRule, PATTERN_BOOL_AND_BOOL_OR_REST);
		ruleMap.set(boolOrRule, idRule, PATTERN_BOOL_AND_BOOL_OR_REST);

		ruleMap.set(boolOrRestRule, opOrRule, PATTERN_OP_OR_BOOL_OR);
		ruleMap.set(boolOrRestRule, LexerRule.EPSILON, boolOrRestRule.getRulePattern(1));

		ruleMap.set(boolAndRule, opNegateRule, PATTERN_BOOL_ELEMENTARY_BOOL_AND_REST);
		ruleMap.set(boolAndRule, zahlRule, PATTERN_BOOL_ELEMENTARY_BOOL_AND_REST);
		ruleMap.set(boolAndRule, idRule, PATTERN_BOOL_ELEMENTARY_BOOL_AND_REST);

		ruleMap.set(boolAndRestRule, opAndRule, PATTERN_OP_AND_BOOL_AND);
		ruleMap.set(boolAndRestRule, LexerRule.EPSILON, boolAndRestRule.getRulePattern(1));

		ruleMap.set(boolElementaryRule, zahlRule, PATTERN_EXP_OP_COMPARE_EXP);
		ruleMap.set(boolElementaryRule, idRule, PATTERN_EXP_OP_COMPARE_EXP);
		ruleMap.set(boolElementaryRule, boolLiteralRule, PATTERN_BOOL_LITERAL);
	}
}