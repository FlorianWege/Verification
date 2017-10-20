package latex.app;

import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.exp.Exp;
import latex.LatexSemanticTree;
import latex.LatexStream;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class Semantic {
    private File outputDir = new File("H:\\MA\\paper");

    @Test()
    public void A1() throws Lexer.LexerException, Parser.ParserException, IOException {
        LatexStream stream = new LatexStream(new File(outputDir, "fig\\tree_semanticTree_semantic_A1.tex"));

        SemanticNode node = Exp.fromString("A+1");

        new LatexSemanticTree(node).print(stream);

        stream.close();
    }
}
