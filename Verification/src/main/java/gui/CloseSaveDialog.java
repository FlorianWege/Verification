package gui;

import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CloseSaveDialog implements Initializable {
	private Alert _alert;

	public interface CloseSaveInterface {
		void result(CloseSaveDialog.Result result);
	}
	
	private CloseSaveInterface _callback;

	private ButtonType _buttonType_yes;
	private ButtonType _buttonType_no;
	private ButtonType _buttonType_cancel;
	
	public enum Result {
		YES,
		NO,
		CANCEL
	}
	
	void show() {
		_alert.showAndWait();
		
		if (_alert.getResult().equals(_buttonType_yes)) {
			_callback.result(Result.YES);
		} else if (_alert.getResult().equals(_buttonType_no)) {
			_callback.result(Result.NO);
		} else if (_alert.getResult().equals(_buttonType_cancel)) {
			_callback.result(Result.CANCEL);
		}
	}
	
	CloseSaveDialog(FileTab tab, CloseSaveInterface callback) throws IOException {
		_alert = new Alert(AlertType.CONFIRMATION, ((tab.getName() != null) ? tab.getName() : tab.getFile().getName()) + " has been edited. Do you want to save it before closing?");
		_callback = callback;
		
		_buttonType_yes = new ButtonType("yes");
		_buttonType_no = new ButtonType("no");
		_buttonType_cancel = new ButtonType("cancel");
		
		_alert.getButtonTypes().setAll(_buttonType_yes, _buttonType_no, _buttonType_cancel);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
}