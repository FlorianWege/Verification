package grammars;

import core.structures.LexerRule;
import core.structures.Terminal;
import core.structures.NonTerminal;
import core.structures.ParserRule;
import util.StringUtil;

public class BoolExpGrammar extends ExpGrammar {
	public final Terminal TERMINAL_BOOL_LIT;
	public final Terminal TERMINAL_BRACKET_OPEN;
	public final Terminal TERMINAL_BRACKET_CLOSE;
	public final Terminal TERMINAL_OP_NEG;
	public final Terminal TERMINAL_OP_AND;
	public final Terminal TERMINAL_OP_OR;
	public final Terminal TERMINAL_OP_COMP;

	public final LexerRule RULE_TRUE;
	public final LexerRule RULE_FALSE;

	public final LexerRule RULE_LESS;
	public final LexerRule RULE_LESS_EQUAL;
	public final LexerRule RULE_GREATER;
	public final LexerRule RULE_GREATER_EQUAL;
	public final LexerRule RULE_EQUAL;
	public final LexerRule RULE_UNEQUAL;

	public final NonTerminal NON_TERMINAL_BOOL_EXP;
	public final NonTerminal NON_TERMINAL_BOOL_OR;
	public final NonTerminal NON_TERMINAL_BOOL_OR_;
	public final NonTerminal NON_TERMINAL_BOOL_AND;
	public final NonTerminal NON_TERMINAL_BOOL_AND_;
	public final NonTerminal NON_TERMINAL_BOOL_NEG;
	public final NonTerminal NON_TERMINAL_BOOL_ELEM;
	
	public final ParserRule RULE_BOOL_AND_BOOL_OR_;
	public final ParserRule RULE_BOOL_OR;
	public final ParserRule RULE_OP_OR_BOOL_AND_BOOL_OR_;
	public final ParserRule RULE_BOOL_NEG_BOOL_AND_;
	public final ParserRule RULE_OP_AND_BOOL_NEG_BOOL_AND_;
	public final ParserRule RULE_BOOL_ELEM;
	public final ParserRule RULE_NEG_BOOL_ELEM;
	public final ParserRule RULE_EXP_OP_COMP_EXP;
	public final ParserRule RULE_BOOL_LIT;
	public final ParserRule RULE_PAREN_BOOL_EXP;

	public BoolExpGrammar() {
		//lexer rules
		TERMINAL_BOOL_LIT = createTerminal("BOOL_LITERAL").setKeyword();
		TERMINAL_BRACKET_OPEN = createTerminal("BRACKET_OPEN");
		TERMINAL_BRACKET_CLOSE = createTerminal("BRACKET_CLOSE");
		TERMINAL_OP_NEG = createTerminal("OP_NEGATE");
		TERMINAL_OP_AND = createTerminal("OP_AND");
		TERMINAL_OP_OR = createTerminal("OP_OR");
		TERMINAL_OP_COMP = createTerminal("OP_COMPARE");
		
		RULE_TRUE = TERMINAL_BOOL_LIT.addRule("true");
		RULE_FALSE = TERMINAL_BOOL_LIT.addRule("false");
		
		TERMINAL_BRACKET_OPEN.addRule("[");
		TERMINAL_BRACKET_CLOSE.addRule("]");

		TERMINAL_OP_NEG.addRule(StringUtil.bool_neg);
		TERMINAL_OP_NEG.addRule("~");

		TERMINAL_OP_AND.addRule(StringUtil.bool_and);
		TERMINAL_OP_AND.addRule("&");

		TERMINAL_OP_OR.addRule(StringUtil.bool_or);
		TERMINAL_OP_OR.addRule("|");
		
		RULE_LESS = TERMINAL_OP_COMP.addRule("<");
		RULE_LESS_EQUAL = TERMINAL_OP_COMP.addRule("<=");
		RULE_GREATER = TERMINAL_OP_COMP.addRule(">");
		RULE_GREATER_EQUAL = TERMINAL_OP_COMP.addRule(">=");
		RULE_EQUAL = TERMINAL_OP_COMP.addRule("=");
		RULE_UNEQUAL = TERMINAL_OP_COMP.addRule("<>");
		
		//parser rules
		NON_TERMINAL_BOOL_EXP = createNonTerminal("bool_exp");
		NON_TERMINAL_BOOL_OR = createNonTerminal("bool_or");
		NON_TERMINAL_BOOL_OR_ = createNonTerminal("bool_or'");
		NON_TERMINAL_BOOL_AND = createNonTerminal("bool_and");
		NON_TERMINAL_BOOL_AND_ = createNonTerminal("bool_and'");
		NON_TERMINAL_BOOL_NEG = createNonTerminal("bool_neg");
		NON_TERMINAL_BOOL_ELEM = createNonTerminal("bool_elem");

		RULE_BOOL_OR = createRule(NON_TERMINAL_BOOL_EXP, "bool_or");

		RULE_BOOL_AND_BOOL_OR_ = createRule(NON_TERMINAL_BOOL_OR, "bool_and bool_or'");

		RULE_OP_OR_BOOL_AND_BOOL_OR_ = createRule(NON_TERMINAL_BOOL_OR_, "OP_OR bool_and bool_or'");
		NON_TERMINAL_BOOL_OR_.createRule(Terminal.EPSILON);
		
		RULE_BOOL_NEG_BOOL_AND_ = createRule(NON_TERMINAL_BOOL_AND, "bool_neg bool_and'");
		
		RULE_OP_AND_BOOL_NEG_BOOL_AND_ = createRule(NON_TERMINAL_BOOL_AND_, "OP_AND bool_neg bool_and'");
		NON_TERMINAL_BOOL_AND_.createRule(Terminal.EPSILON);

		RULE_BOOL_ELEM = createRule(NON_TERMINAL_BOOL_NEG, "bool_elem");
		RULE_NEG_BOOL_ELEM = createRule(NON_TERMINAL_BOOL_NEG, "OP_NEGATE bool_elem");
		
		RULE_EXP_OP_COMP_EXP = createRule(NON_TERMINAL_BOOL_ELEM, "exp OP_COMPARE exp");
		RULE_BOOL_LIT = createRule(NON_TERMINAL_BOOL_ELEM, "BOOL_LITERAL");
		RULE_PAREN_BOOL_EXP = createRule(NON_TERMINAL_BOOL_ELEM, "BRACKET_OPEN bool_exp BRACKET_CLOSE");

		//finalize
		setStartSymbol(NON_TERMINAL_BOOL_EXP);

		updateParserTable();
	}

	private static BoolExpGrammar _instance = new BoolExpGrammar();

	public static BoolExpGrammar getInstance() {
		return _instance;
	}
}