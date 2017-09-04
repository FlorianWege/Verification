package core;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.UnaryOperator;

import javax.swing.plaf.synth.SynthSpinnerUI;

import core.structures.Terminal;
import core.structures.ParserRule;
import grammars.ExpGrammar;

public class SyntaxTreeNode {
	private Vector<SyntaxTreeNode> _children = new Vector<>();
	
	public Vector<SyntaxTreeNode> getChildren() {
		return _children;
	}
	
	public SyntaxTreeNode findChild(SymbolKey symbolKey, int index) {
		int c = 0;

		for (SyntaxTreeNode child : _children) {
			Symbol childSymbol = child.getSymbol();

			if (childSymbol == null) continue;
			
			SyntaxTreeNode found = null;
			
			if (childSymbol.getKey().equals(symbolKey)) {
				found = child;
			} else {
				found = child.findChild(symbolKey, 1);
			}
			
			if (found != null) {
				c++;
				
				if (c >= index) return found;
			}
		}
		
		return null;
	}

	public SyntaxTreeNode findChild(Symbol symbol, int index) {
		return findChild(symbol.getKey(), index);
	}
	
	public SyntaxTreeNode findChild(SymbolKey symbolKey) {
		return findChild(symbolKey, 1);
	}
	
	public SyntaxTreeNode findChild(Symbol symbol) {
		return findChild(symbol, 1);
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
		if (this instanceof SyntaxTreeNodeTerminal) {
			Token token = ((SyntaxTreeNodeTerminal) this).getToken();
			
			if (token == null) return "";
			
			return token.getText();
		}
		
		StringBuilder sb = new StringBuilder();
		
		for (SyntaxTreeNode child : getChildren()) {
			String childS = child.synthesize();
			
			if (sb.length() != 0 && !childS.isEmpty()) sb.append(" ");
			
			boolean addParentheses = false;
			
			if (child.getSymbol().equals("exp")) {
				
				SyntaxTreeNode erest_child = child.getChildren().get(1);
				
				if (erest_child.getChildren().size() > 1) {
					SyntaxTreeNode opChild = erest_child.getChildren().get(0);

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
		
		for (SyntaxTreeNode child : getChildren()) {
			ret.addAll(child.tokenize());
		}
		
		return ret;
	}
	
	public SyntaxTreeNode copy() {
		SyntaxTreeNode ret = new SyntaxTreeNode(_symbol, _subRule);
		
		for (SyntaxTreeNode child : _children) {
			ret.addChild(child.copy());
		}
		
		return ret;
	}
	
	public void replace(Terminal terminal, String var, SyntaxTreeNode exp) {
		for (SyntaxTreeNode child : _children) {
			child.replace(terminal, var, exp);
		}
		
		_children.replaceAll(new UnaryOperator<SyntaxTreeNode>() {
			@Override
			public SyntaxTreeNode apply(SyntaxTreeNode child) {
				if (child instanceof SyntaxTreeNodeTerminal) {
					Token token = ((SyntaxTreeNodeTerminal) child).getToken();

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
			for (SyntaxTreeNode child : _children) {
				child.print(nestDepth + 1);
			}
		}
	}
	
	public void addChild(SyntaxTreeNode child) {
		_children.add(child);
	}
	
	public SyntaxTreeNode(Symbol symbol, ParserRule subRule) {
		_symbol = symbol;
		_subRule = subRule;
	}
}