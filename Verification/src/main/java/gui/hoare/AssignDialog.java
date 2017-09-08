package gui.hoare;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import core.SyntaxNode;

import core.Hoare;
import core.structures.nodes.Exp;
import core.structures.hoareCond.HoareCond;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import util.ErrorUtil;

public class AssignDialog extends HoareDialog implements Initializable {
	@FXML
	private Button _button_continue;

	private Hoare.Executer.Assign_callback _callback;
	private String _var;
	private Exp _exp;
	
	public AssignDialog(SyntaxNode node, HoareCond preCond, HoareCond postCond, Hoare.Executer.Assign_callback callback, String var, Exp exp) throws IOException {
		super(node, preCond, postCond);

		_callback = callback;
		_var = var;
		_exp = exp;
		
		inflate(new File("AssignDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Variable Assignment";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare axiom 2 (assignment): {p[u:=t]} u:=t {p}");

		sb.addParam("{p}", _postCond.toStringEx());
		sb.addParam("u", _var);
		sb.addParam("t", _exp.synthesize());

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