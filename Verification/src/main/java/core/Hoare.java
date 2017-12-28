package core;

import core.Lexer.LexerException;
import core.Parser.ParserException;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolAnd;
import core.structures.semantics.boolExp.BoolNeg;
import core.structures.semantics.exp.Exp;
import core.structures.semantics.exp.Id;
import core.structures.semantics.prog.*;
import util.IOUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class Hoare {
	private ActionInterface _actionHandler;

	public Hoare(@Nonnull ActionInterface actionHandler) {
		_actionHandler = actionHandler;
	}

	public interface ActionInterface {
		void beginNode(@Nonnull SemanticNode node, @Nonnull HoareCond postCond);
		void endNode(@Nonnull SemanticNode node, @Nonnull HoareCond preCond);

		void reqSkipDialog(@Nonnull Skip skip, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond, @Nonnull Skip_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;

		void reqAssignDialog(@Nonnull Assign assign, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond, @Nonnull Assign_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;

		void reqCompNextDialog(@Nonnull wlp_comp comp, @Nonnull CompNext_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
		void reqCompMergeDialog(@Nonnull wlp_comp comp, @Nonnull CompMerge_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;

		void reqAltFirstDialog(@Nonnull wlp_alt alt, @Nonnull AltThen_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
		void reqAltElseDialog(@Nonnull wlp_alt alt, @Nonnull AltElse_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
		void reqAltMergeDialog(@Nonnull wlp_alt alt, @Nonnull AltMerge_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;

		void reqLoopAskInvDialog(@Nonnull wlp_loop loop, @Nonnull LoopAskInv_callback callback) throws IOException, HoareException, ParserException, LexerException;
		void reqLoopCheckPostCondDialog(@Nonnull wlp_loop loop, @Nonnull LoopCheckPostCond_callback callback) throws IOException, HoareException, ParserException, LexerException;
		void reqLoopGetBodyCondDialog(@Nonnull wlp_loop loop, @Nonnull LoopGetBodyCond_callback callback) throws IOException, LexerException, HoareException, ParserException, SemanticNode.CopyException;
		void reqLoopCheckBodyCondDialog(@Nonnull wlp_loop loop, @Nonnull LoopCheckBodyCond_callback callback) throws IOException, HoareException, ParserException, LexerException;
		void reqLoopAcceptInvCondDialog(@Nonnull wlp_loop loop, @Nonnull LoopAcceptInv_callback callback) throws IOException, LexerException, HoareException, ParserException, SemanticNode.CopyException;

		void reqConseqCheckPreDialog(@Nonnull SemanticNode node, @Nonnull HoareCond origPreCond, @Nonnull HoareCond newPreCond, @Nonnull ConseqCheck_callback callback) throws IOException, LexerException, ParserException, HoareException;
		void reqConseqCheckPostDialog(@Nonnull SemanticNode node, @Nonnull HoareCond origPostCond, @Nonnull HoareCond newPostCond, @Nonnull ConseqCheck_callback callback) throws IOException, HoareException, ParserException, LexerException;
	}

	public interface Skip_callback {
		void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
	}

	public interface Assign_callback {
		void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
	}

	public interface AltThen_callback {
		void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
	}
	public interface AltElse_callback {
		void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
	}
	public interface AltMerge_callback {
		void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
	}

	public interface CompNext_callback {
		void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
	}
	public interface CompMerge_callback {
		void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
	}

	public interface LoopAskInv_callback {
		void result(@Nullable HoareCond postInvariant) throws HoareException, LexerException, IOException, ParserException;
	}
	public interface LoopCheckPostCond_callback {
		void result() throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException;
	}
	public interface LoopGetBodyCond_callback {
		void result() throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException;
	}
	public interface LoopCheckBodyCond_callback {
		void result() throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException;
	}
	public interface LoopAcceptInv_callback {
		void result() throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException;
	}

	public interface ConseqCheck_callback {
		void result(boolean yes) throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException;
	}

	public interface wlp_callback {
		void result(@Nonnull SemanticNode node, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
	}

	public void wlp_skip(@Nonnull Skip skipNode, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
		HoareCond preCond = postCond;

		_actionHandler.reqSkipDialog(skipNode, preCond, postCond, new Skip_callback() {
			@Override
			public void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
				callback.result(skipNode, postCond, postCond);
			}
		});
	}

	public void wlp_assign(@Nonnull Assign assignNode, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) throws IOException, HoareException, ParserException, LexerException, SemanticNode.CopyException {
		Id varNode = assignNode.getVar();
		Exp expNode = assignNode.getExp();

		HoareCond preCond = (HoareCond) postCond.copy();

		preCond = (HoareCond) preCond.replace(new IOUtil.Func<SemanticNode, SemanticNode>() {
			@Override
			public SemanticNode apply(@Nonnull SemanticNode child) {
				if (child instanceof Id) {
					if (((Id) child).getName().equals(varNode.getName())) {
						return expNode;
					}
				}

				return child;
			}
		});

		HoareCond finalPreCond = preCond;
		System.out.println("SET VAR " + finalPreCond);
		_actionHandler.reqAssignDialog(assignNode, preCond, postCond, new Assign_callback() {
			@Override
			public void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
				callback.result(assignNode, finalPreCond, postCond);
			}
		});
	}

	public class wlp_comp {
		public Comp _compNode;
		public HoareCond _postCond;
		private wlp_callback _callback;

		public Integer _curPart;
		public HoareCond[] _preConds;
		public HoareCond[] _postConds;

		public HoareCond _preCond;

		private void exec_merge() throws LexerException, HoareException, ParserException, IOException, SemanticNode.CopyException {
			_preCond = _preConds[0];

			_actionHandler.reqCompMergeDialog(this, new CompMerge_callback() {
				@Override
				public void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
					_callback.result(_compNode, _preCond, _postCond);
				}
			});
		}

		private void exec_next() throws HoareException, IOException, LexerException, ParserException, SemanticNode.CopyException {
			if (_curPart == null) {
				_curPart = _compNode.getChildren().size() - 1;

				_postConds[_curPart] = _postCond;
			} else {
				_curPart--;

				_postConds[_curPart] = _preConds[_curPart + 1];
			}

			_actionHandler.reqCompNextDialog(this, new CompNext_callback() {
				@Override
				public void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
					wlp(_compNode.getChildren().get(_curPart), _postConds[_curPart], new wlp_callback() {
						@Override
						public void result(@Nonnull SemanticNode nextNode, @Nonnull HoareCond nextPreCond, @Nonnull HoareCond nextPostCond) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
							_preConds[_curPart] = nextPreCond;

							if (_curPart <= 0) {
								exec_merge();
							} else {
								exec_next();
							}
						}
					});
				}
			});
		}

		public void exec() throws LexerException, HoareException, ParserException, IOException, SemanticNode.CopyException {
			exec_next();
		}

		public wlp_comp(@Nonnull Comp compNode, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) {
			_compNode = compNode;
			_postCond = postCond;
			_callback = callback;
			_curPart = null;

			_preConds = new HoareCond[_compNode.getChildren().size()];
			_postConds = new HoareCond[_compNode.getChildren().size()];
		}
	}

	public void wlp_comp(@Nonnull Comp compNode, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) throws HoareException, IOException, LexerException, ParserException, SemanticNode.CopyException {
		new wlp_comp(compNode, postCond, callback).exec();
	}

	public class wlp_alt {
		public Alt _altNode;
		public HoareCond _postCond;
		private wlp_callback _callback;

		public HoareCond _thenPreCond;
		public HoareCond _elsePreCond;

		public HoareCond _preCond;

		private void exec_merge() throws LexerException, HoareException, ParserException, IOException, SemanticNode.CopyException {
			HoareCond boolExpCond = new HoareCond(_altNode.getBoolExp());

			_preCond = HoareCond.makeOr(HoareCond.makeAnd(_thenPreCond, boolExpCond), HoareCond.makeAnd(_elsePreCond, HoareCond.makeNeg(boolExpCond)));

			_actionHandler.reqAltMergeDialog(this, new AltMerge_callback() {
				@Override
				public void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
					_callback.result(_altNode, _preCond, _postCond);
				}
			});
		}

		private void exec_getElsePreCond() throws LexerException, HoareException, ParserException, IOException, SemanticNode.CopyException {
			if (_altNode.getElseProg() != null) {
				_actionHandler.reqAltElseDialog(this, new AltElse_callback() {
					@Override
					public void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
						wlp(_altNode.getElseProg(), _postCond, new wlp_callback() {
							@Override
							public void result(@Nonnull SemanticNode elseProgNode, @Nonnull HoareCond elsePreCond, @Nonnull HoareCond elsePostCond) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
								_elsePreCond = elsePreCond;

								exec_merge();
							}
						});
					}
				});
			} else {
				wlp(new Skip(), _postCond, new wlp_callback() {
					@Override
					public void result(@Nonnull SemanticNode elseProgNode, @Nonnull HoareCond elsePreCond, @Nonnull HoareCond elsePostCond) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
						_elsePreCond = elsePreCond;

						exec_merge();
					}
				});
			}
		}

		private void exec_getThenPreCond() throws LexerException, HoareException, ParserException, IOException, SemanticNode.CopyException {
			_actionHandler.reqAltFirstDialog(this, new AltThen_callback() {
				@Override
				public void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
					wlp(_altNode.getThenProg(), _postCond, new wlp_callback() {
						@Override
						public void result(@Nonnull SemanticNode thenProgNode, @Nonnull HoareCond thenPreCond, @Nonnull HoareCond thenPostCond) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
							_thenPreCond = thenPreCond;

							exec_getElsePreCond();
						}
					});
				}
			});
		}

		public void exec() throws LexerException, HoareException, ParserException, IOException, SemanticNode.CopyException {
			exec_getThenPreCond();
		}

		public wlp_alt(@Nonnull Alt altNode, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) throws LexerException, ParserException {
			_altNode = altNode;
			_postCond = postCond;
			_callback = callback;
		}
	}

	public void wlp_alt(@Nonnull Alt altNode, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) throws LexerException, ParserException, IOException, HoareException, SemanticNode.CopyException {
		new wlp_alt(altNode, postCond, callback).exec();
	}

	public class wlp_loop {
		public While _whileNode;
		public HoareCond _postCond;
		private wlp_callback _callback;

		public HoareCond _matchInvariant;

		public HoareCond _postInvariant;
		public HoareCond _preInvariant;

		public HoareCond _preCond;

		private void exec_acceptInvariant() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
			_preCond = _preInvariant;

			_actionHandler.reqLoopAcceptInvCondDialog(this, new LoopAcceptInv_callback() {
				@Override
				public void result() throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException {
					_callback.result(_whileNode, _preCond, _postCond);
				}
			});
		}

		private void exec_tryInvariant_checkBodyCond_conseqCheck(@Nonnull HoareCond matchCond) throws IOException, HoareException, ParserException, LexerException {
			_actionHandler.reqConseqCheckPreDialog(_whileNode, matchCond, _postInvariant, new ConseqCheck_callback() {
				@Override
				public void result(boolean yes) throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException {
					if (yes) exec_acceptInvariant(); else exec_askInvariant();
				}
			});
		}

		private void exec_tryInvariant_checkBodyCond() throws IOException, HoareException, ParserException, LexerException {
			_actionHandler.reqLoopCheckBodyCondDialog(this, new LoopCheckBodyCond_callback() {
				@Override
				public void result() throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException {
					HoareCond matchCond = HoareCond.makeAnd(_postInvariant, new HoareCond(_whileNode.getBoolExp()));

					exec_tryInvariant_checkBodyCond_conseqCheck(matchCond);
				}
			});
		}

		private void exec_tryInvariant_getBodyCond() throws IOException, LexerException, HoareException, ParserException, SemanticNode.CopyException {
			_actionHandler.reqLoopGetBodyCondDialog(this, new LoopGetBodyCond_callback() {
				@Override
				public void result() throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException {
					wlp(_whileNode.getProg(), _postInvariant, new wlp_callback() {
						@Override
						public void result(@Nonnull SemanticNode progNode, @Nonnull HoareCond preInvariant, @Nonnull HoareCond postCond) throws IOException, HoareException, LexerException, ParserException {
							_preInvariant = preInvariant;

							exec_tryInvariant_checkBodyCond();
						}
					});
				}
			});
		}

		private void exec_tryInvariant() throws HoareException, IOException, LexerException, ParserException {
			//TODO: auto-generate invariants

			HoareCond matchCond = HoareCond.makeAnd(_postInvariant, HoareCond.makeNeg(new HoareCond(_whileNode.getBoolExp())));

			_actionHandler.reqLoopCheckPostCondDialog(this, new LoopCheckPostCond_callback() {
				@Override
				public void result() throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException {
					HoareCond origPostCond = _postCond;
					HoareCond newPostCond = new HoareCond(new BoolAnd(_postInvariant.getBoolExp(), new BoolNeg(_whileNode.getBoolExp())));

					conseq_check_post(_whileNode, origPostCond, newPostCond, new ConseqCheck_callback() {
						@Override
						public void result(boolean yes) throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException {
							if (yes) exec_tryInvariant_getBodyCond(); else exec_askInvariant();
						}
					});
				}
			});
		}

		private void exec_askInvariant() throws IOException, HoareException, ParserException, LexerException {
			_actionHandler.reqLoopAskInvDialog(this, new LoopAskInv_callback() {
				@Override
				public void result(@Nullable HoareCond postInvariant) throws HoareException, IOException, LexerException, ParserException {
					_postInvariant = postInvariant;

					exec_tryInvariant();
				}
			});
		}

		public void exec() throws IOException, HoareException, ParserException, LexerException {
			exec_askInvariant();
		}

		public wlp_loop(@Nonnull While whileNode, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) {
			_whileNode = whileNode;
			_postCond = postCond;
			_callback = callback;
		}
	}

	public void wlp_loop(@Nonnull While node, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) throws HoareException, IOException, LexerException, ParserException {
		new wlp_loop(node, postCond, callback).exec();
	}

	public void conseq_check_pre(@Nonnull SemanticNode node, @Nonnull HoareCond origPreCond, @Nonnull HoareCond newPreCond, @Nonnull ConseqCheck_callback callback) throws IOException, HoareException, LexerException, ParserException {
		_actionHandler.reqConseqCheckPreDialog(node, origPreCond, newPreCond, callback);
	}

	public void conseq_check_post(@Nonnull SemanticNode node, @Nonnull HoareCond origPostCond, @Nonnull HoareCond newPostCond, @Nonnull ConseqCheck_callback callback) throws IOException, HoareException, LexerException, ParserException {
		_actionHandler.reqConseqCheckPostDialog(node, origPostCond, newPostCond, callback);
	}

	public void wlp(@Nonnull SemanticNode node, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) throws HoareException, IOException, LexerException, ParserException, SemanticNode.CopyException {
		_actionHandler.beginNode(node, postCond);

		wlp_callback retCallback = new wlp_callback() {
			@Override
			public void result(@Nonnull SemanticNode node, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
				_actionHandler.endNode(node, preCond);

				callback.result(node, preCond, postCond);
			}
		};

		if (node instanceof Comp) {
			wlp_comp((Comp) node, postCond, retCallback);
		} else if (node instanceof Skip)
			wlp_skip((Skip) node, postCond, retCallback);
		else if (node instanceof Assign) {
			wlp_assign((Assign) node, postCond, retCallback);
		} else if (node instanceof Alt) {
			wlp_alt((Alt) node, postCond, retCallback);
		} else if (node instanceof While) {
			wlp_loop((While) node, postCond, retCallback);
		} else if (node instanceof HoareBlock) {
			wlp(((HoareBlock) node).getProg(), postCond, retCallback);
		} else {
			throw new HoareException("no wlp for " + node);
		}
	}

	public static class HoareException extends Exception {
		public HoareException(@Nonnull String msg) {
			super(msg);
		}
	}
}