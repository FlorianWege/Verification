package core;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.script.ScriptException;

import core.Hoare.HoareException;
import core.Lexer.LexerException;
import core.Parser.NoRuleException;
import core.Parser.ParserException;
import core.structures.BoolExp;
import core.structures.Exp;
import core.structures.Terminal;
import core.structures.hoareCond.HoareCond;
import core.structures.hoareCond.HoareCondAnd;
import core.structures.hoareCond.HoareCondBoolExp;
import core.structures.hoareCond.HoareCondNeg;
import grammars.HoareWhileGrammar;
import gui.CompositionMidDialog;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableMap;
import util.StringUtil;

public class Hoare {
	private HoareWhileGrammar _grammar;
	private ObjectProperty<SyntaxTree> _syntaxTreeP;
	private ObservableMap<SyntaxTreeNode, HoareCond> _preCondMapP;
	private ObservableMap<SyntaxTreeNode, HoareCond> _postCondMapP;
	private ObjectProperty<SyntaxTreeNode> _currentNodeP;
	private ObjectProperty<SyntaxTreeNode> _currentHoareNodeP;
	private ActionInterface _actionInterface;
	
	public interface ActionInterface {
		void finished(SyntaxTreeNode node, HoareCond preCond, HoareCond postCond, boolean yes) throws IOException;
		void requestSkipDialog(SyntaxTreeNode node, HoareCond preCond, HoareCond postCond, Hoare.Executer.SkipInterface callback) throws IOException, HoareException, LexerException, ParserException;
		void requestAssignDialog(SyntaxTreeNode node, HoareCond preCond, HoareCond postCond, Hoare.Executer.AssignInterface callback, String var, Exp exp) throws IOException, HoareException, LexerException, ParserException;
		void requestCompositionStartDialog(SyntaxTreeNode node, HoareCond postCond, Hoare.Executer.CompositionStartInterface callback, SyntaxTreeNode firstNode, SyntaxTreeNode secondNode) throws IOException, HoareException, LexerException, ParserException;
		void requestCompositionMidDialog(SyntaxTreeNode node, HoareCond postCond, Hoare.Executer.CompositionMidInterface callback, SyntaxTreeNode firstNode, SyntaxTreeNode secondNode, HoareCond secondPreCond) throws IOException, HoareException, LexerException, ParserException;
		void requestCompositionEndDialog(SyntaxTreeNode node, HoareCond preCond, HoareCond postCond, Hoare.Executer.CompositionEndInterface callback, SyntaxTreeNode firstNode, SyntaxTreeNode secondNode, HoareCond secondPreCond, HoareCond firstPreCond) throws IOException, HoareException, LexerException, ParserException;
		void requestImplicationDialog(HoareCond a, HoareCond b, Hoare.Executer.ImplicationInterface callback, boolean equal) throws IOException;
		void requestInvariantDialog(SyntaxTreeNode node, HoareCond postCond, Hoare.Executer.InvariantInterface callback) throws IOException;
	}
	
	public static class HoareException extends Exception {
		private static final long serialVersionUID = 1L;

		public HoareException(String msg) {
			super(msg);
		}
	}
	
	private class HoareNode {
		private SyntaxTreeNode _refNode;
		
		public SyntaxTreeNode getRefNode() {
			return _refNode;
		}

		private Vector<HoareNode> _children = new Vector<>();

		public Vector<HoareNode> getChildren() {
			return _children;
		}
		
		public void addChild(HoareNode child) {
			_children.add(child);
		}
		
		public HoareNode(SyntaxTreeNode actualNode) {
			_refNode = actualNode;
		}
	}
	
	private Vector<HoareNode> collectChildren(SyntaxTreeNode node) {
		Vector<HoareNode> ret = new Vector<>();
		
		for (SyntaxTreeNode child : node.getChildren()) {
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
		public void finished() throws HoareException, LexerException, IOException, ParserException;
	}
	
	public static class Executer {
		private HoareNode _node;
		private int _nestDepth;
		private HoareWhileGrammar _grammar;
		private ObservableMap<SyntaxTreeNode, HoareCond> _preCondMap;
		private ObservableMap<SyntaxTreeNode, HoareCond> _postCondMap;
		private ObjectProperty<SyntaxTreeNode> _currentNodeP;
		private ObjectProperty<SyntaxTreeNode> _currentHoareNodeP;
		private ActionInterface _actionInterface;
		private ExecInterface _callback;
		
		private Vector<Executer> _execChain = new Vector<>();
		private Iterator<Executer> _execChainIt;
		
		public interface SkipInterface {
			void result() throws IOException, HoareException, LexerException, ParserException;
		}

		public interface AssignInterface {
			void result() throws IOException, HoareException, LexerException, ParserException;
		}

		public interface CompositionStartInterface {
			void result() throws IOException, HoareException, LexerException, ParserException;
		}

		public interface CompositionMidInterface {
			void result() throws IOException, HoareException, LexerException, ParserException;
		}

		public interface CompositionEndInterface {
			void result() throws IOException, HoareException, LexerException, ParserException;
		}
		
		public interface ImplicationInterface {
			void result(boolean yes) throws HoareException, LexerException, IOException, ParserException;
		}
		
		public interface InvariantInterface {
			void result(HoareCond invariant) throws HoareException, LexerException, IOException, ParserException;
		}
		
		private boolean check(HoareCond a) throws ScriptException {
			/*ScriptEngineManager manager = new ScriptEngineManager();
			
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			
			System.out.println(engine.eval(a.toString()));
			
			return true;*/
			return true;
		}
		
		private boolean implicates(HoareCond a, HoareCond b, ImplicationInterface callback) throws ScriptException, IOException, HoareException, LexerException, ParserException {
			System.out.println("try implication " + a + "->" + b);
			
			//TODO implicit check
			boolean checkSuccess = false;
			boolean checkResult = false;
			
			if (checkSuccess) {
				callback.result(checkResult);
			} else {
				_actionInterface.requestImplicationDialog(a, b, callback, false);
			}
			
			return !check(a) || check(b);
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
			public void result(SyntaxTreeNode node, HoareCond preCond, HoareCond postCond) throws IOException, HoareException, LexerException, ParserException;
		}
		
		private void wlp_skip(SyntaxTreeNode node, HoareCond postCond, wlp_callback callback) throws IOException, HoareException, LexerException, ParserException {
			HoareCond preCond = postCond;
			
			_actionInterface.requestSkipDialog(node, preCond, postCond, new SkipInterface() {
				@Override
				public void result() throws IOException, HoareException, LexerException, ParserException {
					callback.result(node, postCond, postCond);
				}
			});
		}
		
		private void wlp_assign(SyntaxTreeNode node, HoareCond postCond, wlp_callback callback) throws IOException, HoareException, ParserException, LexerException {
			SyntaxTreeNode idNode = node.findChild(_grammar.TERMINAL_ID);
			SyntaxTreeNode expNode = node.findChild(_grammar.NON_TERMINAL_EXP);
			
			String var = idNode.synthesize();
			
			HoareCond preCond = postCond.copy();

			Exp exp = Exp.fromString(expNode.synthesize());
			
			preCond.replace(_grammar.TERMINAL_ID, var, exp.getBaseEx());

			_actionInterface.requestAssignDialog(node, preCond, postCond, new AssignInterface() {
				@Override
				public void result() throws IOException, HoareException, LexerException, ParserException {
					callback.result(node, preCond, postCond);
				}
			}, var, exp);
		}
		
		private void wlp_composite(SyntaxTreeNode node, HoareCond postCond, wlp_callback callback) throws HoareException, IOException, LexerException, ParserException {
			SyntaxTreeNode firstNode = node.findChild(_grammar.NON_TERMINAL_CMD);
			SyntaxTreeNode secondNode = node.findChild(_grammar.NON_TERMINAL_PROG_);
			
			_actionInterface.requestCompositionStartDialog(node, postCond, new CompositionStartInterface() {
				@Override
				public void result() throws IOException, HoareException, LexerException, ParserException {
					wlp(secondNode, postCond, new wlp_callback() {
						@Override
						public void result(SyntaxTreeNode secondNode, HoareCond secondPreCond, HoareCond secondPostCond) throws IOException, HoareException, LexerException, ParserException {
							HoareCond firstPostCond = secondPreCond;
							
							_actionInterface.requestCompositionMidDialog(node, postCond, new CompositionMidInterface() {
								@Override
								public void result() throws IOException, HoareException, LexerException, ParserException {
									wlp(firstNode, firstPostCond, new wlp_callback() {
										@Override
										public void result(SyntaxTreeNode firstNode, HoareCond firstPreCond, HoareCond firstPostCond) throws IOException, HoareException, LexerException, ParserException {
											HoareCond preCond = firstPreCond;
											
											_actionInterface.requestCompositionEndDialog(node, preCond, postCond, new CompositionEndInterface() {
												@Override
												public void result() throws IOException, HoareException, LexerException, ParserException {
													callback.result(node, preCond, postCond);
												}
											}, firstNode, secondNode, secondPreCond, firstPreCond);
										}
									});
								}
							}, firstNode, secondNode, secondPreCond);
						}
					});
				}
			}, firstNode, secondNode);
		}
		
		private interface alt_callback {
			void ret(HoareCond thenPreCond, HoareCond thenPostCond, HoareCond elsePreCond, HoareCond elsePostCond) throws IOException, HoareException, LexerException, ParserException;
		}
		
		private void wlp_alt(SyntaxTreeNode node, HoareCond postCond, wlp_callback callback) throws IOException, HoareException, LexerException, ParserException {
			println_begin();
			
			HoareCond altCond = new HoareCondBoolExp(BoolExp.fromString(node.findChild(_grammar.NON_TERMINAL_BOOL_EXP).synthesize()));
			
			SyntaxTreeNode selectionElseNonTerminal = node.findChild(_grammar.NON_TERMINAL_SELECTION_ELSE);
			
			SyntaxTreeNode thenProg = node.findChild(_grammar.NON_TERMINAL_PROG);
			SyntaxTreeNode elseProgV = null;
			
			if (selectionElseNonTerminal.getSubRule().equals(_grammar.RULE_SELECTION_ELSE)) {
				elseProgV = node.findChild(_grammar.NON_TERMINAL_PROG);
			}
			
			SyntaxTreeNode elseProg = elseProgV;

			String thenProgS = thenProg.synthesize().replaceAll("\n", "");
			String elseProgS = (elseProg != null) ? elseProg.synthesize().replaceAll("\n", "") : "SKIP";
			
			alt_callback retCallback = new alt_callback() {
				@Override
				public void ret(HoareCond thenPreCond, HoareCond thenPostCond, HoareCond elsePreCond, HoareCond elsePostCond) throws IOException, HoareException, LexerException, ParserException {
					HoareCond thenPart = new HoareCondAnd(thenPreCond, altCond);
					HoareCond elsePart = new HoareCondAnd(elsePreCond, new HoareCondNeg(altCond));
					
					HoareCond preCond = new HoareCondAnd(thenPart, elsePart);
					
					println("apply alternative rule:");
					println("\t" + thenPart.toStringEx() + " " + thenProgS + " " + postCond.toStringEx() + ", " + elsePart.toStringEx() + " " + elseProgS + " " + postCond.toStringEx());
					println("\t" + "->");
					println("\t" + preCond.toStringEx() + " if " + "(" + altCond + ")" + "{" + thenProgS + "}" + " else " + "{" + elseProgS + "}" + postCond.toStringEx());
					
					callback.result(node, preCond, postCond);
				}
			};
			
			wlp_callback altCallback = new wlp_callback() {
				@Override
				public void result(SyntaxTreeNode node, HoareCond preCond, HoareCond postCond) throws IOException, HoareException, LexerException, ParserException {
					callback.result(node, preCond, postCond);
				}
			};
			
			wlp(thenProg, postCond, new wlp_callback() {
				@Override
				public void result(SyntaxTreeNode thenNode, HoareCond thenPreCond, HoareCond thenPostCond) throws IOException, HoareException, LexerException, ParserException {
					if (elseProg != null) {
						wlp(elseProg, postCond, new wlp_callback() {
							@Override
							public void result(SyntaxTreeNode node, HoareCond elsePreCond, HoareCond elsePostCond) throws IOException, HoareException, LexerException, ParserException {
								
							}
						});
					} else {
						HoareCond elsePreCond = postCond;
						HoareCond elsePostCond = postCond;
						

					}
				}
			});
			
			println_end();
		}
		
		private void wlp_loop_acceptInvariant(SyntaxTreeNode node, HoareCond invariant, HoareCond postCond, wlp_callback callback) throws IOException, HoareException, LexerException, ParserException {			
			HoareCond loopCond = new HoareCondBoolExp(BoolExp.fromString(node.findChild(_grammar.NON_TERMINAL_BOOL_EXP).synthesize()));
			SyntaxTreeNode body = node.findChild(_grammar.NON_TERMINAL_PROG);
			
			println("accept invariant " + invariant);
			
			HoareCond preCond = invariant;
			
			if (preCond==null) throw new RuntimeException("preCond null");
			if (loopCond==null) throw new RuntimeException("loopCond null");
			
			String bodyS = body.synthesize().replaceAll("\n", "");
			
			println("apply loop rule:");
			println("\t" + new HoareCondAnd(preCond, loopCond).toStringEx() + " " + bodyS + " " + preCond.toStringEx());
			println("\t" + "->");
			println("\t" + new HoareCondAnd(preCond).toStringEx() + " while " + "(" + loopCond + ")" + "{" + bodyS + "}" + " " + new HoareCondAnd(preCond, new HoareCondNeg(loopCond)).toStringEx());
			
			println_end();

			callback.result(node, preCond, postCond);
		}
		
		private void wlp_loop_tryInvariant(SyntaxTreeNode node, HoareCond postCond, HoareCond invariantPost, wlp_callback callback) throws HoareException, IOException, LexerException, ParserException {
			//TODO: auto-generate invariants
			
			if (invariantPost == null) {

			} else {
				println("try invariant: " + invariantPost);
				
				wlp(node.findChild(_grammar.NON_TERMINAL_PROG), invariantPost, new wlp_callback() {
					@Override
					public void result(SyntaxTreeNode node, HoareCond invariantPre, HoareCond invariantPost) throws HoareException, LexerException, IOException, ParserException {
						println("tried invariant " + invariantPost + " resulted in " + invariantPre);
						
						_actionInterface.requestImplicationDialog(invariantPre, invariantPost, new ImplicationInterface() {
							@Override
							public void result(boolean yes) throws HoareException, LexerException, IOException, ParserException {
								if (yes) {
									wlp_loop_acceptInvariant(node, invariantPre, invariantPost, callback);
								} else {
									wlp_loop_tryInvariant(node, postCond, null, callback);
								}
							}
						}, true);
					}
				});
			}
		}
		
		private void wlp_loop(SyntaxTreeNode node, HoareCond postCond, wlp_callback callback) throws HoareException, IOException {
			println_begin();

			println("applying loop rule... needs invariant");

			HoareCond invariantPost = null;

			_actionInterface.requestInvariantDialog(node, postCond, new InvariantInterface() {
				@Override
				public void result(HoareCond invariant) throws HoareException, IOException, LexerException, ParserException {
					if (invariant != null) {
						wlp_loop_tryInvariant(node, invariant, invariantPost, callback);
					} else {
						throw new HoareException("aborted");
					}
				}
			});
		}
		
		private void wlp_consequence_pre(SyntaxTreeNode node, HoareCond origPreCond, HoareCond origPostCond, HoareCond newPreCond, HoareCond newPostCond, wlp_callback callback) throws IOException, HoareException, LexerException, ParserException {
			println_begin();
			
			String bodyS = node.synthesize().replaceAll("\n", "");
			
			System.out.println("apply consequence rule (pre)");
			System.out.println("\t" + newPreCond + "->" + origPreCond + ", " + origPostCond.toStringEx() + " " + bodyS + " " + origPostCond.toStringEx() + ", " + origPostCond + "->" + newPostCond);
			System.out.println("\t" + "->");
			System.out.println("\t" + newPreCond.toStringEx() + " " + bodyS + " " + newPostCond.toStringEx());
			
			println_end();
			
			//TODO: for post as well, merged?
			callback.result(node, newPreCond, origPreCond);
		}
		
		private void wlp(SyntaxTreeNode node, HoareCond postCondV, wlp_callback callback) throws HoareException, IOException, LexerException, ParserException {
			_wlp_nestDepth++;

			_currentNodeP.set(node);
			
			final HoareCond postCond = postCondV.copy();
			
			_postCondMap.put(node, postCond);

			//System.out.println(StringUtil.repeat("\t", _wlp_nestDepth) + "postcond " + node);

			wlp_callback retCallback = new wlp_callback() {
				@Override
				public void result(SyntaxTreeNode node, HoareCond preCond, HoareCond postCond) throws IOException, HoareException, LexerException, ParserException {
					_preCondMap.put(node, preCond);
					_wlp_nestDepth--;
					
					callback.result(node, preCond, postCond);
				}
			};

			Symbol symbol = node.getSymbol();
			
			if (symbol.equals(_grammar.NON_TERMINAL_PROG)) {
				SyntaxTreeNode cmdChild = node.findChild(_grammar.NON_TERMINAL_CMD);
				SyntaxTreeNode lastChild = node.findChild(_grammar.NON_TERMINAL_PROG_); 
				
				assert(cmdChild != null);
				assert(lastChild != null);
				
				if (lastChild.findChild(_grammar.NON_TERMINAL_CMD) != null) {
					System.out.println("find " + node);
					wlp_composite(node, postCond, retCallback);
				} else {
					wlp(cmdChild, postCond, retCallback);
				}
			} else if (symbol.equals(_grammar.NON_TERMINAL_PROG_)) {
				SyntaxTreeNode cmdChild = node.findChild(_grammar.NON_TERMINAL_CMD);
				SyntaxTreeNode lastChild = node.findChild(_grammar.NON_TERMINAL_PROG_); 
				
				assert(cmdChild != null);
				assert(lastChild != null);
				
				if (lastChild.findChild(_grammar.NON_TERMINAL_CMD) != null) {
					wlp_composite(node, postCond, retCallback);
				} else {
					wlp(cmdChild, postCond, retCallback);
				}
			} else if (symbol.equals(_grammar.NON_TERMINAL_CMD)) {
				wlp(node.getChildren().firstElement(), postCond, retCallback);
			} else if (symbol.equals(_grammar.NON_TERMINAL_SKIP))
				wlp_skip(node, postCond, retCallback);
			else if (symbol.equals(_grammar.NON_TERMINAL_ASSIGN)) {
				wlp_assign(node, postCond, retCallback);
			} else if (symbol.equals(_grammar.NON_TERMINAL_SELECTION)) {
				wlp_alt(node, postCond, retCallback);
			} else if (symbol.equals(_grammar.NON_TERMINAL_WHILE)) {
				wlp_loop(node, postCond, retCallback);
			} else if (symbol.equals(_grammar.NON_TERMINAL_HOARE_BLOCK)) {
				SyntaxTreeNode progNode = node.findChild(_grammar.NON_TERMINAL_PROG);
				
				System.out.println("wlp " + progNode.hashCode());
						
				wlp(progNode, postCond, retCallback);
			} else {
				throw new HoareException("no wlp for " + node + " with rule " + node.getSubRule());
			}
		}
		
		public void exec() throws IOException, HoareException, LexerException, ParserException {
			SyntaxTreeNode refNode = _node.getRefNode();

			_currentHoareNodeP.set(refNode);
			
			SyntaxTreeNode preNode = refNode.findChild(_grammar.NON_TERMINAL_HOARE_PRE);
			SyntaxTreeNode postNode = refNode.findChild(_grammar.NON_TERMINAL_HOARE_POST);
			
			HoareCond preCond = new HoareCondBoolExp(BoolExp.fromString(preNode.findChild(_grammar.NON_TERMINAL_BOOL_EXP).synthesize()));
			HoareCond postCond = new HoareCondBoolExp(BoolExp.fromString(postNode.findChild(_grammar.NON_TERMINAL_BOOL_EXP).synthesize()));
			
			System.err.println(StringUtil.repeat("\t", _nestDepth) + "checking " + preCond + "->" + postCond + " at " + _node);
			
			_wlp_nestDepth = 0;
			_wlp_printDepth = 0;
			
			wlp(refNode, postCond, new wlp_callback() {
				@Override
				public void result(SyntaxTreeNode node, HoareCond lastPreCond, HoareCond lastPostCond) throws IOException, HoareException, LexerException, ParserException {
					System.out.println("final preCondition: " + preCond);
					
					try {
						implicates(preCond, lastPreCond, new ImplicationInterface() {
							@Override
							public void result(boolean yes) throws HoareException, LexerException, IOException, ParserException {
								if (yes) {
									System.out.println(preCond + "->" + postCond + " holds true (wlp: " + preCond + ")");
								} else {
									System.out.println(preCond + "->" + postCond + " failed (wlp: " + preCond + ")");
								}
								
								_actionInterface.finished(refNode, preCond, postCond, yes);
								
								_callback.finished();
							}
						});
					} catch (ScriptException e) {
						e.printStackTrace();
					}
				}
			});
		}
		
		public void start() throws IOException, HoareException, LexerException, ParserException {
			_execChainIt.next().exec();
		}
		
		public Executer(HoareNode node, int nestDepth, HoareWhileGrammar grammar, ObservableMap<SyntaxTreeNode, HoareCond> preCondMap, ObservableMap<SyntaxTreeNode, HoareCond> postCondMap, ObjectProperty<SyntaxTreeNode> currentNodeP, ObjectProperty<SyntaxTreeNode> currentHoareNodeP, ActionInterface actionInterface, ExecInterface callback) throws IOException, HoareException, NoRuleException, LexerException {
			_node = node;
			_nestDepth = nestDepth;
			_grammar = grammar;
			_preCondMap = preCondMap;
			_postCondMap = postCondMap;
			_currentNodeP = currentNodeP;
			_currentHoareNodeP = currentHoareNodeP;
			_actionInterface = actionInterface;
			_callback = callback;
			
			ExecInterface childCallback = new ExecInterface() {
				@Override
				public void finished() throws HoareException, LexerException, IOException, ParserException {
					Executer next = _execChainIt.next();

					next.exec();
				}
			};
			
			for (HoareNode child : node.getChildren()) {
				_execChain.add(new Executer(child, nestDepth + 1, _grammar, _preCondMap, _postCondMap, _currentNodeP, _currentHoareNodeP, _actionInterface, childCallback));
			}
			
			_execChain.add(this);
			
			_execChainIt = _execChain.iterator();
		}
	}
	
	private Vector<Executer> _execChain;
	private Iterator<Executer> _execChainIt;
	
	public void exec() throws HoareException, LexerException, IOException, ParserException {
		System.err.println("hoaring...");
System.out.println("exec with " + _syntaxTreeP.get().hashCode());
		Vector<HoareNode> children = collectChildren(_syntaxTreeP.get().getRoot());

		if (children.isEmpty()) {
			System.err.println("no hoareBlocks");
		} else {
			_execChain = new Vector<>();
			
			for (HoareNode child : children) {
				if (children.lastElement().equals(child)) {
					_execChain.add(new Executer(child, 0, _grammar, _preCondMapP, _postCondMapP, _currentNodeP, _currentHoareNodeP, _actionInterface, new ExecInterface() {
						@Override
						public void finished() throws HoareException, NoRuleException, LexerException, IOException {
							System.err.println("hoaring finished");
							
							_currentHoareNodeP.set(null);
						}
					}));
				} else {
					_execChain.add(new Executer(child, 0, _grammar, _preCondMapP, _postCondMapP, _currentNodeP, _currentHoareNodeP, _actionInterface, new ExecInterface() {
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
	
	public Hoare(ObjectProperty<SyntaxTree> syntaxTreeP, ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCond>> preCondMap, ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCond>> postCondMap, ObjectProperty<SyntaxTreeNode> currentNodeP, ObjectProperty<SyntaxTreeNode> currentHoareNodeP, ActionInterface actionInterface) throws Exception {
		_syntaxTreeP = syntaxTreeP;
		_preCondMapP = preCondMap.get();
		_postCondMapP = postCondMap.get();
		_currentNodeP = currentNodeP;
		_currentHoareNodeP = currentHoareNodeP;
		_actionInterface = actionInterface;

		if (_syntaxTreeP == null) throw new Exception("no syntaxTree");
		if (_preCondMapP == null) throw new Exception("no preCondMap");
		if (_postCondMapP == null) throw new Exception("no postCondMap");
		
		_grammar = (HoareWhileGrammar) _syntaxTreeP.get().getGrammar();
	}
}