package core;

import java.util.*;
import java.util.function.UnaryOperator;

import com.google.common.collect.Lists;
import com.sun.istack.internal.NotNull;
import core.structures.Terminal;
import core.structures.ParserRule;

import javax.annotation.Nullable;

public class SyntaxNode {
	private Vector<SyntaxNode> _children = new Vector<>();
	
	public Vector<SyntaxNode> getChildren() {
		return _children;
	}
	
	public SyntaxNode findChild(SymbolKey symbolKey, int index, boolean breath) {
		if (index < 0) return null;

		if (breath) {
			Queue<SyntaxNode> queue = new LinkedList<>();

			queue.add(this);

			while (!queue.isEmpty()) {
				SyntaxNode node = queue.poll();

				if (!node.equals(this) && node.getSymbol().getKey().equals(symbolKey)) {
					index--; if (index < 0) return node;
				}

				queue.addAll(node.getChildren());
			}
		} else {
			Stack<SyntaxNode> stack = new Stack<>();

			stack.push(this);

			while (!stack.empty()) {
				SyntaxNode node = stack.pop();

				if (!node.equals(this) && node.getSymbol().equals(symbolKey)) {
					index--; if (index < 0) return node;
				}

				for (SyntaxNode child : Lists.reverse(node.getChildren())) {
					stack.push(child);
				}
			}
		}

		return null;
	}

	public SyntaxNode findChild(Symbol symbol, int index, boolean breath) {
		return findChild(symbol.getKey(), index, breath);
	}
	
	public SyntaxNode findChild(SymbolKey symbolKey, boolean breath) {
		return findChild(symbolKey, 1, breath);
	}
	
	public SyntaxNode findChild(Symbol symbol, boolean breath) {
		return findChild(symbol, 0, breath);
	}
	
	private Symbol _symbol;
	
	public Symbol getSymbol() {
		return _symbol;
	}
	
	private ParserRule _subRule;
	
	public ParserRule getSubRule() {
		return _subRule;
	}
	
	@Override
	public String toString() {
		return _symbol.toString();
	}

	public String toStringVert() {
		return _symbol.toString();
	}
	
	public String synthesize() {
		if (this instanceof SyntaxNodeTerminal) {
			Token token = ((SyntaxNodeTerminal) this).getToken();
			
			if (token == null) return "";
			
			return token.getText();
		}
		
		StringBuilder sb = new StringBuilder();
		
		for (SyntaxNode child : getChildren()) {
			String childS = child.synthesize();
			
			if (sb.length() != 0 && !childS.isEmpty()) sb.append(" ");
			
			boolean addParentheses = false;
			
			if (child.getSymbol().equals("exp")) {
				
				SyntaxNode erest_child = child.getChildren().get(1);
				
				if (erest_child.getChildren().size() > 1) {
					SyntaxNode opChild = erest_child.getChildren().get(0);

					if (opChild.getSymbol().equals("opMinus") || opChild.getSymbol().equals("opDiv")) {
						addParentheses = true;
					}
				}
			}
			
			if (addParentheses) sb.append("(");
			
			sb.append(childS);
			
			if (addParentheses) sb.append(")");
		}
		
		return sb.toString();
	}
	
	public List<Token> tokenize() {
		List<Token> ret = new ArrayList<>();
		
		for (SyntaxNode child : getChildren()) {
			ret.addAll(child.tokenize());
		}
		
		return ret;
	}
	
	public SyntaxNode copy() {
		SyntaxNode ret = new SyntaxNode(_symbol, _subRule);
		
		for (SyntaxNode child : _children) {
			ret.addChild(child.copy());
		}
		
		return ret;
	}
	
	public void replace(Terminal terminal, String var, SyntaxNode exp) {
		for (SyntaxNode child : _children) {
			child.replace(terminal, var, exp);
		}
		
		_children.replaceAll(new UnaryOperator<SyntaxNode>() {
			@Override
			public SyntaxNode apply(SyntaxNode child) {
				if (child instanceof SyntaxNodeTerminal) {
					Token token = ((SyntaxNodeTerminal) child).getToken();

					if ((token != null) && token.getTerminal().equals(terminal) && token.getText().equals(var)) {
						return exp.copy();
					}
				}

				return child;
			}
		});
	}
	
	public void print(int nestDepth) {
		System.out.println(new String(new char[nestDepth]).replace('\0', '\t') + _symbol);
		
		if (_children.isEmpty()) {
			System.out.println(new String(new char[nestDepth + 1]).replace('\0', '\t') + Terminal.EPSILON);
		} else {
			for (SyntaxNode child : _children) {
				child.print(nestDepth + 1);
			}
		}
	}
	
	void addChild(SyntaxNode child) {
		_children.add(child);
	}
	
	public SyntaxNode(Symbol symbol, ParserRule subRule) {
		_symbol = symbol;
		_subRule = subRule;
	}
}