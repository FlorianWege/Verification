package gui.testPlayground;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import util.ErrorUtil;
import util.IOUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TestPlaygroundWindow implements gui.Initializable {
    @FXML
    private TabPane _tabPane;
    @FXML
    private Tab _tab_exp;
    @FXML
    private Tab _tab_boolExp;
    @FXML
    private Tab _tab_varAssign;
    @FXML
    private Tab _tab_impl;
    @FXML
    private Tab _tab_loop;

    private TestExpView _testExpView;
    private TestBoolExpView _testBoolExpView;
    private TestVarAssignView _testVarAssignView;
    private TestImplView _testImplView;
    private TestLoopView _testLoopView;

    private Scene _scene;

    public Scene getScene() {
        return _scene;
    }

    private Stage _stage;

    public Stage getStage() {
        return _stage;
    }

    private BooleanProperty _shownProperty = new SimpleBooleanProperty(false);

    public BooleanProperty getShownProperty() {
        return _shownProperty;
    }

    public TestPlaygroundWindow() throws IOException {
        _stage = new Stage();

        _scene = IOUtil.inflateFXML(new File("TestPlaygroundWindow.fxml"), this);

        _stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
        _stage.setScene(_scene);
        _stage.setTitle("Testing playground");

        _stage.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                _shownProperty.set(true);
            }
        });
        _stage.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                _shownProperty.set(false);
            }
        });

        _shownProperty.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(@Nonnull ObservableValue<? extends Boolean> observable, @Nullable Boolean oldValue, @Nullable Boolean newValue) {
                if (newValue != null && newValue) _stage.show(); else _stage.hide();
            }
        });
    }

    @Override
    public void initialize(@Nonnull URL url, @Nullable ResourceBundle resources) {
        try {
            _testExpView = new TestExpView();
            _testBoolExpView = new TestBoolExpView();
            _testVarAssignView = new TestVarAssignView();
            _testImplView = new TestImplView();
            _testLoopView = new TestLoopView();

            _tab_exp.setContent(_testExpView.getScene().getRoot());
            _tab_boolExp.setContent(_testBoolExpView.getScene().getRoot());
            _tab_varAssign.setContent(_testVarAssignView.getScene().getRoot());
            _tab_impl.setContent(_testImplView.getScene().getRoot());
            _tab_loop.setContent(_testLoopView.getScene().getRoot());
        } catch (Exception e) {
            ErrorUtil.logEFX(e);
        }
    }
}