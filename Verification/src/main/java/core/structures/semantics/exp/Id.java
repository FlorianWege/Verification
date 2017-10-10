package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Id extends ExpElem {
    private final String _name;

    @Override
    public String toString() {
        return getName();
    }

    public Id(String name) {
        super();

        _name = name;
    }

    private final List<Param> _params = new ArrayList<>();

    public List<Param> getParams() {
        return new ArrayList<>(_params);
    }

    public void addParam(Param param) {
        _params.add(param);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Id) {
            return _name.equals(((Id) other)._name);
        }

        return super.equals(other);
    }

    public String getName() {
        return _name;
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        return mapper.apply(this, _name);
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        return replaceFunc.apply(this);
    }

    @Override
    public Exp reduce() {
        Id ret = new Id(_name);

        for (Param param : getParams()) {
            ret.addParam((Param) param.copy());
        }

        return ret;
    }

    @Override
    public void order() {
    }

    @Override
    public int comp(Exp b) {
        return getName().compareTo(((Id) b).getName());
    }
}