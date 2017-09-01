package core;

import java.util.Vector;

import core.structures.Terminal;
import core.structures.LexerRule;

/**
 * token as gained by the lexer
 * a token is a concatenation of text to a lexer rule abiding entity, storing the lexer rule and the collected text 
 */
public class Token {
	public Terminal _terminal;
	public LexerRule _rule;
	public String _text;
	
	public Terminal getTerminal() {
		return _terminal;
	}
	
	public String getText() {
		return _text;
	}
	
	private int _line;
	
	public int getLine() {
		return _line;
	}
	
	private int _lineOffset;
	
	public int getLineOffset() {
		return _lineOffset;
	}
	
	private int _pos;
	
	public int getPos() {
		return _pos;
	}
	
	public Token copy() {
		return new Token(_terminal, _rule, _text, _line, _lineOffset, _pos);
	}
	
	public void replaceText(String newText) {
		_text = newText;
	}
	
	public Token(Terminal terminal, LexerRule rule, String text, int line, int lineOffset, int pos) {
		_terminal = terminal;
		_rule = rule;
		_text = text;
		
		_line = line;
		_lineOffset = lineOffset;
		_pos = pos;
	}
	
	public static Token createTerminator(Vector<Token> tokens) {
		return new Token(Terminal.TERMINATOR, null, null, tokens.lastElement() == null ? 0 : tokens.lastElement().getLine(), tokens.lastElement() == null ? 0 : tokens.lastElement().getLineOffset() + 1, tokens.lastElement() == null ? 0 : tokens.lastElement().getPos() + 1);
	}
	
	@Override
	public String toString() {
		return String.format("%s (%s)", _terminal.toString(), _text);
	}
	
	public String toStringVert() {
		return String.format("%s%s(%s)", _terminal.toString(), System.lineSeparator(), _text);
	}
}