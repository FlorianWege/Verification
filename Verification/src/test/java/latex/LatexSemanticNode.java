package latex;

import core.structures.semantics.SemanticNode;
import util.StringUtil;

public class LatexSemanticNode extends LatexObject {
    private SemanticNode _node;

    public LatexSemanticNode(SemanticNode node) {
        _node = node;
    }

    @Override
    public void print(LatexStream stream) {
        if (_node.getChildren().isEmpty()) {
            stream.println(StringUtil.latexify(_node.getTypeName().toString()) + " \\treeterminal{" + StringUtil.latexify(_node.getContentString()) + "}");

            return;
        }

        stream.println("." + StringUtil.latexify(_node.getTypeName().toString()));

        for (SemanticNode child : _node.getChildren()) {
            //if (!(_node.getChildren().isEmpty())) {
                stream.println("[");
            //}

            if (child.getChildren().isEmpty()) {
                new LatexSemanticNode(child).print(stream);
            } else {
                stream.begin();

                new LatexSemanticNode(child).print(stream);

                stream.end();
            }

            //if (!(_node.getChildren().isEmpty())) {
                stream.println("]");
            //}
        }
    }
}
