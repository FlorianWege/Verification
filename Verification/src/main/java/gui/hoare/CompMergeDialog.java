package gui.hoare;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import core.SyntaxNode;
import org.fxmisc.richtext.StyleClassedTextArea;

import core.Hoare;
import core.structures.hoareCond.HoareCond;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import util.ErrorUtil;

public class CompMergeDialog extends HoareDialog implements Initializable {
	@FXML
	private StyleClassedTextArea _textArea_output;
	@FXML
	private Button _button_continue;

	private Hoare.Executer.CompMerge_callback _callback;
	private SyntaxNode _firstNode;
	private SyntaxNode _secondNode;
	private HoareCond _secondPreCond;
	private HoareCond _firstPreCond;
	
	public CompMergeDialog(Hoare.Executer.wlp_comp comp, Hoare.Executer.CompMerge_callback callback) throws IOException {
		super(comp._compNode, comp._preCond, comp._postCond);

		_callback = callback;
		_firstNode = comp._firstNode;
		_secondNode = comp._secondNode;
		_secondPreCond = comp._secondPreCond;
		_firstPreCond = comp._firstPreCond;
		
		inflate(new File("CompMergeDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Composition (Step 3)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule: {p} S1 {r}, {r} S2 {q} -> {p} S1; S2 {q}");
		sb.addProse("with:");

		sb.addParam("S<sub>1</sub>", styleNode(_firstNode));
		sb.addParam("S<sub>2</sub>", styleNode(_secondNode));
		sb.addParam("{q}", _postCond.toStringEx());

		sb.addStep("get r = wlp(S<sub>2</sub>, q)");
		sb.addResult(_secondPreCond.toStringEx());
		sb.addStep("get p = wlp(S<sub>1</sub>, r)");
		sb.addResult(_firstPreCond.toStringEx());
		sb.addStep("merge/continue");

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