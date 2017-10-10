package core.structures.semantics.exp;

public class FactId extends Id {
    private final Param _param;

    public Param getParam() {
        return _param;
    }

    public FactId(Param param) {
        super("fact");

        _param = param;

        addParam(param);
    }

    public FactId(ParamList paramList) {
        super("fact");

        if (paramList.getParams().size() != 1) throw new RuntimeException("mismatching params count (1)");

        _param = paramList.getParams().get(0);
    }

    @Override
    public Exp reduce() {
        Param param = (Param) _param.reduce();

        if (param instanceof ExpLit) {
            ((ExpLit) param).fact();

            return param;
        }

        return new FactId(param);
    }
}