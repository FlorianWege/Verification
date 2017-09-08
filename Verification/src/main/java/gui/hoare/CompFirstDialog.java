package gui.hoare;

import core.Hoare;
import core.SyntaxNode;
import core.structures.hoareCond.HoareCond;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import util.ErrorUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CompFirstDialog extends HoareDialog implements Initializable {
	@FXML
	private Button _button_continue;

	private Hoare.Executer.CompFirst_callback _callback;
	private SyntaxNode _firstNode;
	private SyntaxNode _secondNode;
	private HoareCond _secondPreCond;
	
	public CompFirstDialog(Hoare.Executer.wlp_comp comp, Hoare.Executer.CompFirst_callback callback) throws IOException {
		super(comp._compNode, null, comp._postCond);

		_callback = callback;
		_firstNode = comp._firstNode;
		_secondNode = comp._secondNode;
		_secondPreCond = comp._secondPreCond;
		
		inflate(new File("CompFirstDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Composition (Step 2)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule: {p} S<sub>1</sub> {r}, {r} S<sub>2</sub> {q} -> {p} S<sub>1</sub>; S<sub>2</sub> {q}");
		sb.addProse("with:");

		sb.addParam("S<sub>1</sub>", _firstNode.synthesize());
		sb.addParam("S<sub>2</sub>", _secondNode.synthesize());
		sb.addParam("{q}",  _postCond.toStringEx());

		sb.addStep("get r = wlp(S<sub>2</sub>, q)");
		sb.addResult(_secondPreCond.toStringEx());
		sb.addStep("get p = wlp(S<sub>1</sub>, r)");

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