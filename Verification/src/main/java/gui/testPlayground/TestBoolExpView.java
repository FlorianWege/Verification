package gui.testPlayground;

import core.Parser;
import core.structures.semantics.boolExp.BoolExp;
import grammars.BoolExpGrammar;
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

public class TestBoolExpView implements gui.Initializable {
    @FXML
    private Label _label_boolExp_unorderedResult;
    @FXML
    private Label _label_boolExp_orderedResult;
    @FXML
    private CodeArea _textArea_boolExp_input;
    private ExtendedCodeArea _codeArea_boolExp_input;

    private Scene _scene;

    public Scene getScene() {
        return _scene;
    }

    public TestBoolExpView() throws IOException {
        _scene = IOUtil.inflateFXML(new File("TestBoolExpView.fxml"), this);
    }

    @Override
    public void initialize(@Nonnull URL url, @Nullable ResourceBundle resources) {
        try {
            _codeArea_boolExp_input = new ExtendedCodeArea(_textArea_boolExp_input);

            _codeArea_boolExp_input.setParser(new Parser(BoolExpGrammar.getInstance()));
            _textArea_boolExp_input.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        _label_boolExp_unorderedResult.setText("");
                        _label_boolExp_orderedResult.setText("");

                        BoolExp boolExp = (BoolExp) _codeArea_boolExp_input.getParsing();

                        if (boolExp != null) {
                            BoolExp reducedBoolExp = boolExp.reduceEx().getRet();

                            BoolExp orderedBoolExp = reducedBoolExp.order();

                            _label_boolExp_unorderedResult.setText(reducedBoolExp.getContentString() + " [" + reducedBoolExp.getTypeName() + "]");
                            _label_boolExp_orderedResult.setText(orderedBoolExp.getContentString() + " [" + orderedBoolExp.getTypeName() + "]");
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
