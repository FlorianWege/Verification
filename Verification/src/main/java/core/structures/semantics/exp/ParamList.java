package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ParamList extends SemanticNode {
    @Nonnull
    public List<Exp> getParams() {
        List<Exp> ret = new ArrayList<>();

        for (SemanticNode child : getChildren()) {
            ret.add((Exp) child);
        }

        return ret;
    }

    public void addParam(@Nonnull Exp param) {
        addChild(param);
    }

    public void addParamList(@Nonnull ParamList paramList) {
        for (Exp param : paramList.getParams()) {
            addParam(param);
        }
    }

    public ParamList(@Nonnull Exp... params) {
        for (Exp param : params) {
            addParam(param);
        }
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        StringBuilder sb = new StringBuilder();

        for (Exp param : getParams()) {
            if (sb.length() > 0) sb.append(_grammar.TERMINAL_PARAM_SEP.getPrimRule());

            sb.append(param.getContentString(mapper));
        }

        return mapper.apply(this, sb.toString());
    }

    @Override
    @Nonnull
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        ParamList ret = new ParamList();

        for (Exp param : getParams()) {
            ret.addParam((Exp) param.replace(replaceFunc));
        }

        return ret;
    }
}