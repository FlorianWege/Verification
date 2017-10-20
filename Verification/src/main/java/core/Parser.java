package core;

import core.structures.NonTerminal;
import core.structures.ParserRule;
import core.structures.Terminal;
import core.structures.syntax.SyntaxNode;
import core.structures.syntax.SyntaxNodeTerminal;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

/**
 * using a grammar, takes the parser rules and converts a given String into a tree consisting of parser rules
 */
public class Parser {
	private final Grammar _grammar;

	public @Nonnull Grammar getGrammar() {
		return _grammar;
	}

	private List<Token> _tokens;
	private Iterator<Token> _tokensItr;
	private Token _token;
	
	private ParserTable _ruleMap;
	
	public static class ParserException extends Exception {
		private final Token _token;
		
		public Token getToken() {
			return _token;
		}
		
		@Override
		public String getMessage() {
			return "parserException: " + _token;
			
		}
		
		public ParserException(Token token) {
			_token = token;
		}
	}
	
	public static class NoRuleException extends ParserException {
		private final NonTerminal _rule;
		
		@Override
		public String getMessage() {			
			return String.format("line %d.%d: unexpected '%s' (%s) (no rule in %s) ", getToken().getLine() + 1, getToken().getLineOffset() + 1, getToken().getText(), getToken().getTerminal().toString(), _rule);
		}
		
		public NoRuleException(Token token, NonTerminal rule) {
			super(token);
			
			_rule = rule;
		}
	}
	
	public static class NoMoreTokensException extends ParserException {
		private final NonTerminal _nonTerminal;
		private final Terminal _terminal;
		
		@Override
		public String getMessage() {
			if (_terminal != null) {
				return "no more tokens while expecting " + (_terminal).getRules() + " (nonTerminal=" + _nonTerminal + ")";
			}
			
			return "no more tokens but expected " + _nonTerminal;
		}
		
		public NoMoreTokensException(NonTerminal nonTerminal, Terminal terminal) {
			super(null);

			_nonTerminal = nonTerminal;
			_terminal = terminal;
		}

		public NoMoreTokensException(NonTerminal nonTerminal) {
			this(nonTerminal, null);
		}
	}
	
	public static class SuperfluousTokenException extends ParserException {
		@Override
		public String getMessage() {
			return "superfluous input " + getToken();
		}
		
		public SuperfluousTokenException(Token token) {
			super(token);
		}
	}
	
	public static class WrongTokenException extends ParserException {
		private final NonTerminal _rule;
		private final Symbol _childRule;
		
		@Override
		public String getMessage() {
			return String.format("line %d.%d: wrong token %s expected %s (in rule %s)", getToken().getLine(), getToken().getLineOffset(), getToken(), _childRule, _rule);
		}
		
		public WrongTokenException(Token token, NonTerminal rule, Symbol childRule) {
			super(token);

			_rule = rule;
			_childRule = childRule;
		}
	}
	
	private ParserRule selectRule(NonTerminal nonTerminal, Token token) throws ParserException {
		ParserRule rule = _ruleMap.get(nonTerminal, token.getTerminal());

		if (rule == null) throw new NoRuleException(token, nonTerminal);
		
		return rule;
	}
	
	private SyntaxNode getNode(NonTerminal nonTerminal) throws ParserException {
		ParserRule nextRule = selectRule(nonTerminal, _token);

		SyntaxNode node = new SyntaxNode(nonTerminal, nextRule);

		for (Symbol symbol : nextRule.getSymbols()) {
			SyntaxNode child;
			
			if (symbol instanceof NonTerminal) {
				child = getNode((NonTerminal) symbol);
			} else if (symbol.equals(Terminal.EPSILON)) {
				child = new SyntaxNodeTerminal(Terminal.EPSILON);
			} else {
				if (_token == null) throw new NoMoreTokensException(nonTerminal, (Terminal) symbol);
				if (!_token.getTerminal().equals(symbol)) throw new WrongTokenException(_token, nonTerminal, symbol);
				
				child = new SyntaxNodeTerminal(_token);
				
				_token = _tokensItr.hasNext() ? _tokensItr.next() : null;
			}
			
			node.addChild(child);
		}
		
		return node;
	}
	
	public SyntaxNode parse(List<Token> tokens) throws ParserException {
		_tokens = tokens;
		
		if (_tokens.isEmpty()) throw new NoMoreTokensException(_grammar.getStartSymbol());
		
		_tokens.add(Token.createTerminator(tokens));
		
		_tokensItr = _tokens.iterator();
		
		_token = _tokensItr.next();

		NonTerminal selectedStartSymbol = _grammar.getStartSymbol();

		for (NonTerminal startSymbol : _grammar.getStartSymbols()) {
			try {
				selectRule(startSymbol, _token);

				selectedStartSymbol = startSymbol;
			} catch (ParserException ignored) {
			}
		}

		SyntaxNode root = getNode(selectedStartSymbol);
		
		if (!_token.getTerminal().equals(Terminal.TERMINATOR)) throw new SuperfluousTokenException(_token);

		return root;
	}

	public SyntaxNode parse(String input) throws Lexer.LexerException, ParserException {
		Lexer lexer = new Lexer(_grammar);

		return parse(lexer.tokenize(input).getTokens());
	}

	public Parser(@Nonnull Grammar grammar) {
		_grammar = grammar;
		
		_ruleMap = _grammar.getParserTable();
	}
}