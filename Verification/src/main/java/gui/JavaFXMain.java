package gui;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javax.swing.plaf.synth.SynthSpinnerUI;

import core.Grammar;
import core.Hoare;
import core.Hoare.HoareException;
import core.HoareCondition;
import core.Lexer;
import core.Lexer.LexerException;
import core.Lexer.LexerResult;
import core.LexerRule;
import core.Parser;
import core.Parser.NoRuleException;
import core.SyntaxTree;
import core.SyntaxTreeNode;
import core.SyntaxTreeNodeTerminal;
import grammars.WhileGrammar;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Callback;
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
	
	private class SyntaxNode extends TreeItem<SyntaxTreeNode> {
		private SyntaxTreeNode _node;
		
		@Override
		public String toString() {
			return _node.toString();
		}
		
		public SyntaxTreeNode getNode() {
			return _node;
		}
		
		private int _reqChildren = 0;
		
		public int getReqChildren() {
			return _reqChildren;
		}
		
		private boolean updateReqChildren() {
			if (_node instanceof SyntaxTreeNodeTerminal) {
				if (((SyntaxTreeNodeTerminal) _node).getToken() == null) {
					_reqChildren = 0;
					
					return false;
				}
				
				_reqChildren = 1;
				
				return true;
			}
			
			int reqChildren = 0;

			List<TreeItem<SyntaxTreeNode>> children = new ArrayList<>(getChildren());

			for (TreeItem<SyntaxTreeNode> child : children) {
				SyntaxNode childItem = ((SyntaxNode) child);
				
				if (childItem.updateReqChildren()) {
					reqChildren++;
				}
			}

			_reqChildren = reqChildren;

			return (reqChildren > 0);
		}
		
		public void addChild(SyntaxNode child) {
			getChildren().add(child);
			
			updateReqChildren();
		}
		
		public SyntaxNode(SyntaxTreeNode node) {
			_node = node;
			
			setValue(_node);
			
			/*getChildren().addListener(new ListChangeListener<TreeItem<SyntaxTreeNode>>() {
				@Override
				public void onChanged(javafx.collections.ListChangeListener.Change<? extends TreeItem<SyntaxTreeNode>> arg0) {
					if (arg0.wasAdded()) {
						updateReqChildren();
					}
				}
			});*/
			
			//updateReqChildren();
		}
	}
	
	private class SyntaxTreeNodeCell extends TreeCell<SyntaxTreeNode> {
		@Override
		public void cancelEdit() {
			super.cancelEdit();
		}

		@Override
		public void commitEdit(SyntaxTreeNode arg0) {
			super.commitEdit(arg0);
		}

		@Override
		public void startEdit() {
			super.startEdit();
		}
		
		@Override
		public void updateItem(SyntaxTreeNode item, boolean empty) {
			super.updateItem(item, empty);
			
			if (empty) {
				setText(null);
				setGraphic(null);
				//backgroundProperty().unbind();
				backgroundProperty().set(Background.EMPTY);
			} else {
				if (isEditing()) {
					setText(null);
					setGraphic(null);
					//backgroundProperty().unbind();
					backgroundProperty().set(Background.EMPTY);
				} else {
					setText(getItem().toString());
					setGraphic(getTreeItem().getGraphic());
					/*backgroundProperty().bind(new SimpleObjectProperty<Background>() {
						
					});*/
					if (((SyntaxNode) getTreeItem())._reqChildren > 0) {
						//backgroundProperty().set(new Background());
						//styleProperty().set("-fx-background:red;");
						backgroundProperty().set(new Background(new BackgroundFill(Color.ALICEBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
					} else {
						backgroundProperty().set(new Background(new BackgroundFill(Color.BISQUE, CornerRadii.EMPTY, Insets.EMPTY)));
						//styleProperty().set("-fx-background:green;");
					}
				}
			}
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
	private TreeView<SyntaxTreeNode> _treeView_syntaxTree;
	@FXML
	private CheckBox _checkBox_synxtaxTree_filterEps;
	@FXML
	private TextArea _textArea_output;

	private SyntaxTree _syntaxTree;

	@Override
	public void start(Stage primaryStage) throws IOException {
		URL mainWindowURL = getClass().getResource("MainWindow.fxml");
		
		if (mainWindowURL == null) throw new IOException("MainWindow.fxml not found");
		
		FXMLLoader loader = new FXMLLoader(mainWindowURL);
		
		loader.setController(this);
		
		loader.load();
		
		Parent root = loader.getRoot();
		
		Scene scene = new Scene(root);
		
		primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
		primaryStage.setTitle("Verification JavaFX GUI");
		primaryStage.setScene(scene);
		primaryStage.show();
		
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

	public static void main(String[] args) {
		launch(args);
	}

	private SyntaxNode addNode(SyntaxTreeNode node) {
		SyntaxNode nodeItem = new SyntaxNode(node);
		
		nodeItem.setExpanded(true);
		
		for (SyntaxTreeNode child : node.getChildren()) {
			nodeItem.addChild(addNode(child));
		}
		
		return nodeItem;
	}
	
	private void filterNode(SyntaxNode nodeItem) {
		List<TreeItem<SyntaxTreeNode>> children = new ArrayList<>(nodeItem.getChildren());

		for (TreeItem<SyntaxTreeNode> child : children) {
			SyntaxNode childItem = ((SyntaxNode) child);
			
			if (childItem.getReqChildren() == 0) {
				nodeItem.getChildren().remove(childItem);
			} else {
				filterNode(childItem);
			}
		}
		
		/*children = new ArrayList<>(nodeItem.getChildren());
		
		for (TreeItem<SyntaxTreeNode> child : children) {
			SyntaxNode childItem = ((SyntaxNode) child);
			
			filterNode(childItem);
		}*/
	}
	
	private void updateTree() {
		_treeView_syntaxTree.setRoot(null);
		
		if (_syntaxTree == null) return;
		
		SyntaxTreeNode root = _syntaxTree.getRoot();
		
		SyntaxNode rootItem = addNode(root);

		if (!_checkBox_synxtaxTree_filterEps.isSelected()) {
			filterNode(rootItem);
		}
		
		_treeView_syntaxTree.setRoot(rootItem);
	}
	
	private Collection<File> files = Arrays.asList(new File[] {
		new File("Factorial.txt"),
		new File("Euclid.txt"),
		new File("Assign.txt")
	});
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("initialize");
		
		ObservableList<FileItem> fileItems = FXCollections.observableArrayList();
		
		for (File file : files) {
			fileItems.add(new FileItem(file));
		}
		
		_listView_files.setItems(fileItems);
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
		
		_button_parse.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				_treeView_syntaxTree.setRoot(null);
				_textArea_output.clear();
				
				String codeS = _textArea_code.getText();
				
				Grammar grammar = new WhileGrammar();

				try {
					LexerResult lexerResult = new Lexer(grammar).tokenize(codeS);
					
					lexerResult.print();

					Parser parser = new Parser(grammar);
					
					_syntaxTree = parser.parse(lexerResult.getTokens());
					
					updateTree();
				} catch (LexerException | NoRuleException e) {
					e.printStackTrace();
				}
			}
		});
		
		_checkBox_synxtaxTree_filterEps.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) {
				updateTree();
			}
		});
		
		_button_hoare.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (_syntaxTree == null) {
					System.err.println("no syntaxTree");
					
					return;
				}

				Hoare hoare = new Hoare(_syntaxTree);
				
				try {
					hoare.exec(HoareCondition.fromString("x<1"), HoareCondition.fromString("x>4"));
				} catch (HoareException | NoRuleException | LexerException e) {
					e.printStackTrace();
				}
			}
		});
		
		_treeView_syntaxTree.setCellFactory(new Callback<TreeView<SyntaxTreeNode>, TreeCell<SyntaxTreeNode>>() {
			@Override
			public TreeCell<SyntaxTreeNode> call(TreeView<SyntaxTreeNode> arg0) {
				return new SyntaxTreeNodeCell();
			}
		});
		
		System.out.println("initialized");
	}
}