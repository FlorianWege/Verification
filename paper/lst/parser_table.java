public PredictiveParserTable(Grammar g) {
	Map<NonTerminal, Set<Terminal>> firstMap = new LinkedHashMap<>();
	Map<NonTerminal, Set<Terminal>> followMap = new LinkedHashMap<>();
	
	for (NonTerminal p : g.getNonTerminals()) {
		firstMap.put(p, getFirst(p));
		followMap.put(p, getFollow(p, g));
	}
	
	for (NonTerminal p : g.getNonTerminals()) {
		for (ParserRule r : p.getRules()) {
			List<Symbol> symbols = r.getSymbols();
			
			if (symbols.contains(Terminal.EPSILON))
				for (Terminal terminal : followMap.get(p)) {	
					set(p, terminal, r);
				}
			else {
				Symbol symbol = r.getSymbols().get(0);
				
				if (symbol instanceof Terminal) {
					set(p, (Terminal) symbol, r);
				} else if (symbol instanceof NonTerminal) {
					for (Terminal terminal : firstMap.get((NonTerminal) symbol)) {
						set(p, terminal, r);
					}
				}
			}
		}
	}
}