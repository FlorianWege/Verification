package core.structures.semantics.prog;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.exp.Exp;
import core.structures.semantics.exp.Id;
import util.IOUtil;

import javax.annotation.Nonnull;

public class Assign extends Cmd {
    private Id _id;
    private Exp _exp;

    public Assign(@Nonnull Id id, @Nonnull Exp exp) {
        _id = id;
        _exp = exp;

        addChild(id, "var");
        addChild(exp, "val");
    }

    public @Nonnull Id getVar() {
        return _id;
    }

    public @Nonnull Exp getExp() {
        return _exp;
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        return mapper.apply(this, _id.getContentString(mapper) + _grammar.TERMINAL_OP_ASSIGN.getPrimRule() + _exp.getContentString(mapper));
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        _id = (Id) _id.replace(replaceFunc);
        _exp = (Exp) _exp.replace(replaceFunc);

        return replaceFunc.apply(this);
    }
}