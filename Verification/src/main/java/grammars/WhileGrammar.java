package grammars;

import core.structures.Terminal;
import core.structures.NonTerminal;
import core.structures.ParserRule;

public class WhileGrammar extends BoolExpGrammar {
	public final Terminal TERMINAL_STATEMENT_SEPARATOR;
	
	public final Terminal TERMINAL_OP_SKIP;
	
	public final Terminal TERMINAL_OP_ASSIGN;
	
	public final Terminal TERMINAL_IF;
	public final Terminal TERMINAL_THEN;
	public final Terminal TERMINAL_ELSE;
	public final Terminal TERMINAL_FI;
	
	public final Terminal TERMINAL_WHILE;
	public final Terminal TERMINAL_DO;
	public final Terminal TERMINAL_OD;
	
	public final NonTerminal NON_TERMINAL_PROG;
	public final NonTerminal NON_TERMINAL_PROG_;
	public final NonTerminal NON_TERMINAL_SKIP;
	public final NonTerminal NON_TERMINAL_ASSIGN;
	public final NonTerminal NON_TERMINAL_SELECTION;
	public final NonTerminal NON_TERMINAL_SELECTION_ELSE;
	public final NonTerminal NON_TERMINAL_WHILE;
	
	public final ParserRule RULE_PROG_SKIP;
	public final ParserRule RULE_PROG_ASSIGN;
	public final ParserRule RULE_PROG_SELECTION;
	public final ParserRule RULE_PROG_WHILE;
	
	public final ParserRule RULE_PROG__PROG;
	public final ParserRule RULE_SKIP;
	public final ParserRule RULE_ASSIGN;
	public final ParserRule RULE_SELECTION;
	public final ParserRule RULE_SELECTION_ELSE;
	public final ParserRule RULE_WHILE;
	
	public WhileGrammar() {
		super();
		
		TERMINAL_STATEMENT_SEPARATOR = createTerminal("STATEMENT_SEPARATOR");
		TERMINAL_OP_SKIP = createTerminal("OP_SKIP");
		TERMINAL_OP_ASSIGN = createTerminal("OP_ASSIGN");
		TERMINAL_IF = createTerminal("IF");
		TERMINAL_THEN = createTerminal("THEN");
		TERMINAL_ELSE = createTerminal("ELSE");
		TERMINAL_FI = createTerminal("FI");
		TERMINAL_WHILE = createTerminal("WHILE");
		TERMINAL_DO = createTerminal("DO");
		TERMINAL_OD = createTerminal("OD");
		
		TERMINAL_STATEMENT_SEPARATOR.addRule(";");
		
		TERMINAL_OP_SKIP.addRule("SKIP");
		
		TERMINAL_OP_ASSIGN.addRule("=");
		
		TERMINAL_IF.addRule("IF");
		
		TERMINAL_THEN.addRule("THEN");
		
		TERMINAL_ELSE.addRule("ELSE");
		
		TERMINAL_FI.addRule("FI");
		
		TERMINAL_WHILE.addRule("WHILE");
		
		TERMINAL_DO.addRule("DO");
		
		TERMINAL_OD.addRule("OD");
		
		//parser rules
		NON_TERMINAL_PROG = createNonTerminal("prog");
		NON_TERMINAL_PROG_ = createNonTerminal("prog'");
		NON_TERMINAL_SKIP = createNonTerminal("skip");
		NON_TERMINAL_ASSIGN = createNonTerminal("assign");
		NON_TERMINAL_SELECTION = createNonTerminal("selection");
		NON_TERMINAL_SELECTION_ELSE = createNonTerminal("selection_else");
		NON_TERMINAL_WHILE = createNonTerminal("while");
		
		RULE_PROG_SKIP = createRule(NON_TERMINAL_PROG, "skip prog'");
		RULE_PROG_ASSIGN = createRule(NON_TERMINAL_PROG, "assign prog'");
		RULE_PROG_SELECTION = createRule(NON_TERMINAL_PROG, "selection prog'");
		RULE_PROG_WHILE = createRule(NON_TERMINAL_PROG, "while prog'");
		
		RULE_PROG__PROG = createRule(NON_TERMINAL_PROG_, "STATEMENT_SEPARATOR prog");
		createRule(NON_TERMINAL_PROG_, Terminal.EPSILON);
		
		RULE_SKIP = createRule(NON_TERMINAL_SKIP, "OP_SKIP");
		
		RULE_ASSIGN = createRule(NON_TERMINAL_ASSIGN, "ID OP_ASSIGN exp");

		RULE_SELECTION = createRule(NON_TERMINAL_SELECTION, "IF bool_exp THEN prog selection_else FI");
		
		RULE_SELECTION_ELSE = createRule(NON_TERMINAL_SELECTION_ELSE, "ELSE prog");
		createRule(NON_TERMINAL_SELECTION_ELSE, Terminal.EPSILON);
		
		RULE_WHILE = createRule(NON_TERMINAL_WHILE, "WHILE bool_exp DO prog OD");
		
		//finalize
		setStartSymbol(NON_TERMINAL_PROG);
		
		updatePredictiveParserTable();
	}
}