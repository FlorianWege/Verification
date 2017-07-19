package grammars;

import core.PredictiveParserTable;
import core.structures.LexerRule;
import core.structures.ParserRule;
import core.structures.ParserRulePattern;

public class WhileGrammar extends BoolExpGrammar {
	public final LexerRule statementSeparatorRule;
	
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
	public final ParserRule selectionElseRule;
	public final ParserRule whileLoopRule;
	
	public final ParserRulePattern PATTERN_PROG_SKIP;
	public final ParserRulePattern PATTERN_PROG_ASSIGN;
	public final ParserRulePattern PATTERN_PROG_SELECTION;
	public final ParserRulePattern PATTERN_PROG_LOOP;
	
	public final ParserRulePattern PATTERN_PREST_PROG;
	public final ParserRulePattern PATTERN_SKIP;
	public final ParserRulePattern PATTERN_ASSIGN;
	public final ParserRulePattern PATTERN_SELECTION;
	//public final ParserRulePattern PATTERN_SELECTION_SHORT;
	public final ParserRulePattern PATTERN_SELECTION_ELSE;
	public final ParserRulePattern PATTERN_LOOP;
	
	public WhileGrammar() {
		super();
		
		statementSeparatorRule = createTokenInfo("STATEMENT_SEPARATOR");
		
		statementSeparatorRule.addRule(";");
		
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
		selectionElseRule = createParserRule("selectionElse");
		whileLoopRule = createParserRule("whileLoop");
		
		PATTERN_PROG_SKIP = createRulePattern("skip prest");
		PATTERN_PROG_ASSIGN = createRulePattern("assign prest");
		PATTERN_PROG_SELECTION = createRulePattern("selection prest");
		PATTERN_PROG_LOOP = createRulePattern("whileLoop prest");
		
		progRule.addRule(PATTERN_PROG_SKIP);
		progRule.addRule(PATTERN_PROG_ASSIGN);
		progRule.addRule(PATTERN_PROG_SELECTION);
		progRule.addRule(PATTERN_PROG_LOOP);
		
		PATTERN_PREST_PROG = createRulePattern("STATEMENT_SEPARATOR prog");
		
		prestRule.addRule(PATTERN_PREST_PROG);
		prestRule.addRule(LexerRule.EPSILON);
		
		PATTERN_SKIP = createRulePattern("OP_SKIP");
		
		skipRule.addRule(PATTERN_SKIP);
		
		PATTERN_ASSIGN = createRulePattern("ID OP_ASSIGN exp");
		
		assignRule.addRule(PATTERN_ASSIGN);

		PATTERN_SELECTION = createRulePattern("IF boolExp THEN prog selectionElse FI");
		
		selectionRule.addRule(PATTERN_SELECTION);
		
		selectionElseRule.addRule(LexerRule.EPSILON);
		
		PATTERN_SELECTION_ELSE = createRulePattern("ELSE prog");
		
		selectionElseRule.addRule(PATTERN_SELECTION_ELSE);
		
		PATTERN_LOOP = createRulePattern("WHILE boolExp DO prog OD");
		
		whileLoopRule.addRule(PATTERN_LOOP);
		
		setStartParserRule(progRule);
		
		//predictive parser table
		PredictiveParserTable ruleMap = getPredictiveParserTable();
		
		ruleMap.set(progRule, opSkipRule, PATTERN_PROG_SKIP);
		ruleMap.set(progRule, idRule, PATTERN_PROG_ASSIGN);
		ruleMap.set(progRule, ifRule, PATTERN_PROG_SELECTION);
		ruleMap.set(progRule, whileRule, PATTERN_PROG_LOOP);
		
		ruleMap.set(prestRule, statementSeparatorRule, PATTERN_PREST_PROG);
		ruleMap.set(prestRule, LexerRule.EPSILON, prestRule.getRulePattern(1));
		
		ruleMap.set(skipRule, opSkipRule, PATTERN_SKIP);
		
		ruleMap.set(assignRule, idRule, PATTERN_ASSIGN);
		
		ruleMap.set(selectionRule, ifRule, PATTERN_SELECTION);

		ruleMap.set(selectionElseRule, fiRule, selectionElseRule.getRulePattern(0));
		ruleMap.set(selectionElseRule, elseRule, PATTERN_SELECTION_ELSE);
		
		ruleMap.set(whileLoopRule, whileRule, PATTERN_LOOP);
	}
}