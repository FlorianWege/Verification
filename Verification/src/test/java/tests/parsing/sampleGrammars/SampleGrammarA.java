package tests.parsing.sampleGrammars;

import core.Grammar;
import core.structures.Terminal;
import core.structures.NonTerminal;

public class SampleGrammarA extends Grammar {
	public final Terminal a;
	public final Terminal b;
	public final Terminal c;
	public final Terminal d;
	public final Terminal e;
	
	public final NonTerminal S;
	public final NonTerminal A;
	public final NonTerminal B;
	public final NonTerminal C;
	public final NonTerminal D;
	public final NonTerminal E;
	
	public SampleGrammarA() {
		a = createTerminal("a");
		a.addRule("a");
		b = createTerminal("b");
		b.addRule("b");
		c = createTerminal("c");
		c.addRule("c");
		d = createTerminal("d");
		d.addRule("d");
		e = createTerminal("e");
		e.addRule("e");
		
		S = createNonTerminal("S");
		A = createNonTerminal("A");
		B = createNonTerminal("B");
		C = createNonTerminal("C");
		D = createNonTerminal("D");
		E = createNonTerminal("E");

		createRule(S, "A B C D E");
		createRule(A, "a");
		createRule(A, Terminal.EPSILON);
		createRule(B, "b");
		createRule(B, Terminal.EPSILON);
		createRule(C, "c");
		createRule(D, "d");
		createRule(D, Terminal.EPSILON);
		createRule(E, "e");
		createRule(E, Terminal.EPSILON);
		
		setStartSymbol(S);
	}
}