package gui.hoare;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import core.SyntaxNode;

import core.Hoare;
import core.structures.hoareCond.HoareCond;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import util.ErrorUtil;

public class ConseqCheckDialog extends HoareDialog implements Initializable {
	@FXML
	private Button _button_yes;
	@FXML
	private Button _button_no;

	private SyntaxNode _node;
	private HoareCond _origPreCond;
	private HoareCond _newPreCond;
	private HoareCond _origPostCond;
	private HoareCond _newPostCond;
	private Hoare.Executer.ConseqCheck_callback _callback;
	
	public ConseqCheckDialog(SyntaxNode node, HoareCond origPreCond, HoareCond newPreCond, HoareCond origPostCond, HoareCond newPostCond, Hoare.Executer.ConseqCheck_callback callback) throws IOException {
		super(node, null, origPostCond);

		_origPreCond = origPreCond;
		_newPreCond = newPreCond;
		_origPostCond = origPostCond;
		_newPostCond = newPostCond;
		_callback = callback;

		inflate(new File("ConseqCheckDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Consequence Check";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule 6 (consequence): p->p1, {p1} S {q1}, q1->q --> {p} S {q}");

		sb.addParam("p", _newPreCond.toStringEx());
		sb.addParam("p1", _origPreCond.toStringEx());

		return sb.toString();
	}

	@Override
	public String getOutput() {
		return null;
	}

	@Override
	public void initialize(URL url, ResourceBundle resources) {
		super.initialize(url, resources);

		_button_yes.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					_callback.result(true);
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});

		_button_no.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					_callback.result(false);
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
	}
}