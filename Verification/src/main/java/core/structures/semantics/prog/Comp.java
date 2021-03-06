package core.structures.semantics.prog;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Comp extends Prog {
    public @Nonnull List<Prog> getProgs() {
        List<Prog> ret = new ArrayList<>();

        for (SemanticNode child : getChildren()) {
            if (child instanceof Prog) {
                ret.add((Prog) child);
            }
        }

        return ret;
    }

    public void addProg(@Nonnull Prog prog) {
        if (prog instanceof Comp) {
            for (Prog child : ((Comp) prog).getProgs()) {
                addProg(child);
            }
        } else {
            addChild(prog);
        }
    }

    public Comp(Prog... progs) {
        for (Prog prog : progs) {
            addProg(prog);
        }
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        StringBuilder sb = new StringBuilder();

        for (Prog prog : getProgs()) {
            if (sb.length() > 0) sb.append(_grammar.TERMINAL_STATEMENT_SEP.getPrimRule());

            sb.append(prog.getContentString(mapper));
        }

        return mapper.apply(this, sb.toString());
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        Comp ret = new Comp();

        for (Prog prog : getProgs()) {
            ret.addProg(prog);
        }

        return replaceFunc.apply(ret);
    }
}