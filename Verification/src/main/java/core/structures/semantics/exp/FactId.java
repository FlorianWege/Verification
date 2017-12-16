package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;

public class FactId extends IdWithParams {
    private final Exp _param;

    public Exp getParam() {
        return _param;
    }

    public FactId(@Nonnull Exp param) {
        super("fact");

        _param = param;

        addParam(param);
    }

    public FactId(@Nonnull ParamList paramList) {
        super("fact");

        if (paramList.getParams().size() != 1) throw new RuntimeException("mismatching params count (1)");

        _param = paramList.getParams().get(0);
    }

    @Nonnull
    @Override
    public Exp reduce_spec(@Nonnull Reducer reducer) {
        Exp param = _param.reduce(reducer);

        if (param instanceof ExpLit) {
            param = ((ExpLit) param).fact();

            return param;
        }

        return new Fact(param);
    }

    @Nonnull
    @Override
    public Exp order_spec() {
        return new FactId(_param.order());
    }

    @Override
    public int comp_spec(@Nonnull Exp b) {
        return 0;
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        return null;
    }
}