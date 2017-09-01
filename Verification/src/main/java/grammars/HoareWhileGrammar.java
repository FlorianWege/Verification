package grammars;

import core.structures.Terminal;
import core.structures.NonTerminal;

public class HoareWhileGrammar extends WhileGrammar {
	public final Terminal terminal_hoare_pre;
	public final Terminal terminal_hoare_post;
	
	public final NonTerminal nonTerminal_hoare_block;
	public final NonTerminal nonTerminal_hoare_pre;
	public final NonTerminal nonTerminal_hoare_post;
	
	public final HoareExpGrammar _hoareExpGrammar;
	
	public HoareWhileGrammar() {
		super();
		
		_hoareExpGrammar = new HoareExpGrammar();
		
		merge(_hoareExpGrammar);
		
		//lexer rules
		terminal_hoare_pre = createTerminal("HOARE_PRE");
		terminal_hoare_post = createTerminal("HOARE_POST");

		terminal_hoare_pre.addRule("PRE");

		terminal_hoare_post.addRule("POST");

		//parser rules
		nonTerminal_hoare_pre = createNonTerminal("hoare_pre");
		nonTerminal_hoare_post = createNonTerminal("hoare_post");
		nonTerminal_hoare_block = createNonTerminal("hoare_block");
		
		createRule(nonTerminal_hoare_pre, "HOARE_PRE hoare_exp");
		
		createRule(nonTerminal_hoare_post, "HOARE_POST hoare_exp");

		createRule(nonTerminal_hoare_block, "hoare_pre prog hoare_post");
		
		//extend while
		createRule(NON_TERMINAL_PROG, "hoare_block prog'");
		
		//finalize
		updatePredictiveParserTable();
	}
}