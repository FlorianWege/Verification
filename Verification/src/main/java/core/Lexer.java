package core;

import java.util.Collection;
import java.util.Comparator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.structures.Terminal;
import core.structures.LexerRule;

/**
 * using a specified grammar, takes the lexer rules and converts a String into a list of tokens
 */
public class Lexer {
	private Grammar _grammar;
	
	public Lexer(Grammar grammar) {
		_grammar = grammar;
	}
	
	private Token createToken(Terminal terminal, LexerRule rule, String text, int line, int lineOffset, int pos) {
		Token token = new Token(terminal, rule, text, line, lineOffset, pos);
		
		return token;
	}
	
	public class LexerException extends Exception {
		private static final long serialVersionUID = 1L;
		
		private int _y;
		
		public int getLine() {
			return _y;
		}
		
		private int _x;
		
		public int getLineOffset() {
			return _x;
		}
		
		private int _curPos;
		
		public int getCurPos() {
			return _curPos;
		}
		
		private String _inputString;
		
		public String getInputString() {
			return _inputString;
		}
		
		@Override
		public String getMessage() {
			return String.format("cannot find token at pos %d.%d (%d): >>%s<<", _y + 1, _x + 1, _curPos, _inputString.substring(_curPos));
		}
		
		public LexerException(int y, int x, int curPos, String inputString) {
			super();
			
			_y = y;
			_x = x;
			_curPos = curPos;
			_inputString = inputString;
		}
	}
	
	private String removeComments(String s) {
		StringBuilder sb = new StringBuilder();
		
		for (String line : s.split("[\\r\\n]+")) {
			line = line.replaceAll("\\p{Cntrl}", "");
			
			line = line.replaceAll("//.*$", "");
			
			if (sb.length() > 0) sb.append(System.lineSeparator());
			
			sb.append(line);
		}
		
		return sb.toString();
	}
	
	public static class LexerResult {
		private Vector<Token> _tokens;
		
		public Vector<Token> getTokens() {
			return _tokens;
		}
		
		public void print() {
			System.out.println("tokens:");
			
			for (int i = 0; i < getTokens().size(); i++) {
				System.out.println("#" + i + ": " + getTokens().get(i).getTerminal().getKey() + "->" + getTokens().get(i)._text);
			}
		}
		
		public LexerResult(Vector<Token> tokens) {
			_tokens = new Vector<>(tokens);
		}
	}
	
	public LexerResult tokenize(String s) throws LexerException {
		s = removeComments(s);
		
		Vector<Terminal> terminals = new Vector<>(_grammar.getTerminals());
		
		terminals.sort(new Comparator<Terminal>() {
			private boolean isRegEx(Terminal terminal) {
				for (LexerRule rule : terminal.getRules()) if (rule.isRegEx()) return true;
				
				return false;
			}
			
			@Override
			public int compare(Terminal terminalA, Terminal terminalB) {
				if (terminalA.getRules().isEmpty() || terminalB.getRules().isEmpty()) return 0;
				
				if (isRegEx(terminalA)) return 1;
				if (isRegEx(terminalB)) return -1;
				
				return 0;
			}
		});
		
		Terminal wsRule = new Terminal(new SymbolKey("WS"), true);
		
		wsRule.addRule(new LexerRule("\\s+", true));
		
		terminals.add(wsRule);
		
		int curPos = 0; Vector<Token> tokens = new Vector<>(); int x = 0; int y = 0;
		
		while (curPos < s.length()) {
			if ((s.length() - curPos >= System.lineSeparator().length()) && s.substring(curPos, curPos + System.lineSeparator().length()).equals(System.lineSeparator())) {
				curPos += System.lineSeparator().length(); x = 0; y++;
				
				continue;
			}
			
			int curLen = 0;	LexerRule curRule = null; Terminal curTerminal = null;
			
			for (int i = 0; i < terminals.size(); i++) {
				Terminal terminal = terminals.get(i);
				
				for (LexerRule rule : terminal.getRules()) {
					String ruleS = (curPos > 0) ? String.format("^.{%d}(%s)", curPos, rule.getRegEx()) : String.format("^(%s)", rule.getRegEx());

					Pattern adjustedPattern = Pattern.compile(ruleS, Pattern.DOTALL);
					
					Matcher matcher = adjustedPattern.matcher(s);
					
					if (matcher.find() && (matcher.start(1) == curPos)) {
						int newLen = (matcher.end(1) - 1) - matcher.start(1) + 1;

						if (newLen > curLen) {
							curTerminal = terminal;	curRule = rule;	curLen = newLen;
						}
					}
				}
			}
			
			if (curRule == null) throw new LexerException(y, x, curPos, s);
			else {
				String text = s.substring(curPos, curPos + curLen);
				
				for (int i = 0; i < text.length();) {
					if ((text.length() - i >= System.lineSeparator().length()) && text.substring(i, i + System.lineSeparator().length()).equals(System.lineSeparator())) {
						i += System.lineSeparator().length();
					} else {
						i++;
					}
				}
				
				Token token = createToken(curTerminal, curRule, text, y, x, curPos);

				if (!token.getTerminal().isSkipped()) tokens.add(token);
				
				curPos += curLen;
				x += curLen;
			}
		}
		
		return new LexerResult(tokens);
	}
}