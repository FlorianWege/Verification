package gui.testPlayground;

import core.Parser;
import core.structures.semantics.exp.Exp;
import grammars.ExpGrammar;
import gui.ExtendedCodeArea;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import org.fxmisc.richtext.CodeArea;
import util.ErrorUtil;
import util.IOUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TestExpView implements gui.Initializable {
    @FXML
    private CodeArea _textArea_exp_input;
    private ExtendedCodeArea _codeArea_exp_input;
    @FXML
    private Label _label_exp_unorderedResult;
    @FXML
    private Label _label_exp_orderedResult;

    private Scene _scene;

    public Scene getScene() {
        return _scene;
    }

    public TestExpView() throws IOException {
        _scene = IOUtil.inflateFXML(new File("TestExpView.fxml"), this);
    }

    @Override
    public void initialize(@Nonnull URL url, @Nullable ResourceBundle resources) {
        try {
            _codeArea_exp_input = new ExtendedCodeArea(_textArea_exp_input);

            _codeArea_exp_input.setParser(new Parser(ExpGrammar.getInstance()));
            _textArea_exp_input.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        _label_exp_unorderedResult.setText("");
                        _label_exp_orderedResult.setText("");

                        Exp exp = (Exp) _codeArea_exp_input.getParsing();

                        if (exp != null) {
                            Exp reducedExp = exp.reduceEx().getRet();

                            Exp orderedExp = reducedExp.order();

                            _label_exp_unorderedResult.setText(reducedExp.getContentString() + " [" + reducedExp.getTypeName() + "]");
                            _label_exp_orderedResult.setText(orderedExp.getContentString() + " [" + orderedExp.getTypeName() + "]");
                        }
                    } catch (Exception e) {
                        ErrorUtil.logEFX(e);
                    }
                }
            });
        } catch (Exception e) {
            ErrorUtil.logEFX(e);
        }
    }
}
