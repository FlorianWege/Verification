package latex;

import core.Token;
import core.structures.Terminal;
import core.structures.syntax.SyntaxNode;
import core.structures.syntax.SyntaxNodeTerminal;
import util.StringUtil;

public class LatexSyntaxNode extends LatexObject {
    private SyntaxNode _node;

    public LatexSyntaxNode(SyntaxNode node) {
        _node = node;
    }

    @Override
    public void print(LatexStream stream) {
        if (_node instanceof SyntaxNodeTerminal) {
            Token token = ((SyntaxNodeTerminal) _node).getToken();

            String tokenS = (token != null) ? token.getText() : Terminal.EPSILON.toString();

            if (token != null) {
                stream.println(StringUtil.latexify(_node.getSymbol().toString()) + " \\treeterminal{" + StringUtil.latexify(tokenS) + "}");
            } else {
                stream.println("\\treeterminal{" + StringUtil.latexify(tokenS) + "}");
            }

            return;
        }

        stream.println("." + StringUtil.latexify(_node.getSymbol().toString()));

        for (SyntaxNode child : _node.getChildren()) {
            if (!(child instanceof SyntaxNodeTerminal)) {
                stream.println("[");
            }

            if (child instanceof SyntaxNodeTerminal) {
                new LatexSyntaxNode(child).print(stream);
            } else {
                stream.begin();

                new LatexSyntaxNode(child).print(stream);

                stream.end();
            }

            if (!(child instanceof SyntaxNodeTerminal)) {
                stream.println("]");
            }
        }
    }
}
