package latex;

import core.structures.syntax.SyntaxNode;

public class LatexSyntaxTree extends LatexObject {
    private SyntaxNode _node;

    public LatexSyntaxTree(SyntaxNode node) {
        _node = node;
    }

    @Override
    public void print(LatexStream stream) {
        stream.println("\\Tree[");

        stream.begin();

        new LatexSyntaxNode(_node).print(stream);

        stream.end();

        stream.println("]");
    }
}
