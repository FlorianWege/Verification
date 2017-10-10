package core.structures.semantics.exp;

public class GcdId extends Id {
    private final Param _a;
    private final Param _b;

    public Param getA() {
        return _a;
    }

    public Param getB() {
        return _b;
    }

    public GcdId(Param a, Param b) {
        super("gcd");

        _a = a;
        _b = b;

        addParam(a);
        addParam(b);
    }

    public GcdId(ParamList paramList) {
        super("gcd");

        if (paramList.getParams().size() != 2) throw new RuntimeException("mismatching params count (2)");

        _a = paramList.getParams().get(0);
        _b = paramList.getParams().get(1);
    }

    @Override
    public Exp reduce() {
        Param a = (Param) _a.reduce();
        Param b = (Param) _b.reduce();

        if (a instanceof ExpLit && b instanceof ExpLit) {
            ((ExpLit) a).gcd((ExpLit) b);

            return a;
        }

        return new GcdId(a, b);
    }
}