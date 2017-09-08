package gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import core.*;
import gui.hoare.*;
import org.fxmisc.richtext.CodeArea;

import core.Hoare.Executer.Assign_callback;
import core.Hoare.Executer.CompMerge_callback;
import core.Hoare.Executer.CompSecond_callback;
import core.Hoare.Executer.CompFirst_callback;
import core.Hoare.Executer.Skip_callback;
import core.Hoare.Executer.LoopAskInv_callback;
import core.Hoare.HoareException;
import core.Lexer.LexerException;
import core.Lexer.LexerResult;
import core.Parser.ParserException;
import core.structures.nodes.Exp;
import core.structures.hoareCond.HoareCond;
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
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import util.ErrorUtil;
import util.IOUtil;
import util.StringUtil;

public class FileTab extends Tab implements Initializable, MainWindow.ActionInterface {
	@FXML
	private SplitPane _split_main;
	@FXML
	private SplitPane _split_parsing;
	@FXML
	private Pane _pane_code;
	@FXML
	private CodeArea _textArea_code;
	
	@FXML
	private TabPane _tabPane_modus;
	@FXML
	private Tab _tab_parsing;
	@FXML
	private Tab _tab_hoare;
	
	@FXML
	private Pane _pane_tokens;
	@FXML
	private CodeArea _textArea_tokens;
	@FXML
	private Pane _pane_syntaxTree;
	@FXML
	private CheckBox _checkBox_syntaxTree_filterEps;
	@FXML
	private TreeView<SyntaxNode> _treeView_syntaxTreeBase;
	
	@FXML
	private CheckBox _checkBox_hoare_auto;
	@FXML
	private CodeArea _codeArea_hoare;
	private ExtendedCodeArea _extendedCodeArea_hoare;
	@FXML
	private StackPane _pane_hoare_dialogHost;
	
	private SyntaxTreeView _syntaxTreeView;
	private ObjectProperty<SyntaxTree> _syntaxTree = new SimpleObjectProperty<>();
	private ObjectProperty<SyntaxNode> _currentNodeP = new SimpleObjectProperty<>();
	private ObjectProperty<SyntaxNode> _currentHoareNodeP = new SimpleObjectProperty<>();
	
	private ObjectProperty<ObservableMap<SyntaxNode, HoareCond>> _preCondMap = new SimpleObjectProperty<>();
	private ObjectProperty<ObservableMap<SyntaxNode, HoareCond>> _postCondMap = new SimpleObjectProperty<>();
	private SyntaxChart _syntaxChart;
	private Map<KeyCombination, Runnable> _accelerators;
	
	private Scene _scene;
	
	public Scene getScene() {
		return _scene;
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
	
	private boolean _isHoaring = false;
	
	public boolean isHoaring() {
		return _isHoaring;
	}
	
	public void setHoaring(boolean flag) {
		if (flag == _isHoaring) return;
		
		_isHoaring = flag;
		
		for (ActionListener listener : _actionListeners) {
			listener.isHoaredChanged();
		}
		
		if (!_isHoaring) {
			_currentNodeP.set(null);
			_currentHoareNodeP.set(null);
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
		
		_scene = IOUtil.inflateFXML(new File("FileTab.fxml"), this);
		
		_scene.getStylesheets().add(getClass().getResource("ExtendedCodeArea.css").toExternalForm());
		
		setContent(_scene.getRoot());
	}

	public FileTab(Map<KeyCombination, Runnable> accelerators) throws IOException {
		this(null, false, accelerators);
	}
	
	public void select() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				_textArea_code.requestFocus();
				_textArea_code.selectAll();
			}
		});
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			_codeArea = new ExtendedCodeArea(_textArea_code);

			_codeArea.setCurrentNodeP(_currentNodeP, _currentHoareNodeP);
	
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
					setHoaring(false);
					
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
			
			_syntaxChart = new SyntaxChart(_syntaxTree, _preCondMap, _postCondMap, _currentNodeP, _currentHoareNodeP, _accelerators);
			
			updateVisibility();
			
			_extendedCodeArea_hoare = new ExtendedCodeArea(_codeArea_hoare);
		} catch (Exception e) {
			ErrorUtil.logEFX(e);
		}
	}

	private boolean _tokens_shown = false;
	private boolean _syntaxTreeBox_shown = false;
	private boolean _syntaxChart_shown = false;
	
	private double _split_main_dividerPos = 0.4D;
	
	private void updateVisibility() {
		boolean modus_shown = _tokens_shown || _syntaxTreeBox_shown || isHoaring();
		
		if (modus_shown) {
			if (!_split_main.getItems().contains(_tabPane_modus)) {
				_split_main.getItems().add(_tabPane_modus);
				
				_split_main.setDividerPositions(_split_main_dividerPos);

				_split_main.getDividers().get(0).positionProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
						_split_main_dividerPos = newVal.doubleValue();
					}
				});
			}
		} else {
			if (_split_main.getItems().contains(_tabPane_modus)) _split_main.getItems().remove(_tabPane_modus);
		}
		_tabPane_modus.setVisible(modus_shown);
		
		if (_pane_tokens != null) {
			if (_tokens_shown) {
				int index = Math.min(0, _split_parsing.getItems().size());
				
				if (!_split_parsing.getItems().contains(_pane_tokens)) {
					_split_parsing.getItems().add(index, _pane_tokens);
				}
			} else {
				if (_split_parsing.getItems().contains(_pane_tokens)) _split_parsing.getItems().remove(_pane_tokens);
			}

			_pane_tokens.setVisible(_tokens_shown);
		}
		if (_pane_syntaxTree != null) {
			if (_syntaxTreeBox_shown) {
				int index = Math.min(1, _split_parsing.getItems().size());
				
				if (!_split_parsing.getItems().contains(_pane_syntaxTree)) {
					_split_parsing.getItems().add(index, _pane_syntaxTree);
				}
			} else {
				if (_split_parsing.getItems().contains(_pane_syntaxTree)) _split_parsing.getItems().remove(_pane_syntaxTree);
			}
			
			_pane_syntaxTree.setVisible(_syntaxTreeBox_shown);
		}
		
		if (modus_shown) {
			if (_split_parsing.getItems().isEmpty()) {
				_tabPane_modus.getSelectionModel().select(_tab_hoare);
			}
		}
		
		if (_syntaxChart != null) {
			_syntaxChart.setVisible(_syntaxChart_shown);
		}
	}
	
	public void showTokens(boolean show) {
		_tokens_shown = show;
		
		_tabPane_modus.getSelectionModel().select(_tab_parsing);
		
		updateVisibility();
	}

	public void showSyntaxTree(boolean show) {
		_syntaxTreeBox_shown = show;
		
		_tabPane_modus.getSelectionModel().select(_tab_parsing);
		
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

			try {
				Parser parser = new Parser(_grammar);

				try {
					_syntaxTree.set(parser.parse(lexerResult.getTokens()));
				} catch (ParserException e) {
					setParsed(false);

					Token token = e.getToken();

					int pos = (token != null) ? token.getPos() : 0;

					_codeArea.setErrorPos(pos, true);

					throw e;
				}
			} catch (Exception e) {
				throwException(e);
			}
		} catch (LexerException e) {
			setParsed(false);
			
			_codeArea.setErrorPos(e.getCurPos(), true);
			
			throw e;
		}
	}

	@Override
	public void hoare() throws Exception {
		if (isHoaring()) throw new Exception("already in verification process");

		if (!isParsed()) parse();

		if (_syntaxTree == null) throw new Exception("no syntaxTree");

		setHoaring(true);
		_tabPane_modus.getSelectionModel().select(_tab_hoare);
		
		updateVisibility();
		
		try {
			_pane_hoare_dialogHost.getChildren().clear();
			
			Hoare hoare = new Hoare(_syntaxTree, _preCondMap, _postCondMap, _currentNodeP, _currentHoareNodeP, new Hoare.ActionInterface() {
				private int _wlp_printDepth = 0;
				
				private void println_begin() {
					_wlp_printDepth++;
				}
				
				private void println(String s) {
					System.out.println(StringUtil.repeat("\t", _wlp_printDepth - 1) + s);
				}
				
				private void println_end() {
					_wlp_printDepth--;
				}

				private void nodeSynthesize(SyntaxNode node, int nestDepth, StringBuilder sb) {
					for (SyntaxNode child : node.getChildren()) {
						if (child instanceof SyntaxNodeTerminal) sb.append(child.synthesize());
						else {
							sb.append(StringUtil.repeat("\t", nestDepth));
						}
					}
				}

				private String nodeSynthesize(SyntaxNode node) {
					StringBuilder sb = new StringBuilder();

					nodeSynthesize(node, 0, sb);

					return sb.toString();
				}

				private void setNode(SyntaxNode node, HoareCond postCond) {
					_codeArea_hoare.replaceText(((node != null) ? node.synthesize() : null) + " " + ((postCond != null) ? postCond.toStringEx() : null));
					_currentNodeP.set(node);

					/*if (node != null) {
						for (Token token : node.tokenize()) {
							System.out.println(token.getText() + ";" + token.getPos());
							_codeArea_hoare.insertText(token.getPos(), token.getText());
						}
					}*/
				}

				private void pushDialog(HoareDialog dialog) {
					setNode(dialog.getNode(), dialog.getPostCond());

					dialog.setCloseHandler(() -> _pane_hoare_dialogHost.getChildren().remove(dialog.getRoot()));

					_pane_hoare_dialogHost.getChildren().add(dialog.getRoot());
				}

				@Override
				public void reqSkipDialog(SyntaxNode node, HoareCond preCond, HoareCond postCond, Skip_callback callback) throws IOException, HoareException, LexerException, ParserException {
					if (_checkBox_hoare_auto.isSelected()) {
						callback.result();
					} else {
						HoareDialog dialog = new SkipDialog(node, preCond, postCond, new Skip_callback() {
							@Override
							public void result() throws IOException, HoareException, LexerException, ParserException {
								callback.result();
							}
						});

						pushDialog(dialog);
					}
				}

				@Override
				public void reqAssignDialog(SyntaxNode node, HoareCond preCond, HoareCond postCond, Assign_callback callback, String var, Exp exp) throws IOException, HoareException, LexerException, ParserException {
					println_begin();
					
					println("apply assignment rule:");
					println("\t" + postCond.toStringEx(var + ":=" + exp.synthesize()) + " " + var + "=" + exp.synthesize() + " " + postCond.toStringEx());
					println("\t->" + preCond.toStringEx() + " " + var + "=" + exp.synthesize() + " " + postCond.toStringEx());
					
					if (_checkBox_hoare_auto.isSelected()) {
						println_end();
						
						callback.result();
					} else {
						HoareDialog dialog = new AssignDialog(node, preCond, postCond, new Assign_callback() {
							@Override
							public void result() throws IOException, HoareException, LexerException, ParserException {
								println_end();
								
								callback.result();
							}
						}, var, exp);
						
						pushDialog(dialog);
					}
				}

				@Override
				public void reqCompSecondDialog(Hoare.Executer.wlp_comp comp, CompSecond_callback callback) throws IOException, HoareException, LexerException, ParserException {
					println_begin();

					println("applying composition rule...");

					HoareDialog dialog = new CompSecondDialog(comp, new CompSecond_callback() {
						@Override
						public void result() throws IOException, HoareException, LexerException, ParserException {
							callback.result();
						}
					});

					pushDialog(dialog);
				}

				@Override
				public void reqCompFirstDialog(Hoare.Executer.wlp_comp comp, CompFirst_callback callback) throws IOException, HoareException, LexerException, ParserException {
					HoareDialog dialog = new CompFirstDialog(comp, new CompFirst_callback() {
						@Override
						public void result() throws IOException, HoareException, LexerException, ParserException {
							callback.result();
						}
					});

					pushDialog(dialog);
				}

				@Override
				public void reqCompMergeDialog(Hoare.Executer.wlp_comp comp, CompMerge_callback callback) throws IOException, HoareException, LexerException, ParserException {
					HoareDialog dialog = new CompMergeDialog(comp, callback);

					pushDialog(dialog);
				}

				@Override
				public void reqAltFirstDialog(Hoare.Executer.wlp_alt alt, Hoare.Executer.AltThen_callback callback) throws IOException, HoareException, LexerException, ParserException {
					HoareDialog dialog = new AltThenDialog(alt, callback);

					pushDialog(dialog);
				}

				@Override
				public void reqAltElseDialog(Hoare.Executer.wlp_alt alt, Hoare.Executer.AltElse_callback callback) throws IOException, HoareException, LexerException, ParserException {
					HoareDialog dialog = new AltElseDialog(alt, callback);

					pushDialog(dialog);
				}

				@Override
				public void reqAltMergeDialog(Hoare.Executer.wlp_alt alt, Hoare.Executer.AltMerge_callback callback) throws IOException, HoareException, LexerException, ParserException {
					HoareDialog dialog = new AltMergeDialog(alt, callback);

					pushDialog(dialog);
				}

				@Override
				public void reqLoopAskInvDialog(Hoare.Executer.wlp_loop loop, Hoare.Executer.LoopAskInv_callback callback) throws IOException {
					HoareDialog dialog = new LoopAskInvariantDialog(loop, new LoopAskInv_callback() {
						@Override
						public void result(HoareCond postInvariant) throws HoareException, LexerException, IOException, ParserException {
							callback.result(postInvariant);
						}
					});

					pushDialog(dialog);
				}

				@Override
				public void reqLoopCheckPostCondDialog(Hoare.Executer.wlp_loop loop, Hoare.Executer.LoopCheckPostCond_callback callback) throws IOException {
					HoareDialog dialog = new LoopCheckPostCondDialog(loop, callback);

					pushDialog(dialog);
				}

				@Override
				public void reqLoopGetBodyCondDialog(Hoare.Executer.wlp_loop loop, Hoare.Executer.LoopGetBodyCond_callback callback) throws IOException {
					HoareDialog dialog = new LoopGetBodyCondDialog(loop, callback);

					pushDialog(dialog);
				}

				@Override
				public void reqLoopCheckBodyCondDialog(Hoare.Executer.wlp_loop loop, Hoare.Executer.LoopCheckBodyCond_callback callback) throws IOException {
					HoareDialog dialog = new LoopCheckBodyCondDialog(loop, callback);

					pushDialog(dialog);
				}

				@Override
				public void reqLoopAcceptInvCondDialog(Hoare.Executer.wlp_loop loop, Hoare.Executer.LoopAcceptInv_callback callback) throws IOException {
					HoareDialog dialog = new LoopAcceptInvariantDialog(loop, callback);

					pushDialog(dialog);
				}

				@Override
				public void reqConseqPreCheckDialog(SyntaxNode node, HoareCond origPreCond, HoareCond newPreCond, HoareCond origPostCond, HoareCond newPostCond, Hoare.Executer.ConseqCheck_callback callback) throws IOException {
					HoareDialog dialog = new ConseqCheckDialog(node, origPreCond, newPreCond, origPostCond, newPostCond, new Hoare.Executer.ConseqCheck_callback() {
						@Override
						public void result(boolean yes) throws HoareException, LexerException, IOException, ParserException {
							callback.result(yes);
						}
					});

					pushDialog(dialog);
				}

				@Override
				public void finished(SyntaxNode node, HoareCond preCond, HoareCond postCond, boolean yes) throws IOException {
					HoareDialog dialog = new EndDialog(yes);

					pushDialog(dialog);
					setNode(node, postCond);
					
					setHoaring(false);
				}
			});
			
			hoare.exec();
		} catch (Exception e) {
			setHoaring(false);
			
			throw e;
		}
	}
	
	public void hoare_abort() throws Exception {
		if (!isHoaring()) throw new Exception("no verification process active");
		
		_codeArea_hoare.clear();
		_pane_hoare_dialogHost.getChildren().clear();
		
		setHoaring(false);
		
		_tabPane_modus.getSelectionModel().select(_tab_parsing);
		
		updateVisibility();
		
		System.err.println("verification aborted");
	}
}