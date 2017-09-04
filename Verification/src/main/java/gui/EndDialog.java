package gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.StyleClassedTextArea;

import core.Hoare;
import core.structures.hoareCond.HoareCond;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Pair;
import util.ErrorUtil;
import util.IOUtil;

public class EndDialog implements Initializable {
	@FXML
	private StyleClassedTextArea _textArea;
	
	private boolean _success;
	private Parent _root;
	
	public Parent getRoot() {
		return _root;
	}
	
	public EndDialog(boolean success) throws IOException {
		_success = success;
		
		Scene scene = IOUtil.inflateFXML(new File("EndDialog.fxml"), this);
		
		_root = scene.getRoot();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		_textArea.getStylesheets().add(getClass().getResource("Dialog.css").toExternalForm());
		
		if (_success) {
			_textArea.getStyleClass().add("success");
			
			_textArea.replaceText("success");
		} else {
			_textArea.getStyleClass().add("failure");
			
			_textArea.replaceText("failure");
		}
	}
}