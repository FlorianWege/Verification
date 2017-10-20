package core.structures.semantics.boolExp;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.exp.Exp;
import core.structures.semantics.exp.Sum;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.util.*;

public class BoolOr extends BoolList {
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
        if (boolExp instanceof BoolOr) {
            for (BoolExp child : ((BoolOr) boolExp).getBoolExps()) {
                addBoolExp(child);
            }
        } else {
            addChild(boolExp);
        }
    }

    public BoolOr(BoolExp... boolExps) {
        for (BoolExp boolExp : boolExps) {
            addBoolExp(boolExp);
        }
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        StringBuilder sb = new StringBuilder();

        for (BoolExp boolExp : getBoolExps()) {
            if (sb.length() > 0) sb.append(_grammar.TERMINAL_OP_OR.getPrimRule());

            String expS = boolExp.getContentString(mapper);

            if (!(boolExp instanceof BoolElem) && boolExp.compPrecedence(this) < 0) {
                expS = parenthesize(expS);
            }

            sb.append(expS);
        }

        return mapper.apply(this, sb.toString());
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        replaceChildren(replaceFunc);

        return replaceFunc.apply(this);
    }

    @Override
    public BoolExp reduce() {
        BoolOr copy = (BoolOr) copy();

        boolean containsAnd = false;

        for (BoolExp part : copy.getBoolExps()) {
            if (part instanceof BoolAnd) containsAnd = true;
        }

        if (containsAnd) {
            BoolAnd cnf = copy.makeCNF();

            System.out.println("cnf " + cnf);

            copy = cnf.reduce().makeDNF();

            System.out.println("DNF is " + copy);
        }

        //reduce parts and unwrap nested
        BoolOr tmpOr = new BoolOr();

        for (BoolExp boolExp : copy.getBoolExps()) {
            tmpOr.addBoolExp(boolExp.reduce());
        }

        //idempotency
        Set<BoolExp> boolExps = new LinkedHashSet<>(tmpOr.getBoolExps());

        //search true
        boolean hasTrue = false;

        for (BoolExp boolExp : boolExps) {
            if (boolExp instanceof BoolLit && ((BoolLit) boolExp).getVal()) hasTrue = true;
        }

        //true found, nothing to do
        if (hasTrue) return new BoolLit(true);
        System.out.println(boolExps);
        //search for complements
        for (BoolExp boolExp : boolExps) {
            boolExp = (BoolExp) boolExp.copy();

            if (boolExp instanceof ExpComp && ((ExpComp) boolExp).getExpOp().getType().equals(ExpCompOp.Type.EQUAL)) {
                Exp leftExp = ((ExpComp) boolExp).getLeftExp();
                Exp rightExp = ((ExpComp) boolExp).getRightExp();

                if (leftExp instanceof Sum) ((Sum) leftExp).cleanMu();
                if (rightExp instanceof Sum) ((Sum) rightExp).cleanMu();

                BoolExp reducedBoolExp = new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.UNEQUAL), rightExp).reduce();
                BoolExp reducedBoolExp2 = new ExpComp(leftExp, new ExpCompOp(ExpCompOp.Type.EQUAL), rightExp).reduce();

                reducedBoolExp = new BoolOr(reducedBoolExp);
                reducedBoolExp2 = new BoolOr(reducedBoolExp2);

                List<BoolExp> parts = ((BoolOr) reducedBoolExp).getBoolExps();
                List<BoolExp> parts2 = ((BoolOr) reducedBoolExp2).getBoolExps();

                //all parts of reduction contained?
                if (boolExps.containsAll(parts) && boolExps.containsAll(parts2)) {
                    return new BoolLit(true);
                }
            }
        }

        List<BoolExp> newBoolExps = new ArrayList<>();

        //avoid unnecessary falses
        for (BoolExp boolExp : boolExps) {
            if (boolExp instanceof BoolLit) continue;

            newBoolExps.add(boolExp);
        }

        //complete reduction?
        if (newBoolExps.isEmpty()) return new BoolLit(false);
        if (newBoolExps.size() == 1) return newBoolExps.get(0);

        //reassemble
        BoolOr ret = new BoolOr();

        for (BoolExp boolExp : newBoolExps) {
            ret.addChild(boolExp);
        }

        /*BoolOr dnf = ret;
        System.out.println("ret " + dnf);

        BoolAnd cnf = dnf.makeCNF();

        System.out.println("cnf " + cnf);

        dnf = cnf.makeDNF();

        System.out.println("dnf " + dnf);*/

        /*if (!(dnf.equals(ret))) {


            return dnf.reduce();
        }*/

        return ret;
    }

    @Override
    public void order() {
        List<BoolExp> boolExps = getBoolExps();

        for (BoolExp boolExp : boolExps) {
            boolExp.order();
        }

        _children.clear();

        boolExps.sort(new Comparator<BoolExp>() {
            @Override
            public int compare(BoolExp a, BoolExp b) {
                return a.compPrecedence(b);
            }
        });

        for (BoolExp boolExp : boolExps) {
            addChild(boolExp);
        }
    }

    @Override
    public int comp(BoolExp b) {
        List<BoolExp> boolExps = getBoolExps();
        List<BoolExp> bBoolExps = ((BoolOr) b).getBoolExps();

        for (int i = 0;; i++) {
            if (i >= boolExps.size() && (i >= bBoolExps.size())) return 0;

            if (i >= boolExps.size()) return 1;
            if (i >= bBoolExps.size()) return -1;

            int localRet = boolExps.get(i).compPrecedence(bBoolExps.get(i));

            if (localRet != 0) return localRet;
        }
    }
}