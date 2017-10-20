package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class Prod extends Exp {
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
        if (exp instanceof Prod) {
            for (Exp child : ((Prod) exp).getExps()) {
                addExp(child);
            }
        } else {
            addChild(exp);
        }
    }

    public Prod(Exp... exps) {
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

            if (!(exp instanceof ExpElem) && exp.compPrecedence(this) <= 0) expS = parenthesize(expS);

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

    public @Nonnull ExpLit getLit() {
        ExpLit lit = new ExpLit(1);

        List<Exp> exps = getExps();

        for (Exp exp : exps) {
            if (exp instanceof ExpLit) lit.mult((ExpLit) exp);
        }

        return lit;
    }

    public @Nonnull ExpLit cutLit() {
        ExpLit lit = getLit();

        List<Exp> exps = getExps();

        exps.removeIf(new Predicate<Exp>() {
            @Override
            public boolean test(Exp exp) {
                return (exp instanceof ExpLit);
            }
        });

        _children.clear();

        for (Exp exp : exps) {
            addExp(exp);
        }

        return lit;
    }

    public @Nonnull Exp getCoeff(Set<Id> ids) {
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

    public @Nonnull Exp cutCoeff(Set<Id> ids) {
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
    public Exp reduce() {
        List<Exp> exps = getExps();

        exps.replaceAll(new UnaryOperator<Exp>() {
            @Override
            public Exp apply(Exp exp) {
                return exp.reduce();
            }
        });

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
                            sum2.addExp(new Prod(j, k).reduce());
                        }
                    }

                    sum = sum2.reduce();
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

            if (idMap.containsKey(base)) pow = new Pow(base, new Sum(idMap.get(base).getExponent(), pow.getExponent()).reduce());

            idMap.put(base, pow);
        }

        newProd = new Prod();

        for (Exp exp : idMap.values()) {
            newProd.addExp(exp.reduce());
        }

        //merge lits
        ExpLit lit = new ExpLit(1);

        List<Exp> newExps = newProd.getExps();

        newExps.removeIf(new Predicate<Exp>() {
            @Override
            public boolean test(Exp exp) {
                if (exp instanceof ExpLit) {
                    lit.mult((ExpLit) exp);

                    return true;
                }

                return false;
            }
        });

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
        List<Exp> bExps = ((Prod) b).getExps();

        for (int i = 0;; i++) {
            if (i >= exps.size() && (i >= bExps.size())) return 0;

            if (i >= exps.size()) return 1;
            if (i >= bExps.size()) return -1;

            int localRet = exps.get(i).compPrecedence(bExps.get(i));

            if (localRet != 0) return localRet;
        }
    }
}