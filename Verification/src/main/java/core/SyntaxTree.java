package core;

public class SyntaxTree {
	private Grammar _grammar;
	
	public Grammar getGrammar() {
		return _grammar;
	}
	
	private SyntaxTreeNode _rootNode;
	
	public SyntaxTreeNode getRoot() {
		return _rootNode;
	}
	
	public SyntaxTree(Grammar grammar, SyntaxTreeNode rootNode) {
		_grammar = grammar;
		_rootNode = rootNode;
	}
}