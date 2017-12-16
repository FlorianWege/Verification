package core.structures.semantics.boolExp;

import core.structures.semantics.SemanticNode;
import core.structures.syntax.SyntaxNodeTerminal;
import core.structures.LexerRule;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class BoolLit extends BoolElem {
    private final boolean _val;

    public boolean getVal() {
        return _val;
    }

    public BoolLit(boolean val) {
        _val = val;
    }

    public BoolLit(@Nonnull SyntaxNodeTerminal syntaxNodeTerminal) {
        _val = new Function<LexerRule, Boolean>() {
            @Override
            public Boolean apply(LexerRule rule) {
                if (rule.equals(_grammar.RULE_TRUE)) return true;
                if (rule.equals(_grammar.RULE_FALSE)) return false;

                return null;
            }
        }.apply(syntaxNodeTerminal.getToken().getRule());
    }

    public BoolExp neg() {
        return new BoolLit(!_val);
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        return mapper.apply(this, Boolean.toString(_val));
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        return replaceFunc.apply(this);
    }

    @Nonnull
    @Override
    public BoolExp reduce_spec(@Nonnull Reducer reducer) {
        return new BoolLit(_val);
    }

    @Nonnull
    @Override
    public BoolExp order_spec() {
        return new BoolLit(_val);
    }

    @Override
    public int comp_spec(BoolExp b) {
        return Boolean.compare(_val, ((BoolLit) b)._val);
    }
}