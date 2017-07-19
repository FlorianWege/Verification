package gui;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.ResourceBundle;

import core.Grammar;
import core.Hoare;
import core.Lexer;
import core.Lexer.LexerException;
import core.Lexer.LexerResult;
import core.Parser;
import core.Parser.NoRuleException;
import core.structures.HoareCondition;
import core.SyntaxTree;
import core.SyntaxTreeNode;
import grammars.HoareWhileGrammar;
import grammars.WhileGrammar;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import util.IOUtil;

public class JavaFXMain extends Application implements Initializable {
	private class FileItem {
		private File _file;
		
		public File getFile() {
			return _file;
		}
		
		@Override
		public String toString() {
			return _file.toString();
		}
		
		public FileItem(File file) {
			_file = file;
		}
	}
	
	@FXML
	private ListView<FileItem> _listView_files;
	@FXML
	private TextArea _textArea_code;
	@FXML
	private Button _button_parse;
	@FXML
	private Button _button_hoare;
	@FXML
	private TreeView<SyntaxTreeNode> _treeView_syntaxTreeBase;
	@FXML
	private CheckBox _checkBox_synxtaxTree_filterEps;
	@FXML
	private TextArea _textArea_output;

	private SyntaxTreeView _syntaxTreeView;
	private ObjectProperty<SyntaxTree> _syntaxTree = new SimpleObjectProperty<>();
	private ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCondition>> _preCondMap = new SimpleObjectProperty<>();
	private ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCondition>> _postCondMap = new SimpleObjectProperty<>();

	private void wrapPrintStreams() {
		PrintStream stdOut = System.out;
		PrintStream stdErr = System.err;
		
		PrintStream printStreamOut = new PrintStream(stdOut) {
			@Override
			public void write(byte[] arg0, int arg1, int arg2) {
				if (_textArea_output != null) {
					_textArea_output.appendText(new String(arg0, arg1, arg2));
				}
				super.write(arg0, arg1, arg2);
			}
		};
		
		PrintStream printStreamErr = new PrintStream(stdErr) {
			@Override
			public void write(byte[] arg0, int arg1, int arg2) {
				if (_textArea_output != null) {
					_textArea_output.appendText(new String(arg0, arg1, arg2));
				}
				super.write(arg0, arg1, arg2);
			}
		};

		System.setOut(printStreamOut);
		System.setErr(printStreamErr);
	}
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
		primaryStage.setScene(IOUtil.inflateFXML(new File("MainWindow.fxml"), this));
		primaryStage.setTitle("Verification JavaFX GUI");
		primaryStage.show();
		
		wrapPrintStreams();
		
		_preCondMap.set(FXCollections.observableHashMap());
		_postCondMap.set(FXCollections.observableHashMap());
		
		new SyntaxChart(_syntaxTree, _preCondMap, _postCondMap);
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	private Collection<File> files = Arrays.asList(new File[] {
		new File("Factorial.txt"),
		new File("Euclid.txt"),
		new File("Assign.txt")
	});
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ObservableList<FileItem> fileItems = FXCollections.observableArrayList();
		
		for (File file : files) {
			fileItems.add(new FileItem(file));
		}
		
		_listView_files.setItems(fileItems);
		_listView_files.setPrefHeight((_listView_files.getItems().size() + 1) * 25);
		_listView_files.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FileItem>() {
			@Override
			public void changed(ObservableValue<? extends FileItem> obs, FileItem oldVal, FileItem newVal) {
				try {
					String s = IOUtil.getResourceAsString(newVal.getFile().toString());
					
					_textArea_code.setText(s);
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
		
		_syntaxTreeView = new SyntaxTreeView(_treeView_syntaxTreeBase, _checkBox_synxtaxTree_filterEps, _syntaxTree);
		
		_button_parse.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				_syntaxTree.set(null);;

				_textArea_output.clear();
				
				String codeS = _textArea_code.getText();
				
				Grammar grammar = new HoareWhileGrammar();

				try {
					LexerResult lexerResult = new Lexer(grammar).tokenize(codeS);
					
					lexerResult.print();

					Parser parser = new Parser(grammar);
					
					_syntaxTree.set(parser.parse(lexerResult.getTokens()));
				} catch (LexerException | NoRuleException e) {
					e.printStackTrace();
				}
			}
		});
		
		_button_hoare.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (_syntaxTree == null) {
					System.err.println("no syntaxTree");
					
					return;
				}
				
				try {
					Hoare hoare = new Hoare(_syntaxTree, _preCondMap, _postCondMap);
					
					hoare.exec();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}