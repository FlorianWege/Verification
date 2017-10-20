package latex;

import core.structures.semantics.SemanticNode;

public class LatexSemanticTree extends LatexObject {
    private SemanticNode _node;

    public LatexSemanticTree(SemanticNode node) {
        _node = node;
    }

    @Override
    public void print(LatexStream stream) {
        stream.println("\\Tree[");

        stream.begin();

        new LatexSemanticNode(_node).print(stream);

        stream.end();

        stream.println("]");
    }
}
