package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class IdWithParams extends Id {
    private final List<Exp> _params = new ArrayList<>();

    public IdWithParams(String name) {
        super(name);
    }

    public List<Exp> getParams() {
        return new ArrayList<>(_params);
    }

    public void addParam(@Nonnull Exp param) {
        _params.add(param);
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        return mapper.apply(this, getName());
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        return replaceFunc.apply(this);
    }
}
