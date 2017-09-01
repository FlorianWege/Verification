package grammars;

import core.structures.Terminal;
import core.structures.NonTerminal;
import core.structures.ParserRule;

public class BoolExpGrammar extends ExpGrammar {
	public final Terminal TERMINAL_BOOL_LITERAL;
	public final Terminal TERMINAL_BRACKET_OPEN;
	public final Terminal TERMINAL_BRACKET_CLOSE;
	public final Terminal TERMINAL_OP_NEGATE;
	public final Terminal TERMINAL_OP_AND;
	public final Terminal TERMINAL_OP_OR;
	public final Terminal TERMINAL_OP_COMPARE;
	
	public final NonTerminal NON_TERMINAL_BOOL_EXP;
	public final NonTerminal NON_TERMINAL_BOOL_OR;
	public final NonTerminal NON_TERMINAL_BOOL_OR_;
	public final NonTerminal NON_TERMINAL_BOOL_AND;
	public final NonTerminal NON_TERMINAL_BOOL_AND_;
	public final NonTerminal NON_TERMINAL_BOOL_NEG;
	public final NonTerminal NON_TERMINAL_BOOL_ELEMENTARY;
	
	public final ParserRule RULE_BOOL_AND_BOOL_OR_;
	public final ParserRule RULE_BOOL_OR;
	public final ParserRule RULE_OP_OR_BOOL_OR;
	public final ParserRule RULE_BOOL_NEG_BOOL_AND_;
	public final ParserRule RULE_OP_AND_BOOL_AND;
	public final ParserRule RULE_BOOL_ELEMENTARY;
	public final ParserRule RULE_NEGATE_BOOL_ELEMENTARY;
	public final ParserRule RULE_EXP_OP_COMPARE_EXP;
	public final ParserRule RULE_BOOL_LITERAL;
	public final ParserRule RULE_PAREN_BOOL_EXP;
	
	public BoolExpGrammar() {
		//lexer rules
		TERMINAL_BOOL_LITERAL = createTerminal("BOOL_LITERAL");
		TERMINAL_BRACKET_OPEN = createTerminal("BRACKET_OPEN");
		TERMINAL_BRACKET_CLOSE = createTerminal("BRACKET_CLOSE");
		TERMINAL_OP_NEGATE = createTerminal("OP_NEGATE");
		TERMINAL_OP_AND = createTerminal("OP_AND");
		TERMINAL_OP_OR = createTerminal("OP_OR");
		TERMINAL_OP_COMPARE = createTerminal("OP_COMPARE");
		
		TERMINAL_BOOL_LITERAL.addRule("true");
		TERMINAL_BOOL_LITERAL.addRule("false");
		
		TERMINAL_BRACKET_OPEN.addRule("[");
		TERMINAL_BRACKET_CLOSE.addRule("]");
		
		TERMINAL_OP_NEGATE.addRule("~");
		
		TERMINAL_OP_AND.addRule("&&");
		
		TERMINAL_OP_OR.addRule("||");
		
		TERMINAL_OP_COMPARE.addRule("<");
		TERMINAL_OP_COMPARE.addRule("<=");
		TERMINAL_OP_COMPARE.addRule(">");
		TERMINAL_OP_COMPARE.addRule(">=");
		TERMINAL_OP_COMPARE.addRule("==");
		TERMINAL_OP_COMPARE.addRule("!=");
		
		//parser rules
		NON_TERMINAL_BOOL_EXP = createNonTerminal("bool_exp");
		NON_TERMINAL_BOOL_OR = createNonTerminal("bool_or");
		NON_TERMINAL_BOOL_OR_ = createNonTerminal("bool_or'");
		NON_TERMINAL_BOOL_AND = createNonTerminal("bool_and");
		NON_TERMINAL_BOOL_AND_ = createNonTerminal("bool_and'");
		NON_TERMINAL_BOOL_NEG = createNonTerminal("bool_neg");
		NON_TERMINAL_BOOL_ELEMENTARY = createNonTerminal("bool_elem");

		RULE_BOOL_OR = createRule(NON_TERMINAL_BOOL_EXP, "bool_or");

		RULE_BOOL_AND_BOOL_OR_ = createRule(NON_TERMINAL_BOOL_OR, "bool_and bool_or'");

		RULE_OP_OR_BOOL_OR = createRule(NON_TERMINAL_BOOL_OR_, "OP_OR bool_and bool_or'");
		NON_TERMINAL_BOOL_OR_.createRule(Terminal.EPSILON);
		
		RULE_BOOL_NEG_BOOL_AND_ = createRule(NON_TERMINAL_BOOL_AND, "bool_neg bool_and'");
		
		RULE_OP_AND_BOOL_AND = createRule(NON_TERMINAL_BOOL_AND_, "OP_AND bool_neg bool_and'");
		NON_TERMINAL_BOOL_AND_.createRule(Terminal.EPSILON);

		RULE_BOOL_ELEMENTARY = createRule(NON_TERMINAL_BOOL_NEG, "bool_elem");
		RULE_NEGATE_BOOL_ELEMENTARY = createRule(NON_TERMINAL_BOOL_NEG, "OP_NEGATE bool_elem");
		
		RULE_EXP_OP_COMPARE_EXP = createRule(NON_TERMINAL_BOOL_ELEMENTARY, "exp OP_COMPARE exp");
		RULE_BOOL_LITERAL = createRule(NON_TERMINAL_BOOL_ELEMENTARY, "BOOL_LITERAL");
		RULE_PAREN_BOOL_EXP = createRule(NON_TERMINAL_BOOL_ELEMENTARY, "BRACKET_OPEN bool_exp BRACKET_CLOSE");

		//finalize
		setStartSymbol(NON_TERMINAL_BOOL_EXP);

		updatePredictiveParserTable();
	}
}