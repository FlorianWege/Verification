package gui.testPlayground;

import core.Parser;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.BoolImpl;
import grammars.BoolExpGrammar;
import gui.ExtendedCodeArea;
import gui.hoare.ConseqCheckTableView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;
import util.ErrorUtil;
import util.IOUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TestImplView implements gui.Initializable {
    @FXML
    private CodeArea _textArea_impl_source;
    private ExtendedCodeArea _codeArea_impl_source;
    @FXML
    private CodeArea _textArea_impl_target;
    private ExtendedCodeArea _codeArea_impl_target;
    @FXML
    private VBox _pane_impl_tableHost;
    private ConseqCheckTableView _impl_tableView;

    private Scene _scene;

    public Scene getScene() {
        return _scene;
    }

    public TestImplView() throws IOException {
        _scene = IOUtil.inflateFXML(new File("TestImplView.fxml"), this);
    }

    private void updateImpl() throws IOException {
        BoolExp sourceBoolExp = (BoolExp) _codeArea_impl_source.getParsing();
        BoolExp targetBoolExp = (BoolExp) _codeArea_impl_target.getParsing();

        if (sourceBoolExp != null && targetBoolExp != null) {
            _impl_tableView.setBoolImpl(new BoolImpl(sourceBoolExp, targetBoolExp));
        } else {
            _impl_tableView.setBoolImpl(null);
        }
    }

    @Override
    public void initialize(@Nonnull URL url, @Nullable ResourceBundle resources) {
        try {
            _codeArea_impl_source = new ExtendedCodeArea(_textArea_impl_source);

            _codeArea_impl_source.setLineNumbers(ExtendedCodeArea.NumType.NORMAL);

            _codeArea_impl_source.setParser(new Parser(BoolExpGrammar.getInstance()));
            _codeArea_impl_source.getTextArea().textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(@Nonnull ObservableValue<? extends String> observable, @Nullable String oldValue, @Nullable String newValue) {
                    try {
                        updateImpl();
                    } catch (IOException e) {
                        ErrorUtil.logEFX(e);
                    }
                }
            });

            _codeArea_impl_target = new ExtendedCodeArea(_textArea_impl_target);

            _codeArea_impl_target.setLineNumbers(ExtendedCodeArea.NumType.NORMAL);

            _codeArea_impl_target.setParser(new Parser(BoolExpGrammar.getInstance()));
            _codeArea_impl_target.getTextArea().textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(@Nonnull ObservableValue<? extends String> observable, @Nullable String oldValue, @Nullable String newValue) {
                    try {
                        updateImpl();
                    } catch (IOException e) {
                        ErrorUtil.logEFX(e);
                    }
                }
            });

            _impl_tableView = new ConseqCheckTableView(new ConseqCheckTableView.Callback() {
                @Override
                public void result(@Nonnull BoolExp boolExp) {

                }
            });

            _pane_impl_tableHost.getChildren().add(_impl_tableView.getRoot());

            VBox.setVgrow(_impl_tableView.getRoot(), Priority.ALWAYS);
        } catch (Exception e) {
            ErrorUtil.logEFX(e);
        }
    }
}
