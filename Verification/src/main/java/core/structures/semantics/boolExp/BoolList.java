package core.structures.semantics.boolExp;

import javax.annotation.Nonnull;
import java.util.List;

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

    /*@Override
    public BoolExp reduce() {
        //reduce parts and unwrap nested
        BoolOr tmpOr = new BoolOr();

        for (BoolExp boolExp : getBoolExps()) {
            tmpOr.addBoolExp(boolExp.reduce());
        }

        //idempotency
        Set<BoolExp> boolExps = new LinkedHashSet<>(tmpOr.getBoolExps());

        BoolList ret = ownConstruct();

        for (BoolExp boolExp : boolExps) {
            ret.addBoolExp(boolExp);
        }

        return ret;
    }*/
}
