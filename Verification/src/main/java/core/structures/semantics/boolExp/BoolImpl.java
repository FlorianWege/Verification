package core.structures.semantics.boolExp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;

public class BoolImpl extends BoolExp {
    private BoolExp _source;
    private BoolExp _target;

    public BoolExp getSource() {
        return _source;
    }

    public BoolExp getTarget() {
        return _target;
    }

    public BoolImpl(@Nonnull BoolExp source, @Nonnull BoolExp target) {
        _source = source;
        _target = target;

        addChild(source);
        addChild(target);
    }

    private BoolExp reduceB() {
        return new BoolOr(new BoolNeg((BoolExp) _source.copy()), (BoolExp) _target.copy()).reduce();
    }

    private BoolExp reduceA_left() {
        BoolExp left = getSource();
        BoolExp right = getTarget();

        if (left instanceof BoolOr) {
            BoolAnd newAnd = new BoolAnd();

            for (BoolExp leftPart : ((BoolOr) left).getBoolExps()) {
                newAnd.addBoolExp(new BoolImpl(leftPart, right));
            }

            if (newAnd.getBoolExps().size() == 1) return newAnd.getBoolExps().get(0);

            return newAnd;
        } else if (left instanceof BoolAnd) {
            BoolOr newOr = new BoolOr();

            for (BoolExp leftPart : ((BoolAnd) left).getBoolExps()) {
                newOr.addBoolExp(new BoolImpl(leftPart, right));
            }

            if (newOr.getBoolExps().size() == 1) return newOr.getBoolExps().get(0);

            return newOr;
        }

        return new BoolImpl(left, right);
    }

    private BoolExp reduceA_right() {
        BoolExp left = getSource();
        BoolExp right = getTarget();

        if (right instanceof BoolOr) {
            BoolOr newOr = new BoolOr();

            for (BoolExp rightPart : ((BoolOr) right).getBoolExps()) {
                newOr.addBoolExp(new BoolImpl(left, rightPart));
            }

            if (newOr.getBoolExps().size() == 1) return newOr.getBoolExps().get(0);

            return newOr;
        } else if (right instanceof BoolAnd) {
            BoolAnd newAnd = new BoolAnd();

            for (BoolExp rightPart : ((BoolAnd) right).getBoolExps()) {
                newAnd.addBoolExp(new BoolImpl(left, rightPart));
            }

            if (newAnd.getBoolExps().size() == 1) return newAnd.getBoolExps().get(0);

            return newAnd;
        }

        return new BoolImpl(left, right);
    }

    private BoolExp reduceA() {
        BoolExp left = _source.reduce();
        BoolExp right = _target.reduce();

        /*if (right instanceof BoolOr) {
            BoolOr newOr = new BoolOr();

            for (BoolExp rightPart : ((BoolOr) right).getBoolExps()) {
                newOr.addBoolExp(new BoolImpl(left, rightPart));
            }
        } else if (right instanceof BoolAnd) {
            BoolAnd newAnd = new BoolAnd();

            for (BoolExp rightPart : ((BoolAnd) right).getBoolExps()) {
                newAnd.addBoolExp(new BoolImpl(left, rightPart));
            }
        }*/

        if (left instanceof BoolList) {
            BoolExp leftSolved = reduceA_left();

            if (leftSolved instanceof BoolList) {
                BoolList newList = ((BoolList) leftSolved).invertConstruct();

                for (BoolExp part : ((BoolList) leftSolved).getBoolExps()) {
                    newList.addBoolExp(((BoolImpl) part).reduceA_right());
                }

                return newList;
            }
        } else if (right instanceof BoolList) {
            BoolExp rightSolved = reduceA_right();

            if (rightSolved instanceof BoolList) {
                BoolList newList = ((BoolList) rightSolved).ownConstruct();

                for (BoolExp part : ((BoolList) rightSolved).getBoolExps()) {
                    newList.addBoolExp(((BoolImpl) part).reduceA_left());
                }

                return newList;
            }
        }

        return new BoolImpl(left, right);
    }

    public enum ReduceStrat {
        REDUCE_SPLIT,
        REDUCE_MORPH
    }

    public static ReduceStrat _reduceStrat = ReduceStrat.REDUCE_SPLIT;

    @Override
    public BoolExp reduce() {
        if (_reduceStrat.equals(ReduceStrat.REDUCE_SPLIT)) return reduceA();
        if (_reduceStrat.equals(ReduceStrat.REDUCE_MORPH)) return reduceB();

        return reduceA();
    }

    @Override
    public void order() {
        _source.order();
        _target.order();
    }

    @Override
    public int comp(BoolExp b) {
        int ret = _source.compPrecedence(((BoolImpl) b)._source);

        if (ret != 0) return ret;

        return (_target.compPrecedence(((BoolImpl) b)._target));
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        return mapper.apply(this, _source.getContentString(mapper) + "->" + _target.getContentString(mapper));
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        BoolExp newSource = (BoolExp) _source.replace(replaceFunc);
        BoolExp newTarget = (BoolExp) _target.replace(replaceFunc);

        return replaceFunc.apply(new BoolImpl(newSource, newTarget));
    }
}