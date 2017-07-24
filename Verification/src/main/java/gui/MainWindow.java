package gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;

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
import util.ErrorUtil;
import util.IOUtil;

public class MainWindow implements Initializable, JavaFXMain.PrintInterface {
	public interface ActionInterface {
		public void parse() throws Exception;
		public void hoare() throws Exception;
	}
	
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

	public MainWindow(Stage stage) throws IOException {
		_stage = stage;

		_stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
		_stage.setScene(IOUtil.inflateFXML(new File("MainWindow.fxml"), this));
		_stage.setTitle("Verification JavaFX GUI");
		_stage.show();

		_stage.getScene().getStylesheets().add(getClass().getResource("highlight.css").toExternalForm());
	}
	
	private Collection<File> files = Arrays.asList(new File[] {
		new File("Div.txt"),
		new File("Factorial.txt"),
		new File("Euclid.txt"),
		new File("Assign.txt"),
		new File("AssignNested.txt"),
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
		
		if ((tab == null) || !(tab instanceof FileTab)) {
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
		} else {
			_button_parse.setDisable(((FileTab) tab).isParsed());
		}
	}
	
	private void updateHoare() {
		Tab tab = _tabPane_files.getSelectionModel().getSelectedItem();
		
		if ((tab == null) || !(tab instanceof FileTab)) {
			_button_hoare.setDisable(true);
		} else {
			_button_hoare.setDisable(((FileTab) tab).isHoared());
		}
	}
	
	private void addFileTab(FileTab tab) {
		tab.addActionListener(new FileTab.ActionListener() {
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
				ErrorUtil.logEFX(e);
			}
		});
		
		_tabPane_files.getTabs().add(tab);
		
		_tabPane_files.getSelectionModel().select(tab);
	}
	
	private File _savesDir = new File("save");
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		_diag_open.setInitialDirectory(_savesDir);
		_diag_save.setInitialDirectory(_savesDir);
		
		_menu_new.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					FileTab tab = new FileTab();
					
					int c = 0;
					boolean found = false;
					
					while (!found) {
						c++;
						found = true;
						
						for (Tab tab2 : _tabPane_files.getTabs()) {
							if (tab2.getText().equals("Untitled " + c)) {
								found = false;
								
								break;
							}
						}
					}
					
					tab.setText(String.format("Untitled " + c, c));
					
					_tabPane_files.getTabs().add(tab);
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
								FileTab tab = new FileTab(file, false);
								
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

		ObservableList<FileItem> fileItems = FXCollections.observableArrayList();

		for (File file : files) {
			fileItems.add(new FileItem(file));
			
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
								FileTab tab = new FileTab(file, true);
								
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
						if (((FileTab) tab).isInternalFile() || (((FileTab) tab).getFile() == null)) {
							_menu_saveAs.fire();
						} else {
							((FileTab) tab).save(((FileTab) tab).getFile());
						}
					}
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		_menu_saveAs.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
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
					for (Tab tab : _tabPane_files.getTabs()) {
						if (tab instanceof FileTab) {
							((FileTab) tab).showTokens(_menu_tokens.isSelected());
						}
					}
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		_menu_syntaxTree.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					for (Tab tab : _tabPane_files.getTabs()) {
						if (tab instanceof FileTab) {
							((FileTab) tab).showSyntaxTree(_menu_syntaxTree.isSelected());
						}
					}
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		_menu_syntaxChart.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					for (Tab tab : _tabPane_files.getTabs()) {
						if (tab instanceof FileTab) {
							((FileTab) tab).showSyntaxChart(_menu_syntaxChart.isSelected());
						}
					}
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
				} catch (Exception e) {
					ErrorUtil.logEFX(e);
				}
			}
		});
		updateMenu();
		
		_button_parse.setTooltip(new Tooltip("parse"));
		
		_button_parse.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
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
			public void handle(ActionEvent arg0) {
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
}