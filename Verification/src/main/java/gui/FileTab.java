package gui;

import core.*;
import core.Hoare.HoareException;
import core.Lexer.LexerException;
import core.Lexer.LexerResult;
import core.Parser.ParserException;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.BoolImpl;
import core.structures.semantics.boolExp.BoolLit;
import core.structures.semantics.prog.Assign;
import core.structures.semantics.prog.HoareCond;
import core.structures.semantics.prog.Skip;
import core.structures.syntax.SyntaxNode;
import grammars.HoareWhileGrammar;
import gui.hoare.*;
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
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;
import util.ErrorUtil;
import util.IOUtil;
import util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class FileTab extends Tab implements Initializable, MainWindow.ActionInterface {
	@FXML
	private SplitPane _split_main;
	@FXML
	private SplitPane _split_parsing;
	@FXML
	private Pane _pane_code;
	@FXML
	private CodeArea _codeArea_code;
	private ExtendedCodeArea _extendedCodeArea_code;
	
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
	private ExtendedCodeArea _extendedCodeArea_tokens;
	@FXML
	private Pane _pane_syntaxTree;
	@FXML
	private CheckBox _checkBox_syntaxTree_filterEps;
	@FXML
	private TreeView<SyntaxNode> _treeView_syntaxTreeHost;

	@FXML
	private Pane _pane_semanticTree;
	@FXML
	private TreeView<SemanticNode> _treeView_semanticTreeHost;
	
	@FXML
	private CheckBox _checkBox_hoare_auto;
	@FXML
	private CodeArea _codeArea_hoare;
	private ExtendedCodeArea _extendedCodeArea_hoare;
	@FXML
	private StyleClassedTextArea _textArea_hoare_postCond;
	@FXML
	private StackPane _pane_hoare_dialogHost;
	
	private final SyntaxTreeView _syntaxTreeView;
	private final ObjectProperty<SyntaxNode> _syntaxTreeP = new SimpleObjectProperty<>();
	private final SemanticTreeView _semanticTreeView;
	private final ObjectProperty<SemanticNode> _semanticTreeP = new SimpleObjectProperty<>();

	private final ObjectProperty<SemanticNode> _currentNodeP = new SimpleObjectProperty<>();
	private final ObjectProperty<SemanticNode> _currentHoareNodeP = new SimpleObjectProperty<>();
	
	private final ObjectProperty<ObservableMap<SemanticNode, HoareCond>> _preCondMapP = new SimpleObjectProperty<>();
	private final ObjectProperty<ObservableMap<SemanticNode, HoareCond>> _postCondMapP = new SimpleObjectProperty<>();
	private final TreeChartWindow _treeChartWindow;
	private final Map<KeyCombination, Runnable> _accelerators;
	
	private final Scene _scene;
	
	public @Nonnull Scene getScene() {
		return _scene;
	}
	
	public interface ActionListener {
		void isSavedChanged();
		void isParsedChanged();
		void isHoaredChanged();
		void throwException(Exception e);
	}
	
	private Set<ActionListener> _actionListeners = new LinkedHashSet<>();
	
	public void addActionListener(@Nonnull ActionListener listener) {
		_actionListeners.add(listener);
	}

	private final Grammar _grammar = HoareWhileGrammar.getInstance();

	private File _file;
	
	public File getFile() {
		return _file;
	}
	
	private boolean _isInternalFile;
	
	public boolean isInternalFile() {
		return _isInternalFile;
	}
	
	private void setFile(@Nullable File file, boolean isInternalFile) {
		_file = file;
		_isInternalFile = isInternalFile;
		
		updateText();
	}
	
	public void save(@Nonnull File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		writer.write(_codeArea_code.getText());
		
		writer.close();

		setFile(file, false);
		
		setSaved(true);
	}
	
	public FileTab(@Nullable File file, boolean isInternalFile, @Nonnull Map<KeyCombination, Runnable> accelerators) throws IOException {
		super();

		_accelerators = accelerators;
		
		setFile(file, isInternalFile);
		
		_scene = IOUtil.inflateFXML(new File("FileTab.fxml"), this);
		
		_scene.getStylesheets().add(getClass().getResource("ExtendedCodeArea.css").toExternalForm());
		
		setContent(_scene.getRoot());

		_preCondMapP.set(FXCollections.observableHashMap());
		_postCondMapP.set(FXCollections.observableHashMap());

		_syntaxTreeView = new SyntaxTreeView(_treeView_syntaxTreeHost, _syntaxTreeP, _checkBox_syntaxTree_filterEps);
		_semanticTreeView = new SemanticTreeView(_treeView_semanticTreeHost, _semanticTreeP);
		_treeChartWindow = new TreeChartWindow(_syntaxTreeP, _semanticTreeP, _preCondMapP, _postCondMapP, _currentNodeP, _currentHoareNodeP, _accelerators);
	}

	public FileTab(Map<KeyCombination, Runnable> accelerators) throws IOException {
		this(null, false, accelerators);
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

	private boolean _isAutoParsing = false;

	public boolean isAutoParsing() {
		return _isAutoParsing;
	}

	public void setAutoParsing(boolean flag) throws AutoParseException {
		_isAutoParsing = flag;

		if (_isAutoParsing && !isParsed()) {
			try {
				parse_priv();
			} catch (ParserException | LexerException e) {
				throw new AutoParseException(e);
			}
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

		if (_isHoaring) {
			_treeChartWindow.setHoare();
		} else {
			_currentNodeP.set(null);
			_currentHoareNodeP.set(null);
		}

		_codeArea_code.setEditable(!_isHoaring);
	}

	public class AutoParseException extends Exception {
		public AutoParseException(Exception e) {
			super(e);
		}
	}

	public void throwException(@Nonnull Exception e) {
		for (ActionListener listener : _actionListeners) {
			listener.throwException(e);
		}
	}

	private void updateText() {
		StringBuilder text_sb = new StringBuilder();
		StringBuilder tooltip_sb = new StringBuilder();

		if (!_isSaved) text_sb.append("*");

		if (_name != null) {
			text_sb.append(_name);
			tooltip_sb.append(_name);
		} else if (_file != null) {
			text_sb.append(_file.getName());
			tooltip_sb.append(_file.toString());
		}

		if (!_isSaved) tooltip_sb.append(" (edited)");

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

	public void select() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				_codeArea_code.requestFocus();
				_codeArea_code.selectAll();
			}
		});
	}
	
	@Override
	public void initialize(URL url, ResourceBundle resources) {
		try {
			_extendedCodeArea_code = new ExtendedCodeArea(_codeArea_code);

			_extendedCodeArea_code.setCurrentNodeP(_currentNodeP, _currentHoareNodeP);
			_extendedCodeArea_code.enableArrows(true);
			_extendedCodeArea_code.setLineNumbers(ExtendedCodeArea.NumType.EXTENDED);
			_extendedCodeArea_code.setParser(new Parser(HoareWhileGrammar.getInstance()));
	
			if (_file != null) {
				if (_isInternalFile) {
					String s = IOUtil.getResourceAsString(_file.toString());
					
					_codeArea_code.replaceText(s);
				} else {
					BufferedReader reader = new BufferedReader(new FileReader(_file));
					
					StringBuilder sb = new StringBuilder();
					String line;
					
					while ((line = reader.readLine()) != null) {
						if (sb.length() > 0) sb.append(StringUtil.line_sep);

						sb.append(line);
					}
					
					reader.close();
					
					_codeArea_code.replaceText(sb.toString());
				}
			}
			
			_codeArea_code.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					try {
						setSaved(false);
						setParsed(false);
						setHoaring(false);

						if (isAutoParsing()) parse_priv();
					} catch (Exception e) {
						throwException(new AutoParseException(e));
					}
				}
			});
			
			_extendedCodeArea_tokens = new ExtendedCodeArea(_textArea_tokens);

			_extendedCodeArea_tokens.setCurrentNodeP(_currentNodeP, _currentHoareNodeP);
			_extendedCodeArea_tokens.enableArrows(true);
			_extendedCodeArea_tokens.setLineNumbers(ExtendedCodeArea.NumType.EXTENDED);
			
			updateVisibility();
			
			_extendedCodeArea_hoare = new ExtendedCodeArea(_codeArea_hoare);

			_extendedCodeArea_hoare.setCurrentNodeP(_currentNodeP, _currentHoareNodeP);
			_extendedCodeArea_hoare.setLineNumbers(ExtendedCodeArea.NumType.EXTENDED);
			_extendedCodeArea_hoare.setParser(new Parser(HoareWhileGrammar.getInstance()));
		} catch (Exception e) {
			ErrorUtil.logEFX(e);
		}
	}

	private boolean _tokens_shown = false;
	private boolean _syntaxTreeBox_shown = false;
	private boolean _semanticTreeBox_shown = false;
	private boolean _treeChartWindow_shown = false;
	
	private double _split_main_dividerPos = 0.4D;
	
	private void updateVisibility() {
		boolean modus_shown = _tokens_shown || _syntaxTreeBox_shown || _semanticTreeBox_shown || isHoaring();
		
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
				
				if (!_split_parsing.getItems().contains(_pane_tokens)) _split_parsing.getItems().add(index, _pane_tokens);
			} else {
				if (_split_parsing.getItems().contains(_pane_tokens)) _split_parsing.getItems().remove(_pane_tokens);
			}

			_pane_tokens.setVisible(_tokens_shown);
		}
		if (_pane_syntaxTree != null) {
			if (_syntaxTreeBox_shown) {
				int index = Math.min(1, _split_parsing.getItems().size());
				
				if (!_split_parsing.getItems().contains(_pane_syntaxTree)) _split_parsing.getItems().add(index, _pane_syntaxTree);
			} else {
				if (_split_parsing.getItems().contains(_pane_syntaxTree)) _split_parsing.getItems().remove(_pane_syntaxTree);
			}
			
			_pane_syntaxTree.setVisible(_syntaxTreeBox_shown);
		}
		if (_pane_semanticTree != null) {
			if (_semanticTreeBox_shown) {
				int index = Math.min(2, _split_parsing.getItems().size());

				if (!_split_parsing.getItems().contains(_pane_semanticTree)) _split_parsing.getItems().add(index, _pane_semanticTree);
			} else {
				if (_split_parsing.getItems().contains(_pane_semanticTree)) _split_parsing.getItems().remove(_pane_semanticTree);
			}

			_pane_semanticTree.setVisible(_semanticTreeBox_shown);
		}
		
		if (modus_shown) if (_split_parsing.getItems().isEmpty()) _tabPane_modus.getSelectionModel().select(_tab_hoare);
		
		if (_treeChartWindow != null) _treeChartWindow.setVisible(_treeChartWindow_shown);
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

	public void showSemanticTree(boolean show) {
		_semanticTreeBox_shown = show;

		_tabPane_modus.getSelectionModel().select(_tab_parsing);

		updateVisibility();
	}
	
	public void showTreeChartWindow(boolean show) {
		_treeChartWindow_shown = show;
		
		updateVisibility();
	}

	private void parse_priv() throws ParserException, LexerException {
		setParsed(true);

		_syntaxTreeP.set(null);
		_semanticTreeP.set(null);

		_textArea_tokens.clear();

		String codeS = _codeArea_code.getText();

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

				if (x > lastX) sb.append(StringUtil.repeat(" ", x - lastX));

				String tokenS = token.getTerminal().getKey().toString();

				lastX = x + tokenS.length();

				sb.append(tokenS);

				sb.append(" ");
			}

			_textArea_tokens.replaceText(sb.toString());
			_textArea_tokens.positionCaret(0);

			Parser parser = new Parser(_grammar);

			try {
				SyntaxNode tree = parser.parse(lexerResult.getTokens());

				_syntaxTreeP.set(tree);
				_semanticTreeP.set(SemanticNode.fromSyntax(tree));
			} catch (ParserException e) {
				setParsed(false);

				Token token = e.getToken();

				int pos = (token != null) ? token.getPos() : 0;

				_extendedCodeArea_code.setErrorPos(pos, true);

				throw e;
			}
		} catch (LexerException e) {
			setParsed(false);

			_extendedCodeArea_code.setErrorPos(e.getCurPos(), true);

			throw e;
		}
	}

	@Override
	public void parse() {
		try {
			if (_isParsed) throw new Exception("already parsed");

			parse_priv();
		} catch (Exception e) {
			throwException(e);
		}
	}

	@Override
	public void hoare() throws Exception {
		if (isHoaring()) throw new Exception("already in verification process");

		if (!isParsed()) parse();

		if (_semanticTreeP == null) throw new Exception("no syntaxTree");

		setHoaring(true);
		_tabPane_modus.getSelectionModel().select(_tab_hoare);
		
		updateVisibility();
		
		try {
			_pane_hoare_dialogHost.getChildren().clear();
			
			HoareExecuter hoareExecuter = new HoareExecuter(_semanticTreeP, _currentHoareNodeP, new HoareExecuter.ActionInterface() {
				private void print(String s) {
					System.out.println(s);
				}

				private String nodeSynthesize(@Nonnull SemanticNode node) {
					String ret = node.synthesize(false, false, null);

					return (ret != null) ? ret : "<empty>";
				}

				private void setNode(@Nullable SemanticNode node, @Nullable HoareCond postCond) {
					_codeArea_hoare.replaceText(((node != null) ? nodeSynthesize(node) : "<none>"));
					_currentNodeP.set(node);
					_textArea_hoare_postCond.replaceText((postCond != null) ? postCond.getContentString() : "<none>");
				}

				private void pushDialog(@Nonnull HoareDialog dialog) {
					setNode(dialog.getNode(), dialog.getPostCond());

					dialog.setCloseHandler(() -> _pane_hoare_dialogHost.getChildren().remove(dialog.getRoot()));

					_pane_hoare_dialogHost.getChildren().add(dialog.getRoot());
				}

				@Override
				public void reqSkipDialog(Skip skip, HoareCond preCond, HoareCond postCond, Hoare.Skip_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
					if (_checkBox_hoare_auto.isSelected()) {
						callback.result();
					} else {
						HoareDialog dialog = new SkipDialog(skip, preCond, postCond, new SkipDialog.Callback() {
							@Override
							public void result() throws LexerException, HoareException, ParserException, IOException {
								callback.result();
							}
						});

						pushDialog(dialog);
					}
				}

				@Override
				public void reqAssignDialog(Assign assign, HoareCond preCond, HoareCond postCond, Hoare.Assign_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
					if (_checkBox_hoare_auto.isSelected()) {
						callback.result();
					} else {
						HoareDialog dialog = new AssignDialog(assign, preCond, postCond, new AssignDialog.Callback() {
							@Override
							public void result() throws LexerException, HoareException, ParserException, IOException {
								callback.result();
							}
						});
						
						pushDialog(dialog);
					}
				}

				@Override
				public void reqCompNextDialog(Hoare.wlp_comp comp, Hoare.CompNext_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
					if (_checkBox_hoare_auto.isSelected()) {
						callback.result();
					} else {
						HoareDialog dialog = new CompNextDialog(comp, new CompNextDialog.Callback() {
							@Override
							public void result() throws LexerException, HoareException, ParserException, IOException {
								callback.result();
							}
						});

						pushDialog(dialog);
					}
				}

				@Override
				public void reqCompMergeDialog(Hoare.wlp_comp comp, Hoare.CompMerge_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
					if (_checkBox_hoare_auto.isSelected()) {
						callback.result();
					} else {
						HoareDialog dialog = new CompMergeDialog(comp, callback);

						pushDialog(dialog);
					}
				}

				@Override
				public void reqAltFirstDialog(Hoare.wlp_alt alt, Hoare.AltThen_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
					if (_checkBox_hoare_auto.isSelected()) {
						callback.result();
					} else {
						HoareDialog dialog = new AltThenDialog(alt, new AltThenDialog.Callback() {
							@Override
							public void result() throws LexerException, HoareException, ParserException, IOException {
								callback.result();
							}
						});

						pushDialog(dialog);
					}
				}

				@Override
				public void reqAltElseDialog(Hoare.wlp_alt alt, Hoare.AltElse_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
					if (_checkBox_hoare_auto.isSelected()) {
						callback.result();
					} else {
						HoareDialog dialog = new AltElseDialog(alt, new AltElseDialog.Callback() {
							@Override
							public void result() throws LexerException, HoareException, ParserException, IOException {
								callback.result();
							}
						});

						pushDialog(dialog);
					}
				}

				@Override
				public void reqAltMergeDialog(Hoare.wlp_alt alt, Hoare.AltMerge_callback callback) throws IOException, HoareException, LexerException, ParserException, SemanticNode.CopyException {
					if (_checkBox_hoare_auto.isSelected()) {
						callback.result();
					} else {
						HoareDialog dialog = new AltMergeDialog(alt, new AltMergeDialog.Callback() {
							@Override
							public void result() throws LexerException, HoareException, ParserException, IOException {
								callback.result();
							}
						});

						pushDialog(dialog);
					}
				}

				@Override
				public void reqLoopAskInvDialog(Hoare.wlp_loop loop, Hoare.LoopAskInv_callback callback) throws IOException {
					HoareDialog dialog = new LoopAskInvDialog(loop, new LoopAskInvDialog.Callback() {
						@Override
						public void result(@Nonnull HoareCond postInvariant) throws LexerException, HoareException, ParserException, IOException {
							callback.result(postInvariant);
						}
					});

					pushDialog(dialog);
				}

				@Override
				public void reqLoopCheckPostCondDialog(Hoare.wlp_loop loop, Hoare.LoopCheckPostCond_callback callback) throws IOException {
					HoareDialog dialog = new LoopCheckPostCondDialog(loop, new LoopCheckPostCondDialog.Callback() {
						@Override
						public void result() throws LexerException, HoareException, ParserException, IOException {
							callback.result();
						}
					});

					pushDialog(dialog);
				}

				@Override
				public void reqLoopGetBodyCondDialog(Hoare.wlp_loop loop, Hoare.LoopGetBodyCond_callback callback) throws IOException, LexerException, HoareException, ParserException, SemanticNode.CopyException {
					if (_checkBox_hoare_auto.isSelected()) {
						callback.result();
					} else {
						HoareDialog dialog = new LoopGetBodyCondDialog(loop, new LoopGetBodyCondDialog.Callback() {
							@Override
							public void result() throws LexerException, HoareException, ParserException, IOException {
								callback.result();
							}
						});

						pushDialog(dialog);
					}
				}

				@Override
				public void reqLoopCheckBodyCondDialog(Hoare.wlp_loop loop, Hoare.LoopCheckBodyCond_callback callback) throws IOException {
					HoareDialog dialog = new LoopCheckBodyCondDialog(loop, new LoopCheckBodyCondDialog.Callback() {
						@Override
						public void result(boolean yes) throws LexerException, HoareException, ParserException, IOException {
							callback.result(yes);
						}
					});

					pushDialog(dialog);
				}

				@Override
				public void reqLoopAcceptInvCondDialog(Hoare.wlp_loop loop, Hoare.LoopAcceptInv_callback callback) throws IOException, LexerException, HoareException, ParserException, SemanticNode.CopyException {
					if (_checkBox_hoare_auto.isSelected()) {
						callback.result();
					} else {
						HoareDialog dialog = new LoopAcceptInvDialog(loop, new LoopAcceptInvDialog.Callback() {
							@Override
							public void result() throws LexerException, HoareException, ParserException, IOException {
								callback.result();
							}
						});

						pushDialog(dialog);
					}
				}

				@Override
				public void reqConseqCheckPreDialog(SemanticNode node, HoareCond origPreCond, HoareCond newPreCond, Hoare.ConseqCheck_callback callback) throws IOException, LexerException, ParserException, HoareException {
					if (_checkBox_hoare_auto.isSelected()) {
						BoolImpl impl = new BoolImpl(origPreCond.getBoolExp(), newPreCond.getBoolExp());

						BoolExp implReduced = impl.reduce();

						print("preCheck " + impl + "->" + implReduced);

						callback.result(implReduced.equals(new BoolLit(true)));
					} else {
						HoareDialog dialog = new ConseqCheckPreDialog(node, origPreCond, newPreCond, new ConseqCheckPreDialog.Callback() {
							@Override
							public void result(BoolExp boolExp) throws LexerException, HoareException, ParserException, IOException {
								callback.result(boolExp.equals(new BoolLit(true)));
							}
						});

						pushDialog(dialog);
					}
				}

				@Override
				public void reqConseqCheckPostDialog(SemanticNode node, HoareCond origPostCond, HoareCond newPostCond, Hoare.ConseqCheck_callback callback) throws IOException, HoareException, ParserException, LexerException {
					if (_checkBox_hoare_auto.isSelected()) {
						BoolImpl impl = new BoolImpl(newPostCond.getBoolExp(), origPostCond.getBoolExp());

						BoolExp implReduced = impl.reduce();

						System.out.println("postCheck " + impl + "->" + implReduced);

						callback.result(implReduced.equals(new BoolLit(true)));
					} else {
						HoareDialog dialog = new ConseqCheckPostDialog(node, origPostCond, newPostCond, new ConseqCheckPostDialog.Callback() {
							@Override
							public void result(BoolExp boolExp) throws LexerException, HoareException, ParserException, IOException {
								callback.result(boolExp.equals(new BoolLit(true)));
							}
						});

						pushDialog(dialog);
					}
				}

				@Override
				public void beginNode(SemanticNode node, HoareCond postCond) {
					_currentNodeP.set(node);

					_postCondMapP.get().put(node, postCond);
				}

				@Override
				public void endNode(SemanticNode node, HoareCond preCond) {
					_preCondMapP.get().put(node, preCond);
				}

				@Override
				public void finished(SemanticNode node, HoareCond preCond, HoareCond postCond, boolean yes) throws IOException {
					HoareDialog dialog = new EndDialog(node, preCond, postCond, yes);

					pushDialog(dialog);
					setNode(node, postCond);
					
					setHoaring(false);
				}
			});
			
			hoareExecuter.exec();
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