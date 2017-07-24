package gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import core.Hoare;
import core.Hoare.HoareException;
import core.Lexer.LexerException;
import core.Parser.NoRuleException;
import core.Parser.ParserException;
import core.structures.HoareCondition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ImplicationDialog implements Initializable {
	@FXML
	private Label _label_question;
	@FXML
	private Button _button_yes;
	@FXML
	private Button _button_no;
	
	private Stage _stage;
	private Alert _alert;
	
	private HoareCondition _a;
	private HoareCondition _b;
	private Hoare.ImplicationInterface _callback;
	
	private String _msg;
	private ButtonType _buttonType_yes;
	private ButtonType _buttonType_no;
	
	public void show() throws HoareException, LexerException, IOException, ParserException {
		//_stage.show();
		_alert.showAndWait();
		
		if (_alert.getResult().equals(_buttonType_yes)) {
			_callback.result(true);
		} else if (_alert.getResult().equals(_buttonType_no)) {
			_callback.result(false);
		}
	}
	
	public ImplicationDialog(HoareCondition a, HoareCondition b, Hoare.ImplicationInterface callback) throws IOException {
		_a = a;
		_b = b;
		_callback = callback;
		
		_msg = String.format("Does\n%s\nimplicate\n%s\n?", _a, _b);
		
		/*_stage = new Stage();
		
		_stage.setTitle("Implication Dialog");
		_stage.setScene(IOUtil.inflateFXML(new File("ImplicationDialog.fxml"), this));
		_stage.setAlwaysOnTop(true);*/
		
		_alert = new Alert(AlertType.CONFIRMATION, _msg);
		
		_buttonType_yes = new ButtonType("yes");
		_buttonType_no = new ButtonType("no");
		
		_alert.getButtonTypes().setAll(_buttonType_yes, _buttonType_no);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		/*_label_question.setText(_msg);
		
		_button_yes.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				_stage.close();
				
				try {
					_callback.result(true);
				} catch (HoareException | NoRuleException | LexerException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		_button_no.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				_stage.close();
				
				try {
					_callback.result(false);
				} catch (HoareException | NoRuleException | LexerException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});*/
	}
}