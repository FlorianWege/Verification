package gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import core.Symbol;
import core.SyntaxTree;
import core.SyntaxTreeNode;
import core.SyntaxTreeNodeTerminal;
import core.structures.NonTerminal;
import core.structures.hoareCond.HoareCond;
import grammars.HoareWhileGrammar;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import util.IOUtil;

public class SyntaxChart implements Initializable {
	@FXML
	private AnchorPane _root;
	@FXML
	private Pane _pane;
	@FXML
	private VBox _pane_checkBoxes;
	
	private Scale _scale = new Scale();
	
	private ObjectProperty<SyntaxTree> _syntaxTree;
	private Collection<NonTerminal> _includedNonTerminals = new ArrayList<>();
	
	private ObservableMap<SyntaxTreeNode, HoareCond> _preCondMap;
	private ObservableMap<SyntaxTreeNode, HoareCond> _postCondMap;
	
	private Stage _stage;
	
	private HoareWhileGrammar _hoareGrammar = new HoareWhileGrammar();
	
	public SyntaxChart(ObjectProperty<SyntaxTree> syntaxTree, ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCond>> preCondMap, ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCond>> postCondMap, Map<KeyCombination, Runnable> accelerators) throws IOException {
		_syntaxTree = syntaxTree;
		_preCondMap = preCondMap.get();
		_postCondMap = postCondMap.get();
		
		_stage = new Stage();
		
		_stage.setTitle("Syntax Chart");
		_stage.setScene(IOUtil.inflateFXML(new File("SyntaxChart.fxml"), this));
		_stage.setAlwaysOnTop(true);
		
		_stage.getScene().getAccelerators().putAll(accelerators);
		
		_stage.getScene().widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				updateScale();
			}
		});
		_stage.getScene().heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				updateScale();
			}
		});
		_stage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				//updateScale();
			}
		});
		
		_preCondMap.addListener(new MapChangeListener<SyntaxTreeNode, HoareCond>() {
			@Override
			public void onChanged(javafx.collections.MapChangeListener.Change<? extends SyntaxTreeNode, ? extends HoareCond> obs) {
				SyntaxTreeNode node = obs.getKey();
				HoareCond cond = obs.getValueAdded();
				
				Node chartNode = _nodeMap.get(node);
				
				if (chartNode != null) {
					chartNode.setPreCond(cond);
				}
			}
		});
		_postCondMap.addListener(new MapChangeListener<SyntaxTreeNode, HoareCond>() {
			@Override
			public void onChanged(javafx.collections.MapChangeListener.Change<? extends SyntaxTreeNode, ? extends HoareCond> obs) {
				SyntaxTreeNode node = obs.getKey();
				HoareCond cond = obs.getValueAdded();
				
				Node chartNode = _nodeMap.get(node);
				
				if (chartNode != null) {
					chartNode.setPostCond(cond);
				}
			}
		});
	}

	public void setVisible(boolean show) {
		if (show) _stage.show(); else _stage.hide();
	}

	private void updateScale() {
		if (_treeWidth == 0D) {
			_scale.setPivotX(0D);
			_scale.setX(1D);
			_scale.setPivotY(0D);
			_scale.setY(1D);
		} else {		
			_widthFactor = _pane.getWidth() / _treeWidth;
			_heightFactor = _pane.getHeight() / _treeHeight;
			
			_scale.setPivotX(0D);
			_scale.setX(_widthFactor);
			_scale.setPivotY(0D);
			_scale.setY(_heightFactor);
		}
		
		_pane.backgroundProperty().set(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
	}
	
	private class Node {
		private SyntaxTreeNode _refNode;

		private double _x;
		private double _y;
		
		private Text _text;
		
		private HoareCond _preCond;
		private HoareCond _postCond;

		private Text _preCondText;
		private Text _postCondText;
		
		public void setPreCond(HoareCond preCond) {
			_preCond = preCond;

			if (_preCondText == null) {
				_preCondText = new Text();
				
				_pane.getChildren().add(_preCondText);
			}
			
			_preCondText.setStyle("{color: blue;}");
			_preCondText.setText(_preCond.toString());
			_preCondText.setTextAlignment(TextAlignment.CENTER);
			_preCondText.setTextOrigin(VPos.CENTER);
			
			_preCondText.setX(_x - 20);
			_preCondText.setY(_y - 10);
		}
		
		public void setPostCond(HoareCond postCond) {
			_postCond = postCond;
			
			if (_postCondText == null) {
				_postCondText = new Text();
				
				_pane.getChildren().add(_postCondText);
			}
			
			_postCondText.setStyle("{color: blue;}");
			_postCondText.setText(_postCond.toString());
			_postCondText.setTextAlignment(TextAlignment.CENTER);
			_postCondText.setTextOrigin(VPos.CENTER);
			
			_postCondText.setX(_x + 20);
			_postCondText.setY(_y - 10);
		}
		
		private List<Node> _children = new ArrayList<>();
		
		public double getWidth() {
			double ret = 0D;
			
			for (Node child : _children) {
				ret += child.getWidth();
			}
			
			return Math.max(ret, _text.getBoundsInLocal().getWidth() + 20);
		}
		
		public double getHeight() {
			double ret = _text.getBoundsInLocal().getHeight();
			double childHeight = 0D;
			
			for (Node child : _children) {
				childHeight = Math.max(child.getHeight(), childHeight);
			}
			
			ret += childHeight;
			
			return ret;
		}
		
		public void addChild(Node child) {
			_children.add(child);
		}
		
		public void setXY(double x, double y) {
			_x = x;
			_y = y;
			
			double textWidth = _text.getBoundsInLocal().getWidth();
			double textHeight = _text.getBoundsInLocal().getHeight();
			
			_text.setX(_x - textWidth / 2);
			_text.setY(_y + textHeight / 2);
			
			if (!_children.isEmpty()) {			
				double childX = _x - getWidth() / 2;
				
				for (Node child : _children) {
					childX += child.getWidth() / 2;
					
					child.setXY(childX, _y + textHeight + 50);
					
					childX += child.getWidth() / 2;
				}
			}
		}
		
		private List<SyntaxTreeNode> calcChildren(SyntaxTreeNode refNode) {
			List<SyntaxTreeNode> ret = new ArrayList<>();
			
			if (refNode instanceof SyntaxTreeNodeTerminal) return ret;
			
			for (SyntaxTreeNode child : refNode.getChildren()) {
				List<SyntaxTreeNode> sub = calcChildren(child);
				
				for (SyntaxTreeNode subNode : sub) {
					if (_includedNonTerminals.contains(subNode.getSymbol())) sub.remove(sub);
				}
				
				ret.addAll(sub);
			}
			
			return ret;
		}

		private Text makeText(String text) {
			Text ret = new Text(text);
			
			ret.setTextAlignment(TextAlignment.CENTER);
			ret.setTextOrigin(VPos.CENTER);
			//ret.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
			
			return ret;
		}
		
		public Node(SyntaxTreeNode refNode) {
			_refNode = refNode;
			_x = 0D;
			_y = 0D;
			
			_nodeMap.put(refNode, this);
			
			boolean stop = false;

			if (refNode.getSymbol() != null) {
				stop = !_includedNonTerminals.contains(refNode.getSymbol().getKey());
			}
			
			String label = refNode.toStringVert();

			if (stop) {
				label += System.lineSeparator() + " (" + refNode.synthesize() + ")";
			}
			
			_text = makeText(label);
			
			_pane.getChildren().add(_text);
			
			for (SyntaxTreeNode childRefNode : calcChildren(refNode)) {
				addChild(new Node(childRefNode));
			}
		}
		
		public double getMaxY() {
			double textHeight = _text.getBoundsInLocal().getHeight();
			
			return _y + textHeight;
		}
	}
	
	private double _widthFactor = 0D;
	private double _treeWidth = 0D;
	
	private double _heightFactor = 0D;
	private double _treeHeight = 0D;
	
	private Map<SyntaxTreeNode, Node> _nodeMap = new LinkedHashMap<>();
	
	private Map<CheckBox, NonTerminal> _checkBoxNonTerminalMap = new HashMap<>();
	
	private void updateTree() {
		_pane.getChildren().clear();
		_includedNonTerminals.clear();
		
		//
		for (Map.Entry<CheckBox, NonTerminal> checkBoxEntry : _checkBoxNonTerminalMap.entrySet()) {
			CheckBox box = checkBoxEntry.getKey();
			NonTerminal nonTerminal = checkBoxEntry.getValue();
			
			if (box.selectedProperty().get()) {
				_includedNonTerminals.add(nonTerminal);
			}
		}
		
		Node root = new Node(_syntaxTree.get().getRoot());
		
		_treeWidth = root.getWidth() + 100D;
		_treeHeight = root.getHeight() + 100D;
		
		updateScale();
	}
	
	private void updateAll() {
		updateTree();
		
		_pane_checkBoxes.getChildren().clear();
		_checkBoxNonTerminalMap.clear();

		if (_syntaxTree.get() == null) return;

		List<NonTerminal> activeNonTerminals = new ArrayList<>();
		
		activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_PROG);
		activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_PROG_);
		activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_ASSIGN);
		activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_SELECTION);
		activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_WHILE);
		activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_SKIP);

		for (NonTerminal nonTerminal : _syntaxTree.get().getGrammar().getNonTerminals()) {
			CheckBox box = new CheckBox(nonTerminal.toString());
			
			_checkBoxNonTerminalMap.put(box, nonTerminal);
			
			box.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					updateTree();
				}
			});
			
			_pane_checkBoxes.getChildren().add(box);
			
			if (activeNonTerminals.contains(nonTerminal)) box.setSelected(true);
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		_pane.getTransforms().add(_scale);
		
		_syntaxTree.addListener(new ChangeListener<SyntaxTree>() {
			@Override
			public void changed(ObservableValue<? extends SyntaxTree> obs, SyntaxTree oldVal, SyntaxTree newVal) {
				updateAll();
			}
		});
		
		Timeline t = new Timeline(new KeyFrame(Duration.millis(1000), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				updateScale();				
			}
		}));
		
		t.setCycleCount(Animation.INDEFINITE);
		t.play();
	}
}