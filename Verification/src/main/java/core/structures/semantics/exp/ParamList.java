package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.prog.Comp;
import core.structures.semantics.prog.Prog;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ParamList extends SemanticNode {
    public List<Param> getParams() {
        List<Param> ret = new ArrayList<>();

        for (SemanticNode child : getChildren()) {
            if (child instanceof Param) ret.add((Param) child);
        }

        return ret;
    }

    public void addParam(@Nonnull Param param) {
        addChild(param);
    }

    public void addParamList(@Nonnull ParamList paramList) {
        for (Param param : paramList.getParams()) {
            addParam(param);
        }
    }

    public ParamList(Param... params) {
        for (Param param : params) {
            addParam(param);
        }
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        StringBuilder sb = new StringBuilder();

        for (Param param : getParams()) {
            if (sb.length() > 0) sb.append(_grammar.TERMINAL_PARAM_SEP.getPrimRule());

            sb.append(param.getContentString(mapper));
        }

        return mapper.apply(this, sb.toString());
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        replaceChildren(replaceFunc);

        return replaceFunc.apply(this);
    }
}