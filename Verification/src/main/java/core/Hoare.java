package core;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import core.Lexer.LexerException;
import core.Parser.NoRuleException;
import core.Parser.ParserException;
import core.structures.hoareCond.*;
import core.structures.nodes.BoolExp;
import core.structures.nodes.Exp;
import grammars.HoareWhileGrammar;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableMap;
import util.StringUtil;

import javax.annotation.Nonnull;

public class Hoare {
	private HoareWhileGrammar _grammar;
	private ObjectProperty<SyntaxTree> _syntaxTreeP;
	private ObservableMap<SyntaxNode, HoareCond> _preCondMap;
	private ObservableMap<SyntaxNode, HoareCond> _postCondMap;
	private ObjectProperty<SyntaxNode> _currentNodeP;
	private ObjectProperty<SyntaxNode> _currentHoareNodeP;
	private ActionInterface _actionInterface;
	
	public interface ActionInterface {
		void finished(SyntaxNode node, HoareCond preCond, HoareCond postCond, boolean yes) throws IOException;

		void reqSkipDialog(SyntaxNode node, HoareCond preCond, HoareCond postCond, Executer.Skip_callback callback) throws IOException, HoareException, LexerException, ParserException;

		void reqAssignDialog(SyntaxNode node, HoareCond preCond, HoareCond postCond, Executer.Assign_callback callback, String var, Exp exp) throws IOException, HoareException, LexerException, ParserException;

		void reqCompSecondDialog(Executer.wlp_comp comp, Executer.CompSecond_callback callback) throws IOException, HoareException, LexerException, ParserException;
		void reqCompFirstDialog(Executer.wlp_comp comp, Executer.CompFirst_callback callback) throws IOException, HoareException, LexerException, ParserException;
		void reqCompMergeDialog(Executer.wlp_comp comp, Executer.CompMerge_callback callback) throws IOException, HoareException, LexerException, ParserException;

		void reqAltFirstDialog(Executer.wlp_alt alt, Executer.AltThen_callback callback) throws IOException, HoareException, LexerException, ParserException;
		void reqAltElseDialog(Executer.wlp_alt alt, Executer.AltElse_callback callback) throws IOException, HoareException, LexerException, ParserException;
		void reqAltMergeDialog(Executer.wlp_alt alt, Executer.AltMerge_callback callback) throws IOException, HoareException, LexerException, ParserException;

		void reqLoopAskInvDialog(Executer.wlp_loop loop, Executer.LoopAskInv_callback callback) throws IOException;
		void reqLoopCheckPostCondDialog(Executer.wlp_loop loop, Executer.LoopCheckPostCond_callback callback) throws IOException;
		void reqLoopGetBodyCondDialog(Executer.wlp_loop loop, Executer.LoopGetBodyCond_callback callback) throws IOException;
		void reqLoopCheckBodyCondDialog(Executer.wlp_loop loop, Executer.LoopCheckBodyCond_callback callback) throws IOException;
		void reqLoopAcceptInvCondDialog(Executer.wlp_loop loop, Executer.LoopAcceptInv_callback callback) throws IOException;

		void reqConseqPreCheckDialog(SyntaxNode node, HoareCond origPreCond, HoareCond newPreCond, HoareCond origPostCond, HoareCond newPostCond, Executer.ConseqCheck_callback callback) throws IOException;
	}
	
	public static class HoareException extends Exception {
		private static final long serialVersionUID = 1L;

		HoareException(String msg) {
			super(msg);
		}
	}
	
	private class HoareNode {
		private SyntaxNode _refNode;
		
		SyntaxNode getRefNode() {
			return _refNode;
		}

		private Vector<HoareNode> _children = new Vector<>();

		public Vector<HoareNode> getChildren() {
			return _children;
		}
		
		void addChild(HoareNode child) {
			_children.add(child);
		}
		
		HoareNode(SyntaxNode actualNode) {
			_refNode = actualNode;
		}
	}
	
	private Vector<HoareNode> collectChildren(SyntaxNode node) {
		Vector<HoareNode> ret = new Vector<>();
		
		for (SyntaxNode child : node.getChildren()) {
			Vector<HoareNode> hoareChildren = collectChildren(child);

			ret.addAll(hoareChildren);
		}
		
		if ((node.getSymbol() != null) && node.getSymbol().equals(_grammar.NON_TERMINAL_HOARE_BLOCK)) {
			HoareNode selfNode = new HoareNode(node);
			
			for (HoareNode child : ret) {
				selfNode.addChild(child);
			}
			
			ret.clear();
			
			ret.add(selfNode);
		}
		
		return ret;
	}
	
	private interface ExecInterface {
		void finished() throws HoareException, LexerException, IOException, ParserException;
	}
	
	public static class Executer {
		private HoareNode _node;
		private int _nestDepth;
		private HoareWhileGrammar _grammar;
		private ObservableMap<SyntaxNode, HoareCond> _preCondMap;
		private ObservableMap<SyntaxNode, HoareCond> _postCondMap;
		private ObjectProperty<SyntaxNode> _currentNodeP;
		private ObjectProperty<SyntaxNode> _currentHoareNodeP;
		private ActionInterface _actionHandler;
		private ExecInterface _callback;
		
		private Vector<Executer> _execChain = new Vector<>();
		private Iterator<Executer> _execChainIt;
		
		public interface Skip_callback {
			void result() throws IOException, HoareException, LexerException, ParserException;
		}

		public interface Assign_callback {
			void result() throws IOException, HoareException, LexerException, ParserException;
		}

		public interface AltThen_callback {
			void result() throws IOException, HoareException, LexerException, ParserException;
		}
		public interface AltElse_callback {
			void result() throws IOException, HoareException, LexerException, ParserException;
		}
		public interface AltMerge_callback {
			void result() throws IOException, HoareException, LexerException, ParserException;
		}

		public interface CompSecond_callback {
			void result() throws IOException, HoareException, LexerException, ParserException;
		}
		public interface CompFirst_callback {
			void result() throws IOException, HoareException, LexerException, ParserException;
		}
		public interface CompMerge_callback {
			void result() throws IOException, HoareException, LexerException, ParserException;
		}
		
		public interface LoopAskInv_callback {
			void result(HoareCond postInvariant) throws HoareException, LexerException, IOException, ParserException;
		}
		public interface LoopCheckPostCond_callback {
			void result(boolean yes) throws HoareException, LexerException, IOException, ParserException;
		}
		public interface LoopGetBodyCond_callback {
			void result() throws HoareException, LexerException, IOException, ParserException;
		}
		public interface LoopCheckBodyCond_callback {
			void result(boolean yes) throws HoareException, LexerException, IOException, ParserException;
		}
		public interface LoopAcceptInv_callback {
			void result() throws HoareException, LexerException, IOException, ParserException;
		}

		public interface ConseqCheck_callback {
			void result(boolean yes) throws HoareException, LexerException, IOException, ParserException;
		}

		private int _wlp_nestDepth = 0;
		private int _wlp_printDepth = 0;
		
		private void println_begin() {
			_wlp_printDepth++;
		}
		
		private void println(String s) {
			System.out.println(StringUtil.repeat("\t", _wlp_printDepth - 1) + s);
		}
		
		private void println_end() {
			_wlp_printDepth--;
		}
		
		private interface wlp_callback {
			void result(SyntaxNode node, HoareCond preCond, HoareCond postCond) throws IOException, HoareException, LexerException, ParserException;
		}
		
		private void wlp_skip(SyntaxNode node, HoareCond postCond, wlp_callback callback) throws IOException, HoareException, LexerException, ParserException {
			HoareCond preCond = postCond;
			
			_actionHandler.reqSkipDialog(node, preCond, postCond, new Skip_callback() {
				@Override
				public void result() throws IOException, HoareException, LexerException, ParserException {
					callback.result(node, postCond, postCond);
				}
			});
		}
		
		private void wlp_assign(SyntaxNode node, HoareCond postCond, wlp_callback callback) throws IOException, HoareException, ParserException, LexerException {
			SyntaxNode idNode = node.findChild(_grammar.TERMINAL_ID, true);
			SyntaxNode expNode = node.findChild(_grammar.NON_TERMINAL_EXP, true);
			
			String var = idNode.synthesize();
			
			HoareCond preCond = postCond.copy();

			Exp exp = Exp.fromString(expNode.synthesize());
			
			preCond.replace(_grammar.TERMINAL_ID, var, exp.getBaseEx());

			_actionHandler.reqAssignDialog(node, preCond, postCond, new Assign_callback() {
				@Override
				public void result() throws IOException, HoareException, LexerException, ParserException {
					callback.result(node, preCond, postCond);
				}
			}, var, exp);
		}

		public class wlp_comp {
			public SyntaxNode _compNode;
			public HoareCond _postCond;
			private wlp_callback _callback;

			public SyntaxNode _firstNode;
			public SyntaxNode _secondNode;

			public HoareCond _secondPreCond;
			private HoareCond _firstPostCond;
			public HoareCond _firstPreCond;

			public HoareCond _preCond;

			private void exec_merge() throws LexerException, HoareException, ParserException, IOException {
				_preCond = _firstPreCond;

				_actionHandler.reqCompMergeDialog(this, new CompMerge_callback() {
					@Override
					public void result() throws IOException, HoareException, LexerException, ParserException {
						_callback.result(_compNode, _preCond, _postCond);
					}
				});
			}

			private void exec_getFirst() throws LexerException, HoareException, ParserException, IOException {
				_firstPostCond = _secondPreCond;

				_actionHandler.reqCompFirstDialog(this, new CompFirst_callback() {
					@Override
					public void result() throws IOException, HoareException, LexerException, ParserException {
						wlp(_firstNode, _firstPostCond, new wlp_callback() {
							@Override
							public void result(SyntaxNode firstNode, HoareCond firstPreCond, HoareCond firstPostCond) throws IOException, HoareException, LexerException, ParserException {
								_firstPreCond = firstPreCond;

								exec_merge();
							}
						});
					}
				});
			}

			private void exec_getSecond() throws HoareException, IOException, LexerException, ParserException {
				_actionHandler.reqCompSecondDialog(this, new CompSecond_callback() {
					@Override
					public void result() throws IOException, HoareException, LexerException, ParserException {
						wlp(_secondNode, _postCond, new wlp_callback() {
							@Override
							public void result(SyntaxNode secondNode, HoareCond secondPreCond, HoareCond secondPostCond) throws IOException, HoareException, LexerException, ParserException {
								_secondPreCond = secondPreCond;

								exec_getFirst();
							}
						});
					}
				});
			}

			void exec() throws LexerException, HoareException, ParserException, IOException {
				exec_getSecond();
			}

			wlp_comp(SyntaxNode compNode, HoareCond postCond, wlp_callback callback) {
				_compNode = compNode;
				_postCond = postCond;
				_callback = callback;

				_firstNode = _compNode.findChild(_grammar.NON_TERMINAL_CMD, true);
				_secondNode = _compNode.findChild(_grammar.NON_TERMINAL_PROG_, true);
			}
		}

		private void wlp_comp(SyntaxNode compNode, HoareCond postCond, wlp_callback callback) throws HoareException, IOException, LexerException, ParserException {
			new wlp_comp(compNode, postCond, callback).exec();
		}

		public class wlp_alt {
			public SyntaxNode _altNode;
			public HoareCond _postCond;
			private wlp_callback _callback;

			public BoolExp _boolExp;
			public SyntaxNode _thenProgNode;
			public SyntaxNode _elseProgNode;

			public HoareCond _thenPreCond;
			public HoareCond _elsePreCond;

			public HoareCond _preCond;

			private void exec_merge() throws LexerException, HoareException, ParserException, IOException {
				HoareCond boolExpCond = new HoareCondBoolExp(_boolExp);

				_preCond = new HoareCondOr(new HoareCondAnd(_thenPreCond, boolExpCond), new HoareCondAnd(_elsePreCond, new HoareCondNeg(boolExpCond)));

				_actionHandler.reqAltMergeDialog(this, new AltMerge_callback() {
					@Override
					public void result() throws IOException, HoareException, LexerException, ParserException {
						_callback.result(_altNode, _preCond, _postCond);
					}
				});
			}

			private void exec_getElsePreCond() throws LexerException, HoareException, ParserException, IOException {
				_actionHandler.reqAltElseDialog(this, new AltElse_callback() {
					@Override
					public void result() throws IOException, HoareException, LexerException, ParserException {
						wlp(_elseProgNode, _postCond, new wlp_callback() {
							@Override
							public void result(SyntaxNode elseProgNode, HoareCond elsePreCond, HoareCond elsePostCond) throws IOException, HoareException, LexerException, ParserException {
								_elsePreCond = elsePreCond;

								exec_merge();
							}
						});
					}
				});
			}

			private void exec_getThenPreCond() throws LexerException, HoareException, ParserException, IOException {
				_actionHandler.reqAltFirstDialog(this, new AltThen_callback() {
					@Override
					public void result() throws IOException, HoareException, LexerException, ParserException {
						wlp(_thenProgNode, _postCond, new wlp_callback() {
							@Override
							public void result(SyntaxNode thenProgNode, HoareCond thenPreCond, HoareCond thenPostCond) throws IOException, HoareException, LexerException, ParserException {
								_thenPreCond = thenPreCond;

								exec_getElsePreCond();
							}
						});
					}
				});
			}

			void exec() throws LexerException, HoareException, ParserException, IOException {
				exec_getThenPreCond();
			}

			wlp_alt(SyntaxNode altNode, HoareCond postCond, wlp_callback callback) throws LexerException, ParserException {
				_altNode = altNode;
				_postCond = postCond;
				_callback = callback;

				_boolExp = BoolExp.fromString(_altNode.findChild(_grammar.NON_TERMINAL_BOOL_EXP, true).synthesize());
				_thenProgNode = _altNode.findChild(_grammar.NON_TERMINAL_PROG, true);
				_elseProgNode = (_altNode.findChild(_grammar.NON_TERMINAL_SELECTION_ELSE, true).getSubRule().equals(_grammar.RULE_SELECTION_ELSE)) ? _altNode.findChild(_grammar.NON_TERMINAL_PROG, true) : new SyntaxNode(_grammar.NON_TERMINAL_SKIP, null);
			}
		}

		private void wlp_alt(SyntaxNode altNode, HoareCond postCond, wlp_callback callback) throws LexerException, ParserException, IOException, HoareException {
			new wlp_alt(altNode, postCond, callback).exec();
		}

		public class wlp_loop {
			public SyntaxNode _loopNode;
			public HoareCond _postCond;
			private wlp_callback _callback;

			public BoolExp _boolExp;
			public SyntaxNode _progNode;

			public HoareCond _postInvariant;
			public HoareCond _preInvariant;

			public HoareCond _preCond;

			private void exec_acceptInvariant() throws IOException, HoareException, LexerException, ParserException {
				_preCond = _preInvariant;

				_actionHandler.reqLoopAcceptInvCondDialog(this, new LoopAcceptInv_callback() {
					@Override
					public void result() throws HoareException, LexerException, IOException, ParserException {
						_callback.result(_loopNode, _preCond, _postCond);
					}
				});
			}

			private void exec_tryInvariant_checkBodyCond() throws IOException {
				_actionHandler.reqLoopCheckBodyCondDialog(this, new LoopCheckBodyCond_callback() {
					@Override
					public void result(boolean yes) throws HoareException, LexerException, IOException, ParserException {
						if (yes) {
							exec_acceptInvariant();
						} else {
							exec_askInvariant();
						}
					}
				});
			}

			private void exec_tryInvariant_getBodyCond() throws IOException {
				_actionHandler.reqLoopGetBodyCondDialog(this, new LoopGetBodyCond_callback() {
					@Override
					public void result() throws HoareException, LexerException, IOException, ParserException {
						wlp(_progNode, _postInvariant, new wlp_callback() {
							@Override
							public void result(SyntaxNode progNode, HoareCond preInvariant, HoareCond postCond) throws IOException, HoareException, LexerException, ParserException {
								_preInvariant = preInvariant;

								exec_tryInvariant_checkBodyCond();
							}
						});
					}
				});
			}

			private void exec_tryInvariant() throws HoareException, IOException, LexerException, ParserException {
				//TODO: auto-generate invariants

				HoareCond matchCond = new HoareCondAnd(_postInvariant, new HoareCondNeg(new HoareCondBoolExp(_boolExp)));

				_actionHandler.reqLoopCheckPostCondDialog(this, new LoopCheckPostCond_callback() {
					@Override
					public void result(boolean yes) throws HoareException, LexerException, IOException, ParserException {
						if (yes) {
							exec_tryInvariant_getBodyCond();
						} else {
							exec_askInvariant();
						}
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

			wlp_loop(SyntaxNode loopNode, HoareCond postCond, wlp_callback callback) {
				_loopNode = loopNode;
				_postCond = postCond;
				_callback = callback;

				_boolExp = new BoolExp(_loopNode.findChild(_grammar.NON_TERMINAL_BOOL_EXP, true));
				_progNode = _loopNode.findChild(_grammar.NON_TERMINAL_PROG, true);
			}
		}

		private void wlp_loop(SyntaxNode node, HoareCond postCond, wlp_callback callback) throws HoareException, IOException {
			new wlp_loop(node, postCond, callback).exec();
		}

		private void consequence_pre_check(SyntaxNode node, HoareCond origPreCond, HoareCond newPreCond, HoareCond origPostCond, HoareCond newPostCond, ConseqCheck_callback callback) throws IOException, HoareException, LexerException, ParserException {
			_actionHandler.reqConseqPreCheckDialog(node, origPreCond, newPreCond, origPostCond, newPostCond, callback);
		}

		/*private void wlp_consequence_pre(SyntaxNode node, HoareCond origPreCond, HoareCond newPreCond, HoareCond origPostCond, HoareCond newPostCond, wlp_callback callback) throws IOException, HoareException, LexerException, ParserException {
			//TODO: for post as well, merged?
			callback.result(node, newPreCond, origPreCond);
		}*/
		
		private void wlp(SyntaxNode node, HoareCond postCondV, wlp_callback callback) throws HoareException, IOException, LexerException, ParserException {
			_wlp_nestDepth++;

			_currentNodeP.set(node);
			
			final HoareCond postCond = postCondV.copy();
			
			_postCondMap.put(node, postCond);

			//System.out.println(StringUtil.repeat("\t", _wlp_nestDepth) + "postcond " + node);

			wlp_callback retCallback = new wlp_callback() {
				@Override
				public void result(SyntaxNode node, HoareCond preCond, HoareCond postCond) throws IOException, HoareException, LexerException, ParserException {
					_preCondMap.put(node, preCond);
					_wlp_nestDepth--;
					
					callback.result(node, preCond, postCond);
				}
			};

			Symbol symbol = node.getSymbol();

			if (symbol.equals(_grammar.NON_TERMINAL_PROG)) {
				SyntaxNode cmdChild = node.findChild(_grammar.NON_TERMINAL_CMD, true);
				SyntaxNode lastChild = node.findChild(_grammar.NON_TERMINAL_PROG_, true);
				
				if (lastChild.findChild(_grammar.NON_TERMINAL_CMD, true) != null) {
					wlp_comp(node, postCond, retCallback);
				} else {
					wlp(cmdChild, postCond, retCallback);
				}
			} else if (symbol.equals(_grammar.NON_TERMINAL_PROG_)) {
				SyntaxNode cmdChild = node.findChild(_grammar.NON_TERMINAL_CMD, true);
				SyntaxNode lastChild = node.findChild(_grammar.NON_TERMINAL_PROG_, true);

				if (lastChild.findChild(_grammar.NON_TERMINAL_CMD, true) != null) {
					wlp_comp(node, postCond, retCallback);
				} else {
					wlp(cmdChild, postCond, retCallback);
				}
			} else if (symbol.equals(_grammar.NON_TERMINAL_CMD)) {
				wlp(node.getChildren().firstElement(), postCond, retCallback);
			} else if (symbol.equals(_grammar.NON_TERMINAL_SKIP))
				wlp_skip(node, postCond, retCallback);
			else if (symbol.equals(_grammar.NON_TERMINAL_ASSIGN)) {
				wlp_assign(node, postCond, retCallback);
			} else if (symbol.equals(_grammar.NON_TERMINAL_ALT)) {
				wlp_alt(node, postCond, retCallback);
			} else if (symbol.equals(_grammar.NON_TERMINAL_WHILE)) {
				wlp_loop(node, postCond, retCallback);
			} else if (symbol.equals(_grammar.NON_TERMINAL_HOARE_BLOCK)) {
				SyntaxNode progNode = node.findChild(_grammar.NON_TERMINAL_PROG, true);
						
				wlp(progNode, postCond, retCallback);
			} else {
				throw new HoareException("no wlp for " + node + " with rule " + node.getSubRule());
			}
		}
		
		public void exec() throws IOException, HoareException, LexerException, ParserException {
			SyntaxNode refNode = _node.getRefNode();

			_currentHoareNodeP.set(refNode);
			
			SyntaxNode preNode = refNode.findChild(_grammar.NON_TERMINAL_HOARE_PRE, true);
			SyntaxNode postNode = refNode.findChild(_grammar.NON_TERMINAL_HOARE_POST, true);
			
			HoareCond preCond = new HoareCondBoolExp(BoolExp.fromString(preNode.findChild(_grammar.NON_TERMINAL_BOOL_EXP, true).synthesize()));
			HoareCond postCond = new HoareCondBoolExp(BoolExp.fromString(postNode.findChild(_grammar.NON_TERMINAL_BOOL_EXP, true).synthesize()));
			
			System.err.println(StringUtil.repeat("\t", _nestDepth) + "checking " + preCond + "->" + postCond + " at " + _node);
			
			_wlp_nestDepth = 0;
			_wlp_printDepth = 0;
			
			wlp(refNode, postCond, new wlp_callback() {
				@Override
				public void result(SyntaxNode node, HoareCond lastPreCond, HoareCond lastPostCond) throws IOException, HoareException, LexerException, ParserException {
					System.out.println("final preCondition: " + preCond);

					consequence_pre_check(refNode, preCond, postCond, lastPreCond, postCond, new ConseqCheck_callback() {
                        @Override
                        public void result(boolean yes) throws HoareException, LexerException, IOException, ParserException {
                            if (yes) {
                                System.out.println(preCond + "->" + postCond + " holds true (wlp: " + preCond + ")");
                            } else {
                                System.out.println(preCond + "->" + postCond + " failed (wlp: " + preCond + ")");
                            }

                            _actionHandler.finished(refNode, preCond, postCond, yes);

                            _callback.finished();
                        }
                    });
				}
			});
		}
		
		public void start() throws IOException, HoareException, LexerException, ParserException {
			_execChainIt.next().exec();
		}
		
		public Executer(HoareNode node, int nestDepth, HoareWhileGrammar grammar, ObservableMap<SyntaxNode, HoareCond> preCondMap, ObservableMap<SyntaxNode, HoareCond> postCondMap, ObjectProperty<SyntaxNode> currentNodeP, ObjectProperty<SyntaxNode> currentHoareNodeP, ActionInterface actionInterface, ExecInterface callback) throws IOException, HoareException, NoRuleException, LexerException {
			_node = node;
			_nestDepth = nestDepth;
			_grammar = grammar;
			_preCondMap = preCondMap;
			_postCondMap = postCondMap;
			_currentNodeP = currentNodeP;
			_currentHoareNodeP = currentHoareNodeP;
			_actionHandler = actionInterface;
			_callback = callback;
			
			ExecInterface childCallback = new ExecInterface() {
				@Override
				public void finished() throws HoareException, LexerException, IOException, ParserException {
					Executer next = _execChainIt.next();

					next.exec();
				}
			};
			
			for (HoareNode child : node.getChildren()) {
				_execChain.add(new Executer(child, nestDepth + 1, _grammar, _preCondMap, _postCondMap, _currentNodeP, _currentHoareNodeP, _actionHandler, childCallback));
			}
			
			_execChain.add(this);
			
			_execChainIt = _execChain.iterator();
		}
	}
	
	private Vector<Executer> _execChain;
	private Iterator<Executer> _execChainIt;
	
	public void exec() throws HoareException, LexerException, IOException, ParserException {
		System.err.println("hoaring...");

		Vector<HoareNode> children = collectChildren(_syntaxTreeP.get().getRoot());

		if (children.isEmpty()) {
			System.err.println("no hoareBlocks");
		} else {
			_execChain = new Vector<>();
			
			for (HoareNode child : children) {
				if (children.lastElement().equals(child)) {
					_execChain.add(new Executer(child, 0, _grammar, _preCondMap, _postCondMap, _currentNodeP, _currentHoareNodeP, _actionInterface, new ExecInterface() {
						@Override
						public void finished() throws HoareException, NoRuleException, LexerException, IOException {
							System.err.println("hoaring finished");
							
							_currentHoareNodeP.set(null);
						}
					}));
				} else {
					_execChain.add(new Executer(child, 0, _grammar, _preCondMap, _postCondMap, _currentNodeP, _currentHoareNodeP, _actionInterface, new ExecInterface() {
						@Override
						public void finished() throws IOException, HoareException, LexerException, ParserException {
							_execChainIt.next().exec();
						}
					}));
				}
			}
			
			Iterator<Executer> execChainIt = _execChain.iterator();
			
			execChainIt.next().exec();
		}
	}
	
	public Hoare(ObjectProperty<SyntaxTree> syntaxTreeP, ObjectProperty<ObservableMap<SyntaxNode, HoareCond>> preCondMapP, ObjectProperty<ObservableMap<SyntaxNode, HoareCond>> postCondMapP, ObjectProperty<SyntaxNode> currentNodeP, ObjectProperty<SyntaxNode> currentHoareNodeP, ActionInterface actionInterface) throws Exception {
		_syntaxTreeP = syntaxTreeP;
		_preCondMap = preCondMapP.get();
		_postCondMap = postCondMapP.get();
		_currentNodeP = currentNodeP;
		_currentHoareNodeP = currentHoareNodeP;
		_actionInterface = actionInterface;

		if (_syntaxTreeP.get() == null) throw new Exception("no syntaxTree");
		if (_preCondMap == null) throw new Exception("no preCondMap");
		if (_postCondMap == null) throw new Exception("no postCondMap");
		
		_grammar = (HoareWhileGrammar) _syntaxTreeP.get().getGrammar();
	}
}