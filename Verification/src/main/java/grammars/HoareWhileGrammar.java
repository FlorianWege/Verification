package grammars;

import core.structures.Terminal;
import core.structures.NonTerminal;

public class HoareWhileGrammar extends WhileGrammar {
	public final Terminal TERMINAL_HOARE_PRE;
	public final Terminal TERMINAL_HOARE_POST;
	
	public final NonTerminal NON_TERMINAL_HOARE_BLOCK;
	public final NonTerminal NON_TERMINAL_HOARE_PRE;
	public final NonTerminal NON_TERMINAL_HOARE_POST;
	
	public final HoareExpGrammar _hoareExpGrammar;
	
	public HoareWhileGrammar() {
		super();
		
		_hoareExpGrammar = new HoareExpGrammar();
		
		merge(_hoareExpGrammar);
		
		//lexer rules
		TERMINAL_HOARE_PRE = createTerminal("HOARE_PRE");
		TERMINAL_HOARE_POST = createTerminal("HOARE_POST");

		TERMINAL_HOARE_PRE.addRule("PRE");

		TERMINAL_HOARE_POST.addRule("POST");

		//parser rules
		NON_TERMINAL_HOARE_PRE = createNonTerminal("hoare_pre");
		NON_TERMINAL_HOARE_POST = createNonTerminal("hoare_post");
		NON_TERMINAL_HOARE_BLOCK = createNonTerminal("hoare_block");
		
		createRule(NON_TERMINAL_HOARE_PRE, "HOARE_PRE hoare_exp");
		
		createRule(NON_TERMINAL_HOARE_POST, "HOARE_POST hoare_exp");

		createRule(NON_TERMINAL_HOARE_BLOCK, "hoare_pre prog hoare_post");
		
		//extend while
		createRule(NON_TERMINAL_PROG, "hoare_block prog'");
		
		//finalize
		updatePredictiveParserTable();
	}
}