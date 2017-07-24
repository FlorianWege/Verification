package core;

import java.util.Collection;
import java.util.Comparator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.structures.LexerRule;
import core.structures.LexerRulePattern;

/**
 * using a specified grammar, takes the lexer rules and converts a String into a list of tokens
 */
public class Lexer {
	private Grammar _grammar;
	
	public Lexer(Grammar grammar) {
		_grammar = grammar;
	}
	
	private Token createToken(LexerRule info, LexerRulePattern rulePattern, String text, int line, int lineOffset) {
		Token token = new Token(info, rulePattern, text, line, lineOffset);
		
		return token;
	}
	
	public class LexerException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public LexerException(String text) {
			super(text);
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
				System.out.println("#" + i + ": " + getTokens().get(i)._rule._key + "->" + getTokens().get(i)._text);
			}
		}
		
		public LexerResult(Vector<Token> tokens) {
			_tokens = tokens;
		}
	}
	
	public LexerResult tokenize(String s) throws LexerException {
		s = removeComments(s);
		
		System.out.println("tokenizing...");
		
		int curPos = 0;
		Vector<Token> tokens = new Vector<>();
		int x = 0;
		int y = 0;
		
		while (curPos < s.length()) {
			if ((s.length() - curPos >= System.lineSeparator().length()) && s.substring(curPos, curPos + System.lineSeparator().length()).equals(System.lineSeparator())) {
				curPos += System.lineSeparator().length();
				
				x = 0;
				y++;
				
				continue;
			}
			
			int curLen = 0;
			LexerRulePattern curRulePattern = null;
			LexerRule curTokenInfo = null;
			Collection<LexerRulePattern> rulePatterns = new Vector<>();
			
			_grammar.getLexerRules().sort(new Comparator<LexerRule>() {
				@Override
				public int compare(LexerRule ruleA, LexerRule ruleB) {
					if (ruleA.getRulePatterns().isEmpty()) return 0;
					if (ruleB.getRulePatterns().isEmpty()) return 0;
					
					if (ruleA.getRulePatterns().get(0).isRegEx()) return 1;
					if (ruleB.getRulePatterns().get(0).isRegEx()) return -1;
					
					return 0;
				}
			});
			
			Vector<LexerRule> tokenInfos = new Vector<>(_grammar.getLexerRules());
			
			LexerRule wsRule = new LexerRule(new RuleKey("WS"), true);
			
			wsRule.addRule(new LexerRulePattern("\\s+", true));
			
			tokenInfos.add(wsRule);
			
			for (int i = 0; i < tokenInfos.size(); i++) {
				LexerRule tokenInfo = tokenInfos.get(i);
				
				Vector<LexerRulePattern> patterns = new Vector<>(tokenInfo._rulePatterns);
				
				for (int j = 0; j < patterns.size(); j++) {
					LexerRulePattern rulePattern = patterns.get(j);
					
					rulePatterns.add(rulePattern);
					
					String ruleS = (curPos > 0) ? String.format("^.{%d}(%s)", curPos, rulePattern.getRegEx()) : String.format("^(%s)", rulePattern.getRegEx());

					Pattern adjustedPattern = Pattern.compile(ruleS, Pattern.DOTALL);
					
					Matcher matcher = adjustedPattern.matcher(s);
					
					boolean foundA = matcher.find();
					boolean foundB = foundA && (matcher.start(1) == curPos);
					
					boolean found = foundA && foundB;
					
					if (found) {
						int newLen = (matcher.end(1) - 1) - matcher.start(1) + 1;

						if (newLen > curLen) {
							curTokenInfo = tokenInfo;
							curRulePattern = rulePattern;
							curLen = newLen;
						}
					}
				}
			}
			
			if (curRulePattern == null) {
				//System.out.println("TRIED RULES PATTERNS:");
				
				//for (LexerRulePattern rulePattern : rulePatterns) System.out.println(rulePattern);
				
				throw new LexerException(String.format("cannot find token at pos %d.%d (%d): >>%s<<", y + 1, x + 1, curPos, s.substring(curPos)));
			} else {
				String text = s.substring(curPos, curPos + curLen);
				
				for (int i = 0; i < text.length();) {
					if ((text.length() - i >= System.lineSeparator().length()) && text.substring(i, i + System.lineSeparator().length()).equals(System.lineSeparator())) {
						i += System.lineSeparator().length();
					} else {
						i++;
					}
				}
				
				Token token = createToken(curTokenInfo, curRulePattern, text, y, x);
				//System.out.println("TOKEN: " + token._info._key);
				if (!token._rule._skip) tokens.add(token);
				
				curPos += curLen;
				x += curLen;
			}
		}
		
		return new LexerResult(tokens);
	}
}