package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;

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

    @Nonnull
    @Override
    public Exp reduce_spec(@Nonnull Reducer reducer) {
        return new Id(getName());
    }

    @Nonnull
    @Override
    public Exp order_spec() {
        return new Id(getName());
    }

    @Override
    public int comp_spec(@Nonnull Exp b) {
        return getName().compareTo(((Id) b).getName());
    }
}