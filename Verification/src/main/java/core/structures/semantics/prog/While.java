package core.structures.semantics.prog;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolExp;
import util.IOUtil;

import javax.annotation.Nonnull;

public class While extends Cmd {
    private BoolExp _boolExp;
    private Prog _prog;

    public While(@Nonnull BoolExp boolExp, @Nonnull Prog prog) {
        _boolExp = boolExp;
        _prog = prog;

        addChild(_boolExp, "cond");
        addChild(_prog, "prog");
    }

    public @Nonnull BoolExp getBoolExp() {
        return _boolExp;
    }

    public @Nonnull Prog getProg() {
        return _prog;
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        return mapper.apply(this, _grammar.TERMINAL_WHILE.getPrimRule() + " " + _boolExp.getContentString(mapper) + " " + _grammar.TERMINAL_DO + " " + _prog.getContentString(mapper) + " " + _grammar.TERMINAL_OD);
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        _boolExp = (BoolExp) _boolExp.replace(replaceFunc);
        _prog = (Prog) _prog.replace(replaceFunc);

        return replaceFunc.apply(this);
    }
}