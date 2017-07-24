package gui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class ErrorDialog implements Initializable {
	@FXML
	private Label _label_msg;
	@FXML
	private Button _button_ok;
	
	private Stage _stage;
	private Alert _alert;

	private String _msg;
	private Exception _e;

	public void show() {
		//_stage.show();
		_alert.showAndWait();
	}
	
	public ErrorDialog(String msg) throws IOException {
		_msg = msg;
		
		/*_stage = new Stage();
		
		_stage.setTitle("Implication Dialog");
		_stage.setScene(IOUtil.inflateFXML(new File("ImplicationDialog.fxml"), this));
		_stage.setAlwaysOnTop(true);*/
		
		_alert = new Alert(AlertType.ERROR, _msg);
	}

	public ErrorDialog(Exception e) {
		_e = e;
		
		_msg = _e.getMessage();
		
		_alert = new Alert(AlertType.ERROR, _msg);
		
		GridPane expContent = new GridPane();
		
		_alert.getDialogPane().setExpandableContent(expContent);
		
		expContent.setMaxWidth(Double.MAX_VALUE);
		
		Label expLabel = new Label("Stacktrace:");
		
		expContent.add(expLabel, 0, 0);
		
		StringWriter sw = new StringWriter();
		
		PrintWriter pw = new PrintWriter(sw);
		
		e.printStackTrace(pw);
		
		TextArea expTextArea = new TextArea(sw.toString());
		
		expContent.add(expTextArea, 0, 1);
		
		expTextArea.setMaxWidth(Double.MAX_VALUE);
		expTextArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setHgrow(expTextArea, Priority.ALWAYS);
		GridPane.setVgrow(expTextArea, Priority.ALWAYS);
		
		expTextArea.setEditable(false);
		expTextArea.setWrapText(true);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		/*_label_msg.setText(_msg);
		
		_button_ok.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				_stage.close();
			}
		});*/
	}
}