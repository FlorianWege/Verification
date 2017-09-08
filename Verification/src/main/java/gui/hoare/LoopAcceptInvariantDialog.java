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

public class LoopAcceptInvariantDialog extends HoareDialog implements Initializable {
	@FXML
	private Button _button_continue;

	private Hoare.Executer.LoopAcceptInv_callback _callback;
	private BoolExp _boolExp;
	private SyntaxNode _progNode;
	private HoareCond _postInvariant;
	private HoareCond _preInvariant;

	public LoopAcceptInvariantDialog(Hoare.Executer.wlp_loop loop, Hoare.Executer.LoopAcceptInv_callback callback) throws IOException {
		super(loop._loopNode, loop._preCond, loop._postCond);

		_callback = callback;
		_boolExp = loop._boolExp;
		_progNode = loop._progNode;
		_postInvariant = loop._postInvariant;
		_preInvariant = loop._preInvariant;
		
		inflate(new File("LoopAcceptInvariantDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Loop (Step 5)";
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
		sb.addResult(_postInvariant.toStringEx());

		sb.addStep("check p" + StringUtil.bool_and + StringUtil.bool_neg + "B -> q");
		sb.addResult("true");

		sb.addStep("get p**=wlp(S, p*)");
		sb.addResult(_preInvariant.toStringEx());

		sb.addStep("check p** -> p*");
		sb.addResult("true");

		sb.addStep("return p** (which is potentially stronger than p*)");

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