package gui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;

public class ErrorDialog implements gui.Initializable {
	private final Alert _alert;

	public void show() {
		_alert.showAndWait();
	}

	public ErrorDialog(@Nonnull Exception e) {
		_alert = new Alert(AlertType.ERROR, e.getMessage());
		
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
	public void initialize(@Nonnull URL url, @Nullable ResourceBundle resources) {
	}
}