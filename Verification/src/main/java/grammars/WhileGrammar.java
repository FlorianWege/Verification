package grammars;

import core.LexerRule;
import core.ParserRule;
import core.PredictiveParserTable;

public class WhileGrammar extends BoolExpGrammar {
	public final LexerRule statementEndRule;
	
	public final LexerRule opSkipRule;
	
	public final LexerRule opAssignRule;
	
	public final LexerRule ifRule;
	public final LexerRule thenRule;
	public final LexerRule elseRule;
	public final LexerRule fiRule;
	
	public final LexerRule whileRule;
	public final LexerRule doRule;
	public final LexerRule odRule;
	
	public final ParserRule progRule;
	public final ParserRule prestRule;
	public final ParserRule skipRule;
	public final ParserRule assignRule;
	public final ParserRule selectionRule;
	public final ParserRule whileLoopRule;
	
	public WhileGrammar() {
		super();
		
		statementEndRule = createTokenInfo("STATEMENT_END");
		
		statementEndRule.addRule(";");
		
		opSkipRule = createTokenInfo("OP_SKIP");
		
		opSkipRule.addRule("SKIP");
		
		opAssignRule = createTokenInfo("OP_ASSIGN");
		
		opAssignRule.addRule("=");
		
		ifRule = createTokenInfo("IF");
		
		ifRule.addRule("IF");
		
		thenRule = createTokenInfo("THEN");
		
		thenRule.addRule("THEN");
		
		elseRule = createTokenInfo("ELSE");
		
		elseRule.addRule("ELSE");
		
		fiRule = createTokenInfo("FI");
		
		fiRule.addRule("FI");
		
		whileRule = createTokenInfo("WHILE");
		
		whileRule.addRule("WHILE");
		
		doRule = createTokenInfo("DO");
		
		doRule.addRule("DO");

		odRule = createTokenInfo("OD");
		
		odRule.addRule("OD");
		
		//parser rules
		progRule = createParserRule("prog");
		prestRule = createParserRule("prest");
		skipRule = createParserRule("skip");
		assignRule = createParserRule("assign");
		selectionRule = createParserRule("selection");
		whileLoopRule = createParserRule("whileLoop");
		
		progRule.addRule(createRulePattern("skip prest"));
		progRule.addRule(createRulePattern("assign prest"));
		progRule.addRule(createRulePattern("selection prest"));
		progRule.addRule(createRulePattern("whileLoop prest"));
		
		prestRule.addRule(createRulePattern("STATEMENT_END prog"));
		prestRule.addRule(LexerRule.EPSILON);
		
		skipRule.addRule(createRulePattern("OP_SKIP"));
		
		assignRule.addRule(createRulePattern("ID OP_ASSIGN exp"));
		
		//selectionRule.addRule(createRulePattern("IF boolExp THEN prog FI"));
		selectionRule.addRule(createRulePattern("IF boolExp THEN prog ELSE prog FI"));
		
		whileLoopRule.addRule(createRulePattern("WHILE boolExp DO prog OD"));
		
		setStartParserRule(progRule);
		
		//predictive parser table
		PredictiveParserTable ruleMap = getPredictiveParserTable();
		
		ruleMap.set(progRule, opSkipRule, progRule.getRulePattern(0));
		ruleMap.set(progRule, idRule, progRule.getRulePattern(1));
		ruleMap.set(progRule, ifRule, progRule.getRulePattern(2));
		ruleMap.set(progRule, whileRule, progRule.getRulePattern(3));
		
		ruleMap.set(prestRule, statementEndRule, prestRule.getRulePattern(0));
		ruleMap.set(prestRule, LexerRule.EPSILON, prestRule.getRulePattern(1));
		
		ruleMap.set(assignRule, idRule, assignRule.getRulePattern(0));
		
		ruleMap.set(selectionRule, ifRule, selectionRule.getRulePattern(0));
		
		ruleMap.set(whileLoopRule, whileRule, whileLoopRule.getRulePattern(0));
	}
}