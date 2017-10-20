package core.structures.semantics.boolExp;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.exp.*;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ExpComp extends BoolElem {
    private Exp _leftExp;
    private ExpCompOp _expCompOp;
    private Exp _rightExp;

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

    public void neg() {
        _expCompOp.neg();
    }

    public void swap() {
        Exp leftExp = _leftExp;

        _leftExp = _rightExp;
        _expCompOp.swap();
        _rightExp = leftExp;
    }

    public void div(ExpLit lit) {
        assert (!lit.getVal().equals(BigDecimal.ZERO));

        _leftExp = new Prod(_leftExp, lit.makeInv());
        _rightExp = new Prod(_rightExp, lit.makeInv());

        if (lit.isNeg()) _expCompOp.swap();
    }

    @Override
    public BoolExp reduce() {
        Exp leftExp = _leftExp.reduce();
        Exp rightExp = _rightExp.reduce();
        ExpCompOp expCompOp = (ExpCompOp) _expCompOp.copy();

        //split
        if (expCompOp.getType().equals(ExpCompOp.Type.UNEQUAL)) return new BoolOr(new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.LESS), rightExp).reduce(), new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.GREATER), rightExp).reduce());
        if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL_GREATER)) return new BoolOr(new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.EQUAL), rightExp).reduce(), new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.GREATER), rightExp).reduce());
        if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL_LESS)) return new BoolOr(new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.EQUAL), rightExp).reduce(), new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.LESS), rightExp).reduce());

        if (expCompOp.getType().equals(ExpCompOp.Type.LESS)) {
            leftExp = new Sum(leftExp, new ExpMu());

            expCompOp = new ExpCompOp(ExpCompOp.Type.EQUAL);
        }
        if (expCompOp.getType().equals(ExpCompOp.Type.GREATER)) {
            rightExp = new Sum(rightExp, new ExpMu());

            expCompOp = new ExpCompOp(ExpCompOp.Type.EQUAL);
        }

        //both sides of equal shape
        if (leftExp.compPrecedence(rightExp) == 0) {
            if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL)) return new BoolLit(true);
            if (expCompOp.getType().equals(ExpCompOp.Type.UNEQUAL)) return new BoolLit(false);
            if (expCompOp.getType().equals(ExpCompOp.Type.LESS)) return new BoolLit(false);
            if (expCompOp.getType().equals(ExpCompOp.Type.GREATER)) return new BoolLit(false);
            if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL_LESS)) return new BoolLit(true);
            if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL_GREATER)) return new BoolLit(true);
        }

        //shift everything to left side
        rightExp = new Sum(rightExp);

        leftExp = new Sum(leftExp, rightExp.makeNeg()).reduce();

        rightExp = new Sum(rightExp, rightExp.makeNeg()).reduce();

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
            leftExp = new Prod(divLit.makeInv(), leftExp).reduce();
            rightExp = new Prod(divLit.makeInv(), rightExp).reduce();

            expCompOp.swap();
        }

        //a*x^2+b*x+c=0
        Set<Id> ids = leftExp.findType(Id.class);

        Sum leftSum = new Sum(leftExp);

        for (Id id : ids) {
            Map<Exp, Exp> coeffMap = new LinkedHashMap<>();

            for (Exp part : leftSum.getExps()) {
                Prod partProd = new Prod(part.reduce());

                Exp coeff = partProd.cutCoeff(Collections.singleton(id));

                Exp reducedPart = partProd.reduce();

                Exp exponent;

                if (reducedPart instanceof Pow) {
                    exponent = ((Pow) reducedPart).getExponent();
                } else if (reducedPart instanceof Id) {
                    exponent = new ExpLit(1);
                } else {
                    exponent = new ExpLit(0);
                }

                if (!coeffMap.containsKey(exponent)) coeffMap.put(exponent, coeff);

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

                    Exp q = coeff0.makeNeg().reduce();
                    Exp pNegHalf = new Prod(coeff1.makeNeg(), new ExpLit(1, 2)).reduce();

                    Exp radix = new Sum(new Pow(pNegHalf, new ExpLit(2)), q).reduce();

                    Exp sqrt = new Pow(radix, new ExpLit(1, 2)).reduce();
                    System.out.println("q: " + q);
                    System.out.println("-p/2: " + pNegHalf);
                    System.out.println("radix: " + radix);
                    System.out.println("sqrt: " + sqrt);
                    BoolExp pos = new ExpComp(new Sum(pNegHalf, sqrt), new ExpCompOp(ExpCompOp.Type.EQUAL), (Exp) id.copy()).reduce();
                    BoolExp neg = new ExpComp(new Sum(pNegHalf, sqrt.makeNeg()), new ExpCompOp(ExpCompOp.Type.EQUAL), (Exp) id.copy()).reduce();
                    System.out.println("pos " + pos);
                    System.out.println("neg " + neg);
                    return new BoolOr(pos, neg);
                }
            }
        }

        //both sides are literals, evaluate
        if (leftExp instanceof ExpLit && rightExp instanceof ExpLit) {
            BigDecimal leftVal = ((ExpLit) leftExp).getVal();
            BigDecimal rightVal = ((ExpLit) rightExp).getVal();

            if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL)) {
                if (leftVal.compareTo(rightVal) == 0) return new BoolLit(true); else return new BoolLit(false);
            }
            if (expCompOp.getType().equals(ExpCompOp.Type.UNEQUAL)) {
                if (leftVal.compareTo(rightVal) != 0) return new BoolLit(true); else return new BoolLit(false);
            }
            if (expCompOp.getType().equals(ExpCompOp.Type.LESS)) {
                if (leftVal.compareTo(rightVal) < 0) return new BoolLit(true); else return new BoolLit(false);
            }
            if (expCompOp.getType().equals(ExpCompOp.Type.GREATER)) {
                if (leftVal.compareTo(rightVal) > 0) return new BoolLit(true); else return new BoolLit(false);
            }
            if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL_LESS)) {
                if (leftVal.compareTo(rightVal) <= 0) return new BoolLit(true); else return new BoolLit(false);
            }
            if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL_GREATER)) {
                if (leftVal.compareTo(rightVal) >= 0) return new BoolLit(true); else return new BoolLit(false);
            }
        }

        return new ExpComp(leftExp, expCompOp, rightExp);
    }

    @Override
    public void order() {
        _leftExp.order();
        _rightExp.order();

        if (_leftExp.compPrecedence(_rightExp) > 0) swap();
    }

    @Override
    public int comp(BoolExp b) {
        int leftRet = _leftExp.compPrecedence(((ExpComp) b)._leftExp);

        if (leftRet != 0) return leftRet;

        int rightRet = _rightExp.compPrecedence(((ExpComp) b)._rightExp);

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
        _leftExp = (Exp) _leftExp.replace(replaceFunc);
        _expCompOp = (ExpCompOp) _expCompOp.replace(replaceFunc);
        _rightExp = (Exp) _rightExp.replace(replaceFunc);

        return replaceFunc.apply(this);
    }
}