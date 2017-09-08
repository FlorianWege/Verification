package core;

public class SyntaxTree {
	private Grammar _grammar;
	
	public Grammar getGrammar() {
		return _grammar;
	}
	
	private SyntaxNode _rootNode;
	
	public SyntaxNode getRoot() {
		return _rootNode;
	}
	
	SyntaxTree(Grammar grammar, SyntaxNode rootNode) {
		_grammar = grammar;
		_rootNode = rootNode;
	}
}