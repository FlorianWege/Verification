package core.structures.hoareCond;

import core.SyntaxNode;
import core.structures.Terminal;

public class HoareCondNeg extends HoareCond {
	private HoareCond _base = null;
	
	@Override
	public String toString() {
		return "not " + _base.toString();
	}
	
	@Override
	public HoareCond copy() {
		return new HoareCondNeg(_base.copy());
	}
	
	@Override
	public void replace(Terminal lexerRule, String var, SyntaxNode exp) {
		_base.replace(lexerRule, var, exp);
	}
	
	public HoareCondNeg(HoareCond base) {
		super();
		
		_base = base;
	}
}