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

        //swap lit
        if (boolExp instanceof BoolLit) {
            ((BoolLit) boolExp).neg();

            return boolExp.reduce();
        }

        //resolve double negs
        if (boolExp instanceof BoolNeg) return boolExp.reduce();

        //swap comp
        if (boolExp instanceof ExpComp) {
            ((ExpComp) boolExp).neg();

            return boolExp.reduce();
        }

        //deMorgan
        if (boolExp instanceof BoolList) {
            BoolList tmpList = ((BoolList) boolExp).ownConstruct();

            for (BoolExp part : ((BoolList) boolExp).getBoolExps()) {
                tmpList.addBoolExp(part.reduce());
            }

            List<BoolExp> tmpList2 = new ArrayList<>(tmpList.getBoolExps());

            System.out.println("neg " + tmpList2);

            //merge split unequal
            if (boolExp instanceof BoolOr) {
                BoolList copyList = (BoolList) tmpList.copy();

                for (BoolExp orPart : copyList.getBoolExps()) {
                    if (orPart instanceof ExpComp) {
                        orPart = (BoolExp) orPart.copy();

                        Exp leftExp = ((ExpComp) orPart).getLeftExp();
                        Exp rightExp = ((ExpComp) orPart).getRightExp();

                        if (leftExp instanceof Sum) ((Sum) leftExp).cleanMu();
                        if (rightExp instanceof Sum) ((Sum) rightExp).cleanMu();

                        System.out.println("match " + orPart);
                        BoolOr matchBoolExp = new BoolOr(new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.UNEQUAL), rightExp).reduce());
                        System.out.println("matchB " + matchBoolExp);
                        //all parts of reduction contained?
                        if (tmpList2.containsAll(matchBoolExp.getBoolExps())) {
                            System.out.println("contained");
                            tmpList2.removeAll(matchBoolExp.getBoolExps());

                            tmpList2.add(new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.UNEQUAL), rightExp));
                        }
                    }
                }
            }

            System.out.println("negB " + tmpList2);

            BoolList boolList = ((BoolList) boolExp).invertConstruct();

            for (BoolExp part : tmpList2) {
                //avoid unequal split
                if (part instanceof ExpComp && ((ExpComp) part).getExpOp().getType().equals(ExpCompOp.Type.UNEQUAL)) {
                    boolList.addBoolExp(new ExpComp(((ExpComp) part).getLeftExp(), new ExpCompOp(ExpCompOp.Type.EQUAL), ((ExpComp) part).getRightExp()).reduce());
                } else {
                    boolList.addBoolExp(new BoolNeg(part).reduce());
                }
            }

            System.out.println("ret " + boolList);

            BoolExp ret2 = boolList.reduce();

            System.out.println("ret2 " + ret2);

            return ret2;
        }

        //nothing to do
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