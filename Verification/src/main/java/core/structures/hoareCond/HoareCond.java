package core.structures.hoareCond;

import core.SyntaxNode;
import core.structures.Terminal;

public abstract class HoareCond {
	public String toStringEx(String replacement) {
		return (replacement == null) ? "{" + this + "}" : "{" + this + "[" + replacement + "]" + "}";
	}
	
	public String toStringEx() {
		return toStringEx(null);
	}
	
	public abstract HoareCond copy();
	public abstract void replace(Terminal lexerRule, String var, SyntaxNode exp);
	
	public HoareCond() {
		
	}
}