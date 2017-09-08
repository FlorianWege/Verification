package gui.hoare;

import core.*;
import core.Hoare.Executer.LoopAskInv_callback;
import core.structures.hoareCond.HoareCond;
import core.structures.hoareCond.HoareCondBoolExp;
import core.structures.nodes.BoolExp;
import gui.ExtendedCodeArea;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.fxmisc.richtext.CodeArea;
import util.ErrorUtil;
import util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoopAskInvariantDialog extends HoareDialog implements Initializable {
	@FXML
	private CodeArea _codeArea_invariant;
	@FXML
	private Button _button_suggest;

	private LoopAskInv_callback _callback;
	private BoolExp _boolExp;
	private SyntaxNode _progNode;

	private ExtendedCodeArea _extendedCodeArea_invariant;
	
	public LoopAskInvariantDialog(Hoare.Executer.wlp_loop loop, LoopAskInv_callback callback) throws IOException {
		super(loop._loopNode, null, loop._postCond);

		_callback = callback;
		_boolExp = loop._boolExp;
		_progNode = loop._progNode;

		inflate(new File("LoopAskInvariantDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Loop (Step 1)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule 5 (loop): {p" + StringUtil.bool_and + "B} S {p} -> {p} while B do S od {p" + StringUtil.bool_and + StringUtil.bool_neg + "B}");
		sb.addProse("p" + StringUtil.bool_and + StringUtil.bool_neg + "B" + " -> q");

		sb.addParam("B", _boolExp.getBase().synthesize());
		sb.addParam("S", _progNode.synthesize());
		sb.addParam("q", _postCond.toStringEx());

		sb.addStep("get alleged invariant {p*}");

		return sb.toString();
	}

	@Override
	public String getOutput() {
		return null;
	}

	@Override
	public void initialize(URL url, ResourceBundle resources) {
		super.initialize(url, resources);

		_extendedCodeArea_invariant = new ExtendedCodeArea(_codeArea_invariant);

		_button_suggest.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					try {
						_extendedCodeArea_invariant.setErrorPos(null);

						HoareCond invariant = new HoareCondBoolExp(BoolExp.fromString(_codeArea_invariant.getText()));

						close();

						_callback.result(invariant);
					} catch (Lexer.LexerException e) {
						_extendedCodeArea_invariant.setErrorPos(e.getCurPos(), true);
					} catch (Parser.ParserException e) {
						Token token = e.getToken();

						int pos = (token != null) ? token.getPos() : 0;

						_extendedCodeArea_invariant.setErrorPos(pos, true);
					}
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
	}
}