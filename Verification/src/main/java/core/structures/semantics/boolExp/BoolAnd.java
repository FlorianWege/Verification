package core.structures.semantics.boolExp;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.exp.Exp;
import core.structures.semantics.exp.Id;
import core.structures.semantics.exp.Sum;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.util.*;

public class BoolAnd extends BoolList {
    @Override
    public @Nonnull List<BoolExp> getBoolExps() {
        List<BoolExp> ret = new ArrayList<>();

        for (SemanticNode child : getChildren()) {
            if (child instanceof BoolExp) ret.add((BoolExp) child);
        }

        return ret;
    }

    @Override
    public void addBoolExp(@Nonnull BoolExp boolExp) {
        if (boolExp instanceof BoolAnd) {
            for (BoolExp child : ((BoolAnd) boolExp).getBoolExps()) {
                addBoolExp(child);
            }
        } else {
            addChild(boolExp);
        }
    }

    public BoolAnd(BoolExp... boolExps) {
        for (BoolExp boolExp : boolExps) {
            addBoolExp(boolExp);
        }
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        StringBuilder sb = new StringBuilder();

        for (BoolExp boolExp : getBoolExps()) {
            if (sb.length() > 0) sb.append(_grammar.TERMINAL_OP_AND.getPrimRule());

            String expS = boolExp.getContentString(mapper);

            if (!(boolExp instanceof BoolElem) && boolExp.comp(this) < 0) {
                expS = parenthesize(expS);
            }

            sb.append(expS);
        }

        return mapper.apply(this, sb.toString());
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        BoolAnd ret = new BoolAnd();

        for (BoolExp boolExp : getBoolExps()) {
            ret.addBoolExp((BoolExp) boolExp.replace(replaceFunc));
        }

        return replaceFunc.apply(ret);
    }

    private Exp isolateId(@Nonnull BoolExp boolExp, @Nonnull Id id, @Nonnull Reducer reducer) {
        if (!(boolExp instanceof ExpComp)) return null;

        ExpComp expComp = (ExpComp) boolExp.copy();

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

            leftExp = new Sum(leftExp, toSubtract).reduce(new Exp.Reducer(leftExp));
            rightExp = new Sum(rightExp, toSubtract).reduce(new Exp.Reducer(rightExp));

            //expComp = new ExpComp(leftExp, compOp, rightExp);

            leftExp = leftExp.order();

            if (rightExp.equals(id)) return leftExp;

            return null;
        }

        return leftExp;
    }

    private BoolExp reduce_substitute(@Nonnull Reducer reducer) {
        if (!isPure()) return this;

        System.out.println("SUBSTITUTE " + this);

        Set<Id> ids = findType(Id.class);

        BoolAnd cur = (BoolAnd) copy();
        BoolAnd last;

        do {
            last = cur;

            for (Id id : ids) {
                BoolExp source = null;
                Exp toReplace = null;

                for (BoolExp part : cur.getBoolExps()) {
                    Exp exp = isolateId(part, id, reducer);

                    if (exp != null) {
                        System.out.println("isolated " + id + "->" + exp);
                        source = part;
                        toReplace = exp;

                        break;
                    }
                }

                if (toReplace != null) {
                    List<BoolExp> boolExps = new ArrayList<>();

                    for (BoolExp part : cur.getBoolExps()) {
                        if (part.equals(source)) {
                            boolExps.add(part);

                            continue;
                        }

                        Exp finalToReplace = toReplace;

                        part = (BoolExp) part.replace(new IOUtil.Func<SemanticNode, SemanticNode>() {
                            @Override
                            public SemanticNode apply(SemanticNode semanticNode) {
                                if (semanticNode.equals(id)) {
                                    System.out.println("replace " + semanticNode + " by " + finalToReplace);
                                    return finalToReplace;
                                }

                                return semanticNode;
                            }
                        });

                        part = part.reduce(reducer);

                        boolExps.add(part);
                    }

                    cur = new BoolAnd();

                    for (BoolExp part : boolExps) {
                        System.out.println("part " + part);
                        cur.addBoolExp(part);
                    }

                    System.out.println("cur " + cur);
                }
            }

            System.out.println("new " + cur);
        } while (!last.equals(cur));

        System.out.println("SUBSTITEND " + cur);

        if (!cur.equals(this)) {
            return cur.reduce(reducer);
        }

        return cur;
    }

    @Nonnull
    @Override
    public BoolExp reduce_spec(@Nonnull Reducer reducer) {
        if (getBoolExps().size() == 1) {
            BoolExp ret = getBoolExps().get(0);

            reducer.addEntry(ret, Reducer.Law.IDEMPOTENCY);

            return ret.reduce(reducer);
        }

        //reduce parts and unwrap nested
        BoolAnd tmpAnd = new BoolAnd();

        for (BoolExp boolExp : getBoolExps()) {
            tmpAnd.addBoolExp(boolExp.reduce(reducer));
        }

        //substitute
        if (isPure()) {
            BoolExp substituted = tmpAnd.reduce_substitute(reducer);

            if (!(substituted instanceof BoolAnd)) return substituted.reduce(reducer);

            BoolAnd substitutedAnd = (BoolAnd) substituted;

            tmpAnd = substitutedAnd;
        }

        //idempotency
        Set<BoolExp> boolExps = new LinkedHashSet<>(tmpAnd.getBoolExps());

        boolean hasFalse = false;

        for (BoolExp boolExp : boolExps) {
            if (boolExp instanceof BoolLit && !((BoolLit) boolExp).getVal()) hasFalse = true;
        }

        if (hasFalse) return new BoolLit(false);

        //search for complements
        for (BoolExp boolExp : boolExps) {
            boolExp = (BoolExp) boolExp.copy();

            if (boolExp instanceof ExpComp && ((ExpComp) boolExp).getExpOp().getType().equals(ExpCompOp.Type.EQUAL)) {
                Exp leftExp = ((ExpComp) boolExp).getLeftExp();
                Exp rightExp = ((ExpComp) boolExp).getRightExp();

                if (leftExp instanceof Sum) ((Sum) leftExp).cleanMu(new Exp.Reducer(leftExp));
                if (rightExp instanceof Sum) ((Sum) rightExp).cleanMu(new Exp.Reducer(rightExp));

                BoolExp reducedBoolExp = new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.UNEQUAL), rightExp).reduce(reducer);

                if (reducedBoolExp instanceof BoolOr) {
                    List<BoolExp> parts = ((BoolOr) reducedBoolExp).getBoolExps();

                    //all parts of reduction contained?
                    if (boolExps.containsAll(parts)) return new BoolLit(false);
                }
            }
        }

        List<BoolExp> newBoolExps = new ArrayList<>();

        //avoid unnecessary trues
        for (BoolExp boolExp : boolExps) {
            if (boolExp instanceof BoolLit) continue;

            newBoolExps.add(boolExp);
        }

        //complete reduction?
        if (newBoolExps.isEmpty()) return new BoolLit(true);
        if (newBoolExps.size() == 1) return newBoolExps.get(0);

        //reassemble
        BoolAnd ret = new BoolAnd();

        for (BoolExp boolExp : newBoolExps) {
            ret.addChild(boolExp);
        }

        BoolOr dnf = ret.makeDNFOr();

        if (dnf.getBoolExps().size() > 1) return dnf.reduce(reducer);

        return ret;
    }

//    public BoolExp reduce_CNF() {
//        BoolExp reduced = reduce();
//
//        reduced.order();
//
//        if (!(reduced instanceof BoolAnd)) return reduced;
//
//        BoolAnd reducedAnd = (BoolAnd) reduced;
//
//        for (BoolExp boolExp : getBoolExps()) {
//            if (!(boolExp instanceof ExpComp)) return reduced;
//        }
//
//        //nothing but ExpComps
//
//        Set<Id> ids = findType(Id.class);
//        Map<BoolExp, Set<Id>> idMap = new LinkedHashMap<>();
//
//        for (BoolExp part : getBoolExps()) {
//            idMap.put(part, part.findType(Id.class));
//        }
//
//        BoolExp last;
//        System.out.println("BEFORE " + reducedAnd);
//        do {
//            last = reducedAnd;
//
//            for (Id id : ids) {
//                BoolExp source = null;
//                Exp toReplace = null;
//
//                for (BoolExp part : reducedAnd.getBoolExps()) {
//                    Exp exp = isolateId((ExpComp) part, id);
//
//                    if (exp != null) {
//                        System.out.println("isolated " + id + "->" + exp);
//                        source = part;
//                        toReplace = exp;
//
//                        break;
//                    }
//                }
//
//                if (toReplace != null) {
//                    List<BoolExp> boolExps = new ArrayList<>();
//
//                    for (BoolExp part : reducedAnd.getBoolExps()) {
//                        System.out.println("source is " + source);
//
//                        if (part.equals(source)) {
//                            boolExps.add(part);
//
//                            continue;
//                        }
//
//                        System.out.println("in " + part + " replace " + id + " by " + toReplace);
//
//                        Exp finalToReplace = toReplace;
//
//                        part = (BoolExp) part.replace(new IOUtil.IdWithParams<SemanticNode, SemanticNode>() {
//                            @Override
//                            public SemanticNode apply(SemanticNode semanticNode) {
//                                if (semanticNode.equals(id)) {
//                                    System.out.println("replace " + semanticNode + " by " + finalToReplace);
//                                    return finalToReplace;
//                                }
//
//                                return semanticNode;
//                            }
//                        });
//
//                        part = part.reduce();
//                        System.out.println("new part " + part);
//                        boolExps.add(part);
//                    }
//
//                    reducedAnd = new BoolAnd();
//
//                    for (BoolExp boolExp : boolExps) {
//                        reducedAnd.addBoolExp(boolExp);
//                    }
//                }
//            }
//        } while (!last.equals(reducedAnd));
//
//        reduced = reducedAnd.reduce();
//
//        reduced.order();
//        System.out.println("return " + reduced);
//        return reduced;
//    }

    @Nonnull
    @Override
    public BoolExp order_spec() {
        List<BoolExp> newBoolExps = new ArrayList<>();

        for (BoolExp boolExp : getBoolExps()) {
            newBoolExps.add(boolExp.order());
        }

        newBoolExps.sort(new Comparator<BoolExp>() {
            @Override
            public int compare(BoolExp a, BoolExp b) {
                return a.comp(b);
            }
        });

        BoolAnd ret = new BoolAnd();

        for (BoolExp boolExp : newBoolExps) {
            ret.addChild(boolExp);
        }

        return ret;
    }

    @Override
    public int comp_spec(BoolExp b) {
        List<BoolExp> boolExps = getBoolExps();
        List<BoolExp> bBoolExps = ((BoolAnd) b).getBoolExps();

        for (int i = 0;; i++) {
            if (i >= boolExps.size() && (i >= bBoolExps.size())) return 0;

            if (i >= boolExps.size()) return 1;
            if (i >= bBoolExps.size()) return -1;

            int localRet = boolExps.get(i).comp(bBoolExps.get(i));

            if (localRet != 0) return localRet;
        }
    }
}