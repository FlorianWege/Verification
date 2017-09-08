package gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Console implements Initializable, JavaFXMain.PrintInterface {
	@FXML
	private TextArea _textArea_log;
	
	private Parent _root;
	
	public Parent getRoot() {
		return _root;
	}
	
	public Console() throws IOException {
		_root = IOUtil.inflateFXML(new File("Console.fxml"), this).getRoot();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		updateVisibility();
	}

	private boolean _shown = false;

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