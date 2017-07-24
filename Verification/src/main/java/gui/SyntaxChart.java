package gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import core.Rule;
import core.RuleKey;
import core.SyntaxTree;
import core.SyntaxTreeNode;
import core.structures.HoareCondition;
import core.structures.ParserRule;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
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
	private Collection<RuleKey> _excludedRules = new ArrayList<>();
	
	private ObservableMap<SyntaxTreeNode, HoareCondition> _preCondMap;
	private ObservableMap<SyntaxTreeNode, HoareCondition> _postCondMap;
	
	private Stage _stage;
	
	public SyntaxChart(ObjectProperty<SyntaxTree> syntaxTree, ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCondition>> preCondMap, ObjectProperty<ObservableMap<SyntaxTreeNode, HoareCondition>> postCondMap) throws IOException {
		_syntaxTree = syntaxTree;
		_preCondMap = preCondMap.get();
		_postCondMap = postCondMap.get();
		
		_stage = new Stage();
		
		_stage.setTitle("Syntax Chart");
		_stage.setScene(IOUtil.inflateFXML(new File("SyntaxChart.fxml"), this));
		_stage.setAlwaysOnTop(true);
		
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
		
		_preCondMap.addListener(new MapChangeListener<SyntaxTreeNode, HoareCondition>() {
			@Override
			public void onChanged(javafx.collections.MapChangeListener.Change<? extends SyntaxTreeNode, ? extends HoareCondition> obs) {
				SyntaxTreeNode node = obs.getKey();
				HoareCondition cond = obs.getValueAdded();
				
				Node chartNode = _nodeMap.get(node);
				
				if (chartNode != null) {
					chartNode.setPreCond(cond);
				}
			}
		});
		_postCondMap.addListener(new MapChangeListener<SyntaxTreeNode, HoareCondition>() {
			@Override
			public void onChanged(javafx.collections.MapChangeListener.Change<? extends SyntaxTreeNode, ? extends HoareCondition> obs) {
				SyntaxTreeNode node = obs.getKey();
				HoareCondition cond = obs.getValueAdded();
				
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
		
		//System.out.println("update " + _widthFactor + ";" + _heightFactor);
		
		_pane.backgroundProperty().set(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
	}
	
	private Pair<Integer, Integer> getTreeSize(SyntaxTreeNode node, int nestDepth) {
		Pair<Integer, Integer> ret = new Pair<>(Math.max(1, node.getChildren().size()), nestDepth);
		
		if ((node.getRule() != null) && _excludedRules.contains(node.getRule().getKey())) return ret;
		
		for (SyntaxTreeNode child : node.getChildren()) {
			Pair<Integer, Integer> subSize = getTreeSize(child, nestDepth + 1);
			
			ret = new Pair<>(Math.max(ret.getKey(), subSize.getKey()), Math.max(ret.getValue(), subSize.getValue()));
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
	
	private class Node {
		private SyntaxTreeNode _node;
		
		private double _x;
		private double _y;
		
		private Text _text;
		
		private HoareCondition _preCond;
		private HoareCondition _postCond;

		private Text _preCondText;
		private Text _postCondText;
		
		public void setPreCond(HoareCondition preCond) {
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
		
		public void setPostCond(HoareCondition postCond) {
			System.out.println("set post cond " + postCond);
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
		
		public Node(SyntaxTreeNode node, double x, double y) {
			_node = node;
			_x = x;
			_y = y;
			
			boolean stop = false;

			if (node.getRule() != null) {
				stop = _excludedRules.contains(node.getRule().getKey());
			}
			
			String label = _node.toStringVert();

			if (stop) {
				label += System.lineSeparator() + " (" + _node.synthesize() + ")";
			}
			
			_text = makeText(label);
			
			double textWidth = _text.getBoundsInLocal().getWidth();
			double textHeight = _text.getBoundsInLocal().getHeight();
			
			_text.setX(_x - textWidth / 2);
			_text.setY(_y + textHeight / 2);
			
			_pane.getChildren().add(_text);
		}
		
		public double getMaxY() {
			double textHeight = _text.getBoundsInLocal().getHeight();
			
			return _y + textHeight;
		}
	}
	
	private double getTreeWidth(SyntaxTreeNode node) {
		boolean stop = false;
		
		if (node.getChildren().size() == 0) {
			stop = true;
		} else {
			if ((node.getRule() != null) && _excludedRules.contains(node.getRule().getKey())) {
				stop = true;
			}
		}
		
		if (stop) {
			return makeText(node.toString()).getBoundsInLocal().getWidth() + 20;
		}
		
		double childrenWidth = 0;
		double[] childrenWidths = new double[node.getChildren().size()];

		for (int i = 0; i < node.getChildren().size(); i++) {
			SyntaxTreeNode child = node.getChildren().get(i);

			double childWidth = getTreeWidth(child);

			childrenWidth += childWidth;
		}
		
		return childrenWidth;
	}
	
	private double _widthFactor = 0D;
	private double _treeWidth = 0D;
	
	private double _heightFactor = 0D;
	private double _treeHeight = 0D;
	
	private Map<SyntaxTreeNode, Node> _nodeMap = new HashMap<>();
	
	private Node buildTree(SyntaxTreeNode node, int nestDepth, double x, double y) {
		boolean stop = false;
		
		if (node.getRule() != null) {
			RuleKey ruleKey = node.getRule().getKey();

			stop = _excludedRules.contains(ruleKey);
		}
		
		Node ret = new Node(node, x, y);
		
		_nodeMap.put(node, ret);
		
		y = ret.getMaxY();
		
		if (stop) return ret;
		
		double childrenWidth = 0;
		double[] childrenWidths = new double[node.getChildren().size()];

		for (int i = 0; i < node.getChildren().size(); i++) {
			SyntaxTreeNode child = node.getChildren().get(i);
			
			double childWidth = getTreeWidth(child);

			childrenWidths[i] = childWidth;
			childrenWidth += childWidth;
		}

		for (int i = 0; i < node.getChildren().size(); i++) {
			SyntaxTreeNode child = node.getChildren().get(i);
		
			double childX = x - childrenWidth / 2;

			for (int j = 0; j < i; j++) {
				childX += childrenWidths[j];
			}
		
			childX += childrenWidths[i] / 2;

			double childY = y + 50;
			
			Line line = new Line(x, y, childX, childY);
			
			_pane.getChildren().add(line);
			
			/*Line leftLimit = new Line(x - childrenWidth / 2, y, x - childrenWidth / 2, y+20);
			Line rightLimit = new Line(x + childrenWidth / 2, y, x + childrenWidth / 2, y+20);
			
			_pane.getChildren().add(leftLimit);
			_pane.getChildren().add(rightLimit);*/
			
			buildTree(child, nestDepth + 1, childX, childY);
		}
		
		if (y > _treeHeight) {
			_treeHeight = y;
		}
		
		return ret;
	}
	
	private Map<CheckBox, Rule> _checkBoxRuleMap = new HashMap<>();
	
	private void updateTree() {
		_pane.getChildren().clear();
		_excludedRules.clear();
		
		//
		for (Map.Entry<CheckBox, Rule> checkBoxEntry : _checkBoxRuleMap.entrySet()) {
			CheckBox box = checkBoxEntry.getKey();
			Rule rule = checkBoxEntry.getValue();
			
			if (box.selectedProperty().get()) {
				_excludedRules.add(rule.getKey());
			}
		}
		
		getTreeSize(_syntaxTree.get().getRoot(), 1);
		
		_treeWidth = getTreeWidth(_syntaxTree.get().getRoot()) + 100;
		_treeHeight = 0D;
		
		buildTree(_syntaxTree.get().getRoot(), 1, _treeWidth / 2, 50);
		
		_treeHeight += 100D;
		
		updateScale();
	}
	
	private void updateAll() {
		updateTree();
		
		_pane_checkBoxes.getChildren().clear();
		_checkBoxRuleMap.clear();

		if (_syntaxTree.get() == null) return;

		for (ParserRule rule : _syntaxTree.get().getGrammar().getParserRules()) {
			CheckBox box = new CheckBox(rule.toString());
			
			_checkBoxRuleMap.put(box, rule);
			
			box.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					updateTree();
				}
			});
			
			_pane_checkBoxes.getChildren().add(box);
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