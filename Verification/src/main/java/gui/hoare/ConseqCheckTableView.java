package gui.hoare;

import core.structures.semantics.boolExp.BoolAnd;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.BoolImpl;
import core.structures.semantics.boolExp.BoolOr;
import gui.ExtendedCodeArea;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.fxmisc.richtext.CodeArea;
import util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConseqCheckTableView implements Initializable {
    @FXML
    private TableView<SplitRow> _tableView_split;
    @FXML
    private TableColumn<SplitRow, BoolExp> _tableCol_split;
    @FXML
    private TableColumn<SplitRow, BoolExp> _tableCol_split_calc;
    @FXML
    private TableColumn<SplitRow, BoolExp> _tableCol_split_user;

    @FXML
    private TableView<SplitRow> _tableView_and;
    @FXML
    private TableColumn<SplitRow, BoolExp> _tableCol_and;
    @FXML
    private TableColumn<SplitRow, BoolExp> _tableCol_and_calc;
    @FXML
    private TableColumn<SplitRow, BoolExp> _tableCol_and_user;

    @FXML
    private TableView<SplitRow> _tableView_or;
    @FXML
    private TableColumn<SplitRow, BoolExp> _tableCol_or;
    @FXML
    private TableColumn<SplitRow, BoolExp> _tableCol_or_calc;

    private final BoolImpl _origImpl;
    private final BoolOr _or;

    private ObservableList<SplitRow> _split_items = FXCollections.observableArrayList();

    private Scene _scene;

    public Node getRoot() {
        return _scene.getRoot();
    }

    public ConseqCheckTableView(BoolImpl origImpl) throws IOException {
        _origImpl = origImpl;

        _scene = IOUtil.inflateFXML(new File("ConseqCheckTableView.fxml"), this);

        _or = new BoolOr(_origImpl.split(true, false));

        _tableCol_split.setCellFactory(new Callback<TableColumn<SplitRow, BoolExp>, TableCell<SplitRow, BoolExp>>() {
            @Override
            public TableCell<SplitRow, BoolExp> call(TableColumn<SplitRow, BoolExp> param) {
                return new SplitBoolExpCell();
            }
        });
        _tableCol_split.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SplitRow, BoolExp>, ObservableValue<BoolExp>>() {
            @Override
            public ObservableValue<BoolExp> call(TableColumn.CellDataFeatures<SplitRow, BoolExp> param) {
                return new SimpleObjectProperty<>(param.getValue()._impl);
            }
        });

        _tableCol_split_calc.setCellFactory(new Callback<TableColumn<SplitRow, BoolExp>, TableCell<SplitRow, BoolExp>>() {
            @Override
            public TableCell<SplitRow, BoolExp> call(TableColumn<SplitRow, BoolExp> param) {
                return new SplitBoolExpCell();
            }
        });
        _tableCol_split_calc.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SplitRow, BoolExp>, ObservableValue<BoolExp>>() {
            @Override
            public ObservableValue<BoolExp> call(TableColumn.CellDataFeatures<SplitRow, BoolExp> param) {
                return new SimpleObjectProperty<>(param.getValue()._calc);
            }
        });

        _tableCol_split_user.setCellFactory(new Callback<TableColumn<SplitRow, BoolExp>, TableCell<SplitRow, BoolExp>>() {
            @Override
            public TableCell<SplitRow, BoolExp> call(TableColumn<SplitRow, BoolExp> param) {
                return new SplitCodeCell();
            }
        });
        _tableCol_split_user.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SplitRow, BoolExp>, ObservableValue<BoolExp>>() {
            @Override
            public ObservableValue<BoolExp> call(TableColumn.CellDataFeatures<SplitRow, BoolExp> param) {
                return new SimpleObjectProperty<>(param.getValue()._user);
            }
        });

        for (BoolExp orPart : _or.getBoolExps()) {
            BoolAnd and = new BoolAnd(orPart);

            for (BoolExp andPart : and.getBoolExps()) {
                _split_items.add(new SplitRow(andPart));
            }
        }

        _tableView_split.setItems(_split_items);

        _tableView_split.setFixedCellSize(50D);
        _tableView_split.prefHeightProperty().bind(_tableView_split.fixedCellSizeProperty().multiply(_tableView_split.getItems().size() + 1).add(10D));

        //_tableView_split.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        _tableCol_split.prefWidthProperty().bind(_tableView_split.widthProperty().multiply(0.3D));
        _tableCol_split_calc.prefWidthProperty().bind(_tableView_split.widthProperty().multiply(0.3D));
        _tableCol_split_user.prefWidthProperty().bind(_tableView_split.widthProperty().multiply(0.3D));
    }

    private abstract class Row {
    }

    private class SplitRow extends Row {
        public BoolExp _impl;
        public BoolExp _calc;
        public BoolExp _user;

        public SplitRow(BoolExp impl) {
            _impl = impl;

            _calc = _impl.reduce();
            System.out.println("test " + _impl + " ---> " + _calc);
            _user = null;
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

    private class SplitBoolExpCell extends BoolExpCell<SplitRow> {
        public SplitBoolExpCell() {
            super();
        }
    }

    private abstract class CodeCell<RowType> extends TableCell<RowType, BoolExp> {
        private HBox _box;
        private ExtendedCodeArea _codeArea;

        private boolean _setup = false;

        public void setup() {
            if (_setup) return;

            _setup = true;

            _box = new HBox();

            _box.getStylesheets().add(getClass().getResource("HoareDialog.css").toExternalForm());
            _box.getStyleClass().add("table-header");

            _codeArea = new ExtendedCodeArea(new CodeArea(), null, null, ExtendedCodeArea.Type.CODE);

            _box.getChildren().add(_codeArea.getTextArea());

            _codeArea.getTextArea().textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                }
            });
        }

        public CodeCell() {
        }

        @Override
        protected void updateItem(BoolExp item, boolean empty) {
            super.updateItem(item, empty);

            /*if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {*/
                setup();
                System.out.println("update");
                setText(null);
                setGraphic(_box);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            //}
        }
    }

    private class SplitCodeCell extends CodeCell<SplitRow> {
        public SplitCodeCell() {
            super();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resources) {

    }
}
