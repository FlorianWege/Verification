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

public class ImplicationDialog implements Initializable {
	@FXML
	private StyleClassedTextArea _textArea;
	@FXML
	private Button _button_yes;
	@FXML
	private Button _button_no;
	
	private HoareCond _a;
	private HoareCond _b;
	private Hoare.Executer.ImplicationInterface _callback;
	private boolean _equal;
	
	private String _msg;
	private Parent _root;
	
	public Parent getRoot() {
		return _root;
	}
	
	public ImplicationDialog(HoareCond a, HoareCond b, Hoare.Executer.ImplicationInterface callback, boolean equal) throws IOException {
		_a = a;
		_b = b;
		_callback = callback;
		_equal = equal;
		
		_msg = _equal ? String.format("Is\n%s\nequal to\n%s\n?", _a.toStringEx(), _b.toStringEx()) : String.format("Does\n%s\nimplicate\n%s\n?", _a.toStringEx(), _b.toStringEx());
		
		Scene scene = IOUtil.inflateFXML(new File("ImplicationDialog.fxml"), this);
		
		_root = scene.getRoot();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		_button_yes.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					_callback.result(true);
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});

		_button_no.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					_callback.result(false);
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		
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
	}
}