package core;

import core.structures.LexerRule;
import core.structures.Terminal;
import util.StringUtil;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * using a specified grammar, takes the lexer rules and converts a String into a list of tokens
 */
public class Lexer {
	private final Grammar _grammar;
	
	public Lexer(@Nonnull Grammar grammar) {
		_grammar = grammar;
	}
	
	private Token createToken(Terminal terminal, LexerRule rule, String text, int line, int lineOffset, int pos) {
		return new Token(terminal, rule, text, line, lineOffset, pos);
	}
	
	public class LexerException extends Exception {
		private final int _y;
		
		public int getLine() {
			return _y;
		}
		
		private final int _x;
		
		public int getLineOffset() {
			return _x;
		}
		
		private final int _curPos;
		
		public int getCurPos() {
			return _curPos;
		}
		
		private final String _inputString;
		
		public String getInputString() {
			return _inputString;
		}
		
		@Override
		public String getMessage() {
			return String.format("cannot findType token at pos %d.%d (%d): >>%s<<", _y + 1, _x + 1, _curPos, _inputString.substring(_curPos));
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
			
			if (sb.length() > 0) sb.append(StringUtil.line_sep);
			
			sb.append(line);
		}
		
		return sb.toString();
	}
	
	public static class LexerResult {
		private final List<Token> _tokens;
		
		public List<Token> getTokens() {
			return _tokens;
		}
		
		public void print(PrintStream stream) {
			stream.println("tokens:");
			
			for (int i = 0; i < getTokens().size(); i++) {
				stream.println("#" + i + ": " + getTokens().get(i).getTerminal().getKey() + "->" + getTokens().get(i).getText());
			}
		}
		
		LexerResult(List<Token> tokens) {
			_tokens = new ArrayList<>(tokens);
		}
	}
	
	public LexerResult tokenize(String s) throws LexerException {
		s = removeComments(s);
		
		List<Terminal> terminals = new ArrayList<>(_grammar.getTerminals());
		
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
		
		Terminal wsRule = new Terminal(new SymbolKey("WS")).setSkipped();
		
		wsRule.addRule(new LexerRule("\\s+", true));
		
		terminals.add(wsRule);
		
		int curPos = 0; Vector<Token> tokens = new Vector<>(); int x = 0; int y = 0;
		
		while (curPos < s.length()) {
			if ((s.length() - curPos >= StringUtil.line_sep.length()) && s.substring(curPos, curPos + StringUtil.line_sep.length()).equals(StringUtil.line_sep)) {
				curPos += StringUtil.line_sep.length(); x = 0; y++;
				
				continue;
			}
			
			int curLen = 0;	LexerRule curRule = null; Terminal curTerminal = null;

			for (Terminal terminal : terminals) {
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
					if ((text.length() - i >= StringUtil.line_sep.length()) && text.substring(i, i + StringUtil.line_sep.length()).equals(StringUtil.line_sep)) {
						i += StringUtil.line_sep.length();
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