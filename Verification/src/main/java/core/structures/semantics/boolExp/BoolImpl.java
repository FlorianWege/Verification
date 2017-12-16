package core.structures.semantics.boolExp;

import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.exp.Exp;
import core.structures.semantics.exp.Id;
import core.structures.semantics.exp.Sum;
import util.IOUtil;
import util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BoolImpl extends BoolExp {
    private BoolExp _source;
    private BoolExp _target;

    @Nonnull
    public BoolExp getSource() {
        return _source;
    }

    @Nonnull
    public BoolExp getTarget() {
        return _target;
    }

    public BoolImpl(@Nonnull String sourceS, @Nonnull String targetS) throws Lexer.LexerException, Parser.ParserException {
        this(BoolExp.fromString(sourceS), BoolExp.fromString(targetS));
    }

    public BoolImpl(@Nonnull BoolExp source, @Nonnull BoolExp target) {
        _source = source;
        _target = target;

        addChild(source);
        addChild(target);
    }

    @Nullable
    private Exp isolateId(@Nonnull ExpComp expComp, @Nonnull Id id) {
        expComp = (ExpComp) expComp.copy();

        if (!expComp.findType(Id.class).contains(id)) return null;

        //left side=0
        expComp = (ExpComp) expComp.order();

        Exp leftExp = expComp.getLeftExp();
        ExpCompOp compOp = expComp.getExpOp();
        Exp rightExp = expComp.getRightExp();

        assert(compOp.getType().equals(ExpCompOp.Type.EQUAL));

        if (leftExp.equals(id)) return rightExp;
        if (rightExp.equals(id)) return leftExp;

        if (rightExp instanceof Sum) {
            Sum toSubtract = new Sum();

            for (Exp exp : ((Sum) rightExp).getExps()) {
                if (!exp.findType(Id.class).contains(id)) {
                    toSubtract.addExp(exp);
                }
            }

            toSubtract.neg();

            leftExp = new Sum(leftExp, toSubtract).reduce(null);
            rightExp = new Sum(rightExp, toSubtract).reduce(null);

            //expComp = new ExpComp(leftExp, compOp, rightExp);

            leftExp = leftExp.order();

            if (rightExp.equals(id)) return leftExp;

            return null;
        }

        return leftExp;
    }

    @Nonnull
    private BoolExp split_left(boolean splitOr, boolean splitAnd) {
        BoolExp left = getSource();
        BoolExp right = getTarget();

        if (splitOr && left instanceof BoolOr) {
            BoolAnd newAnd = new BoolAnd();

            for (BoolExp leftPart : ((BoolOr) left).getBoolExps()) {
                newAnd.addBoolExp(new BoolImpl(leftPart, right));
            }

            if (newAnd.getBoolExps().size() == 1) return newAnd.getBoolExps().get(0);

            return newAnd;
        } else if (splitAnd && left instanceof BoolAnd) {
            BoolOr newOr = new BoolOr();

            for (BoolExp leftPart : ((BoolAnd) left).getBoolExps()) {
                newOr.addBoolExp(new BoolImpl(leftPart, right));
            }

            if (newOr.getBoolExps().size() == 1) return newOr.getBoolExps().get(0);

            return newOr;
        }

        return new BoolImpl(left, right);
    }

    @Nonnull
    private BoolExp split_right(boolean splitOr, boolean splitAnd) {
        BoolExp left = getSource();
        BoolExp right = getTarget();

        if (splitOr && right instanceof BoolOr) {
            BoolOr newOr = new BoolOr();

            for (BoolExp rightPart : ((BoolOr) right).getBoolExps()) {
                newOr.addBoolExp(new BoolImpl(left, rightPart));
            }

            if (newOr.getBoolExps().size() == 1) return newOr.getBoolExps().get(0);

            return newOr;
        } else if (splitAnd && right instanceof BoolAnd) {
            BoolAnd newAnd = new BoolAnd();

            for (BoolExp rightPart : ((BoolAnd) right).getBoolExps()) {
                newAnd.addBoolExp(new BoolImpl(left, rightPart));
            }

            if (newAnd.getBoolExps().size() == 1) return newAnd.getBoolExps().get(0);

            return newAnd;
        }

        return new BoolImpl(left, right);
    }

    @Nonnull
    public BoolExp split(boolean splitOr, boolean splitAnd) {
        BoolExp left = (BoolExp) _source.copy();
        BoolExp right = (BoolExp) _target.copy();

        if (right instanceof BoolList) {
            BoolExp rightSolved = split_right(splitOr, splitAnd);

            if (rightSolved instanceof BoolList) {
                BoolList newList = ((BoolList) rightSolved).ownConstruct();

                for (BoolExp part : ((BoolList) rightSolved).getBoolExps()) {
                    newList.addBoolExp(((BoolImpl) part).split_left(splitOr, splitAnd));
                }

                return newList;
            }
        } else if (left instanceof BoolList) {
            BoolExp leftSolved = split_left(splitOr, splitAnd);

            if (leftSolved instanceof BoolList) {
                BoolList newList = ((BoolList) leftSolved).ownConstruct();

                for (BoolExp part : ((BoolList) leftSolved).getBoolExps()) {
                    newList.addBoolExp(((BoolImpl) part).split_right(splitOr, splitAnd));
                }

                return newList;
            }
        }

        return new BoolImpl(left, right);
    }

    @Nonnull
    @Override
    public BoolExp reduce_spec(@Nonnull Reducer reducer) {
        //morph both sides to DNF
        BoolExp source = _source.makeDNFOr().reduceShallow();
        BoolExp target = _target.makeDNFOr().reduceShallow();

        BoolImpl impl = new BoolImpl(source, target);

        //split in multiple impls, split ors only
        BoolExp split = impl.split(true, false);

        //if no split could be done/no side had ors (not even nested because of DNF)
        if (split instanceof BoolImpl) {
            //source = reduceC_side((BoolExp) ((BoolImpl) split).getSource().copy());
            //target = reduceC_side((BoolExp) ((BoolImpl) split).getTarget().copy());

//            BoolAnd sourceAnd = new BoolAnd(source);
//            BoolAnd targetAnd = new BoolAnd(target);
//
//            Set<Id> ids = targetAnd.findType(Id.class);
//
//            BoolExp last;
//
//            do {
//                last = targetAnd;
//
//                for (Id id : ids) {
//                    BoolExp origin = null;
//                    Exp toReplace = null;
//
//                    for (BoolExp part : sourceAnd.getBoolExps()) {
//                        Exp exp = isolateId((ExpComp) part, id);
//
//                        if (exp != null) {
//                            origin = part;
//                            toReplace = exp;
//
//                            break;
//                        }
//                    }
//
//                    if (toReplace != null) {
//                        List<BoolExp> boolExps = new ArrayList<>();
//
//                        for (BoolExp part : targetAnd.getBoolExps()) {
//                            if (part.equals(origin)) {
//                                boolExps.add(part);
//
//                                continue;
//                            }
//
//                            Exp finalToReplace = toReplace;
//
//                            part = (BoolExp) part.replace(new IOUtil.IdWithParams<SemanticNode, SemanticNode>() {
//                                @Override
//                                public SemanticNode apply(SemanticNode semanticNode) {
//                                    if (semanticNode.equals(id)) {
//                                        return finalToReplace;
//                                    }
//
//                                    return semanticNode;
//                                }
//                            });
//
//                            part = part.reduce();
//                            boolExps.add(part);
//                        }
//
//                        targetAnd = new BoolAnd();
//
//                        for (BoolExp part : boolExps) {
//                            targetAnd.addBoolExp(part);
//                        }
//                    }
//                }
//            } while (!last.equals(targetAnd));
//
//            target = targetAnd.reduce();
//
//            target.order();

            BoolAnd sourceAnd = new BoolAnd(source);
            BoolAnd targetAnd = new BoolAnd(target);

            if (sourceAnd.isPure() && targetAnd.isPure()) {
                List<BoolExp> sourceAndParts = sourceAnd.getBoolExps();
                List<BoolExp> targetAndParts = targetAnd.getBoolExps();
                System.out.println("LEFTRIGHT " + sourceAndParts + ";" + targetAndParts);
                if (sourceAndParts.containsAll(targetAndParts)) {
                    System.out.println("FOUND TRUE");
                    return new BoolLit(true);
                }
            }

            return new BoolOr(new BoolNeg(source), target).reduce(reducer);
        }
        System.out.println("return " + split.getTypeName());
        return split.reduce(reducer);
    }

    @Nonnull
    @Override
    public BoolExp order_spec() {
        BoolExp newSource = _source.order();
        BoolExp newTarget = _target.order();

        return new BoolImpl(newSource, newTarget);
    }

    @Override
    public int comp_spec(BoolExp b) {
        int ret = _source.comp(((BoolImpl) b)._source);

        if (ret != 0) return ret;

        return (_target.comp(((BoolImpl) b)._target));
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        return mapper.apply(this, _source.getContentString(mapper) + StringUtil.bool_impl + _target.getContentString(mapper));
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        BoolExp newSource = (BoolExp) _source.replace(replaceFunc);
        BoolExp newTarget = (BoolExp) _target.replace(replaceFunc);

        return replaceFunc.apply(new BoolImpl(newSource, newTarget));
    }
}