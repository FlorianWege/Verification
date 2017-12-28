package core.structures.syntax;

import core.Symbol;
import core.Token;
import core.structures.Terminal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SyntaxNodeTerminal extends SyntaxNode {
	private final Token _token;
	
	public Token getToken() {
		return _token;
	}
	
	@Override
	public Symbol getSymbol() {
		return (_token == null) ? Terminal.EPSILON : _token.getTerminal();
	}
	
	@Override
	public String toString() {
		return (_token != null) ? _token.toString() : getSymbol().toString();
	}
	
	@Override
	public String toStringVert() {
		return (_token != null) ? _token.toStringVert() : getSymbol().toString();
	}
	
	@Override
	public List<Token> tokenize(boolean includeWrappingSeps) {
		return (_token != null && (includeWrappingSeps || !_token.getTerminal().isSep())) ? Collections.singletonList(_token) : new ArrayList<>();
	}

	@Override
	public void replace(@Nonnull Terminal terminal, @Nonnull String text) {
		if (_token != null && _token.getTerminal().equals(terminal)) _token.replaceText(text);
	}

	@Override
	public SyntaxNode copy() {
		return new SyntaxNodeTerminal((_token == null) ? null : _token.copy());
	}

	public SyntaxNodeTerminal(@Nonnull Terminal terminal) {
		super(terminal, null);

		_token = null;
	}

	public SyntaxNodeTerminal(@Nullable Token token) {
		super((token != null) ? token.getTerminal() : null, null);

		_token = token;
	}
}