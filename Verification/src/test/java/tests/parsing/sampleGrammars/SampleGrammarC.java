package tests.parsing.sampleGrammars;

import core.Grammar;
import core.structures.Terminal;
import core.structures.NonTerminal;

public class SampleGrammarC extends Grammar {
	public final Terminal id;
	public final Terminal add;
	public final Terminal mult;
	public final Terminal paren_open;
	public final Terminal paren_close;
	
	public final NonTerminal E;
	public final NonTerminal E_;
	public final NonTerminal T;
	public final NonTerminal T_;
	public final NonTerminal F;
	
	public SampleGrammarC() {
		id = createTerminal("id");
		id.addRule("id");
		add = createTerminal("+");
		add.addRule("+");
		mult = createTerminal("*");
		mult.addRule("*");
		paren_open = createTerminal("(");
		paren_open.addRule("(");
		paren_close = createTerminal(")");
		paren_close.addRule(")");
		
		E = createNonTerminal("E");
		E_ = createNonTerminal("E_");
		T = createNonTerminal("T");
		T_ = createNonTerminal("T_");
		F = createNonTerminal("F");

		createRule(E, "T E_");
		createRule(E_, "+ T E_");
		createRule(E_, Terminal.EPSILON);
		createRule(T, "F T_");
		createRule(T_, "* F T_");
		createRule(T_, Terminal.EPSILON);
		createRule(F, "id");
		createRule(F, "( E )");
		
		setStartSymbol(E);
	}
}