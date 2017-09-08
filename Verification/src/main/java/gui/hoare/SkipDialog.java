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

public class SkipDialog extends HoareDialog implements Initializable {
	@FXML
	private Button _button_continue;

	private Hoare.Executer.Skip_callback _callback;
	
	public SkipDialog(SyntaxNode node, HoareCond preCond, HoareCond postCond, Hoare.Executer.Skip_callback callback) throws IOException {
		super(node, preCond, postCond);

		_callback = callback;
		
		inflate(new File("SkipDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Skip";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare axiom 1 (skip): {p} skip {p}");

		sb.addParam("p", _postCond.toStringEx());
		sb.addStep("return p");

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