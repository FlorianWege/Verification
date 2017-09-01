package core.structures.hoareCond;

import java.util.Collection;
import java.util.Vector;

import core.SyntaxTreeNode;
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
	public void replace(Terminal lexerRule, String var, SyntaxTreeNode exp) {
		_base.replace(lexerRule, var, exp);
	}
	
	public HoareCondNeg(HoareCond base) {
		super();
		
		_base = base;
	}
}