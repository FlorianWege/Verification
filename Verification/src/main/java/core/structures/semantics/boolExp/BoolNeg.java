package core.structures.semantics.boolExp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;

public class BoolNeg extends BoolExp {
    private final BoolExp _boolExp;

    public BoolNeg(@Nonnull BoolExp boolExp) {
        _boolExp = boolExp;

        addChild(_boolExp);
    }

    public @Nonnull BoolExp getChild() {
        return _boolExp;
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        String boolExpS = _boolExp.getContentString(mapper);

        if (_boolExp.compPrecedence(this) <= 0) boolExpS = parenthesize(boolExpS);

        return mapper.apply(this, _grammar.TERMINAL_OP_NEG.getPrimRule() + boolExpS);
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        return replaceFunc.apply(this);
    }

    @Override
    public BoolExp reduce() {
        BoolExp boolExp = _boolExp.reduce();

        if (boolExp instanceof BoolLit) {
            ((BoolLit) boolExp).neg();

            return boolExp;
        }

        if (boolExp instanceof BoolNeg) return (((BoolNeg) boolExp).getChild());

        if (boolExp instanceof ExpComp) {
            ((ExpComp) boolExp).neg();

            return boolExp;
        }

        if (boolExp instanceof BoolList) {
            BoolList boolList = ((BoolList) boolExp).invertConstruct();

            for (BoolExp part : ((BoolList) boolExp).getBoolExps()) {
                boolList.addBoolExp(new BoolNeg(part).reduce());
            }

            return boolList;
        }

        return new BoolNeg(boolExp);
    }

    @Override
    public void order() {
        _boolExp.order();
    }

    @Override
    public int comp(BoolExp b) {
        return _boolExp.compPrecedence(((BoolNeg) b)._boolExp);
    }
}