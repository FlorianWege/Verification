package core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * using a grammar, takes the parser rules and converts a given String into a tree consisting of parser rules
 */
public class Parser {
	private Grammar _grammar;
	
	private Collection<Token> _tokens;
	private Iterator<Token> _tokensItr;
	
	private PredictiveParserTable _ruleMap;
	
	public static class NoRuleException extends Exception {
		public NoRuleException(String msg) {
			super(msg);
		}
	}
	
	private ParserRulePattern selectRulePattern(ParserRule nonTerminal, Token terminal) throws NoRuleException {
		try {
			ParserRulePattern rulePattern = _ruleMap.get(nonTerminal, (terminal != null) ? terminal.getRule() : LexerRule.EPSILON);
			
			//System.err.println("get " + nonterminal + ";" + terminal + "->" + rulePattern);
			
			if (rulePattern == null) {
				rulePattern = _ruleMap.get(nonTerminal, LexerRule.EPSILON);
				
				if (rulePattern == null) throw new Exception();
			}
			
			return rulePattern;
		} catch (Exception e) {
			if (terminal == null) throw new RuntimeException("no more tokens but expected " + nonTerminal);

			throw new NoRuleException(String.format("line %d.%d: unexpected '%s' (%s) (no rule in %s) ", terminal.getLine() + 1, terminal.getLineOffset() + 1, terminal.getText(), terminal.getRule().toString(), nonTerminal));
		}
	}
	
	private Token _token;
	
	private class Node {
		private Vector<Node> _children = new Vector<>();
		private Rule _rule;
		
		@Override
		public String toString() {
			if (_children.isEmpty()) return "eps";
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("[");
			
			for (Node child : _children) {
				if (sb.length() > 0) sb.append(";");
				
				sb.append(child.toString());
			}
			
			sb.append("]");
			
			return sb.toString();
		}
		
		public void print(int nestDepth) {
			System.out.println(new String(new char[nestDepth]).replace('\0', '\t') + _rule);
			if (_children.isEmpty()) {
				System.out.println(new String(new char[nestDepth + 1]).replace('\0', '\t') + "eps");
			} else {
				for (Node child : _children) {
					child.print(nestDepth + 1);
				}
			}
		}
		
		public void addChild(Node child) {
			_children.add(child);
		}
		
		public Node(Rule rule) {
			_rule = rule;
		}
	}
	
	private class TerminalNode extends Node {
		private Token _token;
		
		@Override
		public String toString() {
			return (_token != null) ? _token.toString() : "eps";
		}
		
		@Override
		public void print(int nestDepth) {
			System.out.println(new String(new char[nestDepth]).replace('\0', '\t') + toString());
		}
		
		public TerminalNode(Token token) {
			super(null);
			_token = token;
		}
	}
	
	private Node tree(ParserRule rule, int nestDepth) throws NoRuleException {
		Node thisNode = new Node(rule);
		
		//Token token = _tokensItr.next();
		
		Token token = _token;
		
		System.err.println(new String(new char[nestDepth]).replace('\0', '\t') + "tree " + rule + " token is " + token);
		
		ParserRulePattern nextRulePattern = selectRulePattern(rule, token);
		
		//System.err.println(new String(new char[nestDepth]).replace('\0', '\t') + "select " + nextRulePattern);
		
		//Token token2 = _tokensItr.next();
		
		if (nextRulePattern instanceof ParserRulePatternAnd) {
			ParserRulePatternAnd andPattern = (ParserRulePatternAnd) nextRulePattern;
			
			for (ParserRulePattern childPattern : andPattern.getChildren()) {
				Rule childRule = childPattern.getRule();
				
				System.err.println(new String(new char[nestDepth]).replace('\0', '\t') + "prechild " + childRule);				
			}
			
			for (ParserRulePattern childPattern : andPattern.getChildren()) {
				Rule childRule = childPattern.getRule();
				
				System.err.println(new String(new char[nestDepth]).replace('\0', '\t') + "child " + childRule + ";" + childRule.getClass());
				
				if (childRule instanceof ParserRule) {
					thisNode.addChild(tree((ParserRule) childRule, nestDepth + 1));
				} else {
					if (_token.getRule() != childRule) throw new RuntimeException("wrong token " + _token + " expected " + childRule);
					
					if (_token == null) throw new RuntimeException("no more tokens while expecting " + ((LexerRule) childRule).getRulePatterns() + " (rule=" + rule + ")");
					
					thisNode.addChild(new TerminalNode(_token));
					
					System.err.println(new String(new char[nestDepth]).replace('\0', '\t') + "add token " + _token);
					Token childToken = _tokensItr.hasNext() ? _tokensItr.next() : null;
					
					_token = childToken;
					System.err.println(new String(new char[nestDepth]).replace('\0', '\t') + "new token " + childToken);
				}
			}
		}
		if (nextRulePattern instanceof ParserRulePatternAnd) {
		}
		
		//ParserRulePattern nextRulePattern = selectRulePattern(nextRulePattern, token2);
		
		/*nextRule.exec();

		tree(newNT);*/
		
		return thisNode;
	}
	
	public void parse(Collection<Token> tokens, ParserRule firstRule) throws NoRuleException {
		System.out.println("parsing...");
		_tokens = tokens;
		
		_tokensItr = _tokens.iterator();
		
		_token = _tokensItr.next();

		Node root = tree(firstRule, 0);
		
		if (_tokensItr.hasNext()) System.err.println("superfluous input " + _tokensItr.next());
		
		root.print(0);
	}
	
	public Parser(Grammar grammar, PredictiveParserTable ruleMap) {
		_grammar = grammar;
		_ruleMap = ruleMap;
	}
}