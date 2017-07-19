package core;

import core.structures.LexerRule;

public class SyntaxTreeNodeTerminal extends SyntaxTreeNode {
	private Token _token;
	
	public Token getToken() {
		return _token;
	}
	
	@Override
	public Rule getRule() {
		return (_token == null) ? LexerRule.EPSILON : _token.getRule();
	}
	
	@Override
	public String toString() {
		return (_token != null) ? _token.toString() : getRule().toString();
	}
	
	@Override
	public String toStringVert() {
		return (_token != null) ? _token.toStringVert() : getRule().toString();
	}
	
	@Override
	public void print(int nestDepth) {
		System.out.println(new String(new char[nestDepth]).replace('\0', '\t') + toString());
	}
	
	@Override
	public SyntaxTreeNode copy() {
		return new SyntaxTreeNodeTerminal((_token == null) ? null : _token.copy());
	}
	
	public SyntaxTreeNodeTerminal(Token token) {
		super(null, null);

		_token = token;
	}
}