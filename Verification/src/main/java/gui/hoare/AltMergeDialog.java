package gui.hoare;

import core.Hoare;
import core.SyntaxNode;
import core.structures.hoareCond.HoareCond;
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

public class AltMergeDialog extends HoareDialog implements Initializable {
	@FXML
	private Button _button_continue;

	private Hoare.Executer.AltMerge_callback _callback;
	private BoolExp _boolExp;
	private SyntaxNode _thenProgNode;
	private SyntaxNode _elseProgNode;
	private HoareCond _thenPreCond;
	private HoareCond _elsePreCond;

	public AltMergeDialog(Hoare.Executer.wlp_alt alt, Hoare.Executer.AltMerge_callback callback) throws IOException {
		super(alt._altNode, alt._preCond, alt._postCond);

		_callback = callback;
		_boolExp = alt._boolExp;
		_thenProgNode = alt._thenProgNode;
		_elseProgNode = alt._elseProgNode;
		_thenPreCond = alt._thenPreCond;
		_elsePreCond = alt._elsePreCond;
		
		inflate(new File("AltThenDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Alternative (Step 3)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule 4 (conditional): " + "{p" + StringUtil.bool_and + "B} S<sub>1</sub> {q}, {p" + StringUtil.bool_and + StringUtil.bool_neg + "B} S<sub>2</sub>{q}" + " -> " + "{p} if B then S<sub>1</sub> else S<sub>2</sub> fi {q}");

		sb.addParam("B", _boolExp.getBase().synthesize());
		sb.addParam("S<sub>1</sub>", _thenProgNode.synthesize());
		sb.addParam("S<sub>2</sub>", _elseProgNode.synthesize());
		sb.addParam("{q}", _postCond.toStringEx());

		sb.addStep("get p" + StringUtil.bool_and + "B = wlp(S<sub>1</sub>, q)");
		sb.addResult(_thenPreCond.toStringEx());
		sb.addStep("get p" + StringUtil.bool_and + StringUtil.bool_neg + "B = wlp(S<sub>2</sub>, q)");
		sb.addResult(_elsePreCond.toStringEx());
		sb.addStep("p = (p" + StringUtil.bool_and + "B)" + StringUtil.bool_or + "(p" + StringUtil.bool_and + StringUtil.bool_neg + "B)");

		return sb.toString();
	}

	@Override
	public String getOutput() {
		return _preCond.toStringEx() + " " + _node.synthesize() + " " + _postCond.toStringEx();
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