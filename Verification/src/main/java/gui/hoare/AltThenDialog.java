package gui.hoare;

import core.Hoare;
import core.SyntaxNode;
import core.structures.nodes.BoolExp;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import util.ErrorUtil;
import util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AltThenDialog extends HoareDialog implements Initializable {
	@FXML
	private Button _button_continue;

	private Hoare.Executer.AltThen_callback _callback;
	private BoolExp _boolExp;
	private SyntaxNode _thenProgNode;
	private SyntaxNode _elseProgNode;

	public AltThenDialog(Hoare.Executer.wlp_alt alt, Hoare.Executer.AltThen_callback callback) throws IOException {
		super(alt._altNode, null, alt._postCond);

		_callback = callback;
		_boolExp = alt._boolExp;
		_thenProgNode = alt._thenProgNode;
		_elseProgNode = alt._elseProgNode;
		
		inflate(new File("AltThenDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Alternative (Step 1)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule 4 (conditional): " + "{p" + StringUtil.bool_and + "B} S<sub>1</sub> {q}, {p" + StringUtil.bool_and + StringUtil.bool_neg + "B} S<sub>2</sub>{q}" + " -> " + "{p} if B then S<sub>1</sub> else S<sub>2</sub> fi {q}");

		sb.addParam("B", _boolExp.getBase().synthesize());
		sb.addParam("S<sub>1</sub>", _thenProgNode.synthesize());
		sb.addParam("S<sub>2</sub>", _elseProgNode.synthesize());
		sb.addParam("q", _postCond.toStringEx());

		sb.addStep("get p" + StringUtil.bool_and + "B = wlp(S<sub>1</sub>, q)");

		return sb.toString();
	}

	@Override
	public String getOutput() {
		return null;
	}

	@Override
	public void initialize(URL url, ResourceBundle resources) {
		super.initialize(url, resources);

		_button_continue.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					_callback.result();
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
	}
}