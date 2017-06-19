package core;

import java.util.Vector;
import java.util.function.UnaryOperator;

import grammars.ExpGrammar;

public class SyntaxTreeNode {
	private Vector<SyntaxTreeNode> _children = new Vector<>();
	
	public Vector<SyntaxTreeNode> getChildren() {
		return _children;
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
		/*if (_children.isEmpty()) return "eps";
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("[");
		
		for (Node child : _children) {
			if (sb.length() > 0) sb.append(";");
			
			sb.append(child.toString());
		}
		
		sb.append("]");
		
		return sb.toString();*/
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