package core;

/**
 * token as gained by the lexer
 * a token is a concatenation of text to a lexer rule abiding entity, storing the lexer rule and the collected text 
 */
public class Token {
	public LexerRule _rule;
	public LexerRulePattern _rulePattern;
	public String _text;
	
	public LexerRule getRule() {
		return _rule;
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
	
	public Token(LexerRule info, LexerRulePattern rulePattern, String text, int line, int lineOffset) {
		_rule = info;
		_rulePattern = rulePattern;
		_text = text;
		
		_line = line;
		_lineOffset = lineOffset;
	}
	
	@Override
	public String toString() {
		return String.format("%s (%s)", _rule.toString(), _text);
	}
}