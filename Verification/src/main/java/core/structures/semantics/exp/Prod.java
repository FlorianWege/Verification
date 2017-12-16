package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;
import util.Util;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class Prod extends Exp {
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
        if (exp instanceof Prod) {
            for (Exp child : ((Prod) exp).getExps()) {
                addExp(child);
            }
        } else {
            addChild(exp);
        }
    }

    public Prod(@Nonnull Exp... exps) {
        for (Exp exp : exps) {
            addExp(exp);
        }
    }

    public Prod() {

    }

    public void neg() {
        addExp(new ExpLit(-1));
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        StringBuilder sb = new StringBuilder();

        for (Exp exp : getExps()) {
            if (sb.length() > 0) sb.append(_grammar.TERMINAL_OP_MULT.getPrimRule());

            String expS = exp.getContentString(mapper);

            if (!(exp instanceof ExpElem) && exp.comp(this) <= 0) expS = parenthesize(expS);

            sb.append(expS);
        }

        return mapper.apply(this, sb.toString());
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        Prod ret = new Prod();

        for (Exp exp : getExps()) {
            ret.addExp((Exp) exp.replace(replaceFunc));
        }

        return replaceFunc.apply(ret);
    }

    @Nonnull
    public ExpLit getLit() {
        ExpLit lit = new ExpLit(1);

        for (Exp exp : getExps()) {
            if (exp instanceof ExpLit) lit = lit.mult((ExpLit) exp);
        }

        return lit;
    }

    @Nonnull
    public Exp cutLit() {
        List<Exp> newExps = getExps();

        newExps.removeIf(new Predicate<Exp>() {
            @Override
            public boolean test(Exp exp) {
                return (exp instanceof ExpLit);
            }
        });

        if (newExps.isEmpty()) return new ExpLit(1);
        if (newExps.size() == 1) return newExps.get(0);

        Prod ret = new Prod();

        for (Exp exp : newExps) {
            ret.addExp(exp);
        }

        return ret;
    }

    @Nonnull
    public Exp getCoeff(@Nonnull Set<Id> ids) {
        Prod coeff = new Prod();

        for (Exp part : getExps()) {
            if (!part.findType(Id.class).containsAll(ids)) {
                coeff.addExp(part);
            }
        }

        if (coeff.getExps().isEmpty()) return new ExpLit(1);
        if (coeff.getExps().size() == 1) return coeff.getExps().get(0);

        return coeff;
    }

    @Nonnull
    public Exp cutCoeff(@Nonnull Set<Id> ids) {
        Exp coeff = getCoeff(ids);

        List<Exp> exps = getExps();

        exps.removeIf(new Predicate<Exp>() {
            @Override
            public boolean test(Exp exp) {
                return !exp.findType(Id.class).containsAll(ids);
            }
        });

        _children.clear();

        for (Exp exp : exps) {
            addExp(exp);
        }

        return coeff;
    }

    @Override
    @Nonnull
    public Exp reduce_spec(@Nonnull Reducer reducer) {
        List<Exp> exps = getExps();

        //reduce operands
        exps.replaceAll(new UnaryOperator<Exp>() {
            @Override
            public Exp apply(Exp exp) {
                return exp.reduce(reducer);
            }
        });

        //if there is a zero, the whole prod becomes zero
        for (Exp exp : exps) {
            if (exp instanceof ExpLit && exp.equals(new ExpLit(0))) return new ExpLit(0);
        }

        //distributivity
        Prod newProd = new Prod();

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
                            sum2.addExp(new Prod(j, k).reduce(reducer));
                        }
                    }

                    sum = sum2.reduce(reducer);
                }
            }

            newProd.addExp(sum);
        }

        //if (!newLit.getNum().equals(newLit.getDenom())) newExps.add(newLit);

        //potentiate doubly occurences
        Map<Exp, Pow> idMap = new LinkedHashMap<>();

        for (Exp exp : newProd.getExps()) {
            Pow pow = !(exp instanceof Pow) ? new Pow(exp, new ExpLit(1)) : (Pow) exp;

            Exp base = pow.getBase();

            if (idMap.containsKey(base)) pow = new Pow(base, new Sum(idMap.get(base).getExponent(), pow.getExponent()).reduce(reducer));

            idMap.put(base, pow);
        }

        newProd = new Prod();

        for (Exp exp : idMap.values()) {
            newProd.addExp(exp.reduce(reducer));
        }

        //merge lits
        Util.Wrap<ExpLit> litWrap = new Util.Wrap<>(new ExpLit(1));

        List<Exp> newExps = newProd.getExps();

        newExps.removeIf(new Predicate<Exp>() {
            @Override
            public boolean test(Exp exp) {
                if (exp instanceof ExpLit) {
                    litWrap.set(litWrap.get().mult((ExpLit) exp));

                    return true;
                }

                return false;
            }
        });

        ExpLit lit = litWrap.get();

        if (!lit.getNum().equals(lit.getDenom())) newExps.add(lit);

        //rewrap + return
        newProd = new Prod();

        for (Exp exp : newExps) {
            newProd.addExp(exp);
        }

        if (newProd.getExps().isEmpty()) return new ExpLit(1);

        if (newProd.getExps().size() == 1) return newProd.getExps().get(0);

        return newProd;
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

        Prod ret = new Prod();

        for (Exp exp : newExps) {
            ret.addChild(exp);
        }

        return ret;
    }

    @Override
    public int comp_spec(@Nonnull Exp b) {
        List<Exp> exps = getExps();
        List<Exp> bExps = ((Prod) b).getExps();

        for (int i = 0;; i++) {
            if (i >= exps.size() && (i >= bExps.size())) return 0;

            if (i >= exps.size()) return 1;
            if (i >= bExps.size()) return -1;

            int localRet = exps.get(i).comp(bExps.get(i));

            if (localRet != 0) return localRet;
        }
    }
}