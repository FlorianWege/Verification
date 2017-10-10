package core.structures.semantics.boolExp;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolExp;
import grammars.BoolExpGrammar;
import grammars.HoareWhileGrammar;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.UnaryOperator;

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
		System.out.println("replace hoarecond " + _boolExp.getContentString());
		_boolExp = (BoolExp) _boolExp.replace(replaceFunc);
		System.out.println("replace hoarecondB " + _boolExp.getContentString());
		return replaceFunc.apply(this);
	}
}