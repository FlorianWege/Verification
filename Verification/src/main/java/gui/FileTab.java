package gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.fxmisc.richtext.CodeArea;

import core.Grammar;
import core.Hoare;
import core.Hoare.HoareException;
import core.Lexer;
import core.Lexer.LexerException;
import core.Lexer.LexerResult;
import core.Parser;
import core.Parser.NoRuleException;
import core.Parser.ParserException;
import core.structures.hoareCond.HoareCond;
import core.SyntaxTree;
import core.SyntaxTreeNode;
import core.Token;
import grammars.HoareWhileGrammar;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import util.ErrorUtil;
import util.IOUtil;
import util.StringUtil;

public class FileTab extends Tab implements Initializable, MainWindow.ActionInterface {
	@FXML
	private SplitPane _split_center;
	@FXML
	private Pane _pane_code;
	@FXML
	private CodeArea _textArea_code;
	@FXML
	private Pane _pane_tokens;
	@FXML
	private CodeArea _textArea_tokens;
	@FXML
	private Pane _pane_syntaxTree;
	@FXML
	private CheckBox _checkBox_syntaxTree_filterEps;
	@FXML
	private TreeView<SyntaxTreeNode> _treeView_syntaxTreeBase;
	
	private SyntaxTreeView _syntaxTreeView;
	private ObjectProperty<SyntaxTree> _syntaxTree = new SimpleObjectProperty<>();
	
	private ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCond>> _preCondMap = new SimpleObjectProperty<>();
	private ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCond>> _postCondMap = new SimpleObjectProperty<>();
	private SyntaxChart _syntaxChart;
	private Map<KeyCombination, Runnable> _accelerators;
	
	private Parent _root;
	
	public Node getRoot() {
		return _root;
	}
	
	public interface ActionListener {
		public void isSavedChanged();
		public void isParsedChanged();
		public void isHoaredChanged();
		public void throwException(Exception e);
	}
	
	private Set<ActionListener> _actionListeners = new LinkedHashSet<>();
	
	public void addActionListener(ActionListener listener) {
		_actionListeners.add(listener);
	}
	
	private boolean _isSaved = true;

	public boolean isSaved() {
		return _isSaved;
	}
	
	public void setSaved(boolean flag) {
		if (flag == _isSaved) return;

		_isSaved = flag;
		
		for (ActionListener listener : _actionListeners) {
			listener.isSavedChanged();
		}
		
		updateText();
	}
	
	private boolean _isParsed = false;
	
	public boolean isParsed() {
		return _isParsed;
	}
	
	public void setParsed(boolean flag) {
		if (flag == _isParsed) return;
		
		_isParsed = flag;
		
		for (ActionListener listener : _actionListeners) {
			listener.isParsedChanged();
		}
	}
	
	private boolean _isHoared = false;
	
	public boolean isHoared() {
		return _isHoared;
	}
	
	public void setHoared(boolean flag) {
		if (flag == _isHoared) return;
		
		_isHoared = flag;
		
		for (ActionListener listener : _actionListeners) {
			listener.isHoaredChanged();
		}
	}
	
	public class AutoParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public AutoParseException(Exception e) {
			super(e);
		}
	}
	
	public void throwException(Exception e) {
		for (ActionListener listener : _actionListeners) {
			listener.throwException(e);
		}
	}
	
	private void updateText() {
		StringBuilder text_sb = new StringBuilder();
		StringBuilder tooltip_sb = new StringBuilder();
		
		if (!_isSaved) {
			text_sb.append("*");
		}
		
		if (_name != null) {
			text_sb.append(_name);
			tooltip_sb.append(_name);
		} else if (_file != null) {
			text_sb.append(_file.getName());
			tooltip_sb.append(_file.toString());
		}
		
		if (!_isSaved) {
			tooltip_sb.append(" (edited)");
		}
		
		setText(text_sb.toString());
		setTooltip(new Tooltip(tooltip_sb.toString()));
	}

	private String _name = null;

	public String getName() {
		return _name;
	}
	
	public void setName(String val) {
		_name = val;

		updateText();
	}
	
	private Grammar _grammar = new HoareWhileGrammar();
	
	private ExtendedCodeArea _codeArea;
	private ExtendedCodeAreaToken _tokenArea;
	private File _file;
	
	public File getFile() {
		return _file;
	}
	
	private boolean _isInternalFile;
	
	public boolean isInternalFile() {
		return _isInternalFile;
	}
	
	private void setFile(File file, boolean isInternalFile) {
		_file = file;
		_isInternalFile = isInternalFile;
		
		updateText();
	}
	
	public void save(File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		writer.write(_textArea_code.getText());
		
		writer.close();

		setFile(file, false);
		
		setSaved(true);
	}
	
	private MainWindow _mainWindow;
	
	public void setMainWindow(MainWindow mainWindow) {
		_mainWindow = mainWindow;
	}
	
	public FileTab(File file, boolean isInternalFile, Map<KeyCombination, Runnable> accelerators) throws IOException {
		super();

		_accelerators = accelerators;
		
		setFile(file, isInternalFile);
		
		Scene scene = IOUtil.inflateFXML(new File("FileTab.fxml"), this);
		
		_root = scene.getRoot();
		
		setContent(_root);
	}

	public FileTab(Map<KeyCombination, Runnable> accelerators) throws IOException {
		this(null, false, accelerators);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			_codeArea = new ExtendedCodeArea(_textArea_code, _grammar);
	
			if (_file != null) {
				if (_isInternalFile) {
					String s = IOUtil.getResourceAsString(_file.toString());
					
					_textArea_code.replaceText(s);
				} else {
					BufferedReader reader = new BufferedReader(new FileReader(_file));
					
					StringBuilder sb = new StringBuilder();
					String line;
					
					while ((line = reader.readLine()) != null) {
						if (sb.length() > 0) {
							sb.append(System.lineSeparator());
						}

						sb.append(line);
					}
					
					reader.close();
					
					_textArea_code.replaceText(sb.toString());
				}
			}
			
			_textArea_code.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					setSaved(false);
					setParsed(false);
					setHoared(false);
					
					if (_mainWindow != null && _mainWindow.getMenuParseAuto().isSelected()) {
						try {
							parse();
						} catch (Exception e) {
							throwException(new AutoParseException(e));
						}
					}
				}
			});
			
			_tokenArea = new ExtendedCodeAreaToken(_textArea_tokens, _grammar);
			
			_syntaxTreeView = new SyntaxTreeView(_treeView_syntaxTreeBase, _checkBox_syntaxTree_filterEps, _syntaxTree);

			_preCondMap.set(FXCollections.observableHashMap());
			_postCondMap.set(FXCollections.observableHashMap());
			
			_syntaxChart = new SyntaxChart(_syntaxTree, _preCondMap, _postCondMap, _accelerators);
			
			updateVisibility();
		} catch (Exception e) {
			ErrorUtil.logEFX(e);
		}
	}

	private boolean _tokens_shown = false;
	private boolean _syntaxTreeBox_shown = false;
	private boolean _syntaxChart_shown = false;
	
	private void updateVisibility() {
		if (_pane_tokens != null) {
			if (_tokens_shown) {
				int index = Math.min(1, _split_center.getItems().size());
				
				if (!_split_center.getItems().contains(_pane_tokens)) {
					_split_center.getItems().add(index, _pane_tokens);
				}
			} else {
				if (_split_center.getItems().contains(_pane_tokens)) {
					_split_center.getItems().remove(_pane_tokens);
				}
			}

			_pane_tokens.setVisible(_tokens_shown);
		}
		if (_pane_syntaxTree != null) {
			if (_syntaxTreeBox_shown) {
				int index = Math.min(2, _split_center.getItems().size());
				
				if (!_split_center.getItems().contains(_pane_syntaxTree)) {
					_split_center.getItems().add(index, _pane_syntaxTree);
				}
			} else {
				if (_split_center.getItems().contains(_pane_syntaxTree)) {
					_split_center.getItems().remove(_pane_syntaxTree);
				}
			}
			
			_pane_syntaxTree.setVisible(_syntaxTreeBox_shown);
		}
		if (_syntaxChart != null) {
			_syntaxChart.setVisible(_syntaxChart_shown);
		}
	}
	
	public void showTokens(boolean show) {
		_tokens_shown = show;
		
		updateVisibility();
	}

	public void showSyntaxTree(boolean show) {
		_syntaxTreeBox_shown = show;
		
		updateVisibility();
	}
	
	public void showSyntaxChart(boolean show) {
		_syntaxChart_shown = show;
		
		updateVisibility();
	}
	
	@Override
	public void parse() throws Exception {
		if (_isParsed) throw new Exception("already parsed");

		setParsed(true);
		
		_syntaxTree.set(null);

		_textArea_tokens.clear();

		String codeS = _textArea_code.getText();

		try {
			LexerResult lexerResult = new Lexer(_grammar).tokenize(codeS);

			StringBuilder sb = new StringBuilder();
			
			int lastX = 0;
			int lastY = 0;
			
			for (Token token : lexerResult.getTokens()) {
				int x = token.getLineOffset();
				int y = token.getLine();
				
				if (y > lastY) {
					lastX = 0;
					sb.append(StringUtil.repeat("\n", y - lastY));
				}
				
				lastY = y;
				
				if (x > lastX) {
					sb.append(StringUtil.repeat(" ", x - lastX));
				}
				
				String tokenS = token.getTerminal().getKey().toString();
				
				lastX = x + tokenS.length();
				
				sb.append(tokenS);
				
				sb.append(" ");
			}

			_textArea_tokens.replaceText(sb.toString());
			_textArea_tokens.positionCaret(0);

			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					try {
						Parser parser = new Parser(_grammar);
						
						try {
							_syntaxTree.set(parser.parse(lexerResult.getTokens()));
						} catch (ParserException e) {
							
							System.err.println("PARSER EXCP");
							
							setParsed(false);
							
							Token token = e.getToken();
							
							int pos = (token != null) ? token.getPos() : 0;
							
							_textArea_code.positionCaret(pos);
							_textArea_code.requestFocus();
							
							_codeArea.setErrorPos(pos);
							
							throw e;
						}
					} catch (Exception e) {
						throwException(e);
					}
				}
			});
		} catch (LexerException e) {
			setParsed(false);
			
			_textArea_code.positionCaret(e.getCurPos());
			_textArea_code.requestFocus();
			
			_codeArea.setErrorPos(e.getCurPos());
			
			throw e;
		}
	}

	@Override
	public void hoare() throws Exception {
		if (_isHoared) throw new Exception("already hoared");
		
		if (_syntaxTree == null) throw new Exception("no syntaxTree");

		setHoared(true);
		
		try {
			Hoare hoare = new Hoare(_syntaxTree, _preCondMap, _postCondMap);
			
			hoare.exec();
		} catch (HoareException e) {
			setHoared(false);
			
			throw e;
		}
	}
}