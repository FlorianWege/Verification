public Set<Terminal> getFirst(List<Symbol> symbols, Set<NonTerminal> recursiveSet) {
    if (symbols.contains(Terminal.EPSILON)) return new LinkedHashSet<>(Arrays.asList(new Terminal[]{Terminal.EPSILON}));

    Set<Terminal> ret = new LinkedHashSet<>();

    for (int i = 0; i < symbols.size(); i++) {
        Symbol symbol = symbols.get(i);

        if (symbol instanceof Terminal) {
            ret.add((Terminal) symbol); break;
        } else if (symbol instanceof NonTerminal) {
            Set<Terminal> setSub = getFirst((NonTerminal) symbol, recursiveSet);

            if (i < symbols.size() - 1 && setSub.contains(Terminal.EPSILON)) {
                setSub.remove(Terminal.EPSILON); ret.addAll(setSub);
            } else {
                ret.addAll(setSub); break;
            }
        }
    }

    return ret;
}

public Set<Terminal> getFirst(NonTerminal nonTerminal, Set<NonTerminal> recursiveSet) {
    Set<Terminal> ret = new LinkedHashSet<>();

    if (recursiveSet.contains(nonTerminal)) return ret;

    recursiveSet.add(nonTerminal);

    for (ParserRule p : nonTerminal.getRules()) {
        ret.addAll(getFirst(p.getSymbols(), recursiveSet));
    }

    return ret;
}

public Set<Terminal> getFirst(NonTerminal nonTerminal) {
	return getFirst(nonTerminal, new LinkedHashSet<>());
}
