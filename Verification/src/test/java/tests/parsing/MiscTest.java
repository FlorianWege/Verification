package tests.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import core.Symbol;
import core.structures.Terminal;

public class MiscTest {
	@Test()
	public void MiscTest() {
		List<Symbol> symbols = new ArrayList<>();
		
		symbols.add(new Terminal("abc"));
		symbols.add(new Terminal("def"));
		symbols.add(new Terminal("ghi"));
		symbols.add(new Terminal("jkl"));
		symbols.add(new Terminal("mno"));
		
		int i = 3;
		
		List<Symbol> restSymbols = symbols.subList(3, 4);
		
		System.out.println(restSymbols);
	}
}
