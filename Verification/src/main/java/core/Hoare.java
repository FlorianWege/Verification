package core;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.script.ScriptException;
import javax.swing.plaf.synth.SynthSpinnerUI;

import core.Hoare.HoareException;
import core.Lexer.LexerException;
import core.Parser.NoRuleException;
import core.Parser.ParserException;
import core.structures.Exp;
import core.structures.Terminal;
import core.structures.hoareCond.HoareCond;
import core.structures.hoareCond.HoareCondAnd;
import core.structures.hoareCond.HoareCondBoolExpr;
import core.structures.hoareCond.HoareCondNeg;
import core.structures.hoareCond.HoareCondOr;
import grammars.HoareWhileGrammar;
import gui.ImplicationDialog;
import gui.InvariantDialog;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableMap;
import util.StringUtil;

public class Hoare {
	private HoareWhileGrammar _grammar;
	private SyntaxTree _tree;
	private ObservableMap<SyntaxTreeNode, HoareCond> _preCondMap;
	private ObservableMap<SyntaxTreeNode, HoareCond> _postCondMap;
	
	public static class HoareException extends Exception {
		private static final long serialVersionUID = 1L;

		public HoareException(String msg) {
			super(msg);
		}
	}
	
	public HoareCond boolExprToHoareCondition(SyntaxTreeNode node) {
		Symbol rule = node.getSymbol();
		
		if (!rule.equals(_grammar.NON_TERMINAL_BOOL_EXP)) throw new RuntimeException("not a boolExpr");
		
		return new HoareCondBoolExpr(node);
	}
	
	private class HoareNode {
		private SyntaxTreeNode _actualNode;

		private Vector<HoareNode> _children = new Vector<>();

		public Vector<HoareNode> getChildren() {
			return _children;
		}
		
		public void addChild(HoareNode child) {
			_children.add(child);
		}
		
		public HoareNode(SyntaxTreeNode actualNode) {
			_actualNode = actualNode;
		}
	}
	
	private Vector<HoareNode> collectChildren(SyntaxTreeNode node) {
		Vector<HoareNode> ret = new Vector<>();
		
		for (SyntaxTreeNode child : node.getChildren()) {
			Vector<HoareNode> hoareChildren = collectChildren(child);

			ret.addAll(hoareChildren);
		}
		
		if ((node.getSymbol() != null) && node.getSymbol().equals(_grammar.nonTerminal_hoare_block)) {
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
		private ExecInterface _callback;
		
		private Vector<Executer> _execChain = new Vector<>();
		private Iterator<Executer> _execChainIt;
		
		public interface ImplicationInterface {
			public void result(boolean yes) throws HoareException, LexerException, IOException, ParserException;
		}
		
		public interface InvariantInterface {
			public void result(HoareCond invariant) throws HoareException, LexerException, IOException, ParserException;
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
				new ImplicationDialog(a, b, callback, false).show();
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
			public void result(HoareCond cond) throws IOException, HoareException, LexerException, ParserException;
		}
		
		private void wlp_assign(HoareCond postCond, String var, SyntaxTreeNode valNode, wlp_callback callback) throws IOException, HoareException {
			println_begin();
			
			HoareCond preCond = postCond.copy();

			try {
				Exp val = Exp.fromString(valNode.synthesize());
				
				preCond.replace(_grammar.TERMINAL_ID, var, val.getBaseEx());
				
				println("apply assignment rule:");
				println("\t" + postCond.toStringEx(var + ":=" + valNode.synthesize()) + " " + var + "=" + valNode.synthesize() + " " + postCond.toStringEx());
				println("\t->" + preCond.toStringEx() + " " + var + "=" + valNode.synthesize() + " " + postCond.toStringEx());
			
				println_end();

				callback.result(preCond);
			} catch (ParserException | LexerException e) {
				throw new RuntimeException(e);
			}
		}
		
		private void wlp_composite(HoareCond postCond, SyntaxTreeNode first, SyntaxTreeNode second, wlp_callback callback) throws HoareException, IOException, LexerException, ParserException {
			println_begin();
			
			println("applying composition rule...");
			
			wlp(second, postCond, new wlp_callback() {
				@Override
				public void result(HoareCond midCond) throws IOException, HoareException, LexerException, ParserException {
					wlp(first, midCond, new wlp_callback() {
						@Override
						public void result(HoareCond preCond) throws IOException, HoareException, LexerException, ParserException {
							String firstS = first.synthesize().replaceAll("\n", "");
							String secondS = second.synthesize().replaceAll("\n", "");
							
							//System.out.println("{" + postCondition + "}" + " -> " + "{" + midCondition + "}" + " -> " + "{" + ret + "}");
							println("apply composition rule:");
							println("\t" + preCond.toStringEx() + " " + firstS + " " + midCond.toStringEx() + ", " + midCond.toStringEx() + " " + secondS + " " + postCond.toStringEx());
							println("\t" + "->");
							println("\t" + preCond.toStringEx() + " " + firstS + "; " + secondS + " " + postCond.toStringEx());
							
							println_end();
							
							callback.result(preCond);
						}
					});
				}
			});
		}
		
		private void wlp_alt(HoareCond postCond, HoareCondBoolExpr altCond, SyntaxTreeNode first, SyntaxTreeNode second, wlp_callback callback) throws IOException, HoareException, LexerException, ParserException {
			println_begin();
			
			HoareCond preCond = postCond.copy();

			String firstS = first.synthesize().replaceAll("\n", "");
			String secondS = second.synthesize().replaceAll("\n", "");
			
			//TODO
			println("apply alternative rule:");
			println("\t" + new HoareCondAnd(preCond, altCond).toStringEx() + " " + firstS + " " + postCond.toStringEx() + ", " + new HoareCondAnd(preCond, new HoareCondNeg(altCond)).toStringEx() + " " + secondS + " " + postCond.toStringEx());
			println("\t" + "->");
			println("\t" + preCond.toStringEx() + " if " + "(" + altCond + ")" + "{" + firstS + "}" + " else " + "{" + secondS + "}" + postCond.toStringEx());
			
			println_end();
			
			callback.result(preCond);
		}
		
		private void wlp_loop_acceptInvariant(HoareCond invariant, SyntaxTreeNode loopNode, wlp_callback callback) throws IOException, HoareException, LexerException, ParserException {
			HoareCond loopCond = new HoareCondBoolExpr(loopNode.findChild(_grammar.NON_TERMINAL_BOOL_EXP));
			SyntaxTreeNode body = loopNode.findChild(_grammar.NON_TERMINAL_PROG);
			
			println("accept invariant " + invariant);
			
			HoareCond preCond = new HoareCondAnd(invariant);
			
			if (preCond==null) throw new RuntimeException("preCond null");
			if (loopCond==null) throw new RuntimeException("loopCond null");
			
			String bodyS = body.synthesize().replaceAll("\n", "");
			
			println("apply loop rule:");
			println("\t" + new HoareCondAnd(preCond, loopCond).toStringEx() + " " + bodyS + " " + preCond.toStringEx());
			println("\t" + "->");
			println("\t" + new HoareCondAnd(preCond).toStringEx() + " while " + "(" + loopCond + ")" + "{" + bodyS + "}" + " " + new HoareCondAnd(preCond, new HoareCondNeg(loopCond)).toStringEx());
			
			println_end();

			callback.result(preCond);
		}
		
		private void wlp_loop_tryInvariant(SyntaxTreeNode loopNode, HoareCond postCond, HoareCond invariantPost, wlp_callback callback) throws HoareException, IOException, LexerException, ParserException {
			//TODO: auto-generate invariants
			
			if (invariantPost == null) {
				println("failed to guess invariant: ask user");
				
				InvariantDialog diag = new InvariantDialog(_grammar, loopNode, postCond, new InvariantInterface() {
					@Override
					public void result(HoareCond invariant) throws HoareException, IOException, LexerException, ParserException {
						if (invariant != null) {
							wlp_loop_acceptInvariant(invariant, loopNode, callback);
						} else {
							throw new HoareException("aborted");
						}
					}
				});
				
				diag.show();
			} else {
				println("try invariant: " + invariantPost);
				
				wlp(loopNode.findChild(_grammar.NON_TERMINAL_PROG), invariantPost, new wlp_callback() {
					@Override
					public void result(HoareCond invariantPre) throws HoareException, LexerException, IOException, ParserException {
						println("tried invariant " + invariantPost + " resulted in " + invariantPre);
						
						ImplicationDialog diag = new ImplicationDialog(invariantPre, invariantPost, new ImplicationInterface() {
							@Override
							public void result(boolean yes) throws HoareException, LexerException, IOException, ParserException {
								if (yes) {
									wlp_loop_acceptInvariant(invariantPre, loopNode, callback);
								} else {
									wlp_loop_tryInvariant(loopNode, postCond, null, callback);
								}
							}
						}, true);
						
						diag.show();
					}
				});
			}
		}
		
		private void wlp_loop(HoareCond postCond, SyntaxTreeNode loopNode, wlp_callback callback) throws HoareException, IOException {
			println_begin();
			
			try {
				println("applying loop rule... needs invariant");
				
				//HoareCondition invariantPost = HoareCondition.fromString("erg==2^(y-x)");
				HoareCond invariantPost = null;//HoareCond.fromString("y==z!");
				
				wlp_loop_tryInvariant(loopNode, postCond, invariantPost, callback);
			} catch (LexerException | ParserException e) {
				throw new HoareException(e.getMessage());
			}
		}
		
		private void wlp_consequence_pre(HoareCond origPreCond, HoareCond origPostCond, SyntaxTreeNode body, HoareCond newPreCond, HoareCond newPostCond, wlp_callback callback) throws IOException, HoareException, LexerException, ParserException {
			println_begin();
			
			String bodyS = body.synthesize().replaceAll("\n", "");
			
			System.out.println("apply consequence rule");
			System.out.println("\t" + newPreCond + "->" + origPreCond + ", " + origPostCond.toStringEx() + " " + bodyS + " " + origPostCond.toStringEx() + ", " + origPostCond + "->" + newPostCond);
			System.out.println("\t" + "->");
			System.out.println("\t" + newPreCond.toStringEx() + " " + bodyS + " " + newPostCond.toStringEx());
			
			println_end();
			
			//TODO: for post as well, merged?
			callback.result(newPreCond);
		}
		
		private void wlp(SyntaxTreeNode node, HoareCond postCondV, wlp_callback callback) throws HoareException, IOException, LexerException, ParserException {
			_wlp_nestDepth++;
			
			final HoareCond postCond = postCondV.copy();
			
			_postCondMap.put(node, postCond);
			
			//System.out.println(StringUtil.repeat("\t", _wlp_nestDepth) + "postcond " + node);

			wlp_callback retCallback = new wlp_callback() {
				@Override
				public void result(HoareCond cond) throws IOException, HoareException, LexerException, ParserException {
					_preCondMap.put(node, cond);
					_wlp_nestDepth--;
					
					callback.result(cond);
				}
			};

			if (node.getSymbol().equals(_grammar.NON_TERMINAL_PROG)) {
				SyntaxTreeNode firstChild = node.getChildren().firstElement();
				SyntaxTreeNode lastChild = node.getChildren().lastElement(); 
				
				if (lastChild.findChild(_grammar.NON_TERMINAL_PROG) != null) {
					wlp_composite(postCond, firstChild, lastChild.findChild(_grammar.NON_TERMINAL_PROG), retCallback);
				} else {
					wlp(firstChild, postCond, retCallback);
				}
			} else if (node.getSymbol().equals(_grammar.NON_TERMINAL_PROG_)) {
				if (node.getSubRule().equals(_grammar.RULE_PROG__PROG))
					wlp(node.findChild(_grammar.NON_TERMINAL_PROG), postCond, retCallback);
				else
					retCallback.result(postCond);
			} else if (node.getSymbol().equals(_grammar.NON_TERMINAL_SKIP))
				retCallback.result(postCond);
			else if (node.getSymbol().equals(_grammar.NON_TERMINAL_ASSIGN)) {
				SyntaxTreeNode idNode = node.findChild(_grammar.TERMINAL_ID);

				SyntaxTreeNode expNode = node.findChild(_grammar.NON_TERMINAL_EXP);
				
				String var = idNode.synthesize();
				SyntaxTreeNode exp = expNode;
				
				wlp_assign(postCond, var, exp, retCallback);
			} else if (node.getSymbol().equals(_grammar.NON_TERMINAL_SELECTION)) {
				if (node.getSubRule().equals(_grammar.RULE_SELECTION)) {
					SyntaxTreeNode selectionElseRule = node.findChild(_grammar.NON_TERMINAL_PROG);
					
					if (selectionElseRule.getSubRule().equals(Terminal.EPSILON)) {
						wlp(node.findChild(_grammar.NON_TERMINAL_PROG), postCond, new wlp_callback() {
							@Override
							public void result(HoareCond thenCond) throws IOException, HoareException, LexerException, ParserException {
								HoareCond elseCond = postCond;
								
								retCallback.result(new HoareCondOr(thenCond, elseCond));
							}
						});
					} else if (selectionElseRule.getSubRule().equals(_grammar.RULE_SELECTION_ELSE)) {
						wlp(node.findChild(_grammar.NON_TERMINAL_PROG), postCond, new wlp_callback() {
							@Override
							public void result(HoareCond thenCond) throws IOException, HoareException, LexerException, ParserException {
								wlp(node.findChild(_grammar.NON_TERMINAL_PROG, 2), postCond, new wlp_callback() {
									@Override
									public void result(HoareCond elseCond) throws IOException, HoareException, LexerException, ParserException {
										retCallback.result(new HoareCondOr(thenCond, elseCond));
									}
								});
							}
						});
					}
				}
			} else if (node.getSymbol().equals(_grammar.NON_TERMINAL_WHILE)) {
				wlp_loop(postCond, node, retCallback);
			} else if (node.getSymbol().equals(_grammar.nonTerminal_hoare_block)) {
				wlp(node.findChild(_grammar.NON_TERMINAL_PROG), postCond, retCallback);
			} else {
				throw new HoareException("no wlp for " + node + " with rule " + node.getSubRule());
			}
		}
		
		public void exec() throws IOException, HoareException, LexerException, ParserException {
			SyntaxTreeNode preNode = _node._actualNode.findChild(_grammar.nonTerminal_hoare_pre);
			SyntaxTreeNode postNode = _node._actualNode.findChild(_grammar.nonTerminal_hoare_post);
			
			HoareCond preCondition = HoareCond.fromString(preNode.findChild(_grammar.NON_TERMINAL_BOOL_EXP).synthesize());
			HoareCond postCondition = HoareCond.fromString(postNode.findChild(_grammar.NON_TERMINAL_BOOL_EXP).synthesize());
			
			System.err.println(StringUtil.repeat("\t", _nestDepth) + "checking " + preCondition + "->" + postCondition + " at " + _node);
			
			_wlp_nestDepth = 0;
			_wlp_printDepth = 0;
			
			wlp(_node._actualNode, postCondition, new wlp_callback() {
				@Override
				public void result(HoareCond finalPreCondition) throws IOException, HoareException, LexerException, ParserException {
					System.out.println("final preCondition: " + finalPreCondition);
					
					try {
						implicates(preCondition, finalPreCondition, new ImplicationInterface() {
							@Override
							public void result(boolean yes) throws HoareException, LexerException, IOException, ParserException {
								if (yes) {
									System.out.println(preCondition + "->" + postCondition + " holds true (wlp: " + finalPreCondition + ")");
								} else {
									System.out.println(preCondition + "->" + postCondition + " failed (wlp: " + finalPreCondition + ")");
								}
								
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
		
		public Executer(HoareNode node, int nestDepth, HoareWhileGrammar grammar, ObservableMap<SyntaxTreeNode, HoareCond> preCondMap, ObservableMap<SyntaxTreeNode, HoareCond> postCondMap, ExecInterface callback) throws IOException, HoareException, NoRuleException, LexerException {
			_node = node;
			_nestDepth = nestDepth;
			_grammar = grammar;
			_preCondMap = preCondMap;
			_postCondMap = postCondMap;
			_callback = callback;
			
			ExecInterface childCallback = new ExecInterface() {
				@Override
				public void finished() throws HoareException, LexerException, IOException, ParserException {
					Executer next = _execChainIt.next();

					next.exec();
				}
			};
			
			for (HoareNode child : node.getChildren()) {
				_execChain.add(new Executer(child, nestDepth + 1, _grammar, preCondMap, postCondMap, childCallback));
			}
			
			_execChain.add(this);
			
			_execChainIt = _execChain.iterator();
		}
	}
	
	private Vector<Executer> _execChain;
	private Iterator<Executer> _execChainIt;
	
	public void exec() throws HoareException, LexerException, IOException, ParserException {
		System.err.println("hoaring...");

		Vector<HoareNode> children = collectChildren(_tree.getRoot());

		if (children.isEmpty()) {
			System.err.println("no hoareBlocks");
		} else {
			_execChain = new Vector<>();
			
			for (HoareNode child : children) {
				if (children.lastElement().equals(child)) {
					_execChain.add(new Executer(child, 0, _grammar, _preCondMap, _postCondMap, new ExecInterface() {
						@Override
						public void finished() throws HoareException, NoRuleException, LexerException, IOException {
							System.err.println("hoaring finished");
						}
					}));
				} else {
					_execChain.add(new Executer(child, 0, _grammar, _preCondMap, _postCondMap, new ExecInterface() {
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
	
	public Hoare(ObjectProperty<SyntaxTree> tree, ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCond>> preCondMap, ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCond>> postCondMap) throws Exception {
		_tree = tree.get();
		_preCondMap = preCondMap.get();
		_postCondMap = postCondMap.get();

		if (_tree == null) throw new Exception("no syntaxTree");
		if (_preCondMap == null) throw new Exception("no preCondMap");
		if (_postCondMap == null) throw new Exception("no postCondMap");
		
		_grammar = (HoareWhileGrammar) _tree.getGrammar();
	}
}