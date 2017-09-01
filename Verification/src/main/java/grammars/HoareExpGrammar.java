package grammars;

import core.PredictiveParserTable;
import core.structures.Terminal;
import core.structures.NonTerminal;

public class HoareExpGrammar extends BoolExpGrammar {
	public final Terminal terminal_curly_open;
	public final Terminal terminal_curly_close;
	
	public final NonTerminal hoareExpRule;
	
	public HoareExpGrammar() {
		super();
		
		//lexer rules
		terminal_curly_open = createTerminal("CURLY_OPEN");
		terminal_curly_close = createTerminal("CURLY_CLOSE");
		
		terminal_curly_open.addRule("{");
		
		terminal_curly_close.addRule("}");
		
		//parser rules
		hoareExpRule = createNonTerminal("hoare_exp");
		
		createRule(hoareExpRule, "CURLY_OPEN bool_exp CURLY_CLOSE");
		
		//finalize
		setStartSymbol(NON_TERMINAL_BOOL_EXP);

		updatePredictiveParserTable();
	}
}