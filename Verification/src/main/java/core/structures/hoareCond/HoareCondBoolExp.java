package core.structures.hoareCond;

import core.SyntaxNode;
import core.structures.nodes.BoolExp;
import core.structures.Terminal;

public class HoareCondBoolExp extends HoareCond {
	private BoolExp _base = null;
	
	@Override
	public String toString() {
		return _base.getBase().synthesize();
	}
	
	@Override
	public HoareCond copy() {
		return new HoareCondBoolExp(_base.copy());
	}
	
	@Override
	public void replace(Terminal lexerRule, String var, SyntaxNode exp) {
		_base.getBase().replace(lexerRule, var, exp);
	}
	
	public HoareCondBoolExp(BoolExp base) {
		super();
		
		_base = base;
	}
}