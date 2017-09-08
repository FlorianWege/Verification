package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sun.istack.internal.NotNull;
import core.structures.Terminal;

public class SyntaxNodeTerminal extends SyntaxNode {
	private Token _token;
	
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
	public void print(int nestDepth) {
		System.out.println(new String(new char[nestDepth]).replace('\0', '\t') + toString());
	}
	
	@Override
	public List<Token> tokenize() {
		return (_token != null) ? Collections.singletonList(_token) : new ArrayList<>();
	}
	
	@Override
	public SyntaxNode copy() {
		return new SyntaxNodeTerminal((_token == null) ? null : _token.copy());
	}
	
	SyntaxNodeTerminal(Terminal terminal) {
		super(terminal, null);
	}
	
	SyntaxNodeTerminal(Token token) {
		this((token != null) ? token.getTerminal() : null);

		_token = token;
	}
}