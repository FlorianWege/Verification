package gui.hoare;

import core.*;
import core.Hoare.Executer.LoopAskInv_callback;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.HoareCond;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.prog.Prog;
import grammars.BoolExpGrammar;
import gui.ExtendedCodeArea;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.fxmisc.richtext.CodeArea;
import util.ErrorUtil;
import util.StringUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoopAskInvariantDialog extends HoareDialog implements Initializable {
	@FXML
	private CodeArea _codeArea_invariant;
	@FXML
	private Button _button_suggest;

	private final Hoare.Executer.wlp_loop _loop;
	private final LoopAskInv_callback _callback;

	private ExtendedCodeArea _extendedCodeArea_invariant;
	
	public LoopAskInvariantDialog(@Nonnull Hoare.Executer.wlp_loop loop, @Nonnull LoopAskInv_callback callback) throws IOException {
		super(loop._whileNode, null, loop._postCond);

		_loop = loop;
		_callback = callback;

		inflate(new File("LoopAskInvariantDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "While (Step 1)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule 5 (loop): {p" + StringUtil.bool_and + "B} S {p} " + StringUtil.bool_impl + " {p} while B do S od {p" + StringUtil.bool_and + StringUtil.bool_neg + "B}");
		sb.addProse("p" + StringUtil.bool_and + StringUtil.bool_neg + "B" + StringUtil.bool_impl + "q");

		sb.addParam("B", styleNode(_loop._whileNode.getBoolExp()));
		sb.addParam("S", styleNode(_loop._whileNode.getProg()));
		sb.addParam("q", styleCond(_postCond));

		sb.addStep("get alleged invariant {p*}");

		return sb.toString();
	}

	@Override
	public void initialize(URL url, ResourceBundle resources) {
		try {
			super.initialize(url, resources);

			_extendedCodeArea_invariant = new ExtendedCodeArea(_codeArea_invariant, null, null, ExtendedCodeArea.Type.CODE);

			_button_suggest.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						try {
							_extendedCodeArea_invariant.setErrorPos(null);

							HoareCond invariant = new HoareCond((BoolExp) SemanticNode.fromString(_codeArea_invariant.getText(), BoolExpGrammar.getInstance()));

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
		} catch (Exception e) {
			ErrorUtil.logEFX(e);
		}
	}
}