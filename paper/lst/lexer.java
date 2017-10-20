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