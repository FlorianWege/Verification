private ParserRule selectRule(NonTerminal nonTerminal, Token terminal) throws ParserException {
	try {
		ParserRule rule = _ruleMap.get(nonTerminal, terminal.getTerminal());
		
		if (rule == null) throw new Exception();
		
		return rule;
	} catch (Exception e) {
		if (terminal == null) throw new NoMoreTokensException(nonTerminal);
		else throw new NoRuleException(terminal, nonTerminal);
	}
}

private SyntaxTreeNode getNode(NonTerminal rule) throws ParserException {
	ParserRule nextRule = selectRule(rule, _token);

	SyntaxTreeNode thisNode = new SyntaxTreeNode(rule, nextRule);

	for (Symbol symbol : nextRule.getSymbols()) {
		if (symbol instanceof NonTerminal) {
			thisNode.addChild(getNode((NonTerminal) symbol));
		} else {
			if (symbol.equals(Terminal.EPSILON)) {
				thisNode.addChild(new SyntaxTreeNodeTerminal(null));
				
				continue;
			}
			
			if (_token == null) throw new NoMoreTokensException(rule, (Terminal) symbol);
			if (!_token.getTerminal().equals(symbol)) throw new WrongTokenException(_token, rule, symbol);
			
			thisNode.addChild(new SyntaxTreeNodeTerminal(_token));
			
			_token = _tokensItr.hasNext() ? _tokensItr.next() : null;
		}
	}
	
	return thisNode;
}

public SyntaxTree parse(Vector<Token> tokens) throws ParserException {
	_tokens = tokens;
	
	if (_tokens.isEmpty()) throw new NoMoreTokensException(_grammar.getStartParserRule());
	
	_tokens.add(Token.createTerminator(tokens));
	
	_tokensItr = _tokens.iterator();
	
	_token = _tokensItr.next();

	SyntaxTreeNode root = getNode(_grammar.getStartParserRule());
	
	if (!_token.getTerminal().equals(Terminal.TERMINATOR)) throw new SuperfluousTokenException(_token);
	
	return new SyntaxTree(_grammar, root);
}