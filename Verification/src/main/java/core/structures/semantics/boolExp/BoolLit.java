package core.structures.semantics.boolExp;

import core.structures.semantics.SemanticNode;
import core.structures.syntax.SyntaxNodeTerminal;
import core.structures.LexerRule;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class BoolLit extends BoolElem {
    private boolean _val;

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

    public void neg() {
        _val = !_val;
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

    @Override
    public BoolExp reduce() {
        return new BoolLit(_val);
    }

    @Override
    public void order() {
    }

    @Override
    public int comp(BoolExp b) {
        return Boolean.valueOf(_val).compareTo(((BoolLit) b)._val);
    }
}