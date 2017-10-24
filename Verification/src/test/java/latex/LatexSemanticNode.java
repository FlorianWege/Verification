package latex;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.exp.ExpLit;
import core.structures.semantics.exp.Id;
import util.StringUtil;

public class LatexSemanticNode extends LatexObject {
    private SemanticNode _node;

    public LatexSemanticNode(SemanticNode node) {
        _node = node;
    }

    @Override
    public void print(LatexStream stream) {
        stream.println("[");

        if (_node.getChildren().isEmpty()) {
            stream.println(StringUtil.latexify(_node.getTypeName()));

            String terminal = null;

            if (_node instanceof Id) terminal = ((Id) _node).getName();
            if (_node instanceof ExpLit) terminal = ((ExpLit) _node).getVal().toBigInteger().toString();

            if (terminal != null) stream.println("[\\treeterminal{" + terminal + "} ]");

            stream.println("]");

            return;
        }

        stream.println(StringUtil.latexify(_node.getTypeName()) + " ");

        for (SemanticNode child : _node.getChildren()) {
            stream.begin();

            new LatexSemanticNode(child).print(stream);

            stream.end();
        }

        stream.println("]");
    }
}
