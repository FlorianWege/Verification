package core.structures;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * right side (one option) of a rule
 */
public class LexerRule implements Serializable {
	private final String _text;

	public String getText() {
		return _text;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof LexerRule) {
			if (!getText().equals(((LexerRule) other).getText())) return false;
			if (isRegEx() != ((LexerRule) other).isRegEx()) return false;
			if (!getRegEx().equals(((LexerRule) other).getRegEx())) return false;

			return true;
		}

		return super.equals(other);
	}

	@Override
	public String toString() {
		return _text;
	}

	private final boolean _isRegEx;

	public boolean isRegEx() {
		return _isRegEx;
	}

	private final String _regEx;
	
	public String getRegEx() {
		return _regEx;
	}
	
	public LexerRule(String text, boolean isRegEx) {
		_text = text;
		
		_isRegEx = isRegEx;

		_regEx = (_isRegEx) ? _text : Pattern.quote(_text);
	}
}