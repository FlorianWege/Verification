public Set<Terminal> getFollow(NonTerminal nonTerminal, Grammar grammar, Set<NonTerminal> recursiveSet) {		
	Set<Terminal> ret = new LinkedHashSet<>();

	if (recursiveSet.contains(nonTerminal)) return ret;

	recursiveSet.add(nonTerminal);

	if (grammar.getStartParserRule().equals(nonTerminal)) ret.add(Terminal.TERMINATOR);

	for (NonTerminal p : grammar.getNonTerminals()) {
		for (ParserRule rule : p.getRules()) {
			List<Symbol> symbols = rule.getSymbols();
			
			for (int i = 0; i < symbols.size(); i++) {
				Symbol symbol = rule.getSymbols().get(i);
				
				if (symbol.equals(nonTerminal)) {
					List<Symbol> restSymbols = (i < symbols.size() - 1) ? symbols.subList(i + 1, symbols.size()) : new ArrayList<>();
					
					if (restSymbols.isEmpty()) {
						ret.addAll(getFollow(p, grammar, recursiveSet));
					} else {
						Set<Terminal> subSet = getFirst(restSymbols, new LinkedHashSet<NonTerminal>());

						if (subSet.contains(Terminal.EPSILON)) {
							subSet.remove(Terminal.EPSILON);
							
							ret.addAll(subSet);

							ret.addAll(getFollow(p, grammar, recursiveSet));
						} else {
							ret.addAll(subSet);
						}
					}
				}
			}
		}
	}

	return ret;
}

public Set<Terminal> getFollow(NonTerminal nonTerminal, Grammar grammar) {
	return getFollow(nonTerminal, grammar, new LinkedHashSet<NonTerminal>());
}