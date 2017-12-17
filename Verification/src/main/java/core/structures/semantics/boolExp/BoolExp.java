package core.structures.semantics.boolExp;

import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import grammars.BoolExpGrammar;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.UnaryOperator;

import static core.structures.semantics.boolExp.BoolExp.Reducer.Law.START;

public abstract class BoolExp extends SemanticNode {
	@CheckReturnValue
	@Nonnull
	public abstract BoolExp reduce_spec(@Nonnull Reducer reducer);

	private Reducer _reducer = null;

	@CheckReturnValue
	@Nonnull
	public final BoolExp reduce(@Nullable Reducer reducer) {
		if (reducer == null) reducer = new Reducer(this);

		BoolExp ret = reduce_spec(reducer);

		reducer.addEntry(this, Reducer.Law.UNKNOWN);

		return ret;
	}

	@CheckReturnValue
	@Nonnull
	public final BoolExp reduce() {
		return reduce(null);
	}

	public static class Reducer implements Serializable {
		public enum Law {
			START,
			IDEMPOTENCY,
			UNKNOWN,
			UNWRAP
		}

		private class Entry implements Serializable {
			private BoolExp _boolExp;
			private Law _law;

			public BoolExp getBoolExp() {
				return _boolExp;
			}

			public Law getLaw() {
				return _law;
			}

			public Entry(@Nonnull BoolExp boolExp, @Nonnull Law law) {
				_boolExp = boolExp;
				_law = law;
			}
		}

		private List<Entry> _entries = new LinkedList<>();
		private BoolExp _ret = null;

		@Nonnull
		public List<Entry> getEntries() {
			return _entries;
		}

		@Nonnull
		public BoolExp getRet() {
			return _ret;
		}

		public void exec() {

		}

		public void addEntry(@Nonnull BoolExp boolExp, @Nonnull Law law) {
			if (_entries.isEmpty() || !_entries.get(_entries.size() - 1).getBoolExp().getContentString().equals(boolExp.getContentString())) {
				_entries.add(new Entry(boolExp, law));

			}

			_ret = boolExp;
		}

		public Reducer(@Nonnull BoolExp boolExp) {
			_entries.add(new Entry(boolExp, START));
		}
	}

	public final Reducer reduceEx() {
		Reducer ret = new Reducer(this);

		_reducer = ret;

		BoolExp boolExp = reduce(ret);

		_reducer = null;

		return ret;
	}

	private static Stack<BoolExp> _orderStack = new Stack<>();

	@CheckReturnValue
	@Nonnull
	public abstract BoolExp order_spec();

	@CheckReturnValue
	@Nonnull
	public final BoolExp order() {
		/*_printer.println("enter " + getTypeName());
		_printer.begin();

		_orderStack.push(this);*/

		BoolExp ret = order_spec();

		/*_orderStack.pop();

		_printer.end();
		_printer.println("leave " + getTypeName());

		if (_reduceStack.isEmpty()) {
			_printer.println("finished");
		} else {
			_printer.println("reenter" + _reduceStack.peek().getTypeName());
		}*/

		return ret;
	}

	@Override
	public int hashCode() {
		BoolExp copy = (BoolExp) copy();

		copy = copy.order();

		return copy.toString().hashCode();
	}

	private boolean superEquals(Object other) {
		return super.equals(other);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof BoolExp) {
			BoolExp a = this;
			BoolExp b = ((BoolExp) other);

			a = a.order();
			b = b.order();

			boolean ret = a.superEquals(b);

			return ret;
		}

		return super.equals(other);
	}

	public abstract int comp_spec(BoolExp b);

	public final int comp(BoolExp b) {
		List<Class<? extends BoolExp>> types = new ArrayList<>();

		types.add(BoolLit.class);
		types.add(ExpComp.class);
		types.add(BoolOr.class);
		types.add(BoolAnd.class);
		types.add(BoolNeg.class);

		if (types.indexOf(getClass()) < types.indexOf(b.getClass())) return -1;
		if (types.indexOf(getClass()) > types.indexOf(b.getClass())) return 1;

		return comp_spec(b);
	}

	private static int _changesC = 0;

	@Nonnull
	private BoolExp resolveNeg_step() {
		BoolExp ret = this;

		if (ret instanceof BoolNeg) {
			BoolExp child = ((BoolNeg) ret).getChild();

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

	@Nonnull
	public BoolExp resolveNeg() {
		BoolExp ret = (BoolExp) copy();

		do {
			_changesC = 0;

			ret = ret.resolveNeg_step();
		} while (_changesC > 0);

		return ret;
	}

	@Nonnull
	private BoolExp makeDNF_resolveDistributive() {
		BoolExp ret = this;

		if (ret instanceof BoolAnd) {
			List<BoolExp> boolExps = ((BoolAnd) ret).getBoolExps();
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

	@Nonnull
	public BoolExp makeDNF() {
		BoolExp ret = resolveNeg();

		do {
			_changesC = 0;

			ret = ret.makeDNF_resolveDistributive();
		} while (_changesC > 0);

		return ret;
	}

	@Nonnull
	public BoolOr makeDNFOr() {
		BoolExp boolExp = makeDNF();

		if (boolExp instanceof BoolOr) return (BoolOr) boolExp;

		return new BoolOr(boolExp);
	}

	@Nonnull
	protected BoolExp makeCNF_resolveDistributive() {
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
//		Assert.assertTrue(ret.toString().length() < 30);

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

	@Nonnull
	public BoolExp makeCNF() {
		BoolExp ret = resolveNeg();

		do {
			_changesC = 0;

			ret = ret.makeCNF_resolveDistributive();
		} while (_changesC > 0);

		return ret;
	}

	@Nonnull
	public BoolAnd makeCNFAnd() {
		BoolExp boolExp = makeCNF();

		if (boolExp instanceof BoolAnd) return (BoolAnd) boolExp;

		return new BoolAnd(boolExp);
	}

	@Nonnull
	public String parenthesize(@Nonnull String s) {
		return _grammar.TERMINAL_BRACKET_OPEN.getPrimRule() + s + _grammar.TERMINAL_BRACKET_CLOSE.getPrimRule();
	}

	@Nonnull
	public static BoolExp fromString(@Nonnull String s) throws Lexer.LexerException, Parser.ParserException {
		return (BoolExp) fromString(s, BoolExpGrammar.getInstance());
	}
}