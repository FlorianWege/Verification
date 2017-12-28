package core.structures.semantics.exp;

import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import grammars.ExpGrammar;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class Exp extends SemanticNode {
    @CheckReturnValue
    @Nonnull
    public Exp makeNeg() {
        Prod ret = new Prod(this);

        ret.neg();

        return ret;
    }

    @CheckReturnValue
    @Nonnull
    public Exp makeInv() {
        return new Pow(this, new ExpLit(-1));
    }

    @CheckReturnValue
    @Nonnull
    public abstract Exp reduce_spec(@Nonnull Reducer reducer);

    @CheckReturnValue
    @Nonnull
    public final Exp reduce(@Nullable Reducer reducer) {
        if (reducer == null) reducer = new Reducer(this);

        Exp ret = reduce_spec(reducer);

        reducer.addEntry(this, Reducer.Law.UNKNOWN);

        return ret;
    }

    @CheckReturnValue
    @Nonnull
    public final Exp reduce() {
        return reduce(null);
    }

    public static class Reducer implements Serializable {
        public enum Law {
            START,
            APPLY_GCD,

            UNKNOWN
        }

        private class Entry implements Serializable {
            private Exp _exp;
            private Exp.Reducer.Law _law;

            public Exp getExp() {
                return _exp;
            }

            public Exp.Reducer.Law getLaw() {
                return _law;
            }

            public Entry(@Nonnull Exp exp, @Nonnull Exp.Reducer.Law law) {
                _exp = exp;
                _law = law;
            }
        }

        private List<Exp.Reducer.Entry> _entries = new LinkedList<>();
        private Exp _ret = null;

        @Nonnull
        public List<Exp.Reducer.Entry> getEntries() {
            return _entries;
        }

        @Nonnull
        public Exp getRet() {
            return _ret;
        }

        public void addEntry(@Nonnull Exp exp, @Nonnull Exp.Reducer.Law law) {
            if (_entries.isEmpty() || !_entries.get(_entries.size() - 1).getExp().getContentString().equals(exp.getContentString())) {
                _entries.add(new Exp.Reducer.Entry(exp, law));

            }

            _ret = exp;
        }

        public Reducer(@Nonnull Exp exp) {
            _entries.add(new Exp.Reducer.Entry(exp, Law.START));
        }
    }

    @Nonnull
    public final Reducer reduceEx() {
        Reducer ret = new Reducer(this);

        Exp exp = reduce(ret);

        return ret;
    }

    @CheckReturnValue
    @Nonnull
    public abstract Exp order_spec();

    @CheckReturnValue
    @Nonnull
    public final Exp order() {
        return ((Exp) copy()).order_spec();
    }

    public abstract int comp_spec(@Nonnull Exp b);

    public final int comp(@Nonnull Exp b) {
        List<Class<? extends Exp>> types = new ArrayList<>();

        types.add(ExpMu.class);
        types.add(ExpLit.class);
        types.add(Id.class);
        types.add(Sum.class);
        types.add(Prod.class);
        types.add(Pow.class);
        types.add(Fact.class);

        if (types.indexOf(getClass()) < types.indexOf(b.getClass())) return -1;
        if (types.indexOf(getClass()) > types.indexOf(b.getClass())) return 1;

        return comp_spec(b);
    }

    @Nonnull
    public Exp getMuCoeff() {
        if (this instanceof Prod) {
            //overwritten
        }
        if (this instanceof Sum) {
            Sum ret = new Sum();

            for (Exp exp : ((Sum) this).getExps()) {
                ret.addExp(exp.getMuCoeff());
            }

            return ret.reduce();
        }
        if (this instanceof ExpMu) {
            return new ExpLit(1);
        }

        return new ExpLit(0);
    }

    @Nonnull
    public Exp cutMuCoeff() {
        System.out.println("cut " + this);
        if (this instanceof Prod) {
            if (!getMuCoeff().equals(new ExpLit(0))) return new ExpLit(0);
        }
        if (this instanceof Sum) {
            Sum ret = new Sum();

            for (Exp exp : ((Sum) this).getExps()) {
                if (exp.getMuCoeff().equals(new ExpLit(0))) {
                    ret.addExp(exp);
                }
            }

            return ret.reduce();
        }
        if (this instanceof ExpMu) {
            return new ExpLit(0);
        }

        return (Exp) copy();
    }

    public String parenthesize(String s) {
        return _grammar.TERMINAL_PAREN_OPEN.getPrimRule() + s + _grammar.TERMINAL_PAREN_CLOSE.getPrimRule();
    }

    public static Exp fromString(String s) throws Lexer.LexerException, Parser.ParserException {
        return (Exp) SemanticNode.fromString(s, ExpGrammar.getInstance());
    }
}