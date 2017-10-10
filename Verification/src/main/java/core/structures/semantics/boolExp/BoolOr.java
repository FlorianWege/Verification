package core.structures.semantics.boolExp;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.exp.*;
import javafx.util.Pair;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class BoolOr extends BoolList {
    @Override
    public @Nonnull List<BoolExp> getBoolExps() {
        List<BoolExp> ret = new ArrayList<>();

        for (SemanticNode child : getChildren()) {
            if (child instanceof BoolExp) ret.add((BoolExp) child);
        }

        return ret;
    }

    @Override
    public void addBoolExp(@Nonnull BoolExp boolExp) {
        if (boolExp instanceof BoolOr) {
            for (BoolExp child : ((BoolOr) boolExp).getBoolExps()) {
                addBoolExp(child);
            }
        } else {
            addChild(boolExp);
        }
    }

    public BoolOr(BoolExp... boolExps) {
        for (BoolExp boolExp : boolExps) {
            addBoolExp(boolExp);
        }
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        StringBuilder sb = new StringBuilder();

        for (BoolExp boolExp : getBoolExps()) {
            if (sb.length() > 0) sb.append(_grammar.TERMINAL_OP_OR.getPrimRule());

            String expS = boolExp.getContentString(mapper);

            if (!(boolExp instanceof BoolElem) && boolExp.compPrecedence(this) < 0) {
                expS = parenthesize(expS);
            }

            sb.append(expS);
        }

        return mapper.apply(this, sb.toString());
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        replaceChildren(replaceFunc);

        return replaceFunc.apply(this);
    }

    @Override
    public BoolExp reduce() {
        List<BoolExp> boolExps = getBoolExps();

        boolExps.replaceAll(new UnaryOperator<BoolExp>() {
            @Override
            public BoolExp apply(BoolExp boolExp) {
                return boolExp.reduce();
            }
        });

        boolean hasTrue = false;

        for (BoolExp boolExp : boolExps) {
            if (boolExp instanceof BoolLit && ((BoolLit) boolExp).getVal()) hasTrue = true;
        }

        List<BoolExp> newBoolExps = new ArrayList<>();

        if (hasTrue) {
            return new BoolLit(true);
        } else {
            for (BoolExp boolExp : boolExps) {
                if (boolExp instanceof BoolLit) continue;

                newBoolExps.add(boolExp);
            }

            if (newBoolExps.isEmpty()) return new BoolLit(false);
        }

        BoolOr ret = new BoolOr();

        for (BoolExp boolExp : newBoolExps) {
            ret.addChild(boolExp);
        }

        return ret;
    }

    @Override
    public void order() {
        List<BoolExp> boolExps = getBoolExps();

        for (BoolExp boolExp : boolExps) {
            boolExp.order();
        }

        _children.clear();

        boolExps.sort(new Comparator<BoolExp>() {
            @Override
            public int compare(BoolExp a, BoolExp b) {
                return a.compPrecedence(b);
            }
        });

        for (BoolExp boolExp : boolExps) {
            addChild(boolExp);
        }
    }

    @Override
    public int comp(BoolExp b) {
        List<BoolExp> boolExps = getBoolExps();
        List<BoolExp> bBoolExps = ((BoolOr) b).getBoolExps();

        for (int i = 0;; i++) {
            if (i >= boolExps.size() && (i >= bBoolExps.size())) return 0;

            if (i >= boolExps.size()) return 1;
            if (i >= bBoolExps.size()) return -1;

            int localRet = boolExps.get(i).compPrecedence(bBoolExps.get(i));

            if (localRet != 0) return localRet;
        }
    }
}