package gui.hoare;

import core.Hoare;
import core.SyntaxNode;
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

public class CompSecondDialog extends HoareDialog implements Initializable {
	@FXML
	private Button _button_continue;

	private Hoare.Executer.CompSecond_callback _callback;
	private SyntaxNode _firstNode;
	private SyntaxNode _secondNode;
	
	public CompSecondDialog(Hoare.Executer.wlp_comp comp, Hoare.Executer.CompSecond_callback callback) throws IOException {
		super(comp._compNode, null, comp._postCond);

		_callback = callback;
		_firstNode = comp._firstNode;
		_secondNode = comp._secondNode;
		
		inflate(new File("CompSecondDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Composition (Step 1)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule 3 (composition): {p} S<sub>1</sub> {r}, {r} S<sub>2</sub> {q} -> {p} S<sub>1</sub>; S<sub>2</sub> {q}");

		sb.addParam("S<sub>1</sub>", styleNode(_firstNode));
		sb.addParam("S<sub>2</sub>", styleNode(_secondNode));
		sb.addParam("{q}", _postCond.toStringEx());

		sb.addStep("get r = wlp(S<sub>2</sub>, q)");

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