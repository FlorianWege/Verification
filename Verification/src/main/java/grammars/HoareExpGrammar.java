package grammars;

import core.PredictiveParserTable;
import core.structures.LexerRule;
import core.structures.ParserRule;

public class HoareExpGrammar extends BoolExpGrammar {
	public final LexerRule curlyOpenRule;
	public final LexerRule curlyCloseRule;
	
	public final ParserRule hoareExpRule;
	
	public HoareExpGrammar() {
		super();
		
		//lexer rules
		curlyOpenRule = createTokenInfo("CURLY_OPEN");
		
		curlyOpenRule.addRule("{");
		
		curlyCloseRule = createTokenInfo("CURLY_CLOSE");
		
		curlyCloseRule.addRule("}");
		
		//parser rules
		hoareExpRule = createParserRule("hoareExp");
		
		hoareExpRule.addRule(createRulePattern("CURLY_OPEN boolExp CURLY_CLOSE"));
		
		setStartParserRule(boolExpRule);

		//predictive parser table
		PredictiveParserTable ruleMap = getPredictiveParserTable();
		
		ruleMap.set(hoareExpRule, curlyOpenRule, hoareExpRule.getRulePattern(0));
	}
}