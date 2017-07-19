package core;

import java.util.Vector;
import java.util.function.UnaryOperator;

import core.structures.LexerRule;
import core.structures.ParserRulePattern;
import grammars.ExpGrammar;

public class SyntaxTreeNode {
	private Vector<SyntaxTreeNode> _children = new Vector<>();
	
	public Vector<SyntaxTreeNode> getChildren() {
		return _children;
	}
	
	public SyntaxTreeNode findChild(RuleKey ruleKey, int index) {
		int c = 0;
		System.err.println("find " + ruleKey + " " + _children.size());
		for (SyntaxTreeNode child : _children) {
			Rule rule = child.getRule();
			System.err.println("rule " + rule + ";" + child);
			if (rule == null) continue;
			
			SyntaxTreeNode found = null;
			
			if (rule.getKey().equals(ruleKey)) {
				found = child;
			} else {
				found = child.findChild(ruleKey, 1);
			}
			
			if (found != null) {
				c++;
				
				if (c >= index) return found;
			}
		}
		
		return null;
	}
	
	public SyntaxTreeNode findChild(String ruleKeyS, int index) {
		return findChild(new RuleKey(ruleKeyS), index);
	}
	
	public SyntaxTreeNode findChild(RuleKey ruleKey) {
		return findChild(ruleKey, 1);
	}
	
	public SyntaxTreeNode findChild(String ruleKeyS) {
		return findChild(new RuleKey(ruleKeyS), 1);
	}
	
	private Rule _rule;
	
	public Rule getRule() {
		return _rule;
	}
	
	private ParserRulePattern _childrenPattern;
	
	public ParserRulePattern getChildrenPattern() {
		return _childrenPattern;
	}
	
	@Override
	public String toString() {
		return _rule.toString();
	}

	public String toStringVert() {
		return _rule.toString();
	}
	
	public String synthesize() {
		if (this instanceof SyntaxTreeNodeTerminal) {
			Token token = ((SyntaxTreeNodeTerminal) this).getToken();
			
			if (token == null) return "";
			
			return token.getText();
		}
		
		StringBuilder sb = new StringBuilder();
		
		for (SyntaxTreeNode child : getChildren()) {
			sb.append(child.synthesize());
		}
		
		return sb.toString();
	}
	
	public SyntaxTreeNode copy() {
		SyntaxTreeNode ret = new SyntaxTreeNode(_rule, _childrenPattern);
		
		for (SyntaxTreeNode child : _children) {
			ret.addChild(child.copy());
		}
		
		return ret;
	}
	
	public void replace(LexerRule lexerRule, String var, SyntaxTreeNode exp) {
		for (SyntaxTreeNode child : _children) {
			child.replace(lexerRule, var, exp);
		}
		
		_children.replaceAll(new UnaryOperator<SyntaxTreeNode>() {
			@Override
			public SyntaxTreeNode apply(SyntaxTreeNode child) {
				if (child instanceof SyntaxTreeNodeTerminal) {
					Token token = ((SyntaxTreeNodeTerminal) child).getToken();

					if ((token != null) && token.getRule().equals(lexerRule) && token.getText().equals(var)) {
						return exp.copy();
					}
				}

				return child;
			}
		});
	}
	
	public void print(int nestDepth) {
		System.out.println(new String(new char[nestDepth]).replace('\0', '\t') + _rule);
		if (_children.isEmpty()) {
			System.out.println(new String(new char[nestDepth + 1]).replace('\0', '\t') + "eps");
		} else {
			for (SyntaxTreeNode child : _children) {
				child.print(nestDepth + 1);
			}
		}
	}
	
	public void addChild(SyntaxTreeNode child) {
		_children.add(child);
	}
	
	public SyntaxTreeNode(Rule rule, ParserRulePattern childrenPattern) {
		_rule = rule;
		_childrenPattern = childrenPattern;
	}
}