package gui.hoare;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.prog.HoareCond;
import javafx.fxml.FXML;
import org.fxmisc.richtext.StyleClassedTextArea;
import util.ErrorUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EndDialog extends HoareDialog implements gui.Initializable {
	@FXML
	private StyleClassedTextArea _textArea_result;
	
	private final boolean _success;
	
	public EndDialog(SemanticNode node, HoareCond preCond, HoareCond postCond, boolean success) throws IOException {
		super(node, preCond, postCond);

		_success = success;
		
		inflate(new File("EndDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Verification finished";
	}

	@Override
	public String getRationale() {
		return null;
	}

	@Override
	public void initialize(@Nonnull URL url, @Nullable ResourceBundle resources) {
		try {
			super.initialize(url, resources);

			prepareTextArea(_textArea_result);

			if (_success) {
				_textArea_result.getStyleClass().add("success");

				_textArea_result.replaceText("success");
			} else {
				_textArea_result.getStyleClass().add("failure");

				_textArea_result.replaceText("failure");
			}
		} catch (Exception e) {
			ErrorUtil.logEFX(e);
		}
	}
}