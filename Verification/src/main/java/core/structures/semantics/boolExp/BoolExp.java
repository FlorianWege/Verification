package core.structures.semantics.boolExp;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.exp.*;
import util.StringUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public abstract class BoolExp extends SemanticNode {
	public abstract BoolExp reduce();
	public abstract void order();

	public abstract int comp(BoolExp b);

	public int compPrecedence(BoolExp b) {
		List<Class<? extends BoolExp>> types = new ArrayList<>();

		types.add(BoolLit.class);
		types.add(ExpComp.class);
		types.add(BoolOr.class);
		types.add(BoolAnd.class);
		types.add(BoolNeg.class);

		if (types.indexOf(getClass()) < types.indexOf(b.getClass())) return -1;
		if (types.indexOf(getClass()) > types.indexOf(b.getClass())) return 1;

		return comp(b);
	}

	private int _changesC = 0;

	private @Nonnull BoolExp resolveNeg_step() {
		BoolExp ret = this;

		if (this instanceof BoolNeg) {
			BoolExp child = ((BoolNeg) BoolExp.this).getChild();

			if (child instanceof BoolNeg) {
				ret = ((BoolNeg) child).getChild();
			} else if (child instanceof BoolAnd) {
				ret = new BoolOr();

				for (BoolExp child2 : ((BoolAnd) child).getBoolExps()) {
					ret.addChild(new BoolNeg(child2));
				}
			} else if (child instanceof BoolOr) {
				ret = new BoolAnd();

				for (BoolExp child2 : ((BoolOr) child).getBoolExps()) {
					ret.addChild(new BoolNeg(child2));
				}
			}
		}

		ret.getChildren().replaceAll(new UnaryOperator<SemanticNode>() {
			@Override
			public SemanticNode apply(SemanticNode child) {
				if (child instanceof BoolExp) {
					return ((BoolExp) child).resolveNeg_step();
				}

				return child;
			}
		});

		if (!ret.equals(this)) _changesC++;

		return ret;
	}

	public @Nonnull BoolExp resolveNeg() {
		BoolExp ret = (BoolExp) copy();

		do {
			_changesC = 0;

			ret = resolveNeg_step();
		} while (_changesC > 0);

		return ret;
	}

	private @Nonnull BoolExp makeDNF_resolveDistributive() {
		BoolExp ret = this;

		if (this instanceof BoolAnd) {
			List<BoolExp> boolExps = ((BoolAnd) this).getBoolExps();
			BoolOr newOr = new BoolOr();

			boolExps.replaceAll(new UnaryOperator<BoolExp>() {
				@Override
				public BoolExp apply(BoolExp boolExp) {
					if (boolExp instanceof BoolOr) return boolExp;

					return new BoolOr(boolExp);
				}
			});

			int[] amounts = new int[boolExps.size()];

			for (int i = 0; i < boolExps.size(); i++) {
				amounts[i] = ((BoolOr) boolExps.get(i)).getBoolExps().size();
			}

			int[] indexes = new int[boolExps.size()];

			int dims = indexes.length;

			for (int i = 0; i < dims; i++) {
				indexes[i] = 0;
			}

			int dim = dims - 1;

			while (true) {
				if (indexes[dim] >= amounts[dim]) {
					indexes[dim] = 0;
					dim--;

					if (dim < 0) break;
				} else {
					BoolAnd newAnd = new BoolAnd();

					for (int i = 0; i < dims; i++) {
						newAnd.addBoolExp(((BoolOr) boolExps.get(i)).getBoolExps().get(indexes[i]));
					}

					newOr.addBoolExp(newAnd);

					if (dim < dims - 1) {
						dim = dims - 1;
					}
				}

				indexes[dim]++;
			}

			if (newOr.getBoolExps().size() > 1) {
				ret = newOr;
			}
		}

		ret.print();

		if (!ret.equals(this)) {
			ret.getChildren().replaceAll(new UnaryOperator<SemanticNode>() {
				@Override
				public SemanticNode apply(SemanticNode child) {
					if (child instanceof BoolExp) {
						return ((BoolExp) child).makeDNF_resolveDistributive();
					}

					return child;
				}
			});

			_changesC++;
		}

		return ret;
	}

	public @Nonnull BoolOr makeDNF() {
		BoolExp ret = resolveNeg();

		do {
			_changesC = 0;

			ret = ret.makeDNF_resolveDistributive();
		} while (_changesC > 0);

		if (ret instanceof BoolOr) return (BoolOr) ret;

		return new BoolOr(ret);
	}

	protected @Nonnull BoolExp makeCNF_resolveDistributive() {
		BoolExp ret = this;

		if (this instanceof BoolOr) {
			List<BoolExp> boolExps = ((BoolOr) this).getBoolExps();
			BoolAnd newAnd = new BoolAnd();

			boolExps.replaceAll(new UnaryOperator<BoolExp>() {
				@Override
				public BoolExp apply(BoolExp boolExp) {
					if (boolExp instanceof BoolAnd) return boolExp;

					return new BoolAnd(boolExp);
				}
			});

			int[] amounts = new int[boolExps.size()];

			for (int i = 0; i < boolExps.size(); i++) {
				amounts[i] = ((BoolAnd) boolExps.get(i)).getBoolExps().size();
			}

			int[] indexes = new int[boolExps.size()];

			int dims = indexes.length;

			for (int i = 0; i < dims; i++) {
				indexes[i] = 0;
			}

			int dim = dims - 1;

			while (true) {
				if (indexes[dim] >= amounts[dim]) {
					indexes[dim] = 0;
					dim--;

					if (dim < 0) break;
				} else {
					BoolOr newOr = new BoolOr();

					for (int i = 0; i < dims; i++) {
						newOr.addBoolExp(((BoolAnd) boolExps.get(i)).getBoolExps().get(indexes[i]));
					}

					newAnd.addBoolExp(newOr);

					if (dim < dims - 1) {
						dim = dims - 1;
					}
				}

				indexes[dim]++;
			}

			if (newAnd.getBoolExps().size() > 1) {
				ret = newAnd;
			}
		}

		if (!ret.equals(this)) {
			ret.getChildren().replaceAll(new UnaryOperator<SemanticNode>() {
				@Override
				public SemanticNode apply(SemanticNode child) {
					if (child instanceof BoolExp) {
						return ((BoolExp) child).makeCNF_resolveDistributive();
					}

					return child;
				}
			});

			_changesC++;
		}

		return ret;
	}

	public @Nonnull BoolAnd makeCNF() {
		BoolExp ret = resolveNeg();

		do {
			_changesC = 0;

			ret = ret.makeCNF_resolveDistributive();
		} while (_changesC > 0);

		if (ret instanceof BoolAnd) return (BoolAnd) ret;

		return new BoolAnd(ret);
	}

	public String parenthesize(String s) {
		return _grammar.TERMINAL_BRACKET_OPEN.getPrimRule() + s + _grammar.TERMINAL_BRACKET_CLOSE.getPrimRule();
	}
}