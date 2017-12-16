package core.structures.semantics.prog;

import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import grammars.HoareWhileGrammar;

public abstract class Prog extends SemanticNode {
    public static Prog fromString(String s) throws Lexer.LexerException, Parser.ParserException {
        return (Prog) SemanticNode.fromString(s, HoareWhileGrammar.getInstance());
    }
}