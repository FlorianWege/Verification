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
import core.SyntaxTreeNode;
import core.structures.hoareCond.HoareCond;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.util.Pair;
import util.ErrorUtil;
import util.IOUtil;

public class CompositionEndDialog implements Initializable {
	@FXML
	private StyleClassedTextArea _textArea_rationale;
	@FXML
	private StyleClassedTextArea _textArea_output;
	@FXML
	private Button _button_continue;
	
	private SyntaxTreeNode _node;
	private HoareCond _preCond;
	private HoareCond _postCond;
	private Hoare.Executer.CompositionEndInterface _callback;
	private SyntaxTreeNode _firstNode;
	private SyntaxTreeNode _secondNode;
	private HoareCond _secondPreCond;
	private HoareCond _firstPreCond;

	private Parent _root;
	
	public Parent getRoot() {
		return _root;
	}
	
	public CompositionEndDialog(SyntaxTreeNode node, HoareCond preCond, HoareCond postCond, Hoare.Executer.CompositionEndInterface callback, SyntaxTreeNode firstNode, SyntaxTreeNode secondNode, HoareCond secondPreCond, HoareCond firstPreCond) throws IOException {
		_node = node;
		_preCond = preCond;
		_postCond = postCond;
		_callback = callback;
		_firstNode = firstNode;
		_secondNode = secondNode;
		_secondPreCond = secondPreCond;
		_firstPreCond = firstPreCond;
		
		Scene scene = IOUtil.inflateFXML(new File("CompositionEndDialog.fxml"), this);
		
		_root = scene.getRoot();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("using Hoare rule: {p} S1 {r}, {r} S2 {q} -> {p} S1; S2 {q}");
		sb.append(System.lineSeparator());
		sb.append("with: {p} " + _firstNode.synthesize() + " {r}, {r} " + _secondNode.synthesize() + " {" + _postCond + "} -> {p} " + _firstNode.synthesize() + "; " + _secondNode.synthesize() + "{" + _postCond + "}");
		sb.append(System.lineSeparator());
		sb.append("step 1: get weakest liberal precondition of S2");
		sb.append(System.lineSeparator());
		sb.append("with: {p} " + _firstNode.synthesize() + " {" + _secondPreCond + "}, {" + _secondPreCond + "} " + _secondNode.synthesize() + " {" + _postCond + "} -> {p} " + _firstNode.synthesize() + "; " + _secondNode.synthesize() + "{" + _postCond + "}");
		sb.append(System.lineSeparator());
		sb.append("step 2: get weakest liberal precondition of S1");
		sb.append(System.lineSeparator());
		sb.append("with: {" + _firstPreCond + "} " + _firstNode.synthesize() + " {" + _secondPreCond + "}, {" + _secondPreCond + "} " + _secondNode.synthesize() + " {" + _postCond + "} -> {" + _preCond + "} " + _firstNode.synthesize() + "; " + _secondNode.synthesize() + "{" + _postCond + "}");
		sb.append(System.lineSeparator());
		sb.append("step 3: merge");
		
		_textArea_rationale.getStylesheets().add(getClass().getResource("Dialog.css").toExternalForm());
		_textArea_output.getStylesheets().add(getClass().getResource("Dialog.css").toExternalForm());
		
		_textArea_rationale.replaceText(sb.toString()); 
		
		String _msg = _preCond.toStringEx() + " " + _node.synthesize() + " " + _postCond.toStringEx();
		
		Pattern pattern = Pattern.compile(Pattern.quote("{") + "(.*?)" + Pattern.quote("}"), Pattern.DOTALL);
		
		_textArea_output.replaceText(_msg);
		
		Matcher matcher = pattern.matcher(_msg);
		
		Set<Pair<Integer, Integer>> styleSpans = new LinkedHashSet<>();
		
		int c = 0;
		
		while (matcher.find()) {
			int offsetStart = c*("<b>".length() + "</b>".length() - 1);
			int offsetEnd = c*("<b>".length() + "</b>".length());
			
			Pair<Integer, Integer> styleSpan = new Pair<>(matcher.start(), matcher.end());// - "<b>".length() - "</b>".length());
			
			styleSpans.add(styleSpan);
			
			System.err.println("set style " + styleSpan.getKey() + ";" + styleSpan.getValue());

			c++;
		}
		
		for (Pair<Integer, Integer> styleSpan : styleSpans) {
			_textArea_output.setStyle(styleSpan.getKey(), styleSpan.getValue(), Collections.singleton("dialogassertion"));
		}
		
		_button_continue.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					_callback.result();
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
	}
}