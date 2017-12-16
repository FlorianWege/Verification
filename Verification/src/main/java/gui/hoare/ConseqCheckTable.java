package gui.hoare;

import core.Parser;
import core.structures.semantics.boolExp.BoolExp;
import grammars.BoolExpGrammar;
import gui.ExtendedCodeArea;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import org.fxmisc.richtext.CodeArea;
import util.IOUtil;
import util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ConseqCheckTable implements gui.Initializable {
    @FXML
    private TableView<Row> _tableView;
    @FXML
    private TableColumn<Row, Row> _tableCol_source;
    @FXML
    private TableColumn<Row, Row> _tableCol_reduced;
    @FXML
    private TableColumn<Row, Row> _tableCol_reduced_auto;
    @FXML
    private TableColumn<Row, Row> _tableCol_reduced_user;

    private final double ROW_HEIGHT = 50D;

    private Scene _scene;

    public Node getRoot() {
        return _scene.getRoot();
    }

    private ActionInterface _actionInterface;

    public interface ActionInterface {
        void set(int index, BoolExp result);
    }

    private List<BoolExp> _parts = new ArrayList<>();

    public void clear() {
        _parts.clear();
        _tableView.getItems().clear();
    }

    public void addBoolExp(BoolExp boolExp) {
        int index = _parts.size();

        _parts.add(boolExp);

        Row row = new Row(boolExp);

        _tableView.getItems().add(row);

        row._result.addListener(new ChangeListener<BoolExp>() {
            @Override
            public void changed(ObservableValue<? extends BoolExp> observable, BoolExp oldValue, BoolExp newValue) {
                _actionInterface.set(index, newValue);
            }
        });

        row.calc();
    }

    public ConseqCheckTable(ActionInterface actionInterface) throws IOException {
        _actionInterface = actionInterface;

        _scene = IOUtil.inflateFXML(new File("ConseqCheckTable.fxml"), this);

        _tableView.getItems().addListener(new ListChangeListener<Row>() {
            @Override
            public void onChanged(Change<? extends Row> c) {
                scaleTable();
            }
        });

        _tableCol_source.setCellFactory(new Callback<TableColumn<Row, Row>, TableCell<Row, Row>>() {
            @Override
            public TableCell<Row, Row> call(TableColumn<Row, Row> param) {
                return new SourceBoolExpCell();
            }
        });
        _tableCol_source.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Row, Row>, ObservableValue<Row>>() {
            @Override
            public ObservableValue<Row> call(TableColumn.CellDataFeatures<Row, Row> param) {
                return new SimpleObjectProperty<>(param.getValue());
            }
        });

        _tableCol_reduced_auto.setCellFactory(new Callback<TableColumn<Row, Row>, TableCell<Row, Row>>() {
            @Override
            public TableCell<Row, Row> call(TableColumn<Row, Row> param) {
                return new CalcBoolExpCell();
            }
        });
        _tableCol_reduced_auto.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Row, Row>, ObservableValue<Row>>() {
            @Override
            public ObservableValue<Row> call(TableColumn.CellDataFeatures<Row, Row> param) {
                return new SimpleObjectProperty<>(param.getValue());
            }
        });

        _tableCol_reduced_user.setCellFactory(new Callback<TableColumn<Row, Row>, TableCell<Row, Row>>() {
            @Override
            public TableCell<Row, Row> call(TableColumn<Row, Row> param) {
                return new UserCodeCell();
            }
        });
        _tableCol_reduced_user.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Row, Row>, ObservableValue<Row>>() {
            @Override
            public ObservableValue<Row> call(TableColumn.CellDataFeatures<Row, Row> param) {
                return new SimpleObjectProperty<>(param.getValue());
            }
        });

        scaleTable();
    }

    private void scaleTable() {
        _tableView.setFixedCellSize(ROW_HEIGHT);
        _tableView.setPrefHeight(52D + ROW_HEIGHT * _tableView.getItems().size());

        final int split_columnsCount = _tableView.getColumns().size();

        for (TableColumn<?, ?> col : _tableView.getColumns()) {
            col.prefWidthProperty().bind(_tableView.widthProperty().subtract(15D).divide(split_columnsCount));
            col.setSortable(false);

            for (TableColumn<?, ?> col2 : col.getColumns()) {
                col2.prefWidthProperty().bind(col.widthProperty().divide(col.getColumns().size()));
                col2.setSortable(false);
            }
        }
    }

    private class Row {
        public final BoolExp _source;
        public BoolExp _calc = null;
        public BoolExp _user = null;

        public ObjectProperty<BoolExp> _result = new SimpleObjectProperty<>();

        private void updateResult() {
            _result.set(_user != null ? _user : _calc);
        }

        public void setUser(BoolExp user) {
            _user = user;

            updateResult();
        }

        public void calc() {
            _calc = _source.reduceEx().getRet();

            updateResult();
        }

        public Row(@Nonnull BoolExp impl) {
            _source = impl;
        }
    }

    private abstract class BoolExpCell<RowType> extends TableCell<RowType, RowType> {
        private Pane _box;
        protected WebView _label;

        private boolean _setup = false;

        public void setup() {
            if (_setup) return;

            _setup = true;

            _box = new Pane();

            _box.setMaxHeight(ROW_HEIGHT);

            _box.getStylesheets().add(getClass().getResource("HoareDialog.css").toExternalForm());
            _box.getStyleClass().add("table-cell-fixed");

            _label = new WebView();

            _box.getChildren().add(_label);

            _label.prefWidthProperty().bind(_box.widthProperty());
            _label.getStylesheets().add(getClass().getResource("HoareDialog.css").toExternalForm());
            _label.getStyleClass().add("table-cell-fixed");
        }

        public BoolExpCell() {
        }

        protected abstract void updateItemSpec(RowType item);

        @Override
        protected void updateItem(RowType item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setup();

                updateItemSpec(item);

                setText(null);
                setGraphic(_box);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        }
    }

    //split
    private class SourceBoolExpCell extends BoolExpCell<Row> {
        public SourceBoolExpCell() {
            super();
        }

        @Override
        protected void updateItemSpec(Row item) {
            _label.getEngine().loadContent("<center>" + StringUtil.escapeHTML(item._source.getContentString()) + "</center>");
        }
    }

    private class CalcBoolExpCell extends BoolExpCell<Row> {
        public CalcBoolExpCell() {
            super();
        }

        @Override
        protected void updateItemSpec(Row item) {
            _label.getEngine().loadContent("<center>" + StringUtil.escapeHTML(item._calc.getContentString()) + "</center>");
        }
    }

    private class UserCodeCell extends TableCell<Row, Row> {
        private HBox _box;
        private ExtendedCodeArea _codeArea;

        private boolean _setup = false;

        public void setup() {
            if (_setup) return;

            _setup = true;

            _box = new HBox();

            _box.setMaxHeight(ROW_HEIGHT);

            _box.getStylesheets().add(getClass().getResource("HoareDialog.css").toExternalForm());
            _box.getStyleClass().add("table-header");

            _codeArea = new ExtendedCodeArea(new CodeArea());

            _codeArea.setParser(new Parser(BoolExpGrammar.getInstance()));

            _codeArea.getTextArea().prefHeightProperty().bind(_box.heightProperty());
            _codeArea.getTextArea().prefWidthProperty().bind(_box.widthProperty());

            _codeArea.getTextArea().getStylesheets().add("HoareDialog.css");
            _codeArea.getTextArea().getStyleClass().add("center");

            _box.getChildren().add(_codeArea.getTextArea());
        }

        public UserCodeCell() {
        }

        private ChangeListener<String> _codeArea_changeListener = null;

        @Override
        protected void updateItem(Row item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setup();

                if (_codeArea_changeListener != null) _codeArea.getTextArea().textProperty().removeListener(_codeArea_changeListener);

                _codeArea_changeListener = new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        BoolExp boolExp = (BoolExp) _codeArea.getParsing();

                        getItem().setUser(boolExp);
                    }
                };

                _codeArea.getTextArea().textProperty().addListener(_codeArea_changeListener);

                setText(null);
                setGraphic(_box);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        }
    }

    @Override
    public void initialize(@Nonnull URL url, @Nullable ResourceBundle resources) {

    }
}