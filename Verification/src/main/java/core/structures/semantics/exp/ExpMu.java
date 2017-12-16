package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;

public class ExpMu extends ExpElem {
    @Nonnull
    @Override
    public Exp reduce_spec(@Nonnull Reducer reducer) {
        return new ExpMu();
    }

    @Nonnull
    @Override
    public ExpMu order_spec() {
        return new ExpMu();
    }

    @Override
    public int comp_spec(@Nonnull Exp b) {
        return 0;
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        return mapper.apply(this, "\u03BC");
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        return replaceFunc.apply(this);
    }
}