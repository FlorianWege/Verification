package gui.hoare;

import core.structures.semantics.boolExp.BoolAnd;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.BoolImpl;
import core.structures.semantics.boolExp.BoolOr;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import util.IOUtil;
import util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ConseqCheckTableView implements Initializable {
    @FXML
    private Pane _root;
    @FXML
    private WebView _textArea_split;
    @FXML
    private VBox _box_content;
    @FXML
    private WebView _textArea_result;

    private BoolImpl _origImpl;
    private BoolExp _split;
    private BoolOr _or;

    private Scene _scene;

    public Node getRoot() {
        return _scene.getRoot();
    }

    private Map<Integer, BoolExp> _map = new LinkedHashMap<>();
    private List<BoolExp> _parts;

    public interface Callback {
        void result(BoolExp boolExp);
    }

    private final Callback _callback;

    public ConseqCheckTableView(Callback callback) throws IOException {
        _callback = callback;

        _scene = IOUtil.inflateFXML(new File("ConseqCheckTableView.fxml"), this);
    }

    public void setBoolImpl(BoolImpl origImpl) throws IOException {
        _origImpl = origImpl;

        _split = null;
        _or = null;
        _box_content.getChildren().clear();

        if (_origImpl == null) return;

        _split = _origImpl.split(true, false);

        _or = new BoolOr(_split);

        _textArea_split.getEngine().loadContent("<center><b>" + _split.getContentString() + "</b></center>");
        _textArea_result.getEngine().loadContent("<center><b>" + "result" + "</b></center>");

        _parts = new ArrayList<>();

        _parts.addAll(_or.getBoolExps());

        for (int i = 0; i < _parts.size(); i++) {
            BoolAnd and = new BoolAnd(_parts.get(i));

            int finalI = i;

            ConseqCheckAnd andView = new ConseqCheckAnd(and, new ConseqCheckAnd.CallBack() {
                @Override
                public void set(BoolExp result) {
                    _map.put(finalI, result);

                    for (int i = 0; i < _parts.size(); i++) {
                        if (_map.get(i) == null) return;
                    }

                    BoolOr newOr = new BoolOr();

                    for (int i = 0; i < _parts.size(); i++) {
                        newOr.addBoolExp(_map.get(i));
                    }

                    BoolExp reduced = newOr.reduce();
                    System.out.println("res");
                    _textArea_result.getEngine().loadContent("<center><b>" + StringUtil.escapeHTML(newOr.getContentString() + StringUtil.bool_impl_meta + reduced.getContentString()) + "</b></center>");

                    _callback.result(reduced);
                }
            });

            _box_content.getChildren().add(andView.getRoot());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resources) {
    }
}