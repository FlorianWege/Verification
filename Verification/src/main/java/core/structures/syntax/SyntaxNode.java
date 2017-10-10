package core.structures.syntax;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.google.common.collect.Lists;
import core.Symbol;
import core.SymbolKey;
import core.Token;
import core.structures.TNode;
import core.structures.Terminal;
import core.structures.ParserRule;
import util.StringUtil;

public class SyntaxNode extends TNode<SyntaxNode> implements Serializable {
	private final List<SyntaxNode> _children = new ArrayList<>();
	
	@Override
	public List<SyntaxNode> getChildren() {
		return _children;
	}

	@Override
	public String getTreeText() {
		return this instanceof SyntaxNodeTerminal ? this.toString() : _symbol.toString();
	}

	public SyntaxNode findChildRec(SymbolKey symbolKey, int index, boolean breath) {
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

	public SyntaxNode findChildRec(Symbol symbol, int index, boolean breath) {
		return findChildRec(symbol.getKey(), index, breath);
	}
	
	public SyntaxNode findChildRec(SymbolKey symbolKey, boolean breath) {
		return findChildRec(symbolKey, 1, breath);
	}
	
	public SyntaxNode findChildRec(Symbol symbol, boolean breath) {
		return findChildRec(symbol, 0, breath);
	}

	public SyntaxNode findChildRec(Symbol symbol) {
		return findChildRec(symbol, 0, true);
	}

	public SyntaxNode findChild(Symbol symbol, int count) {
		for (SyntaxNode child : getChildren()) {
			if (child.getSymbol().equals(symbol)) {
				count--;
				if (count < 0) return child;
			}
		}

		return null;
	}

	public SyntaxNode findChild(Symbol symbol) {
		return findChild(symbol, 0);
	}

	private final Symbol _symbol;
	
	public Symbol getSymbol() {
		return _symbol;
	}
	
	private final ParserRule _subRule;
	
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
	
	public List<Token> tokenize(boolean includeWrappingSeps) {
		List<Token> ret = new ArrayList<>();
		
		for (SyntaxNode child : getChildren()) {
			ret.addAll(child.tokenize());
		}

		if (!includeWrappingSeps) {
			while (!ret.isEmpty() && ret.get(0).getTerminal().isSep()) ret.remove(0);
			while (!ret.isEmpty() && ret.get(ret.size() - 1).getTerminal().isSep()) ret.remove(ret.size() - 1);
		}

		return ret;
	}

	public List<Token> tokenize() {
		return tokenize(true);
	}

	public String synthesize(boolean includeWrappingSeps, boolean flatten, Function<Token, String> tokenMapper) {
		List<Token> tokens = tokenize(includeWrappingSeps);

		if (!tokens.isEmpty()) {
			int startX = tokens.get(0).getLineOffset();
			int startY = tokens.get(0).getLine();

			int lastX = startX;
			int lastY = startY;

			StringBuilder sb = new StringBuilder();

			for (Token token : tokens) {
				int tokenY = token.getLine();

				if (tokenY > lastY) {
					sb.append(StringUtil.repeat(StringUtil.line_sep, tokenY - lastY));
					lastX = startX;
				}

				int tokenX = token.getLineOffset();

				sb.append(StringUtil.repeat(" ", tokenX - lastX));

				sb.append((tokenMapper != null) ? tokenMapper.apply(token) : token.getText());

				lastX = tokenX + token.getText().length();
				lastY = tokenY;
			}

			String ret = sb.toString();

			if (flatten) ret = ret.replaceAll(StringUtil.line_sep, " ");

			return ret;
		}

		return "";
	}

	public String synthesize() {
		return synthesize(true, false, null);
	}

	public SyntaxNode copy() {
		SyntaxNode ret = new SyntaxNode(_symbol, _subRule);
		
		for (SyntaxNode child : _children) {
			ret.addChild(child.copy());
		}
		
		return ret;
	}

	public void replace(Terminal terminal, String text) {
		for (SyntaxNode child : _children) {
			child.replace(terminal, text);
		}
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
	
	public void addChild(SyntaxNode child) {
		_children.add(child);
	}
	
	public SyntaxNode(Symbol symbol, ParserRule subRule) {
		_symbol = symbol;
		_subRule = subRule;
	}
}