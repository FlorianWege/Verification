package latex;

import core.structures.semantics.SemanticNode;

public class LatexSemanticTree extends LatexObject {
    private SemanticNode _node;

    public LatexSemanticTree(SemanticNode node) {
        _node = node;
    }

    @Override
    public void print(LatexStream stream) {
        stream.println("\\begin{forest} for tree={");

        stream.begin();

        stream.println("align=center,");
        stream.println("calign=fixed edge angles,");
        stream.println("calign primary angle=-45,");
        stream.println("calign secondary angle=45,");

        stream.end();

        stream.println("}");

        stream.begin();

        new LatexSemanticNode(_node).print(stream);

        stream.end();

        stream.println("]");
        stream.println("\\end{forest}");

        stream.close();
    }
}
