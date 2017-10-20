package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;

public class Pow extends Exp {
    private Exp _base;
    private Exp _exponent;

    public Pow(Exp base, Exp exponent) {
        _base = base;
        _exponent = exponent;

        addChild(_base, "base");
        addChild(_exponent, "exp");
    }

    public @Nonnull Exp getBase() {
        return _base;
    }

    public @Nonnull Exp getExponent() {
        return _exponent;
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        String baseS = _base.getContentString(mapper);
        String exponentS = _exponent.getContentString(mapper);

        //if (_exponent instanceof Pow && compPrecedence(((Pow) _exponent).getBase(), this) < 0) exponentS = parenthesize(exponentS);
        if (!(_base instanceof ExpElem) && _base.compPrecedence(this) < 0) baseS = parenthesize(baseS);
        if (!(_exponent instanceof ExpElem) && _exponent.compPrecedence(this) < 0) exponentS = parenthesize(exponentS);

        return mapper.apply(this, baseS + _grammar.TERMINAL_OP_POW.getPrimRule() + exponentS);
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        _base = (Exp) _base.replace(replaceFunc);
        _exponent = (Exp) _exponent.replace(replaceFunc);

        return replaceFunc.apply(this);
    }

    @Override
    public Exp reduce() {
        Exp base = _base.reduce();
        Exp exponent = _exponent.reduce();

        if (base instanceof ExpLit && exponent instanceof ExpLit) {
            ExpLit ret = (ExpLit) base;

            ret.pow((ExpLit) exponent);

            return ret;
        }

        if (base instanceof Pow) {
            exponent = new Prod(((Pow) base).getExponent(), exponent);

            base = ((Pow) base).getBase();
        }

        if (exponent.equals(new ExpLit(1))) return base;
        if (exponent.equals(new ExpLit(0))) return new ExpLit(1);

        //TODO interferes with prod's potentiation
        /*if (exponent instanceof Sum) {
            System.out.println("disconnect " + exponent.getContentString());
            Prod prod = new Prod();

            for (Exp exp : ((Sum) exponent).getExps()) {
                prod.addExp(new Pow(base, exp));
            }
            System.out.println("return " + prod.getContentString());
            return prod.reduce();
        }*/

        return new Pow(base, exponent);
    }

    @Override
    public void order() {
        _base.order();
        _exponent.order();
    }

    @Override
    public int comp(Exp b) {
        int baseRet = _base.compPrecedence(((Pow) b).getBase());

        if (baseRet != 0) return baseRet;

        return _exponent.compPrecedence(((Pow) b)._exponent);
    }
}