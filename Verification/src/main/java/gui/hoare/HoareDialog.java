package gui.hoare;

import core.SyntaxNode;
import core.Token;
import core.structures.hoareCond.HoareCond;
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

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
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

    protected SyntaxNode _node;

    public SyntaxNode getNode() {
        return _node;
    }

    protected HoareCond _preCond;

    public HoareCond getPreCond() {
        return _preCond;
    }

    protected HoareCond _postCond;

    public HoareCond getPostCond() {
        return _postCond;
    }

    public HoareDialog(SyntaxNode node, HoareCond preCond, HoareCond postCond) {
        _node = node;
        _preCond = preCond;
        _postCond = postCond;
    }

    String styleNode(@Nonnull SyntaxNode node) {
        List<Token> tokens = node.tokenize();

        while (!tokens.isEmpty() && tokens.get(0).getTerminal().isSep()) tokens.remove(0);
        while (!tokens.isEmpty() && tokens.get(tokens.size() - 1).getTerminal().isSep()) tokens.remove(tokens.size() - 1);

        StringBuilder sb = new StringBuilder();

        for (Token token : tokens) {
            if (sb.length() == 0) sb.append(" ");

            sb.append(token.getText());
        }

        return sb.toString();
    }

    protected class RationaleBuilder {
        private StringBuilder sb = new StringBuilder();

        @Override
        public String toString() {
            return sb.toString();
        }

        public void addProse(String s) {
            sb.append("<p class='prose'>").append(s).append("</p>");
        }

        public void addParam(String name, String val) {
            sb.append("<p class='param'>").append(name).append("<span class='indent'>").append(val).append("</span></p>");
        }

        private int _stepC = 0;

        public void addStep(String s) {
            _stepC++;

            sb.append("<p class='step'>" + "step ").append(_stepC).append(":").append(s).append("</p>");
        }

        void addResult(String s) {
            sb.append("<p class='result'>").append(s).append("</p>");
        }

        public RationaleBuilder() {
        }
    }

    private Parent _root;

    public Parent getRoot() {
        return _root;
    }

    private Runnable _closeHandler = null;

    public void setCloseHandler(Runnable handler) {
        _closeHandler = handler;
    }

    void close() {
        if (_closeHandler != null) _closeHandler.run();
    }

    public abstract String getTitle();
    public abstract String getRationale();
    public abstract String getOutput();

    void prepareTextArea(StyleClassedTextArea textArea) {
        textArea.getStylesheets().add(getClass().getResource("HoareDialog.css").toExternalForm());
    }

    private void setText(WebView textArea, String msg) {
        if (msg != null) {
            msg = msg.replaceAll(Pattern.quote("{"), "<b>{");
            msg = msg.replaceAll(Pattern.quote("}"), "}</b>");
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
        String msg = getOutput();

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
            public void initialize(URL location, ResourceBundle resources) {
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