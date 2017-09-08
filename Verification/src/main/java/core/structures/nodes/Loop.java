package core.structures.nodes;

import core.SyntaxNode;

public class Loop extends SyntaxNodeSpec {
    private SyntaxNode _base;

    public SyntaxNode getBase() {
        return _base;
    }

    private BoolExp _boolExp;

    public BoolExp getBoolExp() {
        return _boolExp;
    }

    private Prog _prog;

    public Prog getProg() {
        return _prog;
    }

    public Loop(SyntaxNode base) {
        if (!base.getSymbol().equals(_grammar.NON_TERMINAL_WHILE)) throw new RuntimeException("wrong node " + base);

        _base = base;

        _boolExp = new BoolExp(_base.findChild(_grammar.NON_TERMINAL_BOOL_EXP, true));
        _prog = new Prog(_base.findChild(_grammar.NON_TERMINAL_PROG, true));
    }
}