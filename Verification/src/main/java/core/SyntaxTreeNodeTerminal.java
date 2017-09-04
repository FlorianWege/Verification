package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import core.structures.Terminal;

public class SyntaxTreeNodeTerminal extends SyntaxTreeNode {
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
	
	public List<Token> tokenize() {
		return (_token != null) ? Arrays.asList(_token) : new ArrayList<>();
	}
	
	@Override
	public SyntaxTreeNode copy() {
		return new SyntaxTreeNodeTerminal((_token == null) ? null : _token.copy());
	}
	
	public SyntaxTreeNodeTerminal(Terminal terminal) {
		super(terminal, null);
	}
	
	public SyntaxTreeNodeTerminal(Token token) {
		this((token == null) ? Terminal.EPSILON : token.getTerminal());

		_token = token;
	}
}