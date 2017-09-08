package core.structures.nodes;

import core.SyntaxNode;

public class Prog extends SyntaxNodeSpec {
    private SyntaxNode _base;

    public SyntaxNode getBase() {
        return _base;
    }

    public Prog(SyntaxNode base) {
        if (!base.getSymbol().equals(_grammar.NON_TERMINAL_WHILE)) throw new RuntimeException("wrong node " + base);

        _base = base;
    }
}