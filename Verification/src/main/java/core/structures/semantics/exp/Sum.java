package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class Sum extends Exp {
    public @Nonnull List<Exp> getExps() {
        List<Exp> ret = new ArrayList<>();

        for (SemanticNode child : getChildren()) {
            if (child instanceof Exp) {
                ret.add((Exp) child);
            }
        }

        return ret;
    }

    public void addExp(@Nonnull Exp exp) {
        if (exp instanceof Sum) {
            for (Exp child : ((Sum) exp).getExps()) {
                addExp(child);
            }
        } else {
            addChild(exp);
        }
    }

    public Sum(Exp... exps) {
        for (Exp exp : exps) {
            addExp(exp);
        }
    }

    public Sum() {
    }

    public void neg() {
        List<Exp> exps = getExps();

        _children.clear();

        for (Exp exp : exps) {
            addExp(exp.makeNeg());
        }
    }

    public void cleanMu() {
        _children.removeIf(new Predicate<SemanticNode>() {
            @Override
            public boolean test(SemanticNode semanticNode) {
                if (semanticNode instanceof ExpMu) return true;

                if (semanticNode instanceof Prod) {
                    Prod prod = (Prod) semanticNode.copy();

                    prod.cutLit();

                    if (prod.reduce() instanceof ExpMu) return true;
                }

                return false;
            }
        });
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        StringBuilder sb = new StringBuilder();

        for (Exp exp : getExps()) {
            exp = (Exp) exp.copy();

            boolean isNeg = false;

            if (exp instanceof ExpLit && ((ExpLit) exp).isNeg()) {
                ((ExpLit) exp).neg();

                isNeg = true;
            }

            if (exp instanceof Prod) {
                ExpLit lit = ((Prod) exp).cutLit();

                if (lit.isNeg()) {
                    lit.neg();

                    isNeg = true;
                }

                if (!lit.equals(1)) exp = new Prod(lit, exp);
            }

            if (isNeg) {
                sb.append(_grammar.TERMINAL_OP_MINUS.getPrimRule());
            } else {
                if (sb.length() > 0) sb.append(_grammar.TERMINAL_OP_PLUS.getPrimRule());
            }

            String expS = exp.getContentString(mapper);

            if (!(exp instanceof ExpElem) && exp.compPrecedence(this) < 0) {
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
    public Exp reduce() {
        List<Exp> exps = getExps();

        exps.replaceAll(new UnaryOperator<Exp>() {
            @Override
            public Exp apply(Exp exp) {
                return exp.reduce();
            }
        });

        /*//distributivity
        Prod newSum = new Prod();

        if (!exps.isEmpty()) {
            Exp sum = exps.get(0);

            for (int i = 0; i < exps.size() - 1; i++) {
                Exp a = sum;
                Exp b = exps.get(i + 1);

                if (!(a instanceof Sum) && !(b instanceof Sum)) sum = new Prod(a, b);
                else {
                    if (!(a instanceof Sum)) a = new Sum(a);
                    if (!(b instanceof Sum)) b = new Sum(b);

                    Sum sum2 = new Sum();

                    for (Exp j : ((Sum) a).getExps()) {
                        for (Exp k : ((Sum) b).getExps()) {
                            sum2.addExp(new Prod(j, k).reduce());
                        }
                    }

                    sum = sum2.reduce();
                }
            }

            print("sum is " + sum.getContentString());

            newSum.addExp(sum);
        }*/

        Sum newSum = new Sum();

        for (Exp exp : exps) {
            newSum.addExp(exp);
        }

        //factorize doubly occurences
        Map<Exp, Prod> idMap = new LinkedHashMap<>();

        for (Exp exp : newSum.getExps()) {
            Prod prod = !(exp instanceof Prod) ? new Prod(exp) : (Prod) exp;

            ExpLit lit = prod.cutLit();

            Exp key = (Exp) prod.copy();

            key.order();

            if (idMap.containsKey(key)) {
                ExpLit prev = idMap.get(key).cutLit();

                lit.add(prev);
            }

            Prod newProd = (Prod) prod.copy();

            newProd.addExp(lit);

            idMap.put(key, newProd);
        }

        newSum = new Sum();

        for (Exp exp : idMap.values()) {
            newSum.addExp(exp.reduce());
        }

        //merge lits
        ExpLit lit = new ExpLit(0);

        List<Exp> newExps = newSum.getExps();

        newExps.removeIf(new Predicate<Exp>() {
            @Override
            public boolean test(Exp exp) {
                if (exp instanceof ExpLit) {
                    lit.add((ExpLit) exp);

                    return true;
                }

                return false;
            }
        });

        if (!lit.getNum().equals(BigInteger.ZERO)) newExps.add(lit);

        //rewrap + return
        newSum = new Sum();

        for (Exp exp : newExps) {
            newSum.addExp(exp);
        }

        if (newSum.getExps().isEmpty()) return new ExpLit(0);

        if (newSum.getExps().size() == 1) return newSum.getExps().get(0);

        return newSum;
    }

    @Override
    public void order() {
        List<Exp> exps = getExps();

        for (Exp exp : exps) {
            exp.order();
        }

        _children.clear();

        exps.sort(new Comparator<Exp>() {
            @Override
            public int compare(Exp a, Exp b) {
                return a.compPrecedence(b);
            }
        });

        for (Exp exp : exps) {
            addChild(exp);
        }
    }

    @Override
    public int comp(Exp b) {
        List<Exp> exps = getExps();
        List<Exp> bExps = ((Sum) b).getExps();

        for (int i = 0;; i++) {
            if (i >= exps.size() && (i >= bExps.size())) return 0;

            if (i >= exps.size()) return 1;
            if (i >= bExps.size()) return -1;

            int localRet = exps.get(i).compPrecedence(bExps.get(i));

            if (localRet != 0) return localRet;
        }
    }
}