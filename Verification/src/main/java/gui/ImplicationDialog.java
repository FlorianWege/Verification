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
import core.Hoare.HoareException;
import core.Lexer.LexerException;
import core.Parser.ParserException;
import core.structures.hoareCond.HoareCond;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Pair;
import util.IOUtil;

public class ImplicationDialog implements Initializable {
	@FXML
	private Label _label_question;
	@FXML
	private Button _button_yes;
	@FXML
	private Button _button_no;
	
	private Stage _stage;
	private Alert _alert;
	
	private HoareCond _a;
	private HoareCond _b;
	private Hoare.Executer.ImplicationInterface _callback;
	private boolean _equal;
	
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
	
	public ImplicationDialog(HoareCond a, HoareCond b, Hoare.Executer.ImplicationInterface callback, boolean equal) throws IOException {
		_a = a;
		_b = b;
		_callback = callback;
		_equal = equal;
		
		_msg = _equal ? String.format("Is\n%s\nequal to\n%s\n?", _a.toStringEx(), _b.toStringEx()) : String.format("Does\n%s\nimplicate\n%s\n?", _a.toStringEx(), _b.toStringEx());
		
		_stage = new Stage();
		
		_stage.setTitle("Implication Dialog");
		_stage.setScene(IOUtil.inflateFXML(new File("ImplicationDialog.fxml"), this));
		
		_alert = new Alert(AlertType.CONFIRMATION, _msg);
		//_alert.getDialogPane().setContent(IOUtil.inflateFXML(new File("ImplicationDialog.fxml"), this).getRoot());
		
		IOUtil.inflateFXML(new File("ImplicationDialogContent.fxml"), new Initializable() {
			@FXML
			private StyleClassedTextArea _textArea;
			
			@Override
			public void initialize(URL arg0, ResourceBundle arg1) {
				//_textArea.setDisable(true);
				//_textArea.setEditable(false);
				_textArea.getStylesheets().add(getClass().getResource("Dialog.css").toExternalForm());
				
				/*for (Paragraph ps : _textArea.getParagraphs()) {
					ps.getStyleSpans().append(new StyleSpan());
				}*/
				
				Pattern pattern = Pattern.compile(Pattern.quote("{") + "(.*?)" + Pattern.quote("}"), Pattern.DOTALL);
				
				_textArea.replaceText(_msg);
				
				Matcher matcher = pattern.matcher(_msg);
				
				Set<Pair<Integer, Integer>> styleSpans = new LinkedHashSet<>();
				
				int c = 0;
				
				while (matcher.find()) {
					int offsetStart = c*("<b>".length() + "</b>".length() - 1);
					int offsetEnd = c*("<b>".length() + "</b>".length());
					
					Pair<Integer, Integer> styleSpan = new Pair<>(matcher.start(), matcher.end());// - "<b>".length() - "</b>".length());
					
					styleSpans.add(styleSpan);
					
					System.err.println("set style " + styleSpan.getKey() + ";" + styleSpan.getValue());
					
					//_msg = _msg.substring(0, matcher.start()) + _msg.substring(matcher.end(), _msg.length());
					
					//matcher = pattern.matcher(_msg);
					c++;
				}
				
				for (Pair<Integer, Integer> styleSpan : styleSpans) {
					_textArea.setStyle(styleSpan.getKey(), styleSpan.getValue(), Collections.singleton("dialogassertion"));
				}
				
				_alert.getDialogPane().setContent(_textArea);
			}
		});
		
		_alert.getDialogPane().getStylesheets().add(getClass().getResource("Dialog.css").toExternalForm());
		_alert.getDialogPane().getStyleClass().add("dialog");
		
		_buttonType_yes = new ButtonType("yes");
		_buttonType_no = new ButtonType("no");
		
		_alert.getButtonTypes().setAll(_buttonType_yes, _buttonType_no);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		/*_label_question.setText(_msg);
		
		_button_yes.setVisible(false);
		_button_no.setVisible(false);*/
		
		/*_button_yes.setOnAction(new EventHandler<ActionEvent>() {
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