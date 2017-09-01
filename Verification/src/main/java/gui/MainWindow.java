package gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import core.Hoare.HoareException;
import core.Lexer.LexerException;
import core.Parser.ParserException;
import gui.CloseSaveDialog.Result;
import gui.FileTab.AutoParseException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import util.ErrorUtil;
import util.IOUtil;

public class MainWindow implements Initializable, JavaFXMain.PrintInterface, JavaFXMain.StopInterface {
	@FXML
	private MenuItem _menu_new;
	@FXML
	private MenuItem _menu_open;
	@FXML
	private Menu _menu_openPreset;
	@FXML
	private MenuItem _menu_save;
	@FXML
	private MenuItem _menu_saveAs;
	@FXML
	private MenuItem _menu_exit;

	@FXML
	private CheckMenuItem _menu_tokens;
	@FXML
	private CheckMenuItem _menu_syntaxTree;
	@FXML
	private CheckMenuItem _menu_syntaxChart;
	@FXML
	private CheckMenuItem _menu_console;

	@FXML
	private MenuItem _menu_parse;
	@FXML
	private CheckMenuItem _menu_parse_auto;
	@FXML
	private MenuItem _menu_hoare;
	
	public CheckMenuItem getMenuParseAuto() {
		return _menu_parse_auto;
	}
	
	@FXML
	private SplitPane _split_main;
	
	@FXML
	private TabPane _tabPane_files;

	@FXML
	private Button _button_parse;
	@FXML
	private Button _button_hoare;

	private Stage _stage;

	private FileChooser _diag_open = new FileChooser();
	private FileChooser _diag_save = new FileChooser();

	private Console _console;

	public interface ActionInterface {
		public void parse() throws Exception;
		public void hoare() throws Exception;
	}

	public MainWindow(Stage stage) throws IOException {
		_stage = stage;
		
		_stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Vector<Tab> tabs = new Vector<>(_tabPane_files.getTabs());
				
				_closeWantsCancel = false;
				
				for (Tab tab : tabs) {
					if (_closeWantsCancel) break;
					
					try {
						closeTab(tab);
					} catch (IOException e) {
						ErrorUtil.logEFX(e);
					}
				}
				
				if (_closeWantsCancel) {
					event.consume();
				}
			}
		});

		_stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
		_stage.setScene(IOUtil.inflateFXML(new File("MainWindow.fxml"), this));
		_stage.setTitle("Verification JavaFX GUI");
		_stage.show();

		_stage.getScene().getStylesheets().add(getClass().getResource("Highlight.css").toExternalForm());
	}
	
	private Collection<File> files = Arrays.asList(new File[] {
		new File("Div.txt"),
		new File("Factorial.txt"),
		new File("Factorial2.txt"),
		new File("Euclid.txt"),
		new File("Assign.txt"),
		new File("AssignNested.txt"),
		new File("Power.txt"),
	});

	private double _split_main_dividerPos = 0.8D;

	private void updateConsole() {
		boolean show = _menu_console.isSelected();
		
		if (show) {
			if (!_split_main.getItems().contains(_console.getRoot())) {
				_split_main.getItems().add(_console.getRoot());
				
				_split_main.setDividerPositions(_split_main_dividerPos);

				_split_main.getDividers().get(0).positionProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
						_split_main_dividerPos = newVal.doubleValue();
					}
				});
			}
		} else {
			if (_split_main.getItems().contains(_console.getRoot())) {
				_split_main.getItems().remove(_console.getRoot());
			}
		}
		
		_console.setVisible(show);
	}
	
	private void addButtonAccelerator(Button button, KeyCodeCombination keyCodeCombination) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				button.getTooltip().setText(button.getTooltip().getText() + " " + "(" + keyCodeCombination + ")");
				
				button.getScene().getAccelerators().put(keyCodeCombination, new Runnable() {
					@Override
					public void run() {
						_button_hoare.fire();
					}
				});
			}
		});
	}
	
	private void updateMenu() {
		Tab tab = _tabPane_files.getSelectionModel().getSelectedItem();
		
		if ((tab == null) || !(tab instanceof FileTab) || ((FileTab) tab).isSaved()) {
			_menu_save.setDisable(true);
			_menu_saveAs.setDisable(true);
		} else {
			_menu_save.setDisable(false);
			_menu_saveAs.setDisable(false);
		}
	}
	
	private void updateParse() {
		Tab tab = _tabPane_files.getSelectionModel().getSelectedItem();
		
		if ((tab == null) || !(tab instanceof FileTab)) {
			_button_parse.setDisable(true);
			_menu_parse.setDisable(true);
		} else {
			_button_parse.setDisable(((FileTab) tab).isParsed());
			_menu_parse.setDisable(((FileTab) tab).isParsed());
		}
	}
	
	private void updateHoare() {
		Tab tab = _tabPane_files.getSelectionModel().getSelectedItem();
		
		if ((tab == null) || !(tab instanceof FileTab)) {
			_button_hoare.setDisable(true);
			_menu_hoare.setDisable(true);
		} else {
			_button_hoare.setDisable(((FileTab) tab).isHoared());
			_menu_hoare.setDisable(((FileTab) tab).isHoared());
		}
	}
	
	private void saveFileTab(FileTab tab) throws IOException {
		if (tab.isInternalFile() || (tab.getFile() == null)) {
			_menu_saveAs.fire();
		} else {
			tab.save(tab.getFile());
		}
	}
	
	boolean _closeWantsCancel = false;
	
	private void closeTab(Tab tab) throws IOException {
		_tabPane_files.getSelectionModel().select(tab);
		
		if (tab instanceof FileTab && !((FileTab) tab).isSaved()) {
			CloseSaveDialog diag = new CloseSaveDialog((FileTab) tab, new CloseSaveDialog.CloseSaveInterface() {
				@Override
				public void result(Result result) {
					try {
						switch (result) {
						case YES: {
							saveFileTab((FileTab) tab);
						}
						case NO: {
							_tabPane_files.getTabs().remove(tab);
							
							break;
						}
						case CANCEL: {
							_closeWantsCancel = true;
							
							break;
						}
						}
					} catch (Exception e) {
						ErrorUtil.logEFX(e);
					}
				}
			});
			
			diag.show();
		} else {
			_tabPane_files.getTabs().remove(tab);
		}
	}
	
	private void addFileTab(FileTab tab) {
		if (_tabPane_files.getTabs().contains(tab)) return;
		
		ContextMenu conMenu = new ContextMenu();
		
		tab.setContextMenu(conMenu);
		
		MenuItem delItem = new MenuItem("close");
		
		conMenu.getItems().add(delItem);
		
		delItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					closeTab(tab);
				} catch (IOException e) {
					ErrorUtil.logEFX(e);
				}
			}
		});

		tab.addActionListener(new FileTab.ActionListener() {
			@Override
			public void isSavedChanged() {
				updateMenu();
			}
			
			@Override
			public void isParsedChanged() {
				try {
					updateParse();
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}

			@Override
			public void isHoaredChanged() {
				try {
					if (tab.equals(_tabPane_files.getSelectionModel().getSelectedItem())) {
						_button_hoare.setDisable(tab.isHoared());
					}
					updateHoare();
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}

			@Override
			public void throwException(Exception e) {
				if (e instanceof AutoParseException) {
					ErrorUtil.logE(e.getMessage());
				} else {
					ErrorUtil.logEFX(e);
				}
			}
		});
		
		_tabPane_files.getTabs().add(tab);
		
		_tabPane_files.getSelectionModel().select(tab);
		
		tab.setMainWindow(this);
	}
	
	private File _savesDir = new File("save");
	
	private void updateTabVisibility() {
		for (Tab tab : _tabPane_files.getTabs()) {
			if (tab instanceof FileTab) {
				((FileTab) tab).showTokens(_menu_tokens.isSelected());
			}
		}
		for (Tab tab : _tabPane_files.getTabs()) {
			if (tab instanceof FileTab) {
				((FileTab) tab).showSyntaxTree(_menu_syntaxTree.isSelected());
			}
		}
		for (Tab tab : _tabPane_files.getTabs()) {
			if (tab instanceof FileTab) {
				((FileTab) tab).showSyntaxChart(tab.equals(_tabPane_files.getSelectionModel().getSelectedItem()) && _menu_syntaxChart.isSelected());
			}
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		_diag_open.setInitialDirectory(_savesDir);
		_diag_save.setInitialDirectory(_savesDir);
		
		_menu_new.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					FileTab tab = new FileTab(_stage.getScene().getAccelerators());
					
					int c = 0;
					boolean found = false;
					
					while (!found) {
						c++;
						found = true;
						
						for (Tab tab2 : _tabPane_files.getTabs()) {
							if ((tab2 instanceof FileTab) && (((FileTab) tab2).getName() != null) && ((FileTab) tab2).getName().equals("Untitled " + c)) {
								found = false;
								
								break;
							}
						}
					}
					
					tab.setName(String.format("Untitled " + c, c));

					addFileTab(tab);
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		_menu_open.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					_savesDir.mkdirs();
					
					File file = _diag_open.showOpenDialog(_stage);
					
					if (file != null) {
						FileTab oldTab = null;
						
						for (Tab tab : _tabPane_files.getTabs()) {
							if (tab instanceof FileTab) {
								if (!((FileTab) tab).isInternalFile() && file.equals(((FileTab) tab).getFile())) {
									oldTab = (FileTab) tab;
									
									break;
								}
							}
						}
						
						if (oldTab == null) {
							try {
								FileTab tab = new FileTab(file, false, _stage.getScene().getAccelerators());
								
								addFileTab(tab);
							} catch (IOException e) {
								throw new Exception("path already opened");
							}
						} else {
							_tabPane_files.getSelectionModel().select(oldTab);
							
							throw new Exception("path already opened");
						}
					}
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});

		for (File file : files) {
			MenuItem fileItem = new MenuItem(file.getName());
			
			_menu_openPreset.getItems().add(fileItem);
			
			fileItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						FileTab oldTab = null;
						
						for (Tab tab : _tabPane_files.getTabs()) {
							if (tab instanceof FileTab) {
								if (!((FileTab) tab).isInternalFile() && file.equals(((FileTab) tab).getFile())) {
									oldTab = (FileTab) tab;
									
									break;
								}
							}
						}
						
						if (oldTab == null) {
							try {
								FileTab tab = new FileTab(file, true, _stage.getScene().getAccelerators());
								
								addFileTab(tab);
							} catch (IOException e) {
								throw new Exception("path already opened");
							}
						} else {
							_tabPane_files.getSelectionModel().select(oldTab);
							
							throw new Exception("path already opened");
						}
					} catch (Exception e) {
						ErrorUtil.logEFX(e);
					}
				}
			});
		}
		
		/*_listView_files.setItems(fileItems);
		_listView_files.setPrefHeight((_listView_files.getItems().size() + 1) * 25);
		_listView_files.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FileItem>() {
			@Override
			public void changed(ObservableValue<? extends FileItem> obs, FileItem oldVal, FileItem newVal) {
				try {
					String s = IOUtil.getResourceAsString(newVal.getFile().toString());
					
					_textArea_code.clear();
					_textArea_code.insertText(0, s);
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});*/
		_menu_save.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Tab tab = _tabPane_files.getSelectionModel().getSelectedItem();
					
					if ((tab != null) && (tab instanceof FileTab)) {
						saveFileTab((FileTab) tab);
					}
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		_menu_saveAs.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Tab tab = _tabPane_files.getSelectionModel().getSelectedItem();
					
					if ((tab != null) && (tab instanceof FileTab)) {
						_diag_save.setTitle("Save code");
						_diag_save.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text", "*.txt"));
						
						_savesDir.mkdirs();
						
						File file = _diag_save.showSaveDialog(_stage);
						
						if (file != null) {
							((FileTab) tab).save(file);
						}
					}
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		_menu_exit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Platform.exit();
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		
		_menu_tokens.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					updateTabVisibility();
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		_menu_syntaxTree.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					updateTabVisibility();
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		_menu_syntaxChart.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					updateTabVisibility();
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		_menu_console.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					updateConsole();
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		
		_tabPane_files.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override
			public void changed(ObservableValue<? extends Tab> obs, Tab oldVal, Tab newVal) {
				try {
					updateMenu();
					updateParse();
					updateHoare();
					updateTabVisibility();
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		updateMenu();
		
		_button_parse.setTooltip(new Tooltip("parse"));
		
		_button_parse.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Tab tab = _tabPane_files.getSelectionModel().getSelectedItem();
					
					if (tab instanceof FileTab) {
						((FileTab) tab).parse();
					}
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		updateParse();
		
		_button_hoare.setTooltip(new Tooltip("hoare"));
		
		_button_hoare.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Tab tab = _tabPane_files.getSelectionModel().getSelectedItem();
					
					if (tab instanceof FileTab) {
						((FileTab) tab).hoare();
					}
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		updateHoare();
		
		try {
			_console = new Console();
			
			updateConsole();
		} catch (IOException e) {
			ErrorUtil.logEFX(e);
		}
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				addButtonAccelerator(_button_parse, new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
				addButtonAccelerator(_button_hoare, new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
			}
		});
	}

	@Override
	public void writeToOut(String s) {
		_console.writeToOut(s);
	}

	@Override
	public void writeToErr(String s) {
		_console.writeToErr(s);
	}

	@Override
	public void onStop() {
	}
}