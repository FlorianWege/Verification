package latex;

import util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class LatexFigure extends LatexObject {
    private String _caption;
    private String _label;

    public LatexFigure(String caption, String label) {
        _caption = caption;
        _label = label;
    }

    private List<LatexObject> _contents = new ArrayList<>();

    public void addContent(LatexObject content) {
        _contents.add(content);
    }

    @Override
    public void print(LatexStream stream) {
        stream.println("\\begin{figure}");

        stream.begin();

        stream.println("\\begin{center}");

        stream.begin();

        for (LatexObject content : _contents) {
            content.print(stream);
        }

        stream.end();

        stream.println("\\end{center}");

        stream.println("\\caption{" + StringUtil.latexify(_caption) + "}");
        stream.println("\\label{" + "fig:" + StringUtil.latexify(_label) + "}");

        stream.end();

        stream.println("\\end{figure}");
    }
}