package gui;

import core.Hoare;
import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.BoolImpl;
import core.structures.semantics.exp.Exp;
import core.structures.semantics.exp.Id;
import core.structures.semantics.prog.Assign;
import core.structures.semantics.prog.HoareCond;
import core.structures.semantics.prog.Skip;
import grammars.BoolExpGrammar;
import grammars.ExpGrammar;
import gui.hoare.ConseqCheckTableView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.fxmisc.richtext.CodeArea;
import util.ErrorUtil;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TestPlaygroundWindow implements Initializable {
    @FXML
    private CodeArea _textArea_exp_input;
    private ExtendedCodeArea _codeArea_exp_input;
    @FXML
    private Label _label_exp_unorderedResult;
    @FXML
    private Label _label_exp_orderedResult;

    @FXML
    private Label _label_boolExp_unorderedResult;
    @FXML
    private Label _label_boolExp_orderedResult;

    @FXML
    private CodeArea _textArea_boolExp_input;
    private ExtendedCodeArea _codeArea_boolExp_input;

    @FXML
    private CodeArea _textArea_impl_source;
    private ExtendedCodeArea _codeArea_impl_source;
    @FXML
    private CodeArea _textArea_impl_target;
    private ExtendedCodeArea _codeArea_impl_target;
    @FXML
    private Pane _pane_impl_tableHost;
    private ConseqCheckTableView _impl_tableView;

    @FXML
    private CodeArea _textArea_varAssign_preCond;
    private ExtendedCodeArea _codeArea_varAssign_preCond;
    @FXML
    private CodeArea _textArea_varAssign_exp;
    private ExtendedCodeArea _codeArea_varAssign_exp;
    @FXML
    private CodeArea _textArea_varAssign_postCond;
    private ExtendedCodeArea _codeArea_varAssign_postCond;

    private Scene _scene;

    public Scene getScene() {
        return _scene;
    }

    private Stage _stage;

    public Stage getStage() {
        return _stage;
    }

    private BooleanProperty _shownProperty = new SimpleBooleanProperty(false);

    public BooleanProperty getShownProperty() {
        return _shownProperty;
    }

    public TestPlaygroundWindow() throws IOException {
        _stage = new Stage();

        _scene = IOUtil.inflateFXML(new File("TestPlaygroundWindow.fxml"), this);

        _stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
        _stage.setScene(_scene);
        _stage.setTitle("Testing playground");

        _stage.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                _shownProperty.set(true);
            }
        });
        _stage.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                _shownProperty.set(false);
            }
        });

        _shownProperty.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) _stage.show(); else _stage.hide();
            }
        });
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

    private void updateVarAssign() {
        BoolExp postCond = (BoolExp) _codeArea_varAssign_postCond.getParsing();

        Exp assignExp = (Exp) _codeArea_varAssign_exp.getParsing();

        _codeArea_varAssign_preCond.getTextArea().clear();

        if (postCond != null && assignExp != null) {
            try {
                new Hoare(new Hoare.ActionInterface() {
                    @Override
                    public void beginNode(SemanticNode node, HoareCond postCond) {

                    }

                    @Override
                    public void endNode(SemanticNode node, HoareCond preCond) {

                    }

                    @Override
                    public void reqSkipDialog(Skip skip, HoareCond preCond, HoareCond postCond, Hoare.Skip_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqAssignDialog(Assign assign, HoareCond preCond, HoareCond postCond, Hoare.Assign_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
                        callback.result();
                    }

                    @Override
                    public void reqCompNextDialog(Hoare.wlp_comp comp, Hoare.CompNext_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqCompMergeDialog(Hoare.wlp_comp comp, Hoare.CompMerge_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqAltFirstDialog(Hoare.wlp_alt alt, Hoare.AltThen_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqAltElseDialog(Hoare.wlp_alt alt, Hoare.AltElse_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqAltMergeDialog(Hoare.wlp_alt alt, Hoare.AltMerge_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqLoopAskInvDialog(Hoare.wlp_loop loop, Hoare.LoopAskInv_callback callback) throws IOException {

                    }

                    @Override
                    public void reqLoopCheckPostCondDialog(Hoare.wlp_loop loop, Hoare.LoopCheckPostCond_callback callback) throws IOException {

                    }

                    @Override
                    public void reqLoopGetBodyCondDialog(Hoare.wlp_loop loop, Hoare.LoopGetBodyCond_callback callback) throws IOException, Lexer.LexerException, Hoare.HoareException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqLoopCheckBodyCondDialog(Hoare.wlp_loop loop, Hoare.LoopCheckBodyCond_callback callback) throws IOException {

                    }

                    @Override
                    public void reqLoopAcceptInvCondDialog(Hoare.wlp_loop loop, Hoare.LoopAcceptInv_callback callback) throws IOException, Lexer.LexerException, Hoare.HoareException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqConseqCheckPreDialog(SemanticNode node, HoareCond origPreCond, HoareCond newPreCond, Hoare.ConseqCheck_callback callback) throws IOException {

                    }

                    @Override
                    public void reqConseqCheckPostDialog(SemanticNode node, HoareCond origPostCond, HoareCond newPostCond, Hoare.ConseqCheck_callback callback) throws IOException {

                    }
                }).wlp_assign(new Assign(new Id("x"), assignExp), new HoareCond(postCond), new Hoare.wlp_callback() {
                    @Override
                    public void result(@Nonnull SemanticNode node, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
                        _codeArea_varAssign_preCond.getTextArea().replaceText(preCond.getBoolExp().reduce().getContentString());
                    }
                });
            } catch (Exception e) {
                ErrorUtil.logEFX(e);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resources) {
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
                            Exp reducedExp = exp.reduce();

                            Exp orderedExp = (Exp) reducedExp.copy();

                            orderedExp.order();

                            _label_exp_unorderedResult.setText(reducedExp.getContentString() + " [" + reducedExp.getTypeName() + "]");
                            _label_exp_orderedResult.setText(orderedExp.getContentString() + " [" + orderedExp.getTypeName() + "]");
                        }
                    } catch (Exception e) {
                        ErrorUtil.logEFX(e);
                    }
                }
            });

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
                            BoolExp reducedBoolExp = boolExp.reduce();

                            BoolExp orderedBoolExp = (BoolExp) reducedBoolExp.copy();

                            orderedBoolExp.order();

                            _label_boolExp_unorderedResult.setText(reducedBoolExp.getContentString() + " [" + reducedBoolExp.getTypeName() + "]");
                            _label_boolExp_orderedResult.setText(orderedBoolExp.getContentString() + " [" + orderedBoolExp.getTypeName() + "]");
                        }
                    } catch (Exception e) {
                        ErrorUtil.logEFX(e);
                    }
                }
            });

            _codeArea_impl_source = new ExtendedCodeArea(_textArea_impl_source);

            _codeArea_impl_source.setParser(new Parser(BoolExpGrammar.getInstance()));
            _codeArea_impl_source.getTextArea().textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        updateImpl();
                    } catch (IOException e) {
                        ErrorUtil.logEFX(e);
                    }
                }
            });

            _codeArea_impl_target = new ExtendedCodeArea(_textArea_impl_target);

            _codeArea_impl_target.setParser(new Parser(BoolExpGrammar.getInstance()));
            _codeArea_impl_target.getTextArea().textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        updateImpl();
                    } catch (IOException e) {
                        ErrorUtil.logEFX(e);
                    }
                }
            });

            _impl_tableView = new ConseqCheckTableView(null);

            _pane_impl_tableHost.getChildren().add(_impl_tableView.getRoot());

            _codeArea_varAssign_preCond = new ExtendedCodeArea(_textArea_varAssign_preCond);
            _codeArea_varAssign_exp = new ExtendedCodeArea(_textArea_varAssign_exp);
            _codeArea_varAssign_postCond = new ExtendedCodeArea(_textArea_varAssign_postCond);

            _codeArea_varAssign_exp.setParser(new Parser(ExpGrammar.getInstance()));
            _codeArea_varAssign_postCond.setParser(new Parser(BoolExpGrammar.getInstance()));

            _codeArea_varAssign_exp.getTextArea().textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    updateVarAssign();
                }
            });
            _codeArea_varAssign_postCond.getTextArea().textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    updateVarAssign();
                }
            });
        } catch (Exception e) {
            ErrorUtil.logEFX(e);
        }
    }
}