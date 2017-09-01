package util;

import grammars.HoareWhileGrammar;

public class PrintGrammarLatex {
	public static void main(String[] args) {
		new HoareWhileGrammar().printLatex(System.out);
	}
}
