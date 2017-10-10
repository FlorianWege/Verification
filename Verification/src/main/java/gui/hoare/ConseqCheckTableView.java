package gui.hoare;

import core.structures.semantics.boolExp.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import util.ErrorUtil;
import util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ConseqCheckTableView implements Initializable {
    @FXML
    private TableView<MatrixRow> _tableView_matrix;
    @FXML
    private TableColumn<MatrixRow, BoolExp> _tableCol_matrix_p;

    @FXML
    private TableView<ListRow> _tableView_list;
    @FXML
    private TableColumn<ListRow, BoolExp> _tableCol_list_p0;
    @FXML
    private TableColumn<ListRow, BoolExp> _tableCol_list_p;
    @FXML
    private TableColumn<ListRow, CellData> _tableCol_list_checked;

    private ObservableList<MatrixRow> _matrix_items = FXCollections.observableArrayList();
    private ObservableList<ListRow> _list_items = FXCollections.observableArrayList();
    private BoolList _origBoolList;
    private BoolList _newBoolList;

    private Scene _scene;

    public Node getRoot() {
        return _scene.getRoot();
    }

    public ConseqCheckTableView(BoolList origBoolList, BoolList newBoolList) throws IOException {
        _origBoolList = origBoolList;
        _newBoolList = newBoolList;

        _scene = IOUtil.inflateFXML(new File("ConseqCheckMatrixView.fxml"), this);

        _tableView_matrix.getStylesheets().add(getClass().getResource("HoareDialog.css").toExternalForm());

        /*WebView header = new WebView();

        header.getEngine().load("<html><body><p>p\\p<sub>0</sub><p></body></html>");

        header.setPrefHeight(Region.USE_COMPUTED_SIZE);
        header.setPrefWidth(Region.USE_COMPUTED_SIZE);*/

        StackPane cornerPane = new StackPane();

        TextField pText = new TextField();
        TextField p0Text = new TextField();

        pText.setAlignment(Pos.BOTTOM_CENTER);
        p0Text.setAlignment(Pos.CENTER_RIGHT);

        cornerPane.getChildren().add(pText);
        cornerPane.getChildren().add(p0Text);

        _tableCol_matrix_p.setGraphic(cornerPane);

        _tableCol_matrix_p.setCellFactory(new Callback<TableColumn<MatrixRow, BoolExp>, TableCell<MatrixRow, BoolExp>>() {
            @Override
            public TableCell<MatrixRow, BoolExp> call(TableColumn<MatrixRow, BoolExp> param) {
                return new MatrixBoolExpCell();
            }
        });
        _tableCol_matrix_p.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MatrixRow, BoolExp>, ObservableValue<BoolExp>>() {
            @Override
            public ObservableValue<BoolExp> call(TableColumn.CellDataFeatures<MatrixRow, BoolExp> param) {
                return new SimpleObjectProperty<>(param.getValue()._newPart);
            }
        });

        for (BoolExp origPart : _origBoolList.getBoolExps()) {
            MatrixCol col = new MatrixCol(origPart);

            _tableView_matrix.getColumns().add(col);

            col.setCellFactory(new Callback<TableColumn<MatrixRow, CellData>, TableCell<MatrixRow, CellData>>() {
                @Override
                public TableCell<MatrixRow, CellData> call(TableColumn<MatrixRow, CellData> param) {
                    return new MatrixCheckCell();
                }
            });
            col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MatrixRow, CellData>, ObservableValue<CellData>>() {
                @Override
                public ObservableValue<CellData> call(TableColumn.CellDataFeatures<MatrixRow, CellData> param) {
                    return new SimpleObjectProperty<>(getCellData(col._origPart, param.getValue()._newPart));
                }
            });
        }

        for (BoolExp newPart : _newBoolList.getBoolExps()) {
            MatrixRow row = new MatrixRow(newPart);

            _matrix_items.add(row);
        }

        _tableCol_list_p0.setCellFactory(new Callback<TableColumn<ListRow, BoolExp>, TableCell<ListRow, BoolExp>>() {
            @Override
            public TableCell<ListRow, BoolExp> call(TableColumn<ListRow, BoolExp> param) {
                return new ListBoolExpCell();
            }
        });
        _tableCol_list_p0.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ListRow, BoolExp>, ObservableValue<BoolExp>>() {
            @Override
            public ObservableValue<BoolExp> call(TableColumn.CellDataFeatures<ListRow, BoolExp> param) {
                return new SimpleObjectProperty<>(param.getValue()._origPart);
            }
        });
        _tableCol_list_p.setCellFactory(new Callback<TableColumn<ListRow, BoolExp>, TableCell<ListRow, BoolExp>>() {
            @Override
            public TableCell<ListRow, BoolExp> call(TableColumn<ListRow, BoolExp> param) {
                return new ListBoolExpCell();
            }
        });
        _tableCol_list_p.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ListRow, BoolExp>, ObservableValue<BoolExp>>() {
            @Override
            public ObservableValue<BoolExp> call(TableColumn.CellDataFeatures<ListRow, BoolExp> param) {
                return new SimpleObjectProperty<>(param.getValue()._newPart);
            }
        });
        _tableCol_list_checked.setCellFactory(new Callback<TableColumn<ListRow, CellData>, TableCell<ListRow, CellData>>() {
            @Override
            public TableCell<ListRow, CellData> call(TableColumn<ListRow, CellData> param) {
                return new ListCheckCell();
            }
        });
        _tableCol_list_checked.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ListRow, CellData>, ObservableValue<CellData>>() {
            @Override
            public ObservableValue<CellData> call(TableColumn.CellDataFeatures<ListRow, CellData> param) {
                return new SimpleObjectProperty<>(getCellData(param.getValue()._origPart, param.getValue()._newPart));
            }
        });

        for (BoolExp origPart : _origBoolList.getBoolExps()) {
            for (BoolExp newPart : _newBoolList.getBoolExps()) {
                ListRow row = new ListRow(origPart, newPart);

                _list_items.add(row);
            }
        }

        for (BoolExp origPart : _origBoolList.getBoolExps()) {
            for (BoolExp newPart : _newBoolList.getBoolExps()) {
                solve(origPart, newPart);
            }
        }
    }

    private class Solver {
        private BoolExp _origPart;
        private BoolExp _newPart;

        private BoolExp _origPartEx;
        private BoolExp _newPartEx;

        private Thread _thread;

        private void removeShared() {
            BoolAnd origPartAnd = new BoolAnd(_origPartEx);
            BoolAnd newPartAnd = new BoolAnd(_newPartEx);

            Set<BoolExp> shared = new LinkedHashSet<>();

            for (BoolExp part : origPartAnd.getBoolExps()) {
                if (newPartAnd.getBoolExps().contains(part)) shared.add(part);
            }

            BoolAnd origPartAndNew = new BoolAnd();
            BoolAnd newPartAndNew = new BoolAnd();

            for (BoolExp part : origPartAnd.getBoolExps()) {
                if (!shared.contains(part)) origPartAndNew.addBoolExp(origPartAnd);
            }
            for (BoolExp part : newPartAnd.getBoolExps()) {
                if (!shared.contains(part)) newPartAndNew.addBoolExp(newPartAnd);
            }

            _origPartEx = origPartAndNew.reduce();
            _newPartEx = newPartAndNew.reduce();
        }

        private boolean check() {
            //equal shape
            if (_origPartEx.getContentString().equals(_newPartEx.getContentString())) return true;

            removeShared();

            return false;
        }

        private void exec() {
            getCellData(_origPart, _newPart)._solving.set(true);

            _thread.start();
        }

        public Solver(BoolExp origPart, BoolExp newPart) {
            _origPart = origPart;
            _newPart = newPart;

            _thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    _origPartEx = (BoolExp) origPart.copy();
                    _newPartEx = (BoolExp) newPart.copy();

                    if (_origPartEx instanceof BoolOr) {
                        //is DNF

                        _origPartEx = _origPartEx.reduce();
                        _newPartEx = _newPartEx.reduce();

                        if (_origPartEx instanceof BoolAnd) _origPartEx = new BoolNeg(_origPartEx).reduce();

                        _origPartEx.order();
                        _newPartEx.order();

                        if (check()) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    getCellData(origPart, newPart)._auto.set(true);
                                }
                            });

                            return;
                        }

                        BoolAnd newCNF = newPart.makeCNF();
                    }
                }
            });
        }
    }

    private void solve(BoolExp origPart, BoolExp newPart) {
        new Solver(origPart, newPart).exec();
    }

    private class MatrixCol extends TableColumn<MatrixRow, CellData> {
        public BoolExp _origPart;

        public MatrixCol(BoolExp origPart) {
            super(origPart.getContentString());

            _origPart = origPart;
        }
    }

    private abstract class Row {
    }

    private class ListRow extends Row {
        public BoolExp _origPart;
        public BoolExp _newPart;

        public ListRow(BoolExp origPart, BoolExp newPart) {
            _origPart = origPart;
            _newPart = newPart;
        }
    }

    private class MatrixRow extends Row {
        public BoolExp _newPart;

        public MatrixRow(BoolExp newPart) {
            _newPart = newPart;
        }
    }

    private class CellData {
        public BooleanProperty _auto = new SimpleBooleanProperty(false);
        public BooleanProperty _user = new SimpleBooleanProperty(false);

        private BooleanProperty _solving = new SimpleBooleanProperty(false);

        public CellData() {
        }
    }

    private Map<BoolExp, Map<BoolExp, CellData>> _cellDataMap = new LinkedHashMap<>();

    private CellData getCellData(BoolExp origPart, BoolExp newPart) {
        if (!_cellDataMap.containsKey(origPart)) _cellDataMap.put(origPart, new LinkedHashMap<>());

        Map<BoolExp, CellData> sub = _cellDataMap.get(origPart);

        if (!sub.containsKey(newPart)) sub.put(newPart, new CellData());

        return sub.get(newPart);
    }

    private abstract class CheckCell<RowType> extends TableCell<RowType, CellData> {
        private StackPane _stackPane;
        private HBox _box;
        private CheckBox _checkBox_auto;
        private CheckBox _checkBox_user;
        private ProgressIndicator _progressIndicator;

        private BooleanProperty _checkBox_auto_binding;
        private BooleanProperty _checkBox_user_binding;

        private boolean _setup = false;

        private void setup() {
            if (_setup) return;

            _setup = true;

            _stackPane = new StackPane();

            _box = new HBox();

            /*_box.setMinWidth(Region.USE_COMPUTED_SIZE);
            _box.setMinHeight(Region.USE_COMPUTED_SIZE);
            _box.setPrefWidth(Region.USE_COMPUTED_SIZE);
            _box.setPrefHeight(Region.USE_COMPUTED_SIZE);*/
            _box.setMaxWidth(Region.USE_PREF_SIZE);
            _box.setMaxHeight(Region.USE_PREF_SIZE);
            _box.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
            StackPane.setAlignment(_box, Pos.CENTER);
            _stackPane.getChildren().add(_box);

            _checkBox_auto = new CheckBox();
            _checkBox_user = new CheckBox();

            _box.getChildren().add(_checkBox_auto);
            _box.getChildren().add(_checkBox_user);

            _checkBox_auto.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    updateBackground();
                }
            });
            _checkBox_user.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    updateBackground();
                }
            });

            _progressIndicator = new ProgressIndicator();

            _stackPane.getChildren().add(_progressIndicator);
        }

        public CheckCell() {
        }

        private void updateBackground() {
            if (_checkBox_auto.isSelected()) {
                _box.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
            } else if (_checkBox_user.isSelected()) {
                _box.setBackground(new Background(new BackgroundFill(Color.GREENYELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
            } else {
                _box.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }

        @Override
        protected void updateItem(CellData item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setup();

                if (_checkBox_auto_binding != null) _checkBox_auto.selectedProperty().unbindBidirectional(_checkBox_auto_binding);
                if (_checkBox_user_binding != null) _checkBox_user.selectedProperty().unbindBidirectional(_checkBox_user_binding);
                _progressIndicator.visibleProperty().unbind();

                _checkBox_auto_binding = item._auto;
                _checkBox_user_binding = item._user;

                _checkBox_auto.selectedProperty().bindBidirectional(_checkBox_auto_binding);
                _checkBox_user.selectedProperty().bindBidirectional(_checkBox_user_binding);
                _progressIndicator.visibleProperty().bind(item._solving);

                setText(null);
                setGraphic(_stackPane);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        }
    }

    private class MatrixCheckCell extends CheckCell<MatrixRow> {
        public MatrixCheckCell() {
            super();
        }
    }

    private class ListCheckCell extends CheckCell<ListRow> {
        public ListCheckCell() {
            super();
        }
    }

    private abstract class BoolExpCell<RowType> extends TableCell<RowType, BoolExp> {
        private HBox _box;
        private Text _label;

        private boolean _setup = false;

        public void setup() {
            if (_setup) return;

            _setup = true;

            _box = new HBox();

            _box.getStylesheets().add(getClass().getResource("HoareDialog.css").toExternalForm());
            _box.getStyleClass().add("table-header");

            _label = new Text();

            _box.getChildren().add(_label);

            _label.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                }
            });
        }

        public BoolExpCell() {
        }

        @Override
        protected void updateItem(BoolExp item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setup();

                _label.textProperty().set(item.getContentString());

                setText(null);
                setGraphic(_box);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        }
    }

    private class MatrixBoolExpCell extends BoolExpCell<MatrixRow> {
        public MatrixBoolExpCell() {
            super();
        }
    }

    private class ListBoolExpCell extends BoolExpCell<ListRow> {
        public ListBoolExpCell() {
            super();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        try {
            _tableView_matrix.setItems(_matrix_items);
            _tableView_list.setItems(_list_items);
        } catch (Exception e) {
            ErrorUtil.logEFX(e);
        }
    }
}