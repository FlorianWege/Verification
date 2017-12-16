package gui.testPlayground;

import core.Parser;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.prog.Prog;
import core.structures.semantics.prog.While;
import grammars.WhileGrammar;
import gui.ExtendedCodeArea;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import org.fxmisc.richtext.CodeArea;
import util.ErrorUtil;
import util.IOUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TestLoopView implements gui.Initializable {
    @FXML
    private CodeArea _textArea_loop_raw;
    private ExtendedCodeArea _codeArea_loop_raw;
    @FXML
    private CodeArea _textArea_loop_reduced;
    private ExtendedCodeArea _codeArea_loop_reduced;
    @FXML
    private TextField _textField_loop_loopCond;
    @FXML
    private TextField _textField_loop_loopCond_raw;
    @FXML
    private TextField _textField_loop_loopCond_reduced;
    @FXML
    private TextField _textField_loop_loopCond_CNF;
    @FXML
    private CodeArea _textArea_loop_postCond;
    private ExtendedCodeArea _codeArea_loop_postCond;
    @FXML
    private TextField _textField_loop_postCond_raw;
    @FXML
    private TextField _textField_loop_postCond_reduced;
    @FXML
    private TextField _textField_loop_postCond_CNF;

    private Scene _scene;

    public Scene getScene() {
        return _scene;
    }

    public TestLoopView() throws IOException {
        _scene = IOUtil.inflateFXML(new File("TestLoopView.fxml"), this);
    }

    private void updateLoop() {
        Prog prog = (Prog) _codeArea_loop_raw.getParsing();

        if (!(prog instanceof While)) {
            _textField_loop_loopCond.setText(null);
            _textField_loop_loopCond_reduced.setText(null);
            _textField_loop_loopCond_CNF.setText(null);

            _textArea_loop_postCond.replaceText(null);
            _textField_loop_postCond_reduced.setText(null);
            _textField_loop_postCond_CNF.setText(null);

            return;
        }

        While loop = (While) prog;

        BoolExp loopCond = loop.getBoolExp();


    }

    @Override
    public void initialize(@Nonnull URL location, @Nullable ResourceBundle resources) {
        try {
            _codeArea_loop_raw = new ExtendedCodeArea(_textArea_loop_raw);

            _codeArea_loop_raw.setParser(new Parser(WhileGrammar.getInstance()));

            _codeArea_loop_raw.getTextArea().textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    updateLoop();
                }
            });
        } catch (Exception e) {
            ErrorUtil.logEFX(e);
        }
    }
}
