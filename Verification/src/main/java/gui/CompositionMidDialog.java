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

public class CompositionMidDialog implements Initializable {
	@FXML
	private StyleClassedTextArea _textArea_rationale;
	@FXML
	private Button _button_continue;
	
	private SyntaxTreeNode _node;
	private HoareCond _postCond;
	private Hoare.Executer.CompositionMidInterface _callback;
	private SyntaxTreeNode _firstNode;
	private SyntaxTreeNode _secondNode;
	private HoareCond _secondPreCond;

	private Parent _root;
	
	public Parent getRoot() {
		return _root;
	}
	
	public CompositionMidDialog(SyntaxTreeNode node, HoareCond postCond, Hoare.Executer.CompositionMidInterface callback, SyntaxTreeNode firstNode, SyntaxTreeNode secondNode, HoareCond secondPreCond) throws IOException {
		_node = node;
		_postCond = postCond;
		_callback = callback;
		_firstNode = firstNode;
		_secondNode = secondNode;
		_secondPreCond = secondPreCond;
		
		Scene scene = IOUtil.inflateFXML(new File("CompositionMidDialog.fxml"), this);
		
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
		
		_textArea_rationale.getStylesheets().add(getClass().getResource("Dialog.css").toExternalForm());
		
		_textArea_rationale.replaceText(sb.toString());
		
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