package core;

import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.BoolLit;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TheoremSolver {
    private BoolExp _boolExp;
    private Callback _callback;

    public interface Callback {
        void accept() throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException;
        void reject(@Nonnull BoolExp reducedBoolExp) throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException;
    }

    public void exec() throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException {
        BoolExp reducedBoolExp = _boolExp.reduce();

        if (reducedBoolExp.equals(new BoolLit(true))) {
            _callback.accept();
        } else {
            _callback.reject(reducedBoolExp);
        }
    }

    public TheoremSolver(@Nonnull BoolExp boolExp, @Nonnull Callback callback) {
        _boolExp = boolExp;
        _callback = callback;
    }
}
