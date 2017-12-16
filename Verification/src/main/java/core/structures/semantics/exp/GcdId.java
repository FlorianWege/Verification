package core.structures.semantics.exp;

import javax.annotation.Nonnull;

public class GcdId extends IdWithParams {
    private final Exp _a;
    private final Exp _b;

    public Exp getA() {
        return _a;
    }

    public Exp getB() {
        return _b;
    }

    public GcdId(Exp a, Exp b) {
        super("gcd");

        _a = a;
        _b = b;

        addParam(a);
        addParam(b);
    }

    public GcdId(@Nonnull ParamList paramList) {
        super("gcd");

        if (paramList.getParams().size() != 2) throw new RuntimeException("mismatching params count (2)");

        _a = paramList.getParams().get(0);
        _b = paramList.getParams().get(1);
    }

    @Nonnull
    @Override
    public Exp reduce_spec(@Nonnull Reducer reducer) {
        Exp a = _a.reduce(reducer);
        Exp b = _b.reduce(reducer);

        if (a instanceof ExpLit && b instanceof ExpLit) {
            a = ((ExpLit) a).gcd((ExpLit) b);

            return a;
        }

        return new GcdId(a, b);
    }

    //TODO overwrite order_spec/comp_spec
}