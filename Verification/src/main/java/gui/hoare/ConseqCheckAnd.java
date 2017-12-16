package gui.hoare;

import core.structures.semantics.boolExp.BoolAnd;
import core.structures.semantics.boolExp.BoolExp;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import util.IOUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ConseqCheckAnd implements gui.Initializable {
    @FXML
    private TitledPane _titledPane;
    @FXML
    private VBox _box;

    private final BoolAnd _boolAnd;
    private CallBack _callback;

    private Scene _scene;

    public Node getRoot() {
        return _scene.getRoot();
    }

    private ConseqCheckTable _table;
    private ConseqCheckTable _end;

    public interface CallBack {
        void set(@Nonnull BoolExp result);
    }

    private List<BoolExp> _parts = new ArrayList<>();
    private Map<Integer, BoolExp> _map = new LinkedHashMap<>();

    public ConseqCheckAnd(@Nonnull BoolAnd boolAnd, @Nonnull CallBack callback) throws IOException {
        _boolAnd = boolAnd;
        _callback = callback;

        _scene = IOUtil.inflateFXML(new File("ConseqCheckAnd.fxml"), this);

        _titledPane.setText(boolAnd.getContentString());
        _titledPane.getStylesheets().add(getClass().getResource("HoareDialog.css").toExternalForm());

        _titledPane.applyCss();

        _table = new ConseqCheckTable(new ConseqCheckTable.ActionInterface() {
            @Override
            public void set(int index, BoolExp result) {
                _map.put(index, result);

                for (int i = 0; i < _parts.size(); i++) {
                    if (_map.get(i) == null) return;
                }

                BoolAnd newAnd = new BoolAnd();

                for (int i = 0; i < _parts.size(); i++) {
                    newAnd.addBoolExp(_map.get(i));
                }

                _end.clear();

                _end.addBoolExp(newAnd);
            }
        });

        _box.getChildren().add(_table.getRoot());

        _end = new ConseqCheckTable(new ConseqCheckTable.ActionInterface() {
            @Override
            public void set(int index, BoolExp result) {
                _callback.set(result);
            }
        });

        _box.getChildren().add(_end.getRoot());

        _parts.addAll(_boolAnd.getBoolExps());

        for (BoolExp andPart : _parts) {
            _table.addBoolExp(andPart);
        }
    }

    @Override
    public void initialize(@Nonnull URL url, @Nullable ResourceBundle resources) {

    }
}