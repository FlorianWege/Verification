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

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;

import core.Hoare;
import core.Hoare.HoareException;
import core.Lexer.LexerException;
import core.Parser.ParserException;
import core.SyntaxTreeNode;
import core.structures.hoareCond.HoareCond;
import grammars.HoareWhileGrammar;
import grammars.WhileGrammar;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Pair;
import util.ErrorUtil;
import util.IOUtil;

public class InvariantDialog implements Initializable {
	private int _acceptState = 0;
	private Stage _stage;
	private Alert _alert;

	private HoareWhileGrammar _grammar;
	private SyntaxTreeNode _loopNode;
	private HoareCond _postCond;
	private HoareCond _invariant;
	private Hoare.Executer.InvariantInterface _callback;

	private ButtonType _buttonType_yes;
	private ButtonType _buttonType_cancel;
	
	public void show() throws HoareException, LexerException, IOException, ParserException {
		//_stage.show();
		_alert.showAndWait();
		
		/*if (_alert.getResult().equals(_buttonType_yes)) {
			_callback.result(_invariant);
		} else if (_alert.getResult().equals(_buttonType_cancel)) {
			_callback.result(null);
		}*/
	}
	
	public InvariantDialog(HoareWhileGrammar grammar, SyntaxTreeNode loopNode, HoareCond postCond, Hoare.Executer.InvariantInterface callback) throws IOException {
		_grammar = grammar;
		_loopNode = loopNode;
		_postCond = postCond;
		_callback = callback;
		
		Node invariantContent = IOUtil.inflateFXML(new File("InvariantDialog.fxml"), new Initializable() {
			@FXML
			private CodeArea _textArea_loop;
			@FXML
			private TextField _textField_invariant;
			@FXML
			private Button _button_suggest;
			@FXML
			private StyleClassedTextArea _textArea_question_postCond;
			@FXML
			private StyleClassedTextArea _textArea_question_invariant;
			@FXML
			private Button _button_yes;
			@FXML
			private Button _button_abort;
	
			private void updateYesButton() {
				if (_acceptState == 0) {
					_button_yes.setText("Confirm post");
					_button_yes.setDisable(true);
				} else if (_acceptState == 1) {
					_button_yes.setText("Confirm post");
					_button_yes.setDisable(false);
				} else {
					_button_yes.setText("Accept invariant");
					_button_yes.setDisable(false);
				}
			}
			
			@Override
			public void initialize(URL url, ResourceBundle resources) {
				SyntaxTreeNode condNode = _loopNode.findChild(_grammar.NON_TERMINAL_BOOL_EXP);
				SyntaxTreeNode bodyNode = _loopNode.findChild(_grammar.NON_TERMINAL_PROG);
				
				StringBuilder sb = new StringBuilder();
				
				WhileGrammar grammar = new HoareWhileGrammar();
				
				sb.append(grammar.TERMINAL_WHILE);
				sb.append(" ");
				sb.append(condNode.toString());
				sb.append(" ");
				sb.append(grammar.TERMINAL_DO);
				sb.append("\n");
				
				String[] parts = bodyNode.synthesize().split(";");
				
				for (int i = 0; i < parts.length; i++) {
					sb.append(parts[i]);
					if (i != parts.length - 1) {
						sb.append(";");
					}
					sb.append("\n");
				}
				sb.append(grammar.TERMINAL_OD);
				sb.append("\n");
				sb.append(_postCond.toStringEx());
				
				new ExtendedCodeArea(_textArea_loop, grammar);
				
				_textArea_loop.replaceText(sb.toString());
				
				_textArea_question_postCond.setEditable(false);
				_textArea_question_invariant.setEditable(false);
				
				_textArea_question_postCond.getStylesheets().add(getClass().getResource("Dialog.css").toExternalForm());
				_textArea_question_invariant.getStylesheets().add(getClass().getResource("Dialog.css").toExternalForm());
				
				_button_suggest.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						try {
							_invariant = HoareCond.fromString(_textField_invariant.getText());
							
							String msg = String.format("Does\n%s\nimplicate the post condition\n%s\n? (else it won't be a candidate)", _invariant.toStringEx(), _postCond.toStringEx());
							
							_textArea_question_postCond.replaceText(msg);
							
							Pattern pattern = Pattern.compile(Pattern.quote("{") + "(.*?)" + Pattern.quote("}"), Pattern.DOTALL);
							
							Matcher matcher = pattern.matcher(msg);
							
							Set<Pair<Integer, Integer>> styleSpans = new LinkedHashSet<>();
							
							while (matcher.find()) {								
								Pair<Integer, Integer> styleSpan = new Pair<>(matcher.start(), matcher.end());
								
								styleSpans.add(styleSpan);
							}
							
							for (Pair<Integer, Integer> styleSpan : styleSpans) {
								_textArea_question_postCond.setStyle(styleSpan.getKey(), styleSpan.getValue(), Collections.singleton("dialogassertion"));
							}
							
							_acceptState = 1;
							
							updateYesButton();
						} catch (Exception e) {
							ErrorUtil.logEFX(e);
						}
					}
				});
				
				_button_yes.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						try {
							_acceptState++;
							
							updateYesButton();
							
							if (_acceptState == 2) {
								String msg = String.format("Does\n%s\nimplicate\n%s\n? (first will be variant)", _invariant.toStringEx(), _postCond.toStringEx());
								
								_textArea_question_invariant.replaceText(msg);
								
								Pattern pattern = Pattern.compile(Pattern.quote("{") + "(.*?)" + Pattern.quote("}"), Pattern.DOTALL);
								
								Matcher matcher = pattern.matcher(msg);
								
								Set<Pair<Integer, Integer>> styleSpans = new LinkedHashSet<>();
								
								while (matcher.find()) {								
									Pair<Integer, Integer> styleSpan = new Pair<>(matcher.start(), matcher.end());
									
									styleSpans.add(styleSpan);
								}
								
								for (Pair<Integer, Integer> styleSpan : styleSpans) {
									_textArea_question_invariant.setStyle(styleSpan.getKey(), styleSpan.getValue(), Collections.singleton("dialogassertion"));
								}
							} else if (_acceptState == 3) {
								_alert.close();
								
								_callback.result(_invariant);
							}
						} catch (Exception e) {
							ErrorUtil.logEFX(e);
						}
					}
				});
				
				updateYesButton();
				
				_button_abort.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						try {
							_alert.close();
							
							_callback.result(null);
						} catch (Exception e) {
							ErrorUtil.logEFX(e);
						}
					}
				});
			}
		}).getRoot();
		
		_alert = new Alert(AlertType.CONFIRMATION, null);
		
		_alert.getDialogPane().getStylesheets().add(getClass().getResource("Dialog.css").toExternalForm());
		_alert.getDialogPane().getStyleClass().add("dialog");
		
		_alert.getDialogPane().setContent(invariantContent);
		
		_buttonType_yes = new ButtonType("yes");
		_buttonType_cancel = new ButtonType("cancel");
		
		//_alert.getButtonTypes().setAll(_buttonType_yes, _buttonType_cancel);
		_alert.getButtonTypes().setAll();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
}