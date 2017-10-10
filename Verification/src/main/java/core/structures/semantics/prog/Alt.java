package core.structures.semantics.prog;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolExp;
import util.IOUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Alt extends Cmd {
    private BoolExp _boolExp;
    private Prog _thenProg;
    private Prog _elseProg;

    public Alt(@Nonnull BoolExp boolExp, @Nonnull Prog thenProg, @Nullable Prog elseProg) {
        _boolExp = boolExp;
        _thenProg = thenProg;
        _elseProg = elseProg;

        addChild(_boolExp, "cond");
        addChild(_thenProg, "then");
        if (_elseProg != null) addChild(_elseProg, "else");
    }

    public @Nonnull BoolExp getBoolExp() {
        return _boolExp;
    }

    public @Nonnull Prog getThenProg() {
        return _thenProg;
    }

    public @Nullable Prog getElseProg() {
        return _elseProg;
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        return mapper.apply(this, _grammar.TERMINAL_IF.getPrimRule() + " " + _boolExp.getContentString(mapper) + _grammar.TERMINAL_THEN.getPrimRule() + " " + _thenProg.getContentString(mapper) + ((_elseProg != null) ? " " + _grammar.TERMINAL_ELSE.getPrimRule() + " " + _elseProg.getContentString(mapper) + " " : "") + _grammar.TERMINAL_FI.getPrimRule());
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        _boolExp = (BoolExp) _boolExp.replace(replaceFunc);
        _thenProg = (Prog) _thenProg.replace(replaceFunc);
        if (_elseProg != null) _elseProg = (Prog) _elseProg.replace(replaceFunc);

        return replaceFunc.apply(this);
    }
}