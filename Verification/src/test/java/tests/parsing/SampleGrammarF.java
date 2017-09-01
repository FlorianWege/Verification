package tests.parsing;

import core.Grammar;
import core.structures.Terminal;
import core.structures.NonTerminal;

public class SampleGrammarF extends Grammar {
	public final Terminal a;
	public final Terminal b;
	public final Terminal c;
	public final Terminal f;
	public final Terminal g;
	public final Terminal h;
	
	public final NonTerminal S;
	public final NonTerminal B;
	public final NonTerminal C;
	public final NonTerminal D;
	public final NonTerminal E;
	public final NonTerminal F;
	
	public SampleGrammarF() {
		a = createTerminal("a");
		a.addRule("a");
		b = createTerminal("b");
		b.addRule("b");
		c = createTerminal("c");
		c.addRule("c");
		f = createTerminal("f");
		f.addRule("f");
		g = createTerminal("g");
		g.addRule("g");
		h = createTerminal("h");
		h.addRule("h");
		
		S = createNonTerminal("S");
		B = createNonTerminal("B");
		C = createNonTerminal("C");
		D = createNonTerminal("D");
		E = createNonTerminal("E");
		F = createNonTerminal("F");
		
		createRule(S, "a B D h");
		createRule(B, "c C");
		createRule(C, "b C");
		createRule(C, Terminal.EPSILON);
		createRule(D, "E F");
		createRule(E, "g");
		createRule(E, Terminal.EPSILON);
		createRule(F, "f");
		createRule(F, Terminal.EPSILON);
		
		setStartSymbol(S);
	}
}