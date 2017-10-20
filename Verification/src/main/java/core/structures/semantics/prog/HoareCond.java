package core.structures.semantics.prog;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolAnd;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.BoolNeg;
import core.structures.semantics.boolExp.BoolOr;
import util.IOUtil;

import javax.annotation.Nonnull;

public class HoareCond extends SemanticNode {
	private BoolExp _boolExp;

	public @Nonnull BoolExp getBoolExp() {
		return _boolExp;
	}

	public HoareCond(@Nonnull BoolExp boolExp) {
		_boolExp = boolExp;

		addChild(_boolExp);
	}

	public static @Nonnull HoareCond makeNeg(@Nonnull HoareCond hoareCond) {
		return new HoareCond(new BoolNeg(hoareCond.getBoolExp()));
	}

	public static @Nonnull HoareCond makeOr(HoareCond... hoareConds) {
		BoolOr boolOr = new BoolOr();

		for (HoareCond hoareCond : hoareConds) {
			boolOr.addBoolExp(hoareCond.getBoolExp());
		}

		return new HoareCond(boolOr);
	}

	public static @Nonnull HoareCond makeAnd(@Nonnull HoareCond... hoareConds) {
		BoolAnd boolAnd = new BoolAnd();

		for (HoareCond hoareCond : hoareConds) {
			boolAnd.addBoolExp(hoareCond.getBoolExp());
		}

		return new HoareCond(boolAnd);
	}

	protected HoareCond() {
	}

	@Override
	public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
		return mapper.apply(this, _grammar.TERMINAL_CURLY_OPEN.getPrimRule() + _boolExp.getContentString(mapper) + _grammar.TERMINAL_CURLY_CLOSE.getPrimRule());
	}

	@Nonnull
	@Override
	public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
		_boolExp = (BoolExp) _boolExp.replace(replaceFunc);

		return replaceFunc.apply(this);
	}
}