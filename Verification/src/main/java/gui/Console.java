package gui;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import util.IOUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Console implements gui.Initializable, JavaFXMain.PrintInterface {
	@FXML
	private TextArea _textArea_log;
	
	private final Parent _root;

	private boolean _shown = false;
	
	public @Nonnull Parent getRoot() {
		return _root;
	}
	
	public Console() throws IOException {
		_root = IOUtil.inflateFXML(new File("Console.fxml"), this).getRoot();
	}

	@Override
	public void initialize(@Nonnull URL url, @Nullable ResourceBundle resources) {
		updateVisibility();
	}

	private void updateVisibility() {
		if (_textArea_log == null) return;
		
		_textArea_log.setVisible(_shown);
	}
	
	void setVisible(boolean show) {
		_shown = show;

		updateVisibility();
	}

	@Override
	public void writeToOut(String s) {
		if (_textArea_log != null) {
			_textArea_log.appendText(s);
		}
	}

	@Override
	public void writeToErr(String s) {
		if (_textArea_log != null) {
			_textArea_log.appendText(s);
		}
	}
}