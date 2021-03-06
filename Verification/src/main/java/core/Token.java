package core;

import core.structures.LexerRule;
import core.structures.Terminal;
import util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

/**
 * token as gained by the lexer
 * a token is a concatenation of text to a lexer rule abiding entity, storing the lexer rule and the collected text 
 */
public class Token implements Serializable {
	private final Terminal _terminal;

	@Nonnull
	public Terminal getTerminal() {
		return _terminal;
	}

	private final LexerRule _rule;

	@Nullable
	public LexerRule getRule() {
		return _rule;
	}

	private String _text;

	@Nullable
	public String getText() {
		return _text;
	}
	
	private final int _line;
	
	public int getLine() {
		return _line;
	}
	
	private final int _lineOffset;
	
	public int getLineOffset() {
		return _lineOffset;
	}
	
	private final int _pos;
	
	public int getPos() {
		return _pos;
	}
	
	public Token copy() {
		return new Token(_terminal, _rule, _text, _line, _lineOffset, _pos);
	}
	
	public void replaceText(@Nonnull String newText) {
		_text = newText;
	}
	
	public Token(@Nonnull Terminal terminal, @Nullable LexerRule rule, @Nullable String text, int line, int lineOffset, int pos) {
		_terminal = terminal;
		_rule = rule;
		_text = text;
		
		_line = line;
		_lineOffset = lineOffset;
		_pos = pos;
	}

	public Token(@Nonnull Terminal terminal, @Nonnull String text) {
		this(terminal, null, text, 0, 0, 0);
	}

	@Nonnull
	public static Token createTerminator(@Nonnull List<Token> tokens) {
		Token last = tokens.get(tokens.size() - 1);

		return new Token(Terminal.TERMINATOR, null, null, last == null ? 0 : last.getLine(), last == null ? 0 : last.getLineOffset() + 1, last == null ? 0 : last.getPos() + 1);
	}
	
	@Override
	@Nonnull
	public String toString() {
		return String.format("%s (%s)", _terminal.toString(), _text);
	}

	@Nonnull
	public String toStringVert() {
		return String.format("%s%s(%s)", _terminal.toString(), StringUtil.line_sep, _text);
	}
}