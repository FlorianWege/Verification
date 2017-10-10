package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Exp extends SemanticNode {
    public abstract Exp reduce();
    public abstract void order();

    public abstract int comp(Exp b);

    public int compPrecedence(Exp b) {
        List<Class<? extends Exp>> types = new ArrayList<>();

        types.add(ExpMu.class);
        types.add(ExpLit.class);
        types.add(Id.class);
        types.add(Sum.class);
        types.add(ExpNeg.class);
        types.add(Prod.class);
        types.add(ExpInv.class);
        types.add(Pow.class);
        types.add(Fact.class);

        if (types.indexOf(getClass()) < types.indexOf(b.getClass())) return -1;
        if (types.indexOf(getClass()) > types.indexOf(b.getClass())) return 1;

        return comp(b);
    }

    public String parenthesize(String s) {
        return _grammar.TERMINAL_PAREN_OPEN.getPrimRule() + s + _grammar.TERMINAL_PAREN_CLOSE.getPrimRule();
    }
}