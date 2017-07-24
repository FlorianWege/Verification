package core;

import java.util.Collection;
import java.util.Iterator;

import core.structures.LexerRule;
import core.structures.ParserRule;
import core.structures.ParserRulePattern;
import core.structures.ParserRulePatternAnd;
import util.StringUtil;

/**
 * using a grammar, takes the parser rules and converts a given String into a tree consisting of parser rules
 */
public class Parser {
	private Grammar _grammar;
	
	private Collection<Token> _tokens;
	private Iterator<Token> _tokensItr;
	
	private PredictiveParserTable _ruleMap;
	
	public static class ParserException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParserException(String msg) {
			super(msg);
		}
	}
	
	public static class NoRuleException extends ParserException {
		private static final long serialVersionUID = 1L;

		public NoRuleException(String msg) {
			super(msg);
		}
	}
	
	private ParserRulePattern selectRulePattern(ParserRule nonTerminal, Token terminal) throws ParserException {
		try {
			ParserRulePattern rulePattern = _ruleMap.get(nonTerminal, (terminal != null) ? terminal.getRule() : LexerRule.EPSILON);
			
			if (rulePattern == null) {
				rulePattern = _ruleMap.get(nonTerminal, LexerRule.EPSILON);
				
				if (rulePattern == null) throw new Exception();
			}
			
			return rulePattern;
		} catch (Exception e) {
			if (terminal == null) throw new ParserException("no more tokens but expected " + nonTerminal);
			
			throw new NoRuleException(String.format("line %d.%d: unexpected '%s' (%s) (no rule in %s) ", terminal.getLine() + 1, terminal.getLineOffset() + 1, terminal.getText(), terminal.getRule().toString(), nonTerminal));
		}
	}
	
	private Token _token;
	
	private SyntaxTreeNode tree(ParserRule rule, int nestDepth) throws ParserException {
		Token token = _token;
		
		//System.err.println(StringUtil.repeat("\t", nestDepth) + "tree " + rule + " token is " + token);
		
		ParserRulePattern nextRulePattern = selectRulePattern(rule, token);
		
		SyntaxTreeNode thisNode = new SyntaxTreeNode(rule, nextRulePattern);
		
		//System.err.println("select " + nextRulePattern);
		
		//System.err.println(new String(new char[nestDepth]).replace('\0', '\t') + "select " + nextRulePattern);
		
		if (nextRulePattern instanceof ParserRulePatternAnd) {
			ParserRulePatternAnd andPattern = (ParserRulePatternAnd) nextRulePattern;
			
			/*for (ParserRulePattern childPattern : andPattern.getChildren()) {
				Rule childRule = childPattern.getRule();
				
				System.err.println(StringUtil.repeat("\t", nestDepth) + "prechild " + childRule);				
			}*/
			
			for (ParserRulePattern childPattern : andPattern.getChildren()) {
				Rule childRule = childPattern.getRule();
				
				//System.err.println(StringUtil.repeat("\t", nestDepth) + "child " + childRule + ";" + childRule.getClass());
				
				if (childRule instanceof ParserRule) {
					thisNode.addChild(tree((ParserRule) childRule, nestDepth + 1));
				} else {
					if (childRule.equals(LexerRule.EPSILON)) {
						thisNode.addChild(new SyntaxTreeNodeTerminal(null));
						
						continue;
					}
					
					if (!_token.getRule().equals(childRule)) {
						throw new ParserException(String.format("line %d.%d: wrong token %s expected %s (in rule %s)", _token.getLine(), _token.getLineOffset(), _token, childRule, rule));
					}
					
					if (_token == null) throw new ParserException("no more tokens while expecting " + ((LexerRule) childRule).getRulePatterns() + " (rule=" + rule + ")");
					
					thisNode.addChild(new SyntaxTreeNodeTerminal(_token));
					
					//System.err.println(StringUtil.repeat("\t", nestDepth) + "add token " + _token);
					Token childToken = _tokensItr.hasNext() ? _tokensItr.next() : null;
					
					_token = childToken;
					//System.err.println(StringUtil.repeat("\t", nestDepth) + "new token " + childToken);
				}
			}
		}
		
		return thisNode;
	}
	
	public SyntaxTree parse(Collection<Token> tokens) throws ParserException {
		System.out.println("parsing...");
		_tokens = tokens;
		
		if (_tokens.isEmpty()) throw new ParserException("no tokens");
		
		_tokensItr = _tokens.iterator();
		
		_token = _tokensItr.next();

		SyntaxTreeNode root = tree(_grammar.getStartParserRule(), 0);
		
		if (_token != null) throw new ParserException("superfluous input " + _token);
		
		return new SyntaxTree(_grammar, root);
	}
	
	public Parser(Grammar grammar) {
		_grammar = grammar;
		
		_ruleMap = _grammar.getPredictiveParserTable();
	}
}