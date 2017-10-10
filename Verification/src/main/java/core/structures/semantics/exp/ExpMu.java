package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;

public class ExpMu extends ExpElem {
    @Override
    public Exp reduce() {
        return new ExpMu();
    }

    @Override
    public void order() {
    }

    @Override
    public int comp(Exp b) {
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