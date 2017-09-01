package tests.parsing;

import core.Grammar;
import core.structures.Terminal;
import core.structures.NonTerminal;

public class SampleGrammarE extends Grammar {
	public final Terminal a;
	public final Terminal b;
	public final Terminal c;
	public final Terminal d;
	
	public final NonTerminal S;
	public final NonTerminal A;
	public final NonTerminal B;
	
	public SampleGrammarE() {
		a = createTerminal("a");
		a.addRule("a");
		b = createTerminal("b");
		b.addRule("b");
		c = createTerminal("c");
		c.addRule("c");
		d = createTerminal("d");
		d.addRule("d");
		
		S = createNonTerminal("S");
		A = createNonTerminal("A");
		B = createNonTerminal("B");

		createRule(S, "a A B b");
		createRule(A, "c");
		createRule(A, Terminal.EPSILON);
		createRule(B, "d");
		createRule(B, Terminal.EPSILON);
		
		setStartSymbol(S);
	}
}