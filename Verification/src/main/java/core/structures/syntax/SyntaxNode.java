package core.structures.syntax;

import com.google.common.collect.Lists;
import core.Symbol;
import core.SymbolKey;
import core.Token;
import core.structures.ParserRule;
import core.structures.TNode;
import core.structures.Terminal;
import util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class SyntaxNode extends TNode<SyntaxNode> implements Serializable {
	private final List<SyntaxNode> _children = new ArrayList<>();
	
	@Nonnull
    @Override
	public List<SyntaxNode> getChildren() {
		return _children;
	}

	@Nonnull
	@Override
	public String getTreeText() {
		return this instanceof SyntaxNodeTerminal ? this.toString() : _symbol.toString();
	}

	public SyntaxNode findChildRec(@Nonnull SymbolKey symbolKey, int index, boolean breath) {
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

	public SyntaxNode findChildRec(@Nonnull Symbol symbol, int index, boolean breath) {
		return findChildRec(symbol.getKey(), index, breath);
	}
	
	public SyntaxNode findChildRec(@Nonnull SymbolKey symbolKey, boolean breath) {
		return findChildRec(symbolKey, 1, breath);
	}
	
	public SyntaxNode findChildRec(@Nonnull Symbol symbol, boolean breath) {
		return findChildRec(symbol, 0, breath);
	}

	public SyntaxNode findChildRec(@Nonnull Symbol symbol) {
		return findChildRec(symbol, 0, true);
	}

	public SyntaxNode findChild(@Nonnull Symbol symbol, int count) {
		for (SyntaxNode child : getChildren()) {
			if (child.getSymbol().equals(symbol)) {
				count--;
				if (count < 0) return child;
			}
		}

		return null;
	}

	public SyntaxNode findChild(@Nonnull Symbol symbol) {
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
		if (_symbol == null) return null;

		return _symbol.toString();
	}

	public String toStringVert() {
		if (_symbol == null) return null;

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

	public String synthesize(boolean includeWrappingSeps, boolean flatten, @Nullable Function<Token, String> tokenMapper) {
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

				lastX = tokenX + ((token.getText() != null) ? token.getText().length() : 0);
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

	public void replace(@Nonnull Terminal terminal, @Nonnull String text) {
		for (SyntaxNode child : _children) {
			child.replace(terminal, text);
		}
	}

	public void replace(@Nonnull Terminal terminal, @Nonnull String var, @Nonnull SyntaxNode exp) {
		for (SyntaxNode child : _children) {
			child.replace(terminal, var, exp);
		}
		
		_children.replaceAll(new UnaryOperator<SyntaxNode>() {
			@Override
			public SyntaxNode apply(SyntaxNode child) {
				if (child instanceof SyntaxNodeTerminal) {
					Token token = ((SyntaxNodeTerminal) child).getToken();

					if ((token != null) && token.getTerminal().equals(terminal) && token.getText() != null && token.getText().equals(var)) {
						return exp.copy();
					}
				}

				return child;
			}
		});
	}
	
	public void addChild(@Nonnull SyntaxNode child) {
		_children.add(child);
	}
	
	public SyntaxNode(@Nullable Symbol symbol, @Nullable ParserRule subRule) {
		_symbol = symbol;
		_subRule = subRule;
	}
}