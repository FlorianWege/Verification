package gui.testPlayground;

import core.Hoare;
import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.exp.Exp;
import core.structures.semantics.exp.Id;
import core.structures.semantics.prog.Assign;
import core.structures.semantics.prog.HoareCond;
import core.structures.semantics.prog.Skip;
import grammars.BoolExpGrammar;
import grammars.ExpGrammar;
import gui.ExtendedCodeArea;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import org.fxmisc.richtext.CodeArea;
import util.ErrorUtil;
import util.IOUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TestVarAssignView implements gui.Initializable {
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

    public TestVarAssignView() throws IOException {
        _scene = IOUtil.inflateFXML(new File("TestVarAssignView.fxml"), this);
    }

    private void updateVarAssign() {
        BoolExp postCond = (BoolExp) _codeArea_varAssign_postCond.getParsing();

        Exp assignExp = (Exp) _codeArea_varAssign_exp.getParsing();

        _codeArea_varAssign_preCond.getTextArea().clear();

        if (postCond != null && assignExp != null) {
            try {
                new Hoare(new Hoare.ActionInterface() {
                    @Override
                    public void beginNode(@Nonnull SemanticNode node, @Nonnull HoareCond postCond) {

                    }

                    @Override
                    public void endNode(@Nonnull SemanticNode node, @Nonnull HoareCond preCond) {

                    }

                    @Override
                    public void reqSkipDialog(@Nonnull Skip skip, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond, @Nonnull Hoare.Skip_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqAssignDialog(@Nonnull Assign assign, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond, @Nonnull Hoare.Assign_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
                        callback.result();
                    }

                    @Override
                    public void reqCompNextDialog(@Nonnull Hoare.wlp_comp comp, @Nonnull Hoare.CompNext_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqCompMergeDialog(@Nonnull Hoare.wlp_comp comp, @Nonnull Hoare.CompMerge_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqAltFirstDialog(@Nonnull Hoare.wlp_alt alt, @Nonnull Hoare.AltThen_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqAltElseDialog(@Nonnull Hoare.wlp_alt alt, @Nonnull Hoare.AltElse_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqAltMergeDialog(@Nonnull Hoare.wlp_alt alt, @Nonnull Hoare.AltMerge_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqLoopAskInvDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopAskInv_callback callback) throws IOException {

                    }

                    @Override
                    public void reqLoopCheckPostCondDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopCheckPostCond_callback callback) throws IOException {

                    }

                    @Override
                    public void reqLoopGetBodyCondDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopGetBodyCond_callback callback) throws IOException, Lexer.LexerException, Hoare.HoareException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqLoopCheckBodyCondDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopCheckBodyCond_callback callback) throws IOException {

                    }

                    @Override
                    public void reqLoopAcceptInvCondDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopAcceptInv_callback callback) throws IOException, Lexer.LexerException, Hoare.HoareException, Parser.ParserException, SemanticNode.CopyException {

                    }

                    @Override
                    public void reqConseqCheckPreDialog(@Nonnull SemanticNode node, @Nonnull HoareCond origPreCond, @Nonnull HoareCond newPreCond, @Nonnull Hoare.ConseqCheck_callback callback) throws IOException {

                    }

                    @Override
                    public void reqConseqCheckPostDialog(@Nonnull SemanticNode node, @Nonnull HoareCond origPostCond, @Nonnull HoareCond newPostCond, @Nonnull Hoare.ConseqCheck_callback callback) throws IOException {

                    }
                }).wlp_assign(new Assign(new Id("x"), assignExp), new HoareCond(postCond), new Hoare.wlp_callback() {
                    @Override
                    public void result(@Nonnull SemanticNode node, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
                        _codeArea_varAssign_preCond.getTextArea().replaceText(preCond.getBoolExp().reduceEx().getRet().getContentString());
                    }
                });
            } catch (Exception e) {
                ErrorUtil.logEFX(e);
            }
        }
    }

    @Override
    public void initialize(@Nonnull URL url, @Nullable ResourceBundle resources) {
        try {
            _codeArea_varAssign_preCond = new ExtendedCodeArea(_textArea_varAssign_preCond);
            _codeArea_varAssign_exp = new ExtendedCodeArea(_textArea_varAssign_exp);
            _codeArea_varAssign_postCond = new ExtendedCodeArea(_textArea_varAssign_postCond);

            _codeArea_varAssign_exp.setParser(new Parser(ExpGrammar.getInstance()));
            _codeArea_varAssign_postCond.setParser(new Parser(BoolExpGrammar.getInstance()));

            _codeArea_varAssign_exp.getTextArea().textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(@Nonnull ObservableValue<? extends String> observable, @Nullable String oldValue, @Nullable String newValue) {
                    updateVarAssign();
                }
            });
            _codeArea_varAssign_postCond.getTextArea().textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(@Nonnull ObservableValue<? extends String> observable, @Nullable String oldValue, @Nullable String newValue) {
                    updateVarAssign();
                }
            });
        } catch (Exception e) {
            ErrorUtil.logEFX(e);
        }
    }
}
