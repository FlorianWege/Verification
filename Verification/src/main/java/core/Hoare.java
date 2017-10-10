package core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import core.Lexer.LexerException;
import core.Parser.NoRuleException;
import core.Parser.ParserException;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolAnd;
import core.structures.semantics.boolExp.BoolImpl;
import core.structures.semantics.boolExp.BoolNeg;
import core.structures.semantics.boolExp.HoareCond;
import core.structures.semantics.exp.Exp;
import core.structures.semantics.exp.Id;
import core.structures.semantics.prog.*;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableMap;
import util.IOUtil;
import util.StringUtil;

import javax.annotation.Nonnull;

public class Hoare {
	private ObjectProperty<SemanticNode> _semanticTreeP;
	private ObservableMap<SemanticNode, HoareCond> _preCondMap;
	private ObservableMap<SemanticNode, HoareCond> _postCondMap;
	private ObjectProperty<SemanticNode> _currentNodeP;
	private ObjectProperty<SemanticNode> _currentHoareNodeP;
	private ActionInterface _actionInterface;
	
	public interface ActionInterface {
		void finished(SemanticNode node, HoareCond preCond, HoareCond postCond, boolean yes) throws IOException;

		void reqSkipDialog(Skip skip, HoareCond preCond, HoareCond postCond, Executer.Skip_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;

		void reqAssignDialog(Assign assign, HoareCond preCond, HoareCond postCond, Executer.Assign_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;

		void reqCompNextDialog(Executer.wlp_comp comp, Executer.CompNext_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
		void reqCompMergeDialog(Executer.wlp_comp comp, Executer.CompMerge_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;

		void reqAltFirstDialog(Executer.wlp_alt alt, Executer.AltThen_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
		void reqAltElseDialog(Executer.wlp_alt alt, Executer.AltElse_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
		void reqAltMergeDialog(Executer.wlp_alt alt, Executer.AltMerge_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;

		void reqLoopAskInvDialog(Executer.wlp_loop loop, Executer.LoopAskInv_callback callback) throws IOException;
		void reqLoopCheckPostCondDialog(Executer.wlp_loop loop, Executer.LoopCheckPostCond_callback callback) throws IOException;
		void reqLoopGetBodyCondDialog(Executer.wlp_loop loop, Executer.LoopGetBodyCond_callback callback) throws IOException, LexerException, HoareException, ParserException, SemanticNode.CopyException;
		void reqLoopCheckBodyCondDialog(Executer.wlp_loop loop, Executer.LoopCheckBodyCond_callback callback) throws IOException;
		void reqLoopAcceptInvCondDialog(Executer.wlp_loop loop, Executer.LoopAcceptInv_callback callback) throws IOException, LexerException, HoareException, ParserException, SemanticNode.CopyException;

		void reqConseqCheckPreDialog(SemanticNode node, HoareCond origPreCond, HoareCond newPreCond, Executer.ConseqCheck_callback callback) throws IOException;
		void reqConseqCheckPostDialog(SemanticNode node, HoareCond origPostCond, HoareCond newPostCond, Executer.ConseqCheck_callback callback) throws IOException;
	}
	
	public static class HoareException extends Exception {
		private static final long serialVersionUID = 1L;

		HoareException(String msg) {
			super(msg);
		}
	}
	
	private class HoareNode {
		private HoareBlock _refNode;
		
		public HoareBlock getRefNode() {
			return _refNode;
		}

		private List<HoareNode> _children = new Vector<>();

		public List<HoareNode> getChildren() {
			return _children;
		}
		
		public void addChild(HoareNode child) {
			_children.add(child);
		}
		
		public HoareNode(HoareBlock actualNode) {
			_refNode = actualNode;
		}
	}
	
	private List<HoareNode> collectChildren(SemanticNode node) {
		List<HoareNode> ret = new ArrayList<>();
		
		for (SemanticNode child : node.getChildren()) {
			List<HoareNode> hoareChildren = collectChildren(child);

			ret.addAll(hoareChildren);
		}
		
		if (node instanceof HoareBlock) {
			HoareNode selfNode = new HoareNode((HoareBlock) node);
			
			for (HoareNode child : ret) {
				selfNode.addChild(child);
			}
			
			ret.clear();
			
			ret.add(selfNode);
		}
		
		return ret;
	}
	
	private interface ExecInterface {
		void finished() throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException;
	}
	
	public static class Executer {
		private HoareNode _node;
		private int _nestDepth;
		private ObservableMap<SemanticNode, HoareCond> _preCondMap;
		private ObservableMap<SemanticNode, HoareCond> _postCondMap;
		private ObjectProperty<SemanticNode> _currentNodeP;
		private ObjectProperty<SemanticNode> _currentHoareNodeP;
		private ActionInterface _actionHandler;
		private ExecInterface _callback;
		
		private Vector<Executer> _execChain = new Vector<>();
		private Iterator<Executer> _execChainIt;
		
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
			void result(@Nonnull HoareCond postInvariant) throws HoareException, LexerException, IOException, ParserException;
		}
		public interface LoopCheckPostCond_callback {
			void result() throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException;
		}
		public interface LoopGetBodyCond_callback {
			void result() throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException;
		}
		public interface LoopCheckBodyCond_callback {
			void result(boolean yes) throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException;
		}
		public interface LoopAcceptInv_callback {
			void result() throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException;
		}

		public interface ConseqCheck_callback {
			void result(boolean yes) throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException;
		}
		
		private interface wlp_callback {
			void result(@Nonnull SemanticNode node, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException;
		}
		
		private void wlp_skip(@Nonnull Skip skipNode, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
			HoareCond preCond = postCond;
			
			_actionHandler.reqSkipDialog(skipNode, preCond, postCond, new Skip_callback() {
				@Override
				public void result() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
					callback.result(skipNode, postCond, postCond);
				}
			});
		}
		
		private void wlp_assign(@Nonnull Assign assignNode, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) throws IOException, HoareException, ParserException, LexerException, SemanticNode.CopyException {
			Id varNode = assignNode.getVar();
			Exp expNode = assignNode.getExp();

			HoareCond preCond = (HoareCond) postCond.copy();

			preCond = (HoareCond) preCond.replace(new IOUtil.Func<SemanticNode, SemanticNode>() {
				@Override
				public SemanticNode apply(SemanticNode child) {
					if (child instanceof Id) {
						if (((Id) child).getName().equals(varNode.getName())) {
							return expNode;
						}
					}

					return child;
				}
			});

			HoareCond finalPreCond = preCond;

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

			void exec() throws LexerException, HoareException, ParserException, IOException, SemanticNode.CopyException {
				exec_next();
			}

			wlp_comp(Comp compNode, HoareCond postCond, wlp_callback callback) {
				_compNode = compNode;
				_postCond = postCond;
				_callback = callback;
				_curPart = null;

				_preConds = new HoareCond[_compNode.getChildren().size()];
				_postConds = new HoareCond[_compNode.getChildren().size()];
			}
		}

		private void wlp_comp(@Nonnull Comp compNode, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) throws HoareException, IOException, LexerException, ParserException, SemanticNode.CopyException {
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

			void exec() throws LexerException, HoareException, ParserException, IOException, SemanticNode.CopyException {
				exec_getThenPreCond();
			}

			wlp_alt(@Nonnull Alt altNode, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) throws LexerException, ParserException {
				_altNode = altNode;
				_postCond = postCond;
				_callback = callback;
			}
		}

		private void wlp_alt(@Nonnull Alt altNode, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) throws LexerException, ParserException, IOException, HoareException, SemanticNode.CopyException {
			new wlp_alt(altNode, postCond, callback).exec();
		}

		public class wlp_loop {
			public While _whileNode;
			public HoareCond _postCond;
			private wlp_callback _callback;

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

			private void exec_tryInvariant_checkBodyCond() throws IOException {
				_actionHandler.reqLoopCheckBodyCondDialog(this, new LoopCheckBodyCond_callback() {
					@Override
					public void result(boolean yes) throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException {
						if (yes) exec_acceptInvariant(); else exec_askInvariant();
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

			private void exec_askInvariant() throws IOException {
				_actionHandler.reqLoopAskInvDialog(this, new LoopAskInv_callback() {
					@Override
					public void result(@Nonnull HoareCond postInvariant) throws HoareException, IOException, LexerException, ParserException {
						_postInvariant = postInvariant;

						exec_tryInvariant();
					}
				});
			}

			void exec() throws IOException {
				exec_askInvariant();
			}

			wlp_loop(@Nonnull While whileNode, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) {
				_whileNode = whileNode;
				_postCond = postCond;
				_callback = callback;
			}
		}

		private void wlp_loop(@Nonnull While node, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) throws HoareException, IOException {
			new wlp_loop(node, postCond, callback).exec();
		}

		private void conseq_check_pre(@Nonnull SemanticNode node, @Nonnull HoareCond origPreCond, @Nonnull HoareCond newPreCond, @Nonnull ConseqCheck_callback callback) throws IOException, HoareException, LexerException, ParserException {
			_actionHandler.reqConseqCheckPreDialog(node, origPreCond, newPreCond, callback);
		}

		private void conseq_check_post(@Nonnull SemanticNode node, @Nonnull HoareCond origPostCond, @Nonnull HoareCond newPostCond, @Nonnull ConseqCheck_callback callback) throws IOException, HoareException, LexerException, ParserException {
			_actionHandler.reqConseqCheckPostDialog(node, origPostCond, newPostCond, callback);
		}

		/*private void wlp_conseq_pre(@Nonnull SemanticNode node, @Nonnull HoareCond origPreCond, @Nonnull HoareCond newPreCond, @Nonnull HoareCond origPostCond, @Nonnull HoareCond newPostCond, @Nonnull wlp_callback callback) throws IOException, HoareException, LexerException, ParserException {
			//TODO: for post as well, merged?
			callback.result(node, newPreCond, origPreCond);
		}*/
		
		private void wlp(@Nonnull SemanticNode node, @Nonnull HoareCond postCond, @Nonnull wlp_callback callback) throws HoareException, IOException, LexerException, ParserException, SemanticNode.CopyException {
			_currentNodeP.set(node);
			
			_postCondMap.put(node, postCond);

			wlp_callback retCallback = new wlp_callback() {
				@Override
				public void result(@Nonnull SemanticNode node, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
					_preCondMap.put(node, preCond);
					
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
		
		public void exec() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
			HoareBlock hoareBlockNode = _node.getRefNode();

			_currentHoareNodeP.set(hoareBlockNode);
			
			HoareCond preCond = hoareBlockNode.getPreCond();
			HoareCond postCond = hoareBlockNode.getPostCond();
			
			wlp(hoareBlockNode, postCond, new wlp_callback() {
				@Override
				public void result(@Nonnull SemanticNode node, @Nonnull HoareCond lastPreCond, @Nonnull HoareCond lastPostCond) throws IOException, HoareException, LexerException, ParserException {
					System.out.println("final preCondition: " + lastPreCond);

					conseq_check_pre(hoareBlockNode, lastPreCond, preCond, new ConseqCheck_callback() {
                        @Override
                        public void result(boolean yes) throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException {
                            if (yes) {
                                System.out.println(preCond + "->" + postCond + " holds true (wlp: " + preCond + ")");
                            } else {
                                System.out.println(preCond + "->" + postCond + " failed (wlp: " + preCond + ")");
                            }

                            _actionHandler.finished(hoareBlockNode, preCond, postCond, yes);

                            _callback.finished();
                        }
                    });
				}
			});
		}
		
		public void start() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
			_execChainIt.next().exec();
		}
		
		public Executer(HoareNode node, int nestDepth, ObservableMap<SemanticNode, HoareCond> preCondMap, ObservableMap<SemanticNode, HoareCond> postCondMap, ObjectProperty<SemanticNode> currentNodeP, ObjectProperty<SemanticNode> currentHoareNodeP, ActionInterface actionInterface, ExecInterface callback) throws IOException, HoareException, NoRuleException, LexerException {
			_node = node;
			_nestDepth = nestDepth;
			_preCondMap = preCondMap;
			_postCondMap = postCondMap;
			_currentNodeP = currentNodeP;
			_currentHoareNodeP = currentHoareNodeP;
			_actionHandler = actionInterface;
			_callback = callback;
			
			ExecInterface childCallback = new ExecInterface() {
				@Override
				public void finished() throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException {
					Executer next = _execChainIt.next();

					next.exec();
				}
			};
			
			for (HoareNode child : node.getChildren()) {
				_execChain.add(new Executer(child, nestDepth + 1, _preCondMap, _postCondMap, _currentNodeP, _currentHoareNodeP, _actionHandler, childCallback));
			}
			
			_execChain.add(this);
			
			_execChainIt = _execChain.iterator();
		}
	}
	
	private List<Executer> _execChain;
	private Iterator<Executer> _execChainIt;
	
	public void exec() throws HoareException, LexerException, IOException, ParserException, SemanticNode.CopyException {
		System.err.println("hoaring...");

		List<HoareNode> children = collectChildren(_semanticTreeP.get());

		if (children.isEmpty()) {
			System.err.println("no hoareBlocks");
		} else {
			_execChain = new ArrayList<>();
			
			for (HoareNode child : children) {
				if (children.get(children.size() - 1).equals(child)) {
					_execChain.add(new Executer(child, 0, _preCondMap, _postCondMap, _currentNodeP, _currentHoareNodeP, _actionInterface, new ExecInterface() {
						@Override
						public void finished() throws HoareException, NoRuleException, LexerException, IOException {
							System.err.println("hoaring finished");
							
							_currentHoareNodeP.set(null);
						}
					}));
				} else {
					_execChain.add(new Executer(child, 0, _preCondMap, _postCondMap, _currentNodeP, _currentHoareNodeP, _actionInterface, new ExecInterface() {
						@Override
						public void finished() throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
							_execChainIt.next().exec();
						}
					}));
				}
			}
			
			Iterator<Executer> execChainIt = _execChain.iterator();
			
			execChainIt.next().exec();
		}
	}
	
	public Hoare(ObjectProperty<SemanticNode> syntaxTreeP, ObjectProperty<ObservableMap<SemanticNode, HoareCond>> preCondMapP, ObjectProperty<ObservableMap<SemanticNode, HoareCond>> postCondMapP, ObjectProperty<SemanticNode> currentNodeP, ObjectProperty<SemanticNode> currentHoareNodeP, ActionInterface actionInterface) throws Exception {
		_semanticTreeP = syntaxTreeP;
		_preCondMap = preCondMapP.get();
		_postCondMap = postCondMapP.get();
		_currentNodeP = currentNodeP;
		_currentHoareNodeP = currentHoareNodeP;
		_actionInterface = actionInterface;

		if (_semanticTreeP.get() == null) throw new Exception("no semanticTree");
		if (_preCondMap == null) throw new Exception("no preCondMap");
		if (_postCondMap == null) throw new Exception("no postCondMap");
	}
}