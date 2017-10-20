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

public class BoolImpl extends BoolExp {
    private BoolExp _source;
    private BoolExp _target;

    public BoolExp getSource() {
        return _source;
    }

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

    private Exp isolateId(ExpComp expComp, Id id) {
        expComp = (ExpComp) expComp.copy();

        if (!expComp.findType(Id.class).contains(id)) return null;

        //left side=0
        expComp.order();

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

            leftExp = new Sum(leftExp, toSubtract).reduce();
            rightExp = new Sum(rightExp, toSubtract).reduce();

            expComp = new ExpComp(leftExp, compOp, rightExp);

            leftExp.order();

            if (rightExp.equals(id)) return leftExp;

            return null;
        }

        return leftExp;
    }

    /*private BoolOr reduceC_side(BoolExp boolExp) {
        boolExp = boolExp.reduce();

        BoolOr dnf = boolExp.makeDNF();

        BoolOr newOr = new BoolOr();

        for (BoolExp part : dnf.getBoolExps()) {
            //each part is an And at best

            BoolAnd partAnd = new BoolAnd(part);

            part = reduceC_reduceAnd(partAnd);

            newOr.addBoolExp(part);
        }

        return newOr;
    }*/

    private BoolExp splitB() {
        return new BoolOr(new BoolNeg((BoolExp) _source.copy()), (BoolExp) _target.copy()).reduce();
    }

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
                BoolList newList = ((BoolList) leftSolved).invertConstruct();

                for (BoolExp part : ((BoolList) leftSolved).getBoolExps()) {
                    newList.addBoolExp(((BoolImpl) part).split_right(splitOr, splitAnd));
                }

                return newList;
            }
        }

        return new BoolImpl(left, right);
    }

    @Override
    public BoolExp reduce() {
        //morph both sides to DNF
        BoolExp source = _source.makeDNF().reduce();
        BoolExp target = _target.makeDNF().reduce();

        BoolImpl impl = new BoolImpl(source, target);

        //split in multiple impls, split ors only
        BoolExp split = impl.split(true, false);

        //if no split could be done/no side had ors (not even nested because of DNF)
        if (split instanceof BoolImpl) {
            //source = reduceC_side((BoolExp) ((BoolImpl) split).getSource().copy());
            //target = reduceC_side((BoolExp) ((BoolImpl) split).getTarget().copy());

            /*BoolAnd sourceAnd = new BoolAnd(source);
            BoolAnd targetAnd = new BoolAnd(target);

            Set<Id> ids = targetAnd.findType(Id.class);

            BoolExp last;

            do {
                last = targetAnd;

                for (Id id : ids) {
                    BoolExp origin = null;
                    Exp toReplace = null;

                    for (BoolExp part : sourceAnd.getBoolExps()) {
                        Exp exp = isolateId((ExpComp) part, id);

                        if (exp != null) {
                            origin = part;
                            toReplace = exp;

                            break;
                        }
                    }

                    if (toReplace != null) {
                        List<BoolExp> boolExps = new ArrayList<>();

                        for (BoolExp part : targetAnd.getBoolExps()) {
                            if (part.equals(origin)) {
                                boolExps.add(part);

                                continue;
                            }

                            Exp finalToReplace = toReplace;

                            part = (BoolExp) part.replace(new IOUtil.Func<SemanticNode, SemanticNode>() {
                                @Override
                                public SemanticNode apply(SemanticNode semanticNode) {
                                    if (semanticNode.equals(id)) {
                                        return finalToReplace;
                                    }

                                    return semanticNode;
                                }
                            });

                            part = part.reduce();
                            boolExps.add(part);
                        }

                        targetAnd = new BoolAnd();

                        for (BoolExp part : boolExps) {
                            targetAnd.addBoolExp(part);
                        }
                    }
                }
            } while (!last.equals(targetAnd));

            target = targetAnd.reduce();

            target.order();*/

            BoolExp ret = new BoolOr(new BoolNeg(source), target).reduce();

            return ret;
        }

        return split.reduce();
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