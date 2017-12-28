package core.structures.semantics.boolExp;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.exp.*;
import util.IOUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ExpComp extends BoolElem {
    private final Exp _leftExp;
    private final ExpCompOp _expCompOp;
    private final Exp _rightExp;

    public Exp getLeftExp() {
        return _leftExp;
    }

    public ExpCompOp getExpOp() {
        return _expCompOp;
    }

    public Exp getRightExp() {
        return _rightExp;
    }

    public ExpComp(@Nonnull Exp leftExp, @Nonnull ExpCompOp expCompOp, @Nonnull Exp rightExp) {
        _leftExp = leftExp;
        _expCompOp = expCompOp;
        _rightExp = rightExp;

        addChild(_leftExp);
        addChild(_expCompOp);
        addChild(_rightExp);
    }

    @CheckReturnValue
    @Nonnull
    public ExpComp neg() {
        Exp leftExp = getLeftExp();
        Exp rightExp = getRightExp();

        if (!leftExp.findType(ExpMu.class).isEmpty() || !rightExp.findType(ExpMu.class).isEmpty()) {
            leftExp = (Exp) leftExp.replace(new IOUtil.Func<SemanticNode, SemanticNode>() {
                @Override
                public SemanticNode apply(SemanticNode semanticNode) {
                    if (semanticNode instanceof ExpMu) {
                        return new Prod(new ExpLit(-1), new ExpMu());
                    }

                    return semanticNode;
                }
            });
            rightExp = (Exp) rightExp.replace(new IOUtil.Func<SemanticNode, SemanticNode>() {
                @Override
                public SemanticNode apply(SemanticNode semanticNode) {
                    if (semanticNode instanceof ExpMu) {
                        return new Prod(new ExpLit(-1), new ExpMu());
                    }

                    return semanticNode;
                }
            });

            return new ExpComp(leftExp, getExpOp(), rightExp);
        }

        return new ExpComp(leftExp, getExpOp().neg(), rightExp);
    }

    @CheckReturnValue
    @Nonnull
    public ExpComp swap() {
        return new ExpComp(getRightExp(), getExpOp().swap(), getLeftExp());
    }

    @CheckReturnValue
    @Nonnull
    public ExpComp div(ExpLit lit) {
        assert (!lit.getVal().equals(BigDecimal.ZERO));

        Exp leftExp = new Prod(_leftExp, lit.makeInv());
        ExpCompOp compOp = (lit.isNeg()) ? _expCompOp.swap() : _expCompOp;
        Exp rightExp = new Prod(_rightExp, lit.makeInv());

        return new ExpComp(leftExp, compOp, rightExp);
    }

    @Nonnull
    public ExpComp revertMu() {
        if (_expCompOp.getType().equals(ExpCompOp.Type.EQUAL)) {
            Exp leftExp = (Exp) _leftExp.copy();
            Exp rightExp = (Exp) _rightExp.copy();

            Exp leftMuCoeff = leftExp.getMuCoeff();
            Exp rightMuCoeff = rightExp.getMuCoeff();

            if (leftMuCoeff instanceof ExpLit && rightMuCoeff instanceof ExpLit) {
                int comp = ((ExpLit) leftMuCoeff).getVal().compareTo(((ExpLit) rightMuCoeff).getVal());
                ExpCompOp.Type opType = ExpCompOp.Type.EQUAL;

                if (comp < 0) opType = ExpCompOp.Type.GREATER;
                else if (comp > 0) opType = ExpCompOp.Type.LESS;

                Exp leftExpCut = leftExp.cutMuCoeff();
                Exp rightExpCut = rightExp.cutMuCoeff();

                ExpComp ret = new ExpComp(leftExpCut, new ExpCompOp(opType), rightExpCut);

                return ret;
            }
        }

        return (ExpComp) copy();
    }

    @Nonnull
    @Override
    public BoolExp reduce_spec(@Nonnull Reducer reducer) {
        Exp leftExp = _leftExp.reduce(new Exp.Reducer(_leftExp));

        Exp rightExp = _rightExp.reduce(new Exp.Reducer(_rightExp));
        ExpCompOp expCompOp = (ExpCompOp) _expCompOp.copy();

        //split
        if (expCompOp.getType().equals(ExpCompOp.Type.UNEQUAL)) return new BoolOr(new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.LESS), rightExp).reduce(reducer), new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.GREATER), rightExp).reduce(reducer));
        if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL_GREATER)) return new BoolOr(new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.EQUAL), rightExp).reduce(reducer), new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.GREATER), rightExp).reduce(reducer));
        if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL_LESS)) return new BoolOr(new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.EQUAL), rightExp).reduce(reducer), new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.LESS), rightExp).reduce(reducer));

        if (expCompOp.getType().equals(ExpCompOp.Type.LESS)) {
            leftExp = new Sum(leftExp, new ExpMu());

            expCompOp = new ExpCompOp(ExpCompOp.Type.EQUAL);
        }
        if (expCompOp.getType().equals(ExpCompOp.Type.GREATER)) {
            rightExp = new Sum(rightExp, new ExpMu());

            expCompOp = new ExpCompOp(ExpCompOp.Type.EQUAL);
        }

        //both sides of equal shape
        if (leftExp.comp(rightExp) == 0) {
            if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL)) return new BoolLit(true);
            if (expCompOp.getType().equals(ExpCompOp.Type.UNEQUAL)) return new BoolLit(false);
            if (expCompOp.getType().equals(ExpCompOp.Type.LESS)) return new BoolLit(false);
            if (expCompOp.getType().equals(ExpCompOp.Type.GREATER)) return new BoolLit(false);
            if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL_LESS)) return new BoolLit(true);
            if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL_GREATER)) return new BoolLit(true);
        }

        //shift everything to left side
        rightExp = new Sum(rightExp);

        leftExp = new Sum(leftExp, rightExp.makeNeg()).reduce(new Exp.Reducer(leftExp));

        rightExp = new Sum(rightExp, rightExp.makeNeg()).reduce(new Exp.Reducer(rightExp));

        //remove unnecessary coefficients
        ExpLit divLit = null;

        if (leftExp instanceof Sum) {
            Exp firstExp = ((Sum) leftExp).getExps().get(0);

            if (firstExp instanceof Prod) {
                divLit = ((Prod) firstExp).getLit();
            } else if (firstExp instanceof ExpLit) {
                divLit = (ExpLit) firstExp;
            }
        } else if (leftExp instanceof Prod) {
            divLit = ((Prod) leftExp).getLit();
        } if (leftExp instanceof ExpLit) {
            divLit = (ExpLit) leftExp;
        }

        if ((divLit != null) && (!divLit.isZero())) {
            leftExp = new Prod(divLit.makeInv(), leftExp).reduce(new Exp.Reducer(leftExp));
            rightExp = new Prod(divLit.makeInv(), rightExp).reduce(new Exp.Reducer(rightExp));

            expCompOp = expCompOp.swap();
        }

        //a*x^2+b*x+c=0
        Set<Id> ids = leftExp.findType(Id.class);

        Sum leftSum = new Sum(leftExp);

        for (Id id : ids) {
            Map<Exp, Exp> coeffMap = new LinkedHashMap<>();

            for (Exp part : leftSum.getExps()) {
                Prod partProd = new Prod(part.reduce(new Exp.Reducer(part)));

                Exp coeff = partProd.cutCoeff(Collections.singleton(id));

                Exp reducedPart = partProd.reduce(new Exp.Reducer(partProd));

                Exp exponent;

                if (reducedPart instanceof Pow) {
                    exponent = ((Pow) reducedPart).getExponent();
                } else if (reducedPart instanceof Id) {
                    exponent = new ExpLit(1);
                } else {
                    exponent = new ExpLit(0);
                }

                if (!coeffMap.containsKey(exponent)) coeffMap.put(exponent, new ExpLit(0));

                coeffMap.put(exponent, new Sum(coeffMap.get(exponent), coeff));
            }

            if (coeffMap.containsKey(new ExpLit(2))) {
                boolean found = false;

                for (Exp coeff : coeffMap.keySet()) {
                    if (!coeff.equals(new ExpLit(0)) && !coeff.equals(new ExpLit(1)) && !coeff.equals(new ExpLit(2))) {
                        found = true;

                        break;
                    }
                }

                if (!found) {
                    Exp coeff2 = coeffMap.get(new ExpLit(2));
                    Exp coeff1 = coeffMap.getOrDefault(new ExpLit(1), new ExpLit(0));
                    Exp coeff0 = coeffMap.getOrDefault(new ExpLit(0), new ExpLit(0));

                    coeff1 = new Prod(coeff1, coeff2.makeInv());
                    coeff0 = new Prod(coeff0, coeff2.makeInv());

                    Exp negCoeff0 = coeff0.makeNeg();

                    Exp q = negCoeff0.reduce(new Exp.Reducer(negCoeff0));

                    Prod prod = new Prod(coeff1.makeNeg(), new ExpLit(1, 2));

                    Exp pNegHalf = prod.reduce(new Exp.Reducer(prod));

                    Sum sum = new Sum(new Pow(pNegHalf, new ExpLit(2)), q);

                    Exp radix = sum.reduce(new Exp.Reducer(sum));

                    Pow pow = new Pow(radix, new ExpLit(1, 2));

                    Exp sqrt = pow.reduce(new Exp.Reducer(pow));

                    BoolExp pos = new ExpComp(new Sum(pNegHalf, sqrt), new ExpCompOp(ExpCompOp.Type.EQUAL), (Exp) id.copy()).reduce(reducer);
                    BoolExp neg = new ExpComp(new Sum(pNegHalf, sqrt.makeNeg()), new ExpCompOp(ExpCompOp.Type.EQUAL), (Exp) id.copy()).reduce(reducer);

                    return new BoolOr(pos, neg);
                }
            }
        }

        //both sides are literals, evaluate
        {
            ExpComp expCompTested = revertMu();

            Exp leftExpTested = expCompTested.getLeftExp().reduce();
            ExpCompOp compOpTested = expCompTested.getExpOp();
            Exp rightExpTested = expCompTested.getRightExp().reduce();

            if (leftExpTested instanceof ExpLit && rightExpTested instanceof ExpLit) {
                BigDecimal leftVal = ((ExpLit) leftExpTested).getVal();
                BigDecimal rightVal = ((ExpLit) rightExpTested).getVal();

                ExpCompOp.Type compOpType = compOpTested.getType();

                if (compOpType.equals(ExpCompOp.Type.EQUAL)) {
                    if (leftVal.compareTo(rightVal) == 0) return new BoolLit(true);
                    else return new BoolLit(false);
                }
                if (compOpType.equals(ExpCompOp.Type.UNEQUAL)) {
                    if (leftVal.compareTo(rightVal) != 0) return new BoolLit(true);
                    else return new BoolLit(false);
                }
                if (compOpType.equals(ExpCompOp.Type.LESS)) {
                    if (leftVal.compareTo(rightVal) < 0) return new BoolLit(true);
                    else return new BoolLit(false);
                }
                if (compOpType.equals(ExpCompOp.Type.GREATER)) {
                    if (leftVal.compareTo(rightVal) > 0) return new BoolLit(true);
                    else return new BoolLit(false);
                }
                if (compOpType.equals(ExpCompOp.Type.EQUAL_LESS)) {
                    if (leftVal.compareTo(rightVal) <= 0) return new BoolLit(true);
                    else return new BoolLit(false);
                }
                if (compOpType.equals(ExpCompOp.Type.EQUAL_GREATER)) {
                    if (leftVal.compareTo(rightVal) >= 0) return new BoolLit(true);
                    else return new BoolLit(false);
                }
            }
        }

        if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL)) {
            if (leftExp instanceof ExpMu) return new BoolLit(false);

            if (leftExp instanceof Prod) {
                ExpLit lit = ((Prod) leftExp).getLit();
                Exp cut = ((Prod) leftExp).cutLit();

                if (cut instanceof ExpMu) return new BoolLit(lit.isNeg());
            }

            /*if (leftExp instanceof Sum) {
                for (Exp summand : ((Sum) leftExp).getExps()) {
                    if (summand instanceof ExpMu) return new BoolLit(false);

                    if (summand instanceof Prod) {
                        ExpLit lit = ((Prod) summand).getLit();
                        Exp cut = ((Prod) summand).cutLit();

                        if (cut instanceof ExpMu) return new BoolLit(lit.isNeg());
                    }
                }
            }*/
        }

        return new ExpComp(leftExp, expCompOp, rightExp);
    }

    @Override
    @Nonnull
    public BoolExp order_spec() {
        Exp leftExp = _leftExp;
        Exp rightExp = _rightExp;

        //shift everything to left side
        rightExp = new Sum(rightExp);

        Sum leftSum = new Sum(leftExp, rightExp.makeNeg());

        leftExp = leftSum.reduce(new Exp.Reducer(leftSum));

        Sum rightSum = new Sum(rightExp, rightExp.makeNeg());

        rightExp = rightSum.reduce(new Exp.Reducer(rightSum));

        ExpComp ret = new ExpComp(leftExp, getExpOp(), rightExp);

        if (ret.getLeftExp().comp(ret.getRightExp()) > 0) ret = ret.swap();

        return ret;
    }

    @Override
    public int comp_spec(BoolExp b) {
        int leftRet = _leftExp.comp(((ExpComp) b)._leftExp);

        if (leftRet != 0) return leftRet;

        int rightRet = _rightExp.comp(((ExpComp) b)._rightExp);

        if (rightRet != 0) return rightRet;

        return _expCompOp.comp(((ExpComp) b)._expCompOp);
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        return mapper.apply(this, _leftExp.getContentString(mapper) + _expCompOp.getContentString(mapper) + _rightExp.getContentString(mapper));
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        Exp leftExp = (Exp) _leftExp.replace(replaceFunc);
        ExpCompOp expCompOp = (ExpCompOp) _expCompOp.replace(replaceFunc);
        Exp rightExp = (Exp) _rightExp.replace(replaceFunc);

        return replaceFunc.apply(new ExpComp(leftExp, expCompOp, rightExp));
    }
}