package core.structures.semantics.boolExp;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.exp.Exp;
import core.structures.semantics.exp.Sum;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

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

        if (_boolExp.comp(this) <= 0) boolExpS = parenthesize(boolExpS);

        return mapper.apply(this, _grammar.TERMINAL_OP_NEG.getPrimRule() + boolExpS);
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        return replaceFunc.apply(new BoolNeg((BoolExp) replaceFunc.apply(getChild())));
    }

    @Nonnull
    @Override
    public BoolExp reduce_spec(@Nonnull Reducer reducer) {
        BoolExp boolExp = getChild();

        //swap lit
        if (boolExp instanceof BoolLit) {
            boolExp = ((BoolLit) boolExp).neg();

            return boolExp.reduce(reducer);
        }

        //resolve double negs
        if (boolExp instanceof BoolNeg) return ((BoolNeg) boolExp).getChild().reduce(reducer);

        //swap comp
        if (boolExp instanceof ExpComp) {
            boolExp = ((ExpComp) boolExp).neg();

            BoolExp ret = boolExp.reduce(reducer);

            return ret;
        }

        //deMorgan
        if (boolExp instanceof BoolList) {
            BoolList tmpList = ((BoolList) boolExp).ownConstruct();

            for (BoolExp part : ((BoolList) boolExp).getBoolExps()) {
                tmpList.addBoolExp(part.reduce(reducer));
            }

            List<BoolExp> tmpList2 = new ArrayList<>(tmpList.getBoolExps());

            //merge split unequal
            if (boolExp instanceof BoolOr) {
                BoolList copyList = (BoolList) tmpList.copy();

                for (BoolExp orPart : copyList.getBoolExps()) {
                    if (orPart instanceof ExpComp) {
                        orPart = (BoolExp) orPart.copy();

                        Exp leftExp = ((ExpComp) orPart).getLeftExp();
                        Exp rightExp = ((ExpComp) orPart).getRightExp();

                        if (leftExp instanceof Sum) ((Sum) leftExp).cleanMu(new Exp.Reducer(leftExp));
                        if (rightExp instanceof Sum) ((Sum) rightExp).cleanMu(new Exp.Reducer(rightExp));

                        BoolOr matchBoolExp = new BoolOr(new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.UNEQUAL), rightExp).reduce(reducer));

                        //all parts of reduction contained?
                        if (tmpList2.containsAll(matchBoolExp.getBoolExps())) {
                            tmpList2.removeAll(matchBoolExp.getBoolExps());

                            tmpList2.add(new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.UNEQUAL), rightExp));
                        }
                    }
                }
            }

            BoolList boolList = ((BoolList) boolExp).invertConstruct();

            for (BoolExp part : tmpList2) {
                //avoid unequal split
                if (part instanceof ExpComp && ((ExpComp) part).getExpOp().getType().equals(ExpCompOp.Type.UNEQUAL)) {
                    boolList.addBoolExp(new ExpComp(((ExpComp) part).getLeftExp(), new ExpCompOp(ExpCompOp.Type.EQUAL), ((ExpComp) part).getRightExp()).reduce(reducer));
                } else {
                    boolList.addBoolExp(new BoolNeg(part).reduce(reducer));
                }
            }

            return boolList.reduce(reducer);
        }

        //nothing to do
        return new BoolNeg(boolExp);
    }

    @Nonnull
    @Override
    public BoolExp order_spec() {
        BoolExp newBoolExp = _boolExp.order();

        return new BoolNeg(newBoolExp);
    }

    @Override
    public int comp_spec(BoolExp b) {
        return _boolExp.comp(((BoolNeg) b)._boolExp);
    }
}