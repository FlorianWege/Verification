package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;

public class ExpNeg extends Exp {
    private Exp _exp;

    public @Nonnull Exp getExp() {
        return _exp;
    }

    @Override
    public String toString() {
        return _grammar.TERMINAL_OP_MINUS.getPrimRule() + _exp.toString();
    }

    public ExpNeg(@Nonnull Exp exp) {
        _exp = exp;

        addChild(exp);
    }

    @Override
    public Exp reduce() {
        Exp exp = _exp.reduce();

        if (exp instanceof ExpLit) {
            ((ExpLit) exp).neg();

            return exp;
        }
        if (exp instanceof Sum) {
            ((Sum) exp).neg();

            return exp.reduce();
        }
        if (exp instanceof Prod) {
            ((Prod) exp).neg();

            return exp.reduce();
        }

        return new ExpNeg(exp);
    }

    @Override
    public void order() {
        _exp.order();
    }

    @Override
    public int comp(Exp b) {
        return _exp.comp(((ExpNeg) b)._exp);
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        String expS = _exp.getContentString();

        if (!(_exp instanceof ExpElem) && _exp.compPrecedence(this) <= 0) expS = parenthesize(expS);

        return _grammar.TERMINAL_OP_MINUS.getPrimRule() + expS;
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        _exp = (Exp) _exp.replace(replaceFunc);

        return replaceFunc.apply(this);
    }
}