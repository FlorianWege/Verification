package gui;

import gui.CloseSaveDialog.Result;
import gui.FileTab.AutoParseException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import util.ErrorUtil;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;

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
	private CheckMenuItem _menu_semanticTree;
	@FXML
	private CheckMenuItem _menu_treeChart;
	@FXML
	private CheckMenuItem _menu_console;

	@FXML
	private MenuItem _menu_parse;
	@FXML
	private CheckMenuItem _menu_parse_auto;
	@FXML
	private MenuItem _menu_hoare;
	@FXML
	private MenuItem _menu_hoare_abort;
	@FXML
	private CheckMenuItem _menu_testPlayground;
	
	@FXML
	private SplitPane _split_main;
	
	@FXML
	private TabPane _tabPane_files;

	@FXML
	private Button _button_parse;
	@FXML
	private Button _button_hoare;
	@FXML
	private Button _button_hoare_abort;

	private final Stage _stage;

	private final FileChooser _diag_open = new FileChooser();
	private final FileChooser _diag_save = new FileChooser();

	private final Console _console;

	public interface ActionInterface {
		void parse() throws Exception;
		void hoare() throws Exception;
	}

	public MainWindow(@Nonnull Stage stage) throws IOException {
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
				
				if (_closeWantsCancel) event.consume();
			}
		});

		_console = new Console();

		_stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
		_stage.setScene(IOUtil.inflateFXML(new File("MainWindow.fxml"), this));
		_stage.setTitle("Verification JavaFX GUI");
		_stage.show();

		_stage.getScene().getStylesheets().add(getClass().getResource("Highlight.css").toExternalForm());

		_stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.isAltDown()) event.consume();
			}
		});
	}

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
			if (_split_main.getItems().contains(_console.getRoot())) _split_main.getItems().remove(_console.getRoot());
		}
		
		_console.setVisible(show);
	}
	
	private void addButtonAccelerator(@Nonnull Button button, @Nonnull KeyCodeCombination keyCodeCombination) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Tooltip tooltip = button.getTooltip();
				
				assert(tooltip != null);
				
				tooltip.setText(tooltip.getText() + " " + "(" + keyCodeCombination + ")");
				
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
			_menu_parse_auto.setDisable(true);
		} else {
			_button_parse.setDisable(((FileTab) tab).isParsed());
			_menu_parse.setDisable(((FileTab) tab).isParsed());
			_menu_parse_auto.setDisable(((FileTab) tab).isAutoParsing());
		}
	}
	
	private void initParse() {
		_button_parse.setTooltip(new Tooltip("Parse"));
		
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

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					addButtonAccelerator(_button_parse, new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});

		_menu_parse_auto.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Tab tab = _tabPane_files.getSelectionModel().getSelectedItem();

					if (tab instanceof FileTab) {
						try {
							((FileTab) tab).setAutoParsing(_menu_parse_auto.isSelected());
						} catch (AutoParseException e) {
							ErrorUtil.logE(e);
						}
					}
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		
		updateParse();
	}
	
	private void updateHoare() {
		Tab tab = _tabPane_files.getSelectionModel().getSelectedItem();
		
		_button_hoare.managedProperty().bind(_button_hoare.visibleProperty());
		_button_hoare_abort.managedProperty().bind(_button_hoare_abort.visibleProperty());
		
		if ((tab == null) || !(tab instanceof FileTab)) {
			_button_hoare.setDisable(true);
			_menu_hoare.setDisable(true);
			_button_hoare_abort.setVisible(false);
			_menu_hoare_abort.setVisible(false);
		} else {
			_button_hoare.setDisable(false);
			_menu_hoare.setDisable(false);
			
			FileTab fileTab = (FileTab) tab;
			
			_button_hoare.setVisible(!fileTab.isHoaring());
			_button_hoare_abort.setVisible(fileTab.isHoaring());
			_menu_hoare.setVisible(!fileTab.isHoaring());
			_menu_hoare_abort.setVisible(fileTab.isHoaring());
		}
	}
	
	private void initHoare() {
		_button_hoare.setTooltip(new Tooltip("Verify"));
		_button_hoare_abort.setTooltip(new Tooltip("Abort Verification"));
		
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
		_button_hoare_abort.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Tab tab = _tabPane_files.getSelectionModel().getSelectedItem();
					
					if (tab instanceof FileTab) {
						((FileTab) tab).hoare_abort();
					}
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		_menu_hoare.setOnAction(new EventHandler<ActionEvent>() {
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
		_menu_hoare.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Tab tab = _tabPane_files.getSelectionModel().getSelectedItem();
					
					if (tab instanceof FileTab) {
						((FileTab) tab).hoare_abort();
					}
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				addButtonAccelerator(_button_hoare, new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
				addButtonAccelerator(_button_hoare_abort, new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
			}
		});
		
		updateHoare();
	}

	private TestPlaygroundWindow _testPlaygroundWindow;

	private void initTestPlayground() throws IOException {
		_testPlaygroundWindow = new TestPlaygroundWindow();

		_testPlaygroundWindow.getStage();

		_menu_testPlayground.selectedProperty().bindBidirectional(_testPlaygroundWindow.getShownProperty());
	}

	private void saveFileTab(@Nonnull FileTab tab) throws IOException {
		if (tab.isInternalFile() || (tab.getFile() == null)) {
			_menu_saveAs.fire();
		} else {
			tab.save(tab.getFile());
		}
	}
	
	private boolean _closeWantsCancel = false;
	
	private void closeTab(@Nonnull Tab tab) throws IOException {
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
	
	private void addFileTab(@Nonnull FileTab tab) {
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
	}
	
	private File _savesDir = new File("save");
	
	private void updateTabVisibility() {
		for (Tab tab : _tabPane_files.getTabs()) {
			if (tab instanceof FileTab) {
				FileTab fileTab = (FileTab) tab;

				fileTab.showTokens(_menu_tokens.isSelected());
				fileTab.showSyntaxTree(_menu_syntaxTree.isSelected());
				fileTab.showSemanticTree(_menu_semanticTree.isSelected());
				fileTab.showTreeChartWindow(tab.equals(_tabPane_files.getSelectionModel().getSelectedItem()) && _menu_treeChart.isSelected());
			}
		}
	}
	
	private void initMenu() {
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
					
					tab.select();
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		_menu_open.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					_savesDir.mkdirs(); if (!_savesDir.exists()) throw new Exception("could not create " + _savesDir);
					
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
								
								tab.select();
							} catch (IOException e) {
								throw new Exception("path already opened");
							}
						} else {
							_tabPane_files.getSelectionModel().select(oldTab);
							
							oldTab.select();
							
							throw new Exception("path already opened");
						}
					}
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});

		for (File file : IOUtil.getCodeFiles()) {
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
						_diag_save.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Code", "*.c"));
						
						_savesDir.mkdirs(); if (!_savesDir.exists()) throw new Exception("could not create " + _savesDir);
						
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
		_menu_semanticTree.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					updateTabVisibility();
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		_menu_treeChart.setOnAction(new EventHandler<ActionEvent>() {
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
	}
	
	@Override
	public void initialize(URL url, ResourceBundle resources) {
		try {
			initMenu();
			initParse();
			initHoare();
			initTestPlayground();

			updateConsole();
		} catch (Exception e) {
			ErrorUtil.logEFX(e);
		}
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