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
import java.util.function.Predicate;

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
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.IOUtil;

public class SyntaxChart implements Initializable {
	@FXML
	private AnchorPane _pane_root;
	@FXML
	private Pane _pane;
	@FXML
	private VBox _pane_checkBoxes;
	
	private Scale _scale = new Scale();
	
	private ObjectProperty<SyntaxTree> _syntaxTreeP;
	private ObservableMap<SyntaxTreeNode, HoareCond> _preCondMap;
	private ObservableMap<SyntaxTreeNode, HoareCond> _postCondMap;
	private ObjectProperty<SyntaxTreeNode> _currentNodeP;
	private ObjectProperty<SyntaxTreeNode> _currentHoareNodeP;
	
	private Collection<NonTerminal> _includedNonTerminals = new ArrayList<>();
	private Stage _stage;
	
	private HoareWhileGrammar _hoareGrammar = new HoareWhileGrammar();
	
	public SyntaxChart(ObjectProperty<SyntaxTree> syntaxTree, ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCond>> preCondMap, ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCond>> postCondMap, ObjectProperty<SyntaxTreeNode> currentNodeP, ObjectProperty<SyntaxTreeNode> currentHoareNodeP, Map<KeyCombination, Runnable> accelerators) throws IOException {
		_syntaxTreeP = syntaxTree;
		_preCondMap = preCondMap.get();
		_postCondMap = postCondMap.get();
		_currentNodeP = currentNodeP;
		_currentHoareNodeP = currentHoareNodeP;
		
		_stage = new Stage();
		
		_stage.setTitle("Syntax Chart");
		_stage.setScene(IOUtil.inflateFXML(new File("SyntaxChart.fxml"), this));
		//_stage.setAlwaysOnTop(true);
		
		_stage.getScene().getAccelerators().putAll(accelerators);
		
		_stage.getScene().widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
				updateScale();
			}
		});
		_stage.getScene().heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
				updateScale();
			}
		});
		_stage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) {
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
	
	private class Node {
		private SyntaxTreeNode _refNode;

		private double _x;
		private double _y;
		
		public double getX() {
			return _x;
		}
		
		public double getY() {
			return _y;
		}
		
		private Pane _box = new Pane();
		private List<Text> _boxItems = new ArrayList<>();
		
		private Text _text = new Text();
		private Text _subText = new Text();
		
		private HoareCond _preCond;
		private HoareCond _postCond;

		private Text _preCondText = new Text();
		private Text _postCondText = new Text();
		
		public void setPreCond(HoareCond preCond) {
			_preCond = preCond;
			
			_preCondText.setFill(Color.BLUE);
			_preCondText.setText("pre: " + _preCond.toStringEx());
			
			update();
		}
		
		public void setPostCond(HoareCond postCond) {
			_postCond = postCond;
			
			_postCondText.setFill(Color.BLUE);
			_postCondText.setText("post: " + _postCond.toStringEx());
			
			update();
		}
		
		private List<Node> _children = new ArrayList<>();
		
		private final double PADDING_X = 5D;
		private final double PADDING_Y = 5D;

		
		private double getLocalWidth() {
			double ret = 0D;
			
			for (Shape item : _boxItems) {
				ret = Math.max(item.getBoundsInLocal().getWidth(), ret);
			}

			return ret + PADDING_X;
		}
		
		private double getLocalHeight() {
			double ret = PADDING_Y;
			
			for (Shape item : _boxItems) {
				ret += item.getBoundsInLocal().getHeight();
			}

			return ret;
		}
		
		private final double MARGIN_X = 20D;
		private final double MARGIN_Y = 30D;
		
		public double getWidth() {
			double ret = 0D;
			
			for (Node child : _children) {
				ret += child.getWidth();
			}
			
			return Math.max(getLocalWidth(), ret) + MARGIN_X;
		}
		
		public double getHeight() {
			double ret = getLocalHeight();
			double childHeight = 0D;
			
			for (Node child : _children) {
				childHeight = Math.max(child.getHeight(), childHeight);
			}
			
			ret += childHeight;
			
			return ret + MARGIN_Y;
		}
		
		private Circle _circle = new Circle();
		private Rectangle _rect = new Rectangle();
		
		public void addChild(Node child) {
			_children.add(child);
			
			Line line = new Line();
			
			_lineMap.put(child, line);
			_pane.getChildren().add(line);
		}
		
		private Map<Node, Line> _lineMap = new LinkedHashMap<>();
		
		public void update() {
			double localWidth = getLocalWidth();
			double localHeight = getLocalHeight();
			
			_box.relocate(_x - localWidth/2, _y - localHeight/2);
			
			_circle.setCenterX(_x);
			_circle.setCenterY(_y);
			_circle.setRadius(10D);
			_circle.setVisible(false);
			
			_rect.setWidth(localWidth);
			_rect.setHeight(localHeight);
			_rect.setX(_x-_rect.getWidth() / 2);
			_rect.setY(_y-_rect.getHeight() / 2);
			_rect.setFill(_isCurrentHoare ? Color.YELLOW : _isCurrent ? Color.RED : Color.TRANSPARENT);
			_rect.setStroke(Color.BLACK);
			_rect.setStrokeWidth(1D);
			
			double itemY = 0D;
			
			for (Text item : _boxItems) {
				item.setTextAlignment(TextAlignment.CENTER);
				item.setTextOrigin(VPos.CENTER);
				item.setVisible(true);
				item.setX(localWidth/2-item.getBoundsInLocal().getWidth()/2);
				item.setY(itemY + item.getBoundsInLocal().getHeight()/2);
				
				itemY += item.getBoundsInLocal().getHeight();
			}
			
			if (!_children.isEmpty()) {			
				double childX = _x - getWidth() / 2;
				
				for (Node child : _children) {
					childX += child.getWidth() / 2;
					double childY = _y + localHeight + 50D;
					
					child.setXY(childX, childY);
					
					Line line = _lineMap.get(child);
					
					line.setStartX(_x);
					line.setStartY(_y + localHeight / 2);
					line.setEndX(child.getX());
					line.setEndY(child.getY() - child.getLocalHeight()/2);
					
					childX += child.getWidth() / 2;
				}
			}
		}

		private boolean _isCurrentHoare = false;
		
		public void setCurrentHoare(boolean flag) {
			_isCurrentHoare = flag;
			
			update();
		}

		private boolean _isCurrent = false;
		
		public void setCurrent(boolean flag) {
			_isCurrent = flag;
			
			update();
		}
		
		public void setXY(double x, double y) {
			_x = x;
			_y = y;
			
			update();
		}
		
		private List<SyntaxTreeNode> calcChildren(SyntaxTreeNode refNode) {
			//System.out.println("calc");
			List<SyntaxTreeNode> ret = new ArrayList<>();
			
			if (refNode instanceof SyntaxTreeNodeTerminal) return ret;
			
			for (SyntaxTreeNode child : refNode.getChildren()) {
				List<SyntaxTreeNode> sub = calcChildren(child);

				if (_includedNonTerminals.contains(child.getSymbol())) {
					if (!child.synthesize().isEmpty()) ret.add(child);
				} else {
					sub.removeIf(new Predicate<SyntaxTreeNode>() {
						@Override
						public boolean test(SyntaxTreeNode node) {
							return !_includedNonTerminals.contains(node.getSymbol()) || node.synthesize().isEmpty();
						}
					});
					
					ret.addAll(sub);
				}
			}
//System.out.println("ret " + ret);
			return ret;
		}
		
		public Node(SyntaxTreeNode refNode) {
			_refNode = refNode;
			_x = 0D;
			_y = 0D;
System.out.println("create " + refNode + ";" + refNode.hashCode());
			_pane.getChildren().add(_circle);
			_pane.getChildren().add(_rect);

			_nodeMap.put(refNode, this);
			
			boolean stop = false;

			if (refNode.getSymbol() != null) {
				stop = !_includedNonTerminals.contains(refNode.getSymbol().getKey());
			}
			
			String label = refNode.toStringVert();
			
			_text = new Text(label);
			
			_text.setTextAlignment(TextAlignment.CENTER);
			_text.setTextOrigin(VPos.CENTER);
			_text.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
			_text.setVisible(false);
			
			_subText = new Text(stop ? refNode.synthesize() : null);


			_subText.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
			_subText.setVisible(false);
			
			_boxItems.add(_text);
			_boxItems.add(_preCondText);
			_boxItems.add(_subText);
			_boxItems.add(_postCondText);
			
			for (Text item : _boxItems) {
				_box.getChildren().add(item);
			}
			
			_pane.getChildren().add(_box);
			
			for (SyntaxTreeNode childRefNode : calcChildren(refNode)) {
				addChild(new Node(childRefNode));
			}
		}
	}
	
	private Map<SyntaxTreeNode, Node> _nodeMap = new LinkedHashMap<>();
	
	private Map<CheckBox, NonTerminal> _checkBoxNonTerminalMap = new HashMap<>();
	
	private Node _root;
	
	private void updateScale() {
		_pane.backgroundProperty().set(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		
		if (_root != null) {
			double treeWidth = _root.getWidth();
			double treeHeight = _root.getHeight();
			
			/*if (treeWidth == 0D) {
				_scale.setPivotX(0D);
				_scale.setX(1D);
				_scale.setPivotY(0D);
				_scale.setY(1D);
			} else {		
				double widthFactor = _pane.getWidth() / treeWidth;
				double heightFactor = _pane.getHeight() / treeHeight;
				
				_scale.setPivotX(0D);
				_scale.setX(widthFactor);
				_scale.setPivotY(0D);
				_scale.setY(heightFactor);
			}*/

			_root.setXY(treeWidth / 2, _root.getLocalHeight() / 2 + _root.MARGIN_Y/2);
		}
	}
	
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

		if (_syntaxTreeP.get() != null) {
			System.out.println("updateTree " + _syntaxTreeP.get().getRoot().hashCode());
			_root = new Node(_syntaxTreeP.get().getRoot());
			
			updateScale();
		}
	}
	
	private void updateAll() {
		_pane_checkBoxes.getChildren().clear();
		_checkBoxNonTerminalMap.clear();

		if (_syntaxTreeP.get() != null) {
			List<NonTerminal> activeNonTerminals = new ArrayList<>();
			
			activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_PROG);
			activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_PROG_);
			activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_HOARE_BLOCK);
			activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_ASSIGN);
			activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_SELECTION);
			activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_WHILE);
			activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_SKIP);

			for (NonTerminal nonTerminal : _syntaxTreeP.get().getGrammar().getNonTerminals()) {
				CheckBox box = new CheckBox(nonTerminal.toString());
				
				_checkBoxNonTerminalMap.put(box, nonTerminal);
				
				if (activeNonTerminals.contains(nonTerminal)) box.setSelected(true);
				
				box.selectedProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) {
						updateTree();
					}
				});
				
				_pane_checkBoxes.getChildren().add(box);
			}
		};
		
		updateTree();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		_pane.getTransforms().add(_scale);
		
		_syntaxTreeP.addListener(new ChangeListener<SyntaxTree>() {
			@Override
			public void changed(ObservableValue<? extends SyntaxTree> obs, SyntaxTree oldVal, SyntaxTree newVal) {
				System.out.println("changed " + newVal.hashCode());
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
		
		updateAll();

		_currentNodeP.addListener(new ChangeListener<SyntaxTreeNode>() {
			@Override
			public void changed(ObservableValue<? extends SyntaxTreeNode> obs, SyntaxTreeNode oldVal, SyntaxTreeNode newVal) {
				Node oldNode = _nodeMap.get(oldVal);
				Node newNode = _nodeMap.get(newVal);

				if (oldNode != null) oldNode.setCurrent(false);
				if (newNode != null) newNode.setCurrent(true);
			}
		});
		_currentHoareNodeP.addListener(new ChangeListener<SyntaxTreeNode>() {
			@Override
			public void changed(ObservableValue<? extends SyntaxTreeNode> obs, SyntaxTreeNode oldVal, SyntaxTreeNode newVal) {
				Node oldNode = _nodeMap.get(oldVal);
				Node newNode = _nodeMap.get(newVal);

				if (oldNode != null) oldNode.setCurrentHoare(false);
				if (newNode != null) newNode.setCurrentHoare(true);
			}
		});
	}
}