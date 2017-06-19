package core;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import grammars.WhileGrammar;
import util.StringUtil;

public class Hoare {
	private WhileGrammar _grammar;
	private SyntaxTree _tree;
	
	public static class HoareException extends Exception {
		public HoareException(String msg) {
			super(msg);
		}
	}
	
	public HoareCondition boolExprToHoareCondition(SyntaxTreeNode node) {
		Rule rule = node.getRule();
		
		if (rule != _grammar.boolExpRule) throw new RuntimeException("not a boolExpr");
		
		return new HoareConditionBoolExpr(node);
	}
	
	private HoareCondition wlp(SyntaxTreeNode node, HoareCondition postCondition, int nestDepth) throws HoareException {
		System.out.println(StringUtil.repeat("\t", nestDepth) + "postcond " + node);
		
		HoareCondition ret = null;
		
		if (node.getRule() == _grammar.progRule) {
			SyntaxTreeNode firstChild = node.getChildren().firstElement();
			SyntaxTreeNode lastChild = node.getChildren().lastElement(); 
			
			if (node.getChildrenPattern().equals(_grammar.progRule.getRulePattern(0))) {
				ret = wlp(firstChild, wlp(lastChild, postCondition, nestDepth + 1), nestDepth + 1);
			}
			if (node.getChildrenPattern().equals(_grammar.progRule.getRulePattern(1))) {
				ret = wlp(firstChild, wlp(lastChild, postCondition, nestDepth + 1), nestDepth + 1);
			}
			if (node.getChildrenPattern().equals(_grammar.progRule.getRulePattern(2))) {
				ret = wlp(firstChild, wlp(lastChild, postCondition, nestDepth + 1), nestDepth + 1);
			}
			if (node.getChildrenPattern().equals(_grammar.progRule.getRulePattern(3))) {
				ret = wlp(firstChild, wlp(lastChild, postCondition, nestDepth + 1), nestDepth + 1);
			}
		}
		if (node.getRule() == _grammar.prestRule) {
			if (node.getChildrenPattern().equals(_grammar.prestRule.getRulePattern(0))) {
				ret = wlp(node.getChildren().get(1), postCondition, nestDepth + 1);
			}
			if (node.getChildrenPattern().equals(_grammar.prestRule.getRulePattern(1))) {
				ret = postCondition;
			}
		}
		if (node.getRule() == _grammar.skipRule) {
			ret = postCondition;
		}
		if (node.getRule() == _grammar.assignRule) {
			if (node.getChildrenPattern().equals(_grammar.assignRule.getRulePattern(0))) {
				SyntaxTreeNode idNode = node.getChildren().get(0);
				SyntaxTreeNode expNode = node.getChildren().get(2);
				
				String var = idNode.synthesize();
				SyntaxTreeNode exp = expNode;
				
				System.out.println("replace " + var + " in " + postCondition + " by " + exp.synthesize());
				
				postCondition.replace(_grammar.idRule, var, exp);
				
				ret = postCondition;
			}
		}
		if (node.getRule() == _grammar.selectionRule) {
			if (node.getChildrenPattern().equals(_grammar.selectionRule.getRulePattern(0))) {
				HoareCondition thenCondition = wlp(node.getChildren().get(3), postCondition, nestDepth + 1);
				HoareCondition elseCondition = wlp(node.getChildren().get(5), postCondition, nestDepth + 1);
				
				ret = new HoareConditionOr(thenCondition, elseCondition);
			}
		}
		if (node.getRule() == _grammar.whileRule) {
			//
		}
		
		if (ret == null) throw new HoareException("no wlp for " + node + " with pattern " + node.getChildrenPattern());
		
		System.out.println(StringUtil.repeat("\t", nestDepth) + "precond " + node.getRule() + " -> " + ret);
		
		return ret;
	}
	
	//not a or b
	
	private boolean check(HoareCondition a) throws ScriptException {
		ScriptEngineManager manager = new ScriptEngineManager();
		
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		
		System.out.println(engine.eval(a.toString()));
		
		return true;
	}
	
	private boolean implicates(HoareCondition a, HoareCondition b) throws ScriptException {
		return !check(a) || check(b);
	}
	
	public void exec(HoareCondition preCondition, HoareCondition postCondition) throws HoareException {
		System.err.println("hoaring...");
		HoareCondition finalPreCondition = wlp(_tree.getRoot(), postCondition, 0);
		
		try {
			implicates(preCondition, finalPreCondition);
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Hoare(SyntaxTree tree) {
		_tree = tree;

		_grammar = (WhileGrammar) tree.getGrammar();
	}
}