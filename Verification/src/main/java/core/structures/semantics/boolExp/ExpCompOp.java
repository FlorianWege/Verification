package core.structures.semantics.boolExp;

import core.structures.syntax.SyntaxNodeTerminal;
import core.structures.LexerRule;
import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.function.Function;

public class ExpCompOp extends SemanticNode implements Serializable {
    public enum Type {
        EQUAL,
        LESS,
        EQUAL_LESS,
        GREATER,
        EQUAL_GREATER,
        UNEQUAL
    }

    @Override
    public int hashCode() {
        return _type.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ExpCompOp) return _type.equals(((ExpCompOp) other).getType());

        if (other instanceof Type) return _type.equals(other);

        return super.equals(other);
    }

    private final Type _type;

    public @Nonnull Type getType() {
        return _type;
    }

    @Override
    public String toString() {
        return _typeRuleFunc.apply(_type).toString();
    }

    private final Function<LexerRule, Type> _ruleTypeFunc = new IOUtil.Func<LexerRule, Type>() {
        @Override
        public @Nonnull Type apply(LexerRule rule) {
            if (rule.equals(_grammar.RULE_EQUAL)) return Type.EQUAL;
            if (rule.equals(_grammar.RULE_LESS_EQUAL)) return Type.EQUAL_LESS;
            if (rule.equals(_grammar.RULE_LESS)) return Type.LESS;
            if (rule.equals(_grammar.RULE_GREATER)) return Type.GREATER;
            if (rule.equals(_grammar.RULE_GREATER_EQUAL)) return Type.EQUAL_GREATER;
            if (rule.equals(_grammar.RULE_UNEQUAL)) return Type.UNEQUAL;

            assert(false) : "null";

            return null;
        }
    };
    private final Function<Type, LexerRule> _typeRuleFunc = new IOUtil.Func<Type, LexerRule>() {
        @Override
        public @Nonnull LexerRule apply(Type type) {
            assert(type != null) : "type is null";

            if (type.equals(Type.EQUAL)) return _grammar.RULE_EQUAL;
            if (type.equals(Type.EQUAL_LESS)) return _grammar.RULE_LESS_EQUAL;
            if (type.equals(Type.LESS)) return _grammar.RULE_LESS;
            if (type.equals(Type.GREATER)) return _grammar.RULE_GREATER;
            if (type.equals(Type.EQUAL_GREATER)) return _grammar.RULE_GREATER_EQUAL;
            if (type.equals(Type.UNEQUAL)) return _grammar.RULE_UNEQUAL;

            assert(false) : "null";

            return null;
        }
    };

    public ExpCompOp(@Nonnull Type type) {
        _type = type;
    }

    public ExpCompOp(@Nonnull SyntaxNodeTerminal syntaxNodeTerminal) {
        _type = _ruleTypeFunc.apply(syntaxNodeTerminal.getToken().getRule());
    }

    private final Function<Type, Type> _negFunc = new IOUtil.Func<Type, Type>() {
        @Override
        public Type apply(Type type) {
            switch (type) {
                case EQUAL: return Type.UNEQUAL;
                case UNEQUAL: return Type.EQUAL;
                case LESS: return Type.EQUAL_GREATER;
                case GREATER: return Type.EQUAL_LESS;
                case EQUAL_LESS: return Type.GREATER;
                case EQUAL_GREATER: return Type.LESS;
            }

            return null;
        }
    };

    public ExpCompOp neg() {
        return new ExpCompOp(_negFunc.apply(_type));
    }

    private final Function<Type, Type> _swapFunc = new IOUtil.Func<Type, Type>() {
        @Override
        public Type apply(Type type) {
            switch (type) {
                case EQUAL: return Type.EQUAL;
                case UNEQUAL: return Type.UNEQUAL;
                case LESS: return Type.GREATER;
                case GREATER: return Type.LESS;
                case EQUAL_LESS: return Type.EQUAL_GREATER;
                case EQUAL_GREATER: return Type.EQUAL_LESS;
            }

            return null;
        }
    };

    public ExpCompOp swap() {
        return new ExpCompOp(_swapFunc.apply(_type));
    }

    public int comp(ExpCompOp b) {
        return _type.compareTo(b._type);
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        return mapper.apply(this, _typeRuleFunc.apply(_type).getText());
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        return replaceFunc.apply(this);
    }
}