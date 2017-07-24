package core;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.script.ScriptException;

import core.Lexer.LexerException;
import core.Parser.NoRuleException;
import core.Parser.ParserException;
import core.structures.HoareCondition;
import core.structures.HoareConditionBoolExpr;
import core.structures.HoareConditionOr;
import core.structures.LexerRule;
import grammars.HoareWhileGrammar;
import gui.ImplicationDialog;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableMap;
import util.StringUtil;

public class Hoare {
	private HoareWhileGrammar _grammar;
	private SyntaxTree _tree;
	private ObservableMap<SyntaxTreeNode, HoareCondition> _preCondMap;
	private ObservableMap<SyntaxTreeNode, HoareCondition> _postCondMap;
	
	public static class HoareException extends Exception {
		private static final long serialVersionUID = 1L;

		public HoareException(String msg) {
			super(msg);
		}
	}
	
	public HoareCondition boolExprToHoareCondition(SyntaxTreeNode node) {
		Rule rule = node.getRule();
		
		if (!rule.equals(_grammar.boolExpRule)) throw new RuntimeException("not a boolExpr");
		
		return new HoareConditionBoolExpr(node);
	}
	
	private HoareCondition wlp(SyntaxTreeNode node, HoareCondition postCondition, int nestDepth) throws HoareException {
		_postCondMap.put(node, postCondition);
		
		System.out.println(StringUtil.repeat("\t", nestDepth) + "postcond " + node);
		
		HoareCondition ret = null;
		
		if (node.getRule().equals(_grammar.progRule)) {
			SyntaxTreeNode firstChild = node.getChildren().firstElement();
			SyntaxTreeNode lastChild = node.getChildren().lastElement(); 
			
			ret = wlp(firstChild, wlp(lastChild, postCondition, nestDepth + 1), nestDepth + 1);
			
			/*if (node.getChildrenPattern().equals(_grammar.PATTERN_PROG_SKIP)) {
				ret = wlp(firstChild, wlp(lastChild, postCondition, nestDepth + 1), nestDepth + 1);
			}
			if (node.getChildrenPattern().equals(_grammar.PATTERN_PROG_ASSIGN)) {
				ret = wlp(firstChild, wlp(lastChild, postCondition, nestDepth + 1), nestDepth + 1);
			}
			if (node.getChildrenPattern().equals(_grammar.PATTERN_PROG_SELECTION)) {
				ret = wlp(firstChild, wlp(lastChild, postCondition, nestDepth + 1), nestDepth + 1);
			}
			if (node.getChildrenPattern().equals(_grammar.PATTERN_PROG_LOOP)) {
				ret = wlp(firstChild, wlp(lastChild, postCondition, nestDepth + 1), nestDepth + 1);
			}*/
		}
		if (node.getRule().equals(_grammar.prestRule)) {
			if (node.getChildrenPattern().equals(_grammar.PATTERN_PREST_PROG)) {
				ret = wlp(node.findChild("prog"), postCondition, nestDepth + 1);
			}
			if (node.getChildrenPattern().equals(LexerRule.EPSILON)) {
				ret = postCondition;
			}
		}
		if (node.getRule().equals(_grammar.skipRule)) {
			ret = postCondition;
		}
		if (node.getRule().equals(_grammar.assignRule)) {
			if (node.getChildrenPattern().equals(_grammar.PATTERN_ASSIGN)) {
				SyntaxTreeNode idNode = node.findChild("ID");

				SyntaxTreeNode expNode = node.findChild("exp");
				
				String var = idNode.synthesize();
				SyntaxTreeNode exp = expNode;
				
				//System.out.println("replace " + var + " in " + postCondition + " by " + exp.synthesize());
				
				postCondition.replace(_grammar.idRule, var, exp);
				
				ret = postCondition;
			}
		}
		if (node.getRule().equals(_grammar.selectionRule)) {
			if (node.getChildrenPattern().equals(_grammar.PATTERN_SELECTION)) {
				SyntaxTreeNode selectionElseRule = node.findChild("prog");
				
				if (selectionElseRule.getChildrenPattern().equals(LexerRule.EPSILON)) {
					HoareCondition thenCondition = wlp(node.findChild("prog"), postCondition, nestDepth + 1);
					HoareCondition elseCondition = postCondition;
					
					ret = new HoareConditionOr(thenCondition, elseCondition);
				}
				if (selectionElseRule.getChildrenPattern().equals(_grammar.PATTERN_SELECTION_ELSE)) {
					HoareCondition thenCondition = wlp(node.findChild("prog"), postCondition, nestDepth + 1);
					HoareCondition elseCondition = wlp(node.findChild("prog", 2), postCondition, nestDepth + 1);
					
					ret = new HoareConditionOr(thenCondition, elseCondition);
				}
			}
		}
		if (node.getRule().equals(_grammar.whileRule)) {
			//TODO
		}
		
		if (node.getRule().equals(_grammar.hoareBlockRule)) {
			ret = wlp(node.findChild("prog"), postCondition, nestDepth + 1);
		}
		
		if (ret == null) throw new HoareException("no wlp for " + node + " with pattern " + node.getChildrenPattern());
		
		//System.out.println(StringUtil.repeat("\t", nestDepth) + "precond " + node.getRule() + " -> " + ret);
		
		_preCondMap.put(node, ret);
		
		return ret;
	}
	
	//not a or b
	
	private boolean check(HoareCondition a) throws ScriptException {
		/*ScriptEngineManager manager = new ScriptEngineManager();
		
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		
		System.out.println(engine.eval(a.toString()));
		
		return true;*/
		return true;
	}
	
	public interface ImplicationInterface {
		public void result(boolean yes) throws HoareException, LexerException, IOException, ParserException;
	}
	
	private boolean implicates(HoareCondition a, HoareCondition b, ImplicationInterface callback) throws ScriptException, IOException, HoareException, LexerException, ParserException {
		System.out.println("try implication " + a + "->" + b);
		
		//TODO implicit check
		boolean checkSuccess = false;
		boolean checkResult = false;
		
		if (checkSuccess) {
			callback.result(checkResult);
		} else {
			new ImplicationDialog(a, b, callback).show();
		}
		
		return !check(a) || check(b);
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
		
		if ((node.getRule() != null) && node.getRule().equals(_grammar.hoareBlockRule)) {
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
	
	private class Executer {
		private HoareNode _node;
		private int _nestDepth;
		private ExecInterface _callback;
		
		private Vector<Executer> _execChain = new Vector<>();
		private Iterator<Executer> _execChainIt;
		
		public void exec() throws IOException, HoareException, LexerException, ParserException {
			SyntaxTreeNode preNode = _node._actualNode.findChild(new RuleKey("boolExp"));
			SyntaxTreeNode postNode = _node._actualNode.findChild(new RuleKey("boolExp"));
			
			HoareCondition preCondition = HoareCondition.fromString(preNode.synthesize());
			HoareCondition postCondition = HoareCondition.fromString(postNode.synthesize());
			
			System.err.println(StringUtil.repeat("\t", _nestDepth) + "checking " + preCondition + "->" + postCondition + " at " + _node);
			
			HoareCondition finalPreCondition = wlp(_node._actualNode, postCondition, 0);
			
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
		
		public void start() throws IOException, HoareException, LexerException, ParserException {
			_execChainIt.next().exec();
		}
		
		public Executer(HoareNode node, int nestDepth, ExecInterface callback) throws IOException, HoareException, NoRuleException, LexerException {
			_node = node;
			_nestDepth = nestDepth;
			_callback = callback;
			
			ExecInterface childCallback = new ExecInterface() {
				@Override
				public void finished() throws HoareException, LexerException, IOException, ParserException {
					Executer next = _execChainIt.next();

					next.exec();
				}
			};
			
			for (HoareNode child : node.getChildren()) {
				_execChain.add(new Executer(child, nestDepth + 1, childCallback));
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
					_execChain.add(new Executer(child, 0, new ExecInterface() {
						@Override
						public void finished() throws HoareException, NoRuleException, LexerException, IOException {
							System.err.println("hoaring finished");
						}
					}));
				} else {
					_execChain.add(new Executer(child, 0, new ExecInterface() {
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
	
	public Hoare(ObjectProperty<SyntaxTree> tree, ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCondition>> preCondMap, ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCondition>> postCondMap) throws Exception {
		_tree = tree.get();
		_preCondMap = preCondMap.get();
		_postCondMap = postCondMap.get();

		if (_tree == null) throw new Exception("no syntaxTree");
		if (_preCondMap == null) throw new Exception("no preCondMap");
		if (_postCondMap == null) throw new Exception("no postCondMap");
		
		_grammar = (HoareWhileGrammar) _tree.getGrammar();
	}
}