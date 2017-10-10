package core.structures.semantics.boolExp;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.UnaryOperator;

public abstract class BoolList extends BoolExp {
    public abstract List<BoolExp> getBoolExps();
    public abstract void addBoolExp(BoolExp boolExp);

    public @Nonnull BoolList ownConstruct() {
        if (this instanceof BoolAnd) return new BoolAnd();
        if (this instanceof BoolOr) return new BoolOr();

        return null;
    }

    public @Nonnull BoolList invertConstruct() {
        if (this instanceof BoolAnd) return new BoolOr();
        if (this instanceof BoolOr) return new BoolAnd();

        return null;
    }
}
