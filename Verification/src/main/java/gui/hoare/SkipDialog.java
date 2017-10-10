package gui.hoare;

import core.Hoare;
import core.structures.semantics.boolExp.HoareCond;
import core.structures.semantics.prog.Skip;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import util.ErrorUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SkipDialog extends HoareDialog implements Initializable {
	@FXML
	private Button _button_continue;

	private final Hoare.Executer.Skip_callback _callback;
	
	public SkipDialog(@Nonnull Skip skip, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond, @Nonnull Hoare.Executer.Skip_callback callback) throws IOException {
		super(skip, preCond, postCond);

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

		sb.addParam("p", styleCond(_postCond));
		sb.addStep("return p");

		sb.addOutput();

		return sb.toString();
	}

	@Override
	public void initialize(URL url, ResourceBundle resources) {
		try {
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
		} catch (Exception e) {
			ErrorUtil.logEFX(e);
		}
	}
}