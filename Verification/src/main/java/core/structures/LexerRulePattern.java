package core.structures;

import java.util.regex.Pattern;

/**
 * right side (one option) of a rule
 */
public class LexerRulePattern {
	private String _text;
	
	@Override
	public String toString() {
		return _text;
	}
	
	private String _regEx;
	
	public String getRegEx() {
		return _regEx;
	}
	
	private boolean _isRegEx;
	
	public boolean isRegEx() {
		return _isRegEx;
	}
	
	public LexerRulePattern(String text, boolean isRegEx) {
		_text = text;
		
		_regEx = (isRegEx) ? _text : Pattern.quote(_text);
		
		_isRegEx = isRegEx;
	}
}