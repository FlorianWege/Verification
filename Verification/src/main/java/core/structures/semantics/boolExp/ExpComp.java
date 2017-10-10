package core.structures.semantics.boolExp;

import com.sun.org.apache.xpath.internal.operations.Neg;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.exp.*;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.function.Function;

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

        _leftExp = new Prod(_leftExp, new ExpInv(lit));
        _rightExp = new Prod(_rightExp, new ExpInv(lit));

        if (lit.isNeg()) _expCompOp.swap();
    }

    @Override
    public BoolExp reduce() {
        Exp leftExp = _leftExp.reduce();
        Exp rightExp = _rightExp.reduce();
        ExpCompOp expCompOp = (ExpCompOp) _expCompOp.copy();

        if (expCompOp.getType().equals(ExpCompOp.Type.UNEQUAL)) return new BoolOr(new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.LESS), rightExp), new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.GREATER), rightExp));
        if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL_GREATER)) return new BoolOr(new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.EQUAL), rightExp), new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.GREATER), rightExp));
        if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL_LESS)) return new BoolOr(new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.EQUAL), rightExp), new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.LESS), rightExp));

        if (expCompOp.getType().equals(ExpCompOp.Type.LESS)) {
            leftExp = new Sum(leftExp, new ExpMu());

            expCompOp = new ExpCompOp(ExpCompOp.Type.EQUAL);
        }
        if (expCompOp.getType().equals(ExpCompOp.Type.GREATER)) {
            rightExp = new Sum(rightExp, new ExpMu());

            expCompOp = new ExpCompOp(ExpCompOp.Type.EQUAL);
        }

        if (leftExp.compPrecedence(rightExp) == 0) {
            if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL)) return new BoolLit(true);
            if (expCompOp.getType().equals(ExpCompOp.Type.UNEQUAL)) return new BoolLit(false);
            if (expCompOp.getType().equals(ExpCompOp.Type.LESS)) return new BoolLit(false);
            if (expCompOp.getType().equals(ExpCompOp.Type.GREATER)) return new BoolLit(false);
            if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL_LESS)) return new BoolLit(true);
            if (expCompOp.getType().equals(ExpCompOp.Type.EQUAL_GREATER)) return new BoolLit(true);
        }

        rightExp = new Sum(rightExp);

        leftExp = new Sum(leftExp, new ExpNeg(rightExp)).reduce();

        rightExp = new Sum(rightExp, new ExpNeg(rightExp)).reduce();

        ExpLit divLit = null;

        if (leftExp instanceof Sum) {
            Exp firstExp = ((Sum) leftExp).getExps().get(0);

            if (firstExp instanceof Prod) {
                divLit = ((Prod) firstExp).getLit();
            } else if (firstExp instanceof ExpLit) {
                divLit = (ExpLit) firstExp;
            }
        } else if (leftExp instanceof ExpLit) {
            divLit = (ExpLit) leftExp;
        }

        if ((divLit != null) && (!divLit.isZero())) {
            leftExp = new Prod(new ExpInv(divLit), leftExp).reduce();
            rightExp = new Prod(new ExpInv(divLit), rightExp).reduce();

            expCompOp.swap();
        }

        /*if (leftExp instanceof Prod) {
            ExpLit lit = ((Prod) leftExp).getLit();

            leftExp = new Prod(new ExpInv(lit), leftExp).reduce();
            rightExp = new Prod(new ExpInv(lit), rightExp).reduce();
        }*/

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

        ExpComp ret = new ExpComp(leftExp, expCompOp, rightExp);

        return ret;
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