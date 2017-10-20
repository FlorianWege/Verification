package gui;

import core.structures.NonTerminal;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.prog.HoareCond;
import core.structures.syntax.SyntaxNode;
import core.structures.syntax.SyntaxNodeTerminal;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.ErrorUtil;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

public class TreeChartWindow implements Initializable {
	@FXML
	private AnchorPane _pane_root;
	@FXML
	private TabPane _tabPane;

	@FXML
	private Tab _tab_syntax;
	@FXML
	private Pane _pane_syntax;
	@FXML
	private ScrollPane _scrollPane_syntax;
	@FXML
	private VBox _pane_syntax_checkBoxes;

	@FXML
	private Tab _tab_semantic;
	@FXML
	private Pane _pane_semantic;
	@FXML
	private ScrollPane _scrollPane_semantic;
	@FXML
	private VBox _pane_semantic_checkBoxes;
	
	private final Scale _scale = new Scale();
	
	private final ObjectProperty<SyntaxNode> _syntaxTreeP;
	private final ObjectProperty<SemanticNode> _semanticTreeP;
	private final ObjectProperty<ObservableMap<SemanticNode, HoareCond>> _preCondMapP;
	private final ObjectProperty<ObservableMap<SemanticNode, HoareCond>> _postCondMapP;
	private final ObjectProperty<SemanticNode> _currentNodeP;
	private final ObjectProperty<SemanticNode> _currentHoareNodeP;
	
	private final Collection<NonTerminal> _syntax_includedNonTerminals = new ArrayList<>();
	private final Stage _stage;
	
	private final HoareWhileGrammar _hoareGrammar = HoareWhileGrammar.getInstance();

	public TreeChartWindow(@Nonnull ObjectProperty<SyntaxNode> syntaxTreeP, @Nonnull ObjectProperty<SemanticNode> semanticTreeP, @Nonnull ObjectProperty<ObservableMap<SemanticNode, HoareCond>> preCondMapP, ObjectProperty<ObservableMap<SemanticNode, HoareCond>> postCondMapP, ObjectProperty<SemanticNode> currentNodeP, @Nonnull ObjectProperty<SemanticNode> currentHoareNodeP, @Nonnull Map<KeyCombination, Runnable> accelerators) throws IOException {
		_syntaxTreeP = syntaxTreeP;
		_semanticTreeP = semanticTreeP;
		_preCondMapP = preCondMapP;
		_postCondMapP = postCondMapP;
		_currentNodeP = currentNodeP;
		_currentHoareNodeP = currentHoareNodeP;

		_stage = new Stage();

		_stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
		_stage.setScene(IOUtil.inflateFXML(new File("TreeChartWindow.fxml"), this));
		_stage.setTitle("Syntax Chart");
		
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
		
		_preCondMapP.get().addListener(new MapChangeListener<SemanticNode, HoareCond>() {
			@Override
			public void onChanged(javafx.collections.MapChangeListener.Change<? extends SemanticNode, ? extends HoareCond> obs) {
				SemanticNode node = obs.getKey();

				TreeNode<SemanticNode> chartNode = _semantic_nodeMap.get(node);
				
				if (chartNode != null) chartNode.update();
			}
		});
		_postCondMapP.get().addListener(new MapChangeListener<SemanticNode, HoareCond>() {
			@Override
			public void onChanged(javafx.collections.MapChangeListener.Change<? extends SemanticNode, ? extends HoareCond> obs) {
				SemanticNode node = obs.getKey();
				
				TreeNode<SemanticNode> chartNode = _semantic_nodeMap.get(node);
				
				if (chartNode != null) chartNode.update();
			}
		});
	}

	public void setHoare() {
		_tabPane.getSelectionModel().select(_tab_semantic);
	}

	void setVisible(boolean show) {
		if (show) _stage.show(); else _stage.hide();
	}
	
	private abstract class TreeNode<T> {
		protected final T _refNode;

		private double _x;
		private double _y;
		
		public double getX() {
			return _x;
		}
		
		public double getY() {
			return _y;
		}
		
		private final Pane _box = new Pane();
		private final List<Text> _boxItems = new ArrayList<>();
		
		private final Text _text = new Text();
		private final Text _subText = new Text();

		private final Text _preCondText = new Text();
		private final Text _postCondText = new Text();
		
		private final List<TreeNode> _children = new ArrayList<>();
		
		private final double PADDING_X = 5D;
		private final double PADDING_Y = 5D;
		
		private double getLocalWidth() {
			double ret = 0D;
			
			for (Shape item : _boxItems) {
				ret = Math.max(item.getBoundsInLocal().getWidth(), ret);
			}

			return ret + PADDING_X;
		}
		
		public double getLocalHeight() {
			double ret = PADDING_Y;
			
			for (Shape item : _boxItems) {
				ret += item.getBoundsInLocal().getHeight();
			}

			return ret;
		}
		
		private final double MARGIN_X = 20D;
		public final double MARGIN_Y = 30D;
		
		double getWidth() {
			double ret = 0D;
			
			for (TreeNode child : _children) {
				ret += child.getWidth();
			}
			
			return Math.max(getLocalWidth(), ret) + MARGIN_X;
		}
		
		double getHeight() {
			double ret = getLocalHeight();
			double childHeight = 0D;
			
			for (TreeNode child : _children) {
				childHeight = Math.max(child.getHeight(), childHeight);
			}
			
			ret += childHeight;
			
			return ret + MARGIN_Y;
		}
		
		private final Circle _circle = new Circle();
		private final Rectangle _rect = new Rectangle();

		protected abstract Pane getPane();

		void addChild(@Nonnull TreeNode child) {
			_children.add(child);
			
			Path line = new Path();
			
			_pathMap.put(child, line);
			getPane().getChildren().add(line);
		}
		
		private final Map<TreeNode, Path> _pathMap = new LinkedHashMap<>();
		
		void update() {
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
			_rect.setFill(_refNode.equals(_currentNodeP.get()) ? Color.RED : (_refNode.equals(_currentHoareNodeP.get()) ? Color.LIGHTBLUE : Color.TRANSPARENT));
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

				double minChildY = 0;

				for (TreeNode child : _children) {
					double endY = child.getY() - child.getLocalHeight()/2;

					minChildY = Math.min(minChildY, endY);
				}
				
				for (TreeNode child : _children) {
					childX += child.getWidth() / 2;
					double childY = _y + localHeight + 50D;
					
					child.setXY(childX, childY);
					
					Path line = _pathMap.get(child);

					line.getElements().clear();

					double startX = _x;
					double startY = _y + localHeight / 2;

					double endX = childX;
					double endY = child.getY() - child.getLocalHeight()/2;

					double midX = _x;
					double midY = (startY + endY) / 2;

					LineTo upperLine = new LineTo();

					upperLine.setX(midX);
					upperLine.setY(midY);

					LineTo midLine = new LineTo();

					midLine.setX(endX);
					midLine.setY(midY);

					LineTo lowerLine = new LineTo();

					lowerLine.setX(endX);
					lowerLine.setY(endY);

					line.getElements().add(new MoveTo(startX, startY));
					if (_children.size() > 1) {
						line.getElements().add(upperLine);
						line.getElements().add(midLine);
					}
					line.getElements().add(lowerLine);
					
					childX += child.getWidth() / 2;
				}
			}

			HoareCond preCond = _preCondMapP.get().get(_refNode);
			HoareCond postCond = _postCondMapP.get().get(_refNode);

			_preCondText.setFill(Color.BLUE);
			_preCondText.setText((preCond != null) ? "pre: " + preCond.getContentString() : null);
			_postCondText.setFill(Color.BLUE);
			_postCondText.setText((postCond != null) ? "post: " + postCond.getContentString() : null);
		}
		
		void setXY(double x, double y) {
			_x = x;
			_y = y;
			
			update();
		}

		protected abstract @Nonnull List<T> calcChildren(T node);
		protected abstract @Nonnull Map<T, TreeNode<T>> getNodeMap();
		protected abstract @Nonnull TreeNode<T> createInstance(T refNode);

		protected abstract String getText();
		protected abstract String getSubText();

		public TreeNode(@Nonnull T refNode) {
			_refNode = refNode;
			_x = 0D;
			_y = 0D;

			getPane().getChildren().add(_circle);
			getPane().getChildren().add(_rect);

			getNodeMap().put(_refNode, this);
			
			/*boolean stop = false;

			if (refNode.getSymbol() != null) {
				stop = !_syntax_includedNonTerminals.contains(refNode.getSymbol().getKey());
			}*/
			
			//String label = refNode.toStringVert();
			
			_text.setText(getText());
			
			_text.setTextAlignment(TextAlignment.CENTER);
			_text.setTextOrigin(VPos.CENTER);
			_text.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
			_text.setVisible(false);
			
			//_subText.setText(stop ? refNode.synthesize(false, true, null) : null);
			_subText.setText(getSubText());

			_subText.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
			_subText.setVisible(false);
			
			_boxItems.add(_text);
			_boxItems.add(_preCondText);
			_boxItems.add(_subText);
			_boxItems.add(_postCondText);
			
			for (Text item : _boxItems) {
				_box.getChildren().add(item);
			}
			
			getPane().getChildren().add(_box);

			for (T childRefNode : calcChildren(_refNode)) {
				addChild(createInstance(childRefNode));
			}

			update();
		}
	}

	private class SyntaxTreeNode extends TreeNode<SyntaxNode> {
		@Override
		protected @Nonnull Map<SyntaxNode, TreeNode<SyntaxNode>> getNodeMap() {
			return _syntax_nodeMap;
		}

		@Override
		protected @Nonnull TreeNode<SyntaxNode> createInstance(SyntaxNode refNode) {
			return new SyntaxTreeNode(refNode);
		}

		@Override
		protected String getText() {
			return _refNode.toString();
		}

		@Override
		protected String getSubText() {
			return _refNode.toStringVert();
		}

		@Override
		protected @Nonnull Pane getPane() {
			return _pane_syntax;
		}

		@Override
		protected @Nonnull List<SyntaxNode> calcChildren(@Nonnull SyntaxNode node) {
			List<SyntaxNode> ret = new ArrayList<>();

			if (node instanceof SyntaxNodeTerminal) return ret;

			for (SyntaxNode child : node.getChildren()) {
				List<SyntaxNode> sub = calcChildren(child);

				if (_syntax_includedNonTerminals.contains(child.getSymbol())) {
					if (!child.synthesize(false, true, null).isEmpty()) ret.add(child);
				} else {
					sub.removeIf(new Predicate<SyntaxNode>() {
						@Override
						public boolean test(SyntaxNode node) {
							return !_syntax_includedNonTerminals.contains(node.getSymbol()) || node.synthesize(false, true, null).isEmpty();
						}
					});

					ret.addAll(sub);
				}
			}

			return ret;
		}

		public SyntaxTreeNode(@Nonnull SyntaxNode refNode) {
			super(refNode);
		}
	}

	private class SemanticTreeNode extends TreeNode<SemanticNode> {
		@Override
		protected @Nonnull Map<SemanticNode, TreeNode<SemanticNode>> getNodeMap() {
			return _semantic_nodeMap;
		}

		@Override
		protected @Nonnull TreeNode<SemanticNode> createInstance(SemanticNode refNode) {
			return new SemanticTreeNode(refNode);
		}

		@Override
		protected String getText() {
			return _refNode.getTypeName();
		}

		@Override
		protected String getSubText() {
			return _refNode.getContentString();
			/*SyntaxNode syntaxNode = _refNode.getSyntax();

			return (syntaxNode != null) ? syntaxNode.toStringVert() : null;*/
		}

		@Override
		protected @Nonnull Pane getPane() {
			return _pane_semantic;
		}

		@Override
		protected @Nonnull List<SemanticNode> calcChildren(@Nonnull SemanticNode refNode) {
			List<SemanticNode> ret = new ArrayList<>();

			ret.addAll(refNode.getChildren());

			return ret;
		}

		public SemanticTreeNode(@Nonnull SemanticNode refNode) {
			super(refNode);
		}
	}

	private final Map<SyntaxNode, TreeNode<SyntaxNode>> _syntax_nodeMap = new LinkedHashMap<>();
	private final Map<SemanticNode, TreeNode<SemanticNode>> _semantic_nodeMap = new LinkedHashMap<>();
	
	private final Map<CheckBox, NonTerminal> _syntax_checkBoxNonTerminalMap = new HashMap<>();
	
	private SyntaxTreeNode _syntax_root;
	private SemanticTreeNode _semantic_root;
	
	private void updateScale() {
		//syntax
		_pane_syntax.backgroundProperty().set(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		_scrollPane_syntax.backgroundProperty().set(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		
		if (_syntax_root != null) _syntax_root.setXY(_syntax_root.getWidth() / 2, _syntax_root.getLocalHeight() / 2 + _syntax_root.MARGIN_Y/2);

		_scrollPane_syntax.setViewportBounds(_pane_syntax.getBoundsInLocal());

		//semantic
		_pane_semantic.backgroundProperty().set(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		_scrollPane_semantic.backgroundProperty().set(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

		if (_semantic_root != null) _semantic_root.setXY(_semantic_root.getWidth() / 2, _semantic_root.getLocalHeight() / 2 + _semantic_root.MARGIN_Y/2);

		_scrollPane_semantic.setViewportBounds(_pane_semantic.getBoundsInLocal());
	}
	
	private void updateTree() {
		//syntax
		_pane_syntax.getChildren().clear();
		_syntax_includedNonTerminals.clear();

		for (Map.Entry<CheckBox, NonTerminal> checkBoxEntry : _syntax_checkBoxNonTerminalMap.entrySet()) {
			CheckBox box = checkBoxEntry.getKey();
			NonTerminal nonTerminal = checkBoxEntry.getValue();
			
			if (box.selectedProperty().get()) {
				_syntax_includedNonTerminals.add(nonTerminal);
			}
		}

		if (_syntaxTreeP.get() != null) {
			_syntax_root = new SyntaxTreeNode(_syntaxTreeP.get());
			
			updateScale();
		}

		//semantic
		_pane_semantic.getChildren().clear();

		if (_semanticTreeP.get() != null) {
			_semantic_root = new SemanticTreeNode(_semanticTreeP.get());

			updateScale();
		}
	}
	
	private void updateAll() {
		_pane_syntax_checkBoxes.getChildren().clear();
		_syntax_checkBoxNonTerminalMap.clear();

		if (_syntaxTreeP.get() != null) {
			List<NonTerminal> activeNonTerminals = new ArrayList<>();
			
			activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_PROG);
			activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_PROG_);
			activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_HOARE_BLOCK);
			activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_ASSIGN);
			activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_ALT);
			activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_WHILE);
			activeNonTerminals.add(_hoareGrammar.NON_TERMINAL_SKIP);

			for (NonTerminal nonTerminal : HoareWhileGrammar.getInstance().getNonTerminals()) {
				CheckBox box = new CheckBox(nonTerminal.toString());

				box.setMnemonicParsing(false);

				_syntax_checkBoxNonTerminalMap.put(box, nonTerminal);
				
				if (activeNonTerminals.contains(nonTerminal)) box.setSelected(true);
				
				box.selectedProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) {
						updateTree();
					}
				});
				
				_pane_syntax_checkBoxes.getChildren().add(box);
			}
		}
		
		updateTree();
	}

	private TreeNode<SyntaxNode> getTreeSyntaxNode(SemanticNode semanticNode) {
		if (semanticNode == null) return null;

		if (!_syntax_nodeMap.containsKey(semanticNode)) return null;

		return _syntax_nodeMap.get(semanticNode);
	}

	private TreeNode<SemanticNode> getTreeSemanticNode(SemanticNode semanticNode) {
		if (semanticNode == null) return null;

		if (!_semantic_nodeMap.containsKey(semanticNode)) return null;

		return _semantic_nodeMap.get(semanticNode);
	}

	@Override
	public void initialize(URL url, ResourceBundle resources) {
		try {
			_pane_syntax.getTransforms().add(_scale);

			_syntaxTreeP.addListener(new ChangeListener<SyntaxNode>() {
				@Override
				public void changed(ObservableValue<? extends SyntaxNode> obs, SyntaxNode oldVal, SyntaxNode newVal) {
					updateAll();
				}
			});
			_semanticTreeP.addListener(new ChangeListener<SemanticNode>() {
				@Override
				public void changed(ObservableValue<? extends SemanticNode> obs, SemanticNode oldVal, SemanticNode newVal) {
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

			_currentNodeP.addListener(new ChangeListener<SemanticNode>() {
				@Override
				public void changed(ObservableValue<? extends SemanticNode> obs, SemanticNode oldVal, SemanticNode newVal) {
					TreeNode<SyntaxNode> oldSyntaxNode = getTreeSyntaxNode(oldVal);
					TreeNode<SyntaxNode> newSyntaxNode = getTreeSyntaxNode(newVal);

					if (oldSyntaxNode != null) oldSyntaxNode.update();
					if (newSyntaxNode != null) newSyntaxNode.update();

					TreeNode<SemanticNode> oldSemanticNode = getTreeSemanticNode(oldVal);
					TreeNode<SemanticNode> newSemanticNode = getTreeSemanticNode(newVal);

					if (oldSemanticNode != null) oldSemanticNode.update();
					if (newSemanticNode != null) newSemanticNode.update();
				}
			});
			_currentHoareNodeP.addListener(new ChangeListener<SemanticNode>() {
				@Override
				public void changed(ObservableValue<? extends SemanticNode> obs, SemanticNode oldVal, SemanticNode newVal) {
					TreeNode<SyntaxNode> oldSyntaxNode = getTreeSyntaxNode(oldVal);
					TreeNode<SyntaxNode> newSyntaxNode = getTreeSyntaxNode(newVal);

					if (oldSyntaxNode != null) oldSyntaxNode.update();
					if (newSyntaxNode != null) newSyntaxNode.update();

					TreeNode<SemanticNode> oldSemanticNode = getTreeSemanticNode(oldVal);
					TreeNode<SemanticNode> newSemanticNode = getTreeSemanticNode(newVal);

					if (oldSemanticNode != null) oldSemanticNode.update();
					if (newSemanticNode != null) newSemanticNode.update();
				}
			});
		} catch (Exception e) {
			ErrorUtil.logEFX(e);
		}
	}
}