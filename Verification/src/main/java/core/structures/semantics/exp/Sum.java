package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;
import util.Util;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class Sum extends Exp {
    @Nonnull
    public List<Exp> getExps() {
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

    public Sum(@Nonnull Exp... exps) {
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

    public void cleanMu(@Nonnull Reducer reducer) {
        _children.removeIf(new Predicate<SemanticNode>() {
            @Override
            public boolean test(SemanticNode semanticNode) {
                if (semanticNode instanceof ExpMu) return true;

                if (semanticNode instanceof Prod) {
                    Prod prod = (Prod) semanticNode.copy();

                    Exp exp = prod.cutLit().reduce(reducer);

                    if (exp instanceof ExpMu) return true;
                }

                return false;
            }
        });
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        StringBuilder sb = new StringBuilder();

        for (Exp exp : getExps()) {
            boolean isNeg = false;

            if (exp instanceof ExpLit && ((ExpLit) exp).isNeg()) {
                exp = ((ExpLit) exp).neg();

                isNeg = true;
            }

            if (exp instanceof Prod) {
                ExpLit lit = ((Prod) exp).getLit();

                exp = ((Prod) exp).cutLit();

                if (lit.isNeg()) {
                    lit = lit.neg();

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

            if (!(exp instanceof ExpElem) && exp.comp(this) < 0) {
                expS = parenthesize(expS);
            }

            sb.append(expS);
        }

        return mapper.apply(this, sb.toString());
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        Sum ret = new Sum();

        for (Exp exp : getExps()) {
            ret.addExp((Exp) exp.replace(replaceFunc));
        }

        return replaceFunc.apply(ret);
    }

    /*public Exp factorize() {
        List<Exp> exps = getExps();

        //factorize (no zeroes)
        exps.removeIf(new Predicate<Exp>() {
            @Override
            public boolean test(Exp exp) {
                return exp.equals(new ExpLit(0));
            }
        });

        if (exps.isEmpty()) return new ExpLit(0);

        Set<Exp> sharedFactors = new LinkedHashSet<>();

        Prod firstProd = new Prod(exps.get(0));

        ExpLit lit = firstProd.getLit();

        firstProd = new Prod(firstProd.cutLit());

        sharedFactors.addAll(firstProd.getExps());

        for (Exp exp : exps) {
            Prod prod = new Prod(exp);


        }
    }*/

    @Override
    @Nonnull
    public Exp reduce_spec(@Nonnull Reducer reducer) {
        List<Exp> exps = getExps();

        exps.replaceAll(new UnaryOperator<Exp>() {
            @Override
            public Exp apply(Exp exp) {
                return exp.reduce(reducer);
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

        //
        Sum newSum = new Sum();

        for (Exp exp : exps) {
            newSum.addExp(exp);
        }

        //factorize doubly occurences
        Map<Exp, Prod> idMap = new LinkedHashMap<>();

        for (Exp exp : newSum.getExps()) {
            Prod prod = !(exp instanceof Prod) ? new Prod(exp) : (Prod) exp;

            ExpLit lit = prod.getLit();
            exp = prod.cutLit();

            Exp key = exp.order();

            if (!idMap.containsKey(key)) idMap.put(key, new Prod(new ExpLit(0)));

            ExpLit prev = idMap.get(key).getLit();

            lit = lit.add(prev);

            Prod newProd = new Prod(exp);

            newProd.addExp(lit);

            idMap.put(key, newProd);
        }

        newSum = new Sum();

        for (Exp exp : idMap.values()) {
            newSum.addExp(exp.reduce(reducer));
        }

        //merge lits
        Util.Wrap<ExpLit> litWrap = new Util.Wrap<>(new ExpLit(0));

        List<Exp> newExps = newSum.getExps();

        newExps.removeIf(new Predicate<Exp>() {
            @Override
            public boolean test(Exp exp) {
                if (exp instanceof ExpLit) {
                    litWrap.set(litWrap.get().add((ExpLit) exp));

                    return true;
                }

                return false;
            }
        });

        ExpLit lit = litWrap.get();

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
    @Nonnull
    public Exp order_spec() {
        List<Exp> newExps = new ArrayList<>();

        for (Exp exp : getExps()) {
            newExps.add(exp.order());
        }

        newExps.sort(new Comparator<Exp>() {
            @Override
            public int compare(Exp a, Exp b) {
                return a.comp(b);
            }
        });

        Sum ret = new Sum();

        for (Exp exp : newExps) {
            ret.addChild(exp);
        }

        return ret;
    }

    @Override
    public int comp_spec(@Nonnull Exp b) {
        List<Exp> exps = getExps();
        List<Exp> bExps = ((Sum) b).getExps();

        for (int i = 0;; i++) {
            if (i >= exps.size() && (i >= bExps.size())) return 0;

            if (i >= exps.size()) return 1;
            if (i >= bExps.size()) return -1;

            int localRet = exps.get(i).comp(bExps.get(i));

            if (localRet != 0) return localRet;
        }
    }
}