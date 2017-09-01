package tests.parsing;

import core.Grammar;
import core.structures.Terminal;
import core.structures.NonTerminal;

public class SampleGrammarD extends Grammar {
	public final Terminal a;
	public final Terminal b;
	public final Terminal d;
	public final Terminal g;
	public final Terminal h;
	
	public final NonTerminal S;
	public final NonTerminal A;
	public final NonTerminal B;
	public final NonTerminal C;
	
	public SampleGrammarD() {
		a = createTerminal("a");
		a.addRule("a");
		b = createTerminal("b");
		b.addRule("b");
		d = createTerminal("d");
		d.addRule("d");
		g = createTerminal("g");
		g.addRule("g");
		h = createTerminal("h");
		h.addRule("h");
		
		S = createNonTerminal("S");
		A = createNonTerminal("A");
		B = createNonTerminal("B");
		C = createNonTerminal("C");

		createRule(S, "A C B");
		createRule(S, "C b B");
		createRule(S, "B a");
		createRule(A, "d a");
		createRule(A, "B C");
		createRule(B, "g");
		createRule(B, Terminal.EPSILON);
		createRule(C, "h");
		createRule(C, Terminal.EPSILON);
		
		setStartSymbol(S);
	}
}