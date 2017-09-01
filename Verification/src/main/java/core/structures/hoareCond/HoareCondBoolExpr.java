package core.structures.hoareCond;

import core.SyntaxTreeNode;
import core.structures.Terminal;

public class HoareCondBoolExpr extends HoareCond {
	private SyntaxTreeNode _base = null;
	
	@Override
	public String toString() {
		//return _base.toString();
		return _base.synthesize();
	}
	
	@Override
	public HoareCond copy() {
		return new HoareCondBoolExpr(_base.copy());
	}
	
	@Override
	public void replace(Terminal lexerRule, String var, SyntaxTreeNode exp) {
		_base.replace(lexerRule, var, exp);
	}
	
	public HoareCondBoolExpr(SyntaxTreeNode base) {
		super();
		
		_base = base;
	}
}