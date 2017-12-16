package gui.hoare;

import core.Hoare;
import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.BoolImpl;
import core.structures.semantics.boolExp.BoolLit;
import core.structures.semantics.prog.HoareCond;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import util.ErrorUtil;
import util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class ConseqCheckDialog extends HoareDialog implements gui.Initializable {
    @FXML
    private Pane _pane_tableHost;

    @FXML
    private Button _button_yes;
    @FXML
    private Button _button_no;

    protected final HoareCond _sourceCond;
    protected final HoareCond _targetCond;
    private final Callback _callback;

    protected final BoolExp _sourceDNF;
    protected final BoolExp _targetDNF;

    protected final BoolImpl _impl;
    protected final BoolExp _split;

    public interface Callback {
        void result(@Nonnull BoolExp boolExp) throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException;
    }

    public abstract void getRationale_conseqCheckHeader(@Nonnull RationaleBuilder sb);

    @Override
    public String getRationale() {
        RationaleBuilder sb = new RationaleBuilder();

        getRationale_conseqCheckHeader(sb);

        sb.addStep("merge to implication");

        sb.addParam("impl", styleCond(_impl));

        sb.addStep("split implication by ORs (" + "A" + StringUtil.bool_or + "B" + StringUtil.bool_impl + "C" + StringUtil.bool_or + "D" + StringUtil.bool_impl_meta + "A" + StringUtil.bool_impl + "C" + StringUtil.bool_and + "B" + StringUtil.bool_impl + "C" + StringUtil.bool_or + "A" + StringUtil.bool_impl + "D" + StringUtil.bool_and + "B" + StringUtil.bool_impl + "D" + ")");

        sb.addParam("split", styleCond(_split));

        sb.addStep("apply reduction to each split");

        return sb.toString();
    }

    public ConseqCheckDialog(@Nonnull SemanticNode node, @Nonnull HoareCond sourceCond, @Nonnull HoareCond targetCond, @Nonnull ConseqCheckPostDialog.Callback callback) throws IOException {
        super(node, null, null);

        _sourceCond = sourceCond;
        _targetCond = targetCond;
        _callback = callback;

        _sourceDNF = _sourceCond.getBoolExp().makeDNF();
        _targetDNF = _targetCond.getBoolExp().makeDNF();

        _impl = new BoolImpl(_sourceDNF, _targetDNF);

        _split = _impl.split(true, false);
        System.out.println("preB");
        inflate(new File("ConseqCheckDialog.fxml"));
    }

    @Override
    public void initialize(@Nonnull URL url, @Nullable ResourceBundle resources) {
        try {
            super.initialize(url, resources);

            _button_yes.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(@Nonnull ActionEvent event) {
                    try {
                        _callback.result(new BoolLit(true));
                    } catch (Exception e) {
                        ErrorUtil.logEFX(e);
                    }
                }
            });

            _button_no.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(@Nonnull ActionEvent event) {
                    try {
                        _callback.result(new BoolLit(false));
                    } catch (Exception e) {
                        ErrorUtil.logEFX(e);
                    }
                }
            });

            ConseqCheckTableView tableView = new ConseqCheckTableView(new ConseqCheckTableView.Callback() {
                @Override
                public void result(@Nonnull BoolExp boolExp) {
                    try {
                        _callback.result(boolExp);
                    } catch (Exception e) {
                        ErrorUtil.logEFX(e);
                    }
                }
            });

            _pane_tableHost.getChildren().add(tableView.getRoot());

            tableView.setBoolImpl(_impl);
        } catch (Exception e) {
            ErrorUtil.logEFX(e);
        }
    }
}
