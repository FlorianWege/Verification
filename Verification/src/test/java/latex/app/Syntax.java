package latex.app;

import core.Lexer;
import core.Parser;
import core.structures.syntax.SyntaxNode;
import grammars.ExpGrammar;
import latex.LatexObject;
import latex.LatexStream;
import latex.LatexSyntaxTree;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class Syntax {
    @Test()
    public void A1() throws Lexer.LexerException, Parser.ParserException, IOException {
        LatexStream stream = new LatexStream(new File(LatexObject.outputDir, "fig\\tree_semanticTree_syntax_A1.tex"));

        SyntaxNode syntax = new Parser(ExpGrammar.getInstance()).parse("A+1");

        new LatexSyntaxTree(syntax).print(stream);

        stream.close();
    }
}
