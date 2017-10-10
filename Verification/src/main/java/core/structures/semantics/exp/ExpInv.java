package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;

public class ExpInv extends Exp {
    private Exp _exp;

    public @Nonnull
    Exp getExp() {
        return _exp;
    }

    public ExpInv(@Nonnull Exp exp) {
        _exp = exp;

        addChild(exp);
    }

    @Override
    public Exp reduce() {
        Exp exp = _exp.reduce();

        if (exp instanceof ExpLit) {
            ((ExpLit) exp).inv();

            return exp;
        }

        return new ExpInv(exp);
    }

    @Override
    public void order() {
        _exp.order();
    }

    @Override
    public int comp(Exp b) {
        return _exp.comp(((ExpInv) b)._exp);
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        String expS = _exp.getContentString();

        if (!(_exp instanceof ExpElem) && _exp.compPrecedence(this) <= 0) expS = parenthesize(expS);

        return "1" + _grammar.TERMINAL_OP_DIV.getPrimRule() + expS;
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        _exp = (Exp) _exp.replace(replaceFunc);

        return replaceFunc.apply(this);
    }
}