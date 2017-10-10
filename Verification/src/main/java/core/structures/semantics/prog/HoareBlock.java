package core.structures.semantics.prog;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.HoareCond;
import util.IOUtil;

import javax.annotation.Nonnull;

public class HoareBlock extends Prog {
    private HoareCond _preCond;
    private Prog _prog;
    private HoareCond _postCond;

    public HoareBlock(@Nonnull HoareCond preCond, @Nonnull Prog prog, @Nonnull HoareCond postCond) {
        _preCond = preCond;
        _prog = prog;
        _postCond = postCond;

        addChild(_preCond, "preCond");
        addChild(_prog, "prog");
        addChild(_postCond, "postCond");
    }

    public @Nonnull HoareCond getPreCond() {
        return _preCond;
    }

    public @Nonnull Prog getProg() {
        return _prog;
    }

    public @Nonnull HoareCond getPostCond() {
        return _postCond;
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        return mapper.apply(this, _preCond.getContentString(mapper) + " " + _prog.getContentString(mapper) + " " + _postCond.getContentString(mapper));
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        _preCond = (HoareCond) _preCond.replace(replaceFunc);
        _prog = (Prog) _prog.replace(replaceFunc);
        _postCond = (HoareCond) _postCond.replace(replaceFunc);

        return replaceFunc.apply(this);
    }
}