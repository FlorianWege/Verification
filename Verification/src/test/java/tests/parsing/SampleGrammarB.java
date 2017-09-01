package tests.parsing;

import core.Grammar;
import core.structures.Terminal;
import core.structures.NonTerminal;

public class SampleGrammarB extends Grammar {
	public final Terminal a;
	public final Terminal b;
	public final Terminal c;
	public final Terminal d;
	
	public final NonTerminal S;
	public final NonTerminal B;
	public final NonTerminal C;
	
	public SampleGrammarB() {
		a = createTerminal("a");
		a.addRule("a");
		b = createTerminal("b");
		b.addRule("b");
		c = createTerminal("c");
		c.addRule("c");
		d = createTerminal("d");
		d.addRule("d");
		
		S = createNonTerminal("S");
		B = createNonTerminal("B");
		C = createNonTerminal("C");

		createRule(S, "B b");
		createRule(S, "C d");
		createRule(B, "a B");
		createRule(B, Terminal.EPSILON);
		createRule(C, "c C");
		createRule(C, Terminal.EPSILON);
		
		setStartSymbol(S);
	}
}