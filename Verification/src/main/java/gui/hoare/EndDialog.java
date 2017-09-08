package gui.hoare;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EndDialog extends HoareDialog implements Initializable {
	@FXML
	private StyleClassedTextArea _textArea_result;
	
	private boolean _success;
	
	public EndDialog(boolean success) throws IOException {
		super(null, null, null);

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
	public String getOutput() {
		return null;
	}

	@Override
	public void initialize(URL url, ResourceBundle resources) {
		super.initialize(url, resources);

		prepareTextArea(_textArea_result);

		if (_success) {
			_textArea_result.getStyleClass().add("success");
			
			_textArea_result.replaceText("success");
		} else {
			_textArea_result.getStyleClass().add("failure");
			
			_textArea_result.replaceText("failure");
		}
	}
}