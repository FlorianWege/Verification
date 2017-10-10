package gui.hoare;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.HoareCond;
import grammars.HoareWhileGrammar;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.fxmisc.richtext.StyleClassedTextArea;
import util.ErrorUtil;
import util.IOUtil;
import util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public abstract class HoareDialog implements Initializable {
    @FXML
    private Label _label_title;

    @FXML
    private Pane _pane_rationale;
    @FXML
    private WebView _textArea_rationale;

    @FXML
    private Pane _pane_output;
    @FXML
    private WebView _textArea_output;

    @FXML
    private Pane _pane_specHost;

    protected final HoareWhileGrammar _grammar = HoareWhileGrammar.getInstance();

    protected final SemanticNode _node;

    public @Nonnull SemanticNode getNode() {
        return _node;
    }

    protected final HoareCond _preCond;

    public @Nullable HoareCond getPreCond() {
        return _preCond;
    }

    protected final HoareCond _postCond;

    public @Nullable HoareCond getPostCond() {
        return _postCond;
    }

    public HoareDialog(@Nonnull SemanticNode node, @Nullable HoareCond preCond, @Nullable HoareCond postCond) {
        _node = node;
        _preCond = preCond;
        _postCond = postCond;
    }

    public String styleCond(@Nonnull BoolExp boolExp) {
        return boolExp.getContentString();
    }

    public String styleCond(@Nonnull HoareCond cond) {
        return cond.getContentString();
    }

    public String styleNode(@Nonnull SemanticNode node) {
        //TODO
        /*String ret = node.synthesize(false, true, new Function<Token, String>() {
            @Override
            public String apply(Token token) {
                String text = StringUtil.escapeHTML(token.getName());

                return token.getTerminal().isKeyword() ? "<span class='keyword'>" + text + "</span>" : text;
            }
        });*/
        String ret = node.getContentString();

        ret = ret.replaceAll(StringUtil.line_sep, StringUtil.html_line_sep);

        return ret;
    }

    protected class RationaleBuilder {
        protected StringBuilder _sb = new StringBuilder();

        @Override
        public String toString() {
            endParams();

            return _sb.toString();
        }

        private boolean _inParams = false;

        private void endParams() {
            if (_inParams) {
                _inParams = false;

                _sb.append("</p>");
            }
        }

        public void addProse(String s) {
            endParams();

            _sb.append("<p class='prose'><span class='prose'>").append(s).append("</span></p>");
        }

        public void addParam(String name, String val) {
            if (!_inParams) _sb.append("<p class='param-block'>"); else _sb.append(StringUtil.html_line_sep);

            if (val != null && val.contains(StringUtil.html_line_sep)) val = StringUtil.html_line_sep + val;

            _sb.append("<span class='param'>").append("<span class='param-name'>").append(name).append("</span>").append(": ").append("<span class='param-val'>").append(val).append("</span>").append("</span>");

            _inParams = true;
        }

        private int _stepC = 0;

        public void addStep(String s) {
            endParams();

            _stepC++;

            _sb.append("<p class='step'><span class='step'>" + "step ").append(_stepC).append(":").append(s).append("</span></p>");
        }

        public void addResult(String s) {
            endParams();

            _sb.append("<p class='result'><span class='result'>").append(s).append("</span></p>");
        }

        public void addOutput(IOUtil.BiFunc<SemanticNode, String, String> preCondMapper, IOUtil.BiFunc<SemanticNode, String, String> nodeMapper, IOUtil.BiFunc<SemanticNode, String, String> postCondMapper) {
            endParams();

            if (_preCond != null) {
                String preCondS = (preCondMapper != null) ? _preCond.getContentString(preCondMapper) : _preCond.getContentString();
                String nodeS = (nodeMapper != null) ? _node.getContentString(nodeMapper) : _node.getContentString();
                String postCondS = (postCondMapper != null) ? _postCond.getContentString(postCondMapper) : _postCond.getContentString();

                String s = "output:" + StringUtil.html_line_sep + preCondS + " " + nodeS + " " + postCondS;

                _sb.append("<p class='output'><span class='output'>").append(s).append("</span></p>");
            }
        }

        public void addOutput() {
            addOutput(null, null, null);
        }

        public RationaleBuilder() {
        }
    }

    private Parent _root;

    public @Nonnull Parent getRoot() {
        return _root;
    }

    private Runnable _closeHandler = null;

    public void setCloseHandler(@Nonnull Runnable handler) {
        _closeHandler = handler;
    }

    public void close() {
        if (_closeHandler != null) _closeHandler.run();
    }

    public abstract String getTitle();
    public abstract String getRationale();

    public void prepareTextArea(@Nonnull StyleClassedTextArea textArea) {
        textArea.getStylesheets().add(getClass().getResource("HoareDialog.css").toExternalForm());
    }

    private void setText(@Nonnull WebView textArea, @Nullable String msg) {
        if (msg != null) {
            msg = msg.replaceAll(Pattern.quote(StringUtil.escapeHTML("{")), "<span class='assertion'>" + StringUtil.escapeHTML("{"));
            msg = msg.replaceAll(Pattern.quote(StringUtil.escapeHTML("}")),  StringUtil.escapeHTML("}") + "</span>");
        }

        textArea.getEngine().loadContent(msg);
        _textArea_rationale.getEngine().setUserStyleSheetLocation(getClass().getResource("HoareDialog.css").toExternalForm());
    }

    private void updateRationale() {
        String msg = getRationale();

        _pane_rationale.setVisible(msg != null && !msg.isEmpty());
        _pane_rationale.managedProperty().bind(_pane_rationale.visibleProperty());

        setText(_textArea_rationale, msg);
    }

    private void updateOutput() {
        String msg = null;

        _pane_output.setVisible(msg != null && !msg.isEmpty());
        _pane_output.managedProperty().bind(_pane_output.visibleProperty());

        setText(_textArea_output, msg);
    }

    public void inflate(File file) throws IOException {
        _root = IOUtil.inflateFXML(new File("HoareDialog.fxml"), new Initializable() {
            @FXML
            private Label _label_title;

            @FXML
            private Pane _pane_rationale;
            @FXML
            private WebView _textArea_rationale;

            @FXML
            private Pane _pane_output;
            @FXML
            private WebView _textArea_output;

            @FXML
            private Pane _pane_specHost;

            @Override
            public void initialize(URL url, ResourceBundle resources) {
                HoareDialog.this._label_title = _label_title;
                HoareDialog.this._pane_rationale = _pane_rationale;
                HoareDialog.this._textArea_rationale = _textArea_rationale;
                HoareDialog.this._pane_output = _pane_output;
                HoareDialog.this._textArea_output = _textArea_output;
                HoareDialog.this._pane_specHost = _pane_specHost;

                try {
                    Parent root = IOUtil.inflateFXML(file, HoareDialog.this).getRoot();

                    VBox.setVgrow(root, Priority.NEVER);

                    _pane_specHost.getChildren().add(root);
                } catch (IOException e) {
                    ErrorUtil.logEFX(e);
                }
            }
        }).getRoot();
    }

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        try {
            _label_title.setText(getTitle());
            updateRationale();
            updateOutput();
        } catch (Exception e) {
            ErrorUtil.logEFX(e);
        }
    }
}