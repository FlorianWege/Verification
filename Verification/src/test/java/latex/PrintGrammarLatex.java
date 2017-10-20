package latex;

import grammars.HoareWhileGrammar;
import org.testng.annotations.Test;

public class PrintGrammarLatex {
	@Test()
	public void test() {
		new HoareWhileGrammar().printLatex(System.out);
	}
}