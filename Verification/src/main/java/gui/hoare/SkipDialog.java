package gui.hoare;

import core.Hoare;
import core.Lexer;
import core.Parser;
import core.structures.semantics.prog.HoareCond;
import core.structures.semantics.prog.Skip;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import util.ErrorUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SkipDialog extends HoareDialog implements gui.Initializable {
	@FXML
	private Button _button_continue;

	private final Callback _callback;

	public interface Callback {
		void result() throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException;
	}

	public SkipDialog(@Nonnull Skip skip, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond, @Nonnull Callback callback) throws IOException {
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
	public void initialize(@Nonnull URL url, @Nullable ResourceBundle resources) {
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