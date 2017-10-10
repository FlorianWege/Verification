package grammars;

import core.structures.ParserRule;
import core.structures.Terminal;
import core.structures.NonTerminal;

public class HoareWhileGrammar extends WhileGrammar {
	public final Terminal TERMINAL_HOARE_PRE;
	public final Terminal TERMINAL_HOARE_POST;
	public final Terminal TERMINAL_CURLY_OPEN;
	public final Terminal TERMINAL_CURLY_CLOSE;
	
	public final NonTerminal NON_TERMINAL_HOARE_BLOCK;
	public final NonTerminal NON_TERMINAL_HOARE_PRE;
	public final NonTerminal NON_TERMINAL_HOARE_POST;

	public final ParserRule RULE_HOARE_PRE_CURLIES_BOOL_EXP_CURLY_CLOSE;
	public final ParserRule RULE_HOARE_POST_CURLY_OPEN_BOOL_EXP_CURLY_CLOSE;
	public final ParserRule RULE_HOARE_PRE_PROG_HOARE_POST;

	public final ParserRule RULE_HOARE_BLOCK;

	public HoareWhileGrammar() {
		super();
		
		//lexer rules
		TERMINAL_HOARE_PRE = createTerminal("HOARE_PRE").setKeyword();
		TERMINAL_HOARE_POST = createTerminal("HOARE_POST").setKeyword();
		TERMINAL_CURLY_OPEN = createTerminal("CURLY_OPEN");
		TERMINAL_CURLY_CLOSE = createTerminal("CURLY_CLOSE");

		TERMINAL_HOARE_PRE.addRule("PRE");
		TERMINAL_HOARE_POST.addRule("POST");
		TERMINAL_CURLY_OPEN.addRule("{");
		TERMINAL_CURLY_CLOSE.addRule("}");

		//parser rules
		NON_TERMINAL_HOARE_PRE = createNonTerminal("hoare_pre");
		NON_TERMINAL_HOARE_POST = createNonTerminal("hoare_post");
		NON_TERMINAL_HOARE_BLOCK = createNonTerminal("hoare_block");
		
		RULE_HOARE_PRE_CURLIES_BOOL_EXP_CURLY_CLOSE = createRule(NON_TERMINAL_HOARE_PRE, "HOARE_PRE CURLY_OPEN bool_exp CURLY_CLOSE");
		
		RULE_HOARE_POST_CURLY_OPEN_BOOL_EXP_CURLY_CLOSE = createRule(NON_TERMINAL_HOARE_POST, "HOARE_POST CURLY_OPEN bool_exp CURLY_CLOSE");

		RULE_HOARE_PRE_PROG_HOARE_POST = createRule(NON_TERMINAL_HOARE_BLOCK, "hoare_pre prog hoare_post");
		
		//extend while
		RULE_HOARE_BLOCK = createRule(NON_TERMINAL_CMD, "hoare_block");
		
		//finalize
		updateParserTable();
	}

	private static HoareWhileGrammar _instance = new HoareWhileGrammar();

	public static HoareWhileGrammar getInstance() {
		return _instance;
	}
}