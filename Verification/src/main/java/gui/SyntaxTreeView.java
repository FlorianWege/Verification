package gui;

import java.util.ArrayList;
import java.util.List;

import core.SyntaxNodeTerminal;
import core.SyntaxTree;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.util.Callback;

class SyntaxTreeView {
	private TreeView<core.SyntaxNode> _treeView;
	private CheckBox _checkBox_filterEps;
	
	private ObjectProperty<SyntaxTree> _syntaxTree;
	
	private class SyntaxNode extends TreeItem<core.SyntaxNode> {
		private core.SyntaxNode _node;
		
		@Override
		public String toString() {
			return _node.toString();
		}
		
		public core.SyntaxNode getNode() {
			return _node;
		}
		
		private int _reqChildren = 0;
		
		int getReqChildren() {
			return _reqChildren;
		}
		
		private boolean updateReqChildren() {
			if (_node instanceof SyntaxNodeTerminal) {
				if (((SyntaxNodeTerminal) _node).getToken() == null) {
					_reqChildren = 0;
					
					return false;
				}
				
				_reqChildren = 1;
				
				return true;
			}
			
			int reqChildren = 0;

			List<TreeItem<core.SyntaxNode>> children = new ArrayList<>(getChildren());

			for (TreeItem<core.SyntaxNode> child : children) {
				SyntaxNode childItem = ((SyntaxNode) child);
				
				if (childItem.updateReqChildren()) {
					reqChildren++;
				}
			}

			_reqChildren = reqChildren;

			return (reqChildren > 0);
		}
		
		void addChild(SyntaxNode child) {
			getChildren().add(child);
			
			updateReqChildren();
		}
		
		public SyntaxNode(core.SyntaxNode node) {
			_node = node;
			
			setValue(_node);
			
			/*getChildren().addListener(new ListChangeListener<TreeItem<SyntaxNode>>() {
				@Override
				public void onChanged(javafx.collections.ListChangeListener.Change<? extends TreeItem<SyntaxNode>> arg0) {
					if (arg0.wasAdded()) {
						updateReqChildren();
					}
				}
			});*/
			
			//updateReqChildren();
		}
	}
	
	private class SyntaxTreeNodeCell extends TreeCell<core.SyntaxNode> {
		@Override
		public void cancelEdit() {
			super.cancelEdit();
		}

		@Override
		public void commitEdit(core.SyntaxNode arg0) {
			super.commitEdit(arg0);
		}

		@Override
		public void startEdit() {
			super.startEdit();
		}
		
		@Override
		public void updateItem(core.SyntaxNode item, boolean empty) {
			super.updateItem(item, empty);
			
			if (empty) {
				setText(null);
				setGraphic(null);
				backgroundProperty().set(Background.EMPTY);
			} else {
				if (isEditing()) {
					setText(null);
					setGraphic(null);
					backgroundProperty().set(Background.EMPTY);
				} else {
					setText(getItem().toString());
					setGraphic(getTreeItem().getGraphic());

					if (((SyntaxTreeView.SyntaxNode) getTreeItem())._reqChildren > 0) {
						backgroundProperty().set(new Background(new BackgroundFill(Color.ALICEBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
					} else {
						backgroundProperty().set(new Background(new BackgroundFill(Color.BISQUE, CornerRadii.EMPTY, Insets.EMPTY)));
					}
				}
			}
		}
	}

	private SyntaxNode addNode(core.SyntaxNode node) {
		SyntaxNode nodeItem = new SyntaxNode(node);
		
		nodeItem.setExpanded(true);
		
		for (core.SyntaxNode child : node.getChildren()) {
			nodeItem.addChild(addNode(child));
		}
		
		return nodeItem;
	}
	
	private void filterNode(SyntaxNode nodeItem) {
		List<TreeItem<core.SyntaxNode>> children = new ArrayList<>(nodeItem.getChildren());

		for (TreeItem<core.SyntaxNode> child : children) {
			SyntaxNode childItem = ((SyntaxNode) child);
			
			if (childItem.getReqChildren() == 0) {
				nodeItem.getChildren().remove(childItem);
			} else {
				filterNode(childItem);
			}
		}
	}
	
	private void update() {
		_treeView.setRoot(null);
		
		if (_syntaxTree.get() == null) return;
		
		core.SyntaxNode root = _syntaxTree.get().getRoot();
		
		SyntaxNode rootItem = addNode(root);

		if (!_checkBox_filterEps.isSelected()) {
			filterNode(rootItem);
		}
		
		_treeView.setRoot(rootItem);
	}
	
	SyntaxTreeView(TreeView<core.SyntaxNode> treeView, CheckBox checkBox_filterEps, ObjectProperty<SyntaxTree> syntaxTree) {
		_treeView = treeView;
		_checkBox_filterEps = checkBox_filterEps;
		_syntaxTree = syntaxTree;
		
		_treeView.setCellFactory(new Callback<TreeView<core.SyntaxNode>, TreeCell<core.SyntaxNode>>() {
			@Override
			public TreeCell<core.SyntaxNode> call(TreeView<core.SyntaxNode> arg0) {
				return new SyntaxTreeNodeCell();
			}
		});
		_checkBox_filterEps.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) {
				update();
			}
		});
		
		_syntaxTree.addListener(new ChangeListener<SyntaxTree>() {
			@Override
			public void changed(ObservableValue<? extends SyntaxTree> obs, SyntaxTree oldVal, SyntaxTree newVal) {
				update();
			}
		});
	}
}