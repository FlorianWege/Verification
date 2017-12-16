package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;

public class Fact extends Exp {
    private final Exp _exp;

    public @Nonnull Exp getChild() {
        return _exp;
    }

    public Fact(@Nonnull Exp exp) {
        _exp = exp;

        addChild(_exp);
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        String expS = _exp.getContentString(mapper);

        if (!(_exp instanceof ExpElem) && _exp.comp(this) < 0) expS = parenthesize(expS);

        return mapper.apply(this, expS + _grammar.TERMINAL_OP_FACT.getPrimRule());
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        Exp exp = (Exp) _exp.replace(replaceFunc);

        return replaceFunc.apply(new Fact(exp));
    }

    @Nonnull
    @Override
    public Exp reduce_spec(@Nonnull Reducer reducer) {
        Exp exp = _exp.reduce(reducer);

        if (exp instanceof ExpLit) {
            exp = ((ExpLit) exp).fact();

            return exp;
        }

        if (exp instanceof Sum) {
            ExpLit lit = new ExpLit(0);
            Sum sum = new Sum();

            for (Exp exp2 : ((Sum) exp).getExps()) {
                if (exp2 instanceof ExpLit) {
                    lit = lit.add((ExpLit) exp2);
                } else {
                    sum.addExp(exp2);
                }
            }

            Prod prod = new Prod();

            Exp remExp = sum.reduce(reducer);

            prod.addExp(new Fact(remExp));

            while (lit.comp(new ExpLit(1)) >= 0) {
                remExp = (Exp) remExp.copy();

                Sum newSum = new Sum();

                newSum.addExp(remExp);
                newSum.addExp((Exp) lit.copy());

                prod.addExp(newSum);

                lit = lit.sub(new ExpLit(1));
            }

            return prod.reduce(reducer);
        }

        return new Fact(exp);
    }

    @Nonnull
    @Override
    public Exp order_spec() {
        return new Fact(_exp.order());
    }

    @Override
    public int comp_spec(@Nonnull Exp b) {
        return _exp.comp(((Fact) b)._exp);
    }
}