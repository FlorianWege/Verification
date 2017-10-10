package grammars;

import core.structures.Terminal;
import core.structures.NonTerminal;
import core.structures.ParserRule;

public class WhileGrammar extends BoolExpGrammar {
	public final Terminal TERMINAL_STATEMENT_SEP;
	
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
	public final NonTerminal NON_TERMINAL_CMD;
	public final NonTerminal NON_TERMINAL_SKIP;
	public final NonTerminal NON_TERMINAL_ASSIGN;
	public final NonTerminal NON_TERMINAL_ALT;
	public final NonTerminal NON_TERMINAL_ALT_ELSE;
	public final NonTerminal NON_TERMINAL_WHILE;
	
	public final ParserRule RULE_PROG_CMD_PROG_;
	
	public final ParserRule RULE_CMD_SKIP;
	public final ParserRule RULE_CMD_ASSIGN;
	public final ParserRule RULE_CMD_ALT;
	public final ParserRule RULE_CMD_WHILE;
	
	public final ParserRule RULE_PROG__SEP_CMD_PROG_;
	
	public final ParserRule RULE_SKIP;
	public final ParserRule RULE_ASSIGN;
	public final ParserRule RULE_ALT;
	public final ParserRule RULE_ALT_ELSE;
	public final ParserRule RULE_WHILE;
	
	public WhileGrammar() {
		super();
		
		TERMINAL_STATEMENT_SEP = createTerminal("STATEMENT_SEP").setSep();
		TERMINAL_OP_SKIP = createTerminal("OP_SKIP").setKeyword();
		TERMINAL_OP_ASSIGN = createTerminal("OP_ASSIGN");
		TERMINAL_IF = createTerminal("IF").setKeyword();
		TERMINAL_THEN = createTerminal("THEN").setKeyword();
		TERMINAL_ELSE = createTerminal("ELSE").setKeyword();
		TERMINAL_FI = createTerminal("FI").setKeyword();
		TERMINAL_WHILE = createTerminal("WHILE").setKeyword();
		TERMINAL_DO = createTerminal("DO").setKeyword();
		TERMINAL_OD = createTerminal("OD").setKeyword();
		
		TERMINAL_STATEMENT_SEP.addRule(";");
		
		TERMINAL_OP_SKIP.addRule("SKIP");
		
		TERMINAL_OP_ASSIGN.addRule(":=");
		
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
		NON_TERMINAL_CMD = createNonTerminal("cmd");
		NON_TERMINAL_SKIP = createNonTerminal("skip");
		NON_TERMINAL_ASSIGN = createNonTerminal("assign");
		NON_TERMINAL_ALT = createNonTerminal("alt");
		NON_TERMINAL_ALT_ELSE = createNonTerminal("alt_else");
		NON_TERMINAL_WHILE = createNonTerminal("while");
		
		RULE_PROG_CMD_PROG_ = createRule(NON_TERMINAL_PROG, "cmd prog'");
		
		RULE_CMD_SKIP = createRule(NON_TERMINAL_CMD, "skip");
		RULE_CMD_ASSIGN = createRule(NON_TERMINAL_CMD, "assign");
		RULE_CMD_ALT = createRule(NON_TERMINAL_CMD, "alt");
		RULE_CMD_WHILE = createRule(NON_TERMINAL_CMD, "while");
		
		RULE_PROG__SEP_CMD_PROG_ = createRule(NON_TERMINAL_PROG_, "STATEMENT_SEP cmd prog'");
		createRule(NON_TERMINAL_PROG_, Terminal.EPSILON);
		
		RULE_SKIP = createRule(NON_TERMINAL_SKIP, "OP_SKIP");
		
		RULE_ASSIGN = createRule(NON_TERMINAL_ASSIGN, "ID OP_ASSIGN exp");

		RULE_ALT = createRule(NON_TERMINAL_ALT, "IF bool_exp THEN prog alt_else FI");
		
		RULE_ALT_ELSE = createRule(NON_TERMINAL_ALT_ELSE, "ELSE prog");
		createRule(NON_TERMINAL_ALT_ELSE, Terminal.EPSILON);
		
		RULE_WHILE = createRule(NON_TERMINAL_WHILE, "WHILE bool_exp DO prog OD");
		
		//finalize
		setStartSymbol(NON_TERMINAL_PROG);
		
		updateParserTable();
	}

	private static WhileGrammar _instance = new WhileGrammar();

	public static WhileGrammar getInstance() {
		return _instance;
	}
}