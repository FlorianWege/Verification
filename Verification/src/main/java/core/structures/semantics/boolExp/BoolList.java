package core.structures.semantics.boolExp;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

public abstract class BoolList extends BoolExp {
    public abstract List<BoolExp> getBoolExps();
    public abstract void addBoolExp(BoolExp boolExp);

    @CheckReturnValue
    @Nonnull
    public BoolList ownConstruct() {
        if (this instanceof BoolAnd) return new BoolAnd();
        if (this instanceof BoolOr) return new BoolOr();

        assert(false) : "null";

        return null;
    }

    @CheckReturnValue
    @Nonnull
    public BoolList invertConstruct() {
        if (this instanceof BoolAnd) return new BoolOr();
        if (this instanceof BoolOr) return new BoolAnd();

        assert(false) : "null";

        return null;
    }

    @CheckReturnValue
    @Nonnull
    public BoolExp reduceShallow() {
        if (getBoolExps().size() == 1) return getBoolExps().get(0);

        return this;
    }

    public boolean isPure() {
        for (BoolExp boolExp : getBoolExps()) {
            if (this instanceof BoolAnd && boolExp instanceof BoolOr) return false;
            if (this instanceof BoolOr && boolExp instanceof BoolAnd) return false;
            if (boolExp instanceof BoolNeg) return false;
        }

        return true;
    }
}