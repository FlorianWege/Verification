package gui;

import core.structures.TNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public abstract class NodeTreeView<T extends TNode<T>> {
	protected final TreeView<T> _treeView;
	private final ObjectProperty<T> _nodeTreeP;

	public NodeTreeView(@Nonnull TreeView<T> treeView, @Nonnull ObjectProperty<T> nodeTree) {
		_treeView = treeView;
		_nodeTreeP = nodeTree;

		_treeView.setCellFactory(new Callback<TreeView<T>, TreeCell<T>>() {
			@Override
			public TreeCell<T> call(TreeView<T> val) {
				return new TreeNodeCell();
			}
		});

		_nodeTreeP.addListener(new ChangeListener<T>() {
			@Override
			public void changed(ObservableValue<? extends T> obs, T oldVal, T newVal) {
				update();
			}
		});
	}

	protected abstract class TreeNode extends TreeItem<T> {
		protected T _refNode;

		protected abstract Background getBackground();

		public void addChild(TreeNode child) {
			getChildren().add(child);
		}
		
		public TreeNode(@Nonnull T refNode) {
			_refNode = refNode;
			
			setValue(_refNode);
		}
	}
	
	private class TreeNodeCell extends TreeCell<T> {
		@Override
		public void cancelEdit() {
			super.cancelEdit();
		}

		@Override
		public void commitEdit(T item) {
			super.commitEdit(item);
		}

		@Override
		public void startEdit() {
			super.startEdit();
		}

		@Override
		public void updateItem(T item, boolean empty) {
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
					setText(getItem().getTreeText());
					setGraphic(getTreeItem().getGraphic());

					new Background(new BackgroundFill(Color.BISQUE, CornerRadii.EMPTY, Insets.EMPTY))
					;
					backgroundProperty().set(((TreeNode) getTreeItem()).getBackground());
				}
			}
		}
	}

	private TreeNode addNode(@Nonnull T treeNode) {
		TreeNode nodeItem = createNode(treeNode);
		
		nodeItem.setExpanded(true);
		
		for (T child : treeNode.getChildren()) {
			nodeItem.addChild(addNode(child));
		}
		
		return nodeItem;
	}

	protected abstract boolean filter(@Nonnull TreeItem<T> nodeItem);
	protected abstract TreeNode createNode(@Nonnull T refNode);

	protected void filterNode(@Nonnull TreeNode nodeItem) {
		nodeItem.getChildren().removeIf(new Predicate<TreeItem<T>>() {
			@Override
			public boolean test(TreeItem<T> child) {
				return filter(child);
			}
		});

		for (TreeItem<T> child : nodeItem.getChildren()) {
			filterNode((TreeNode) child);
		}
	}
	
	protected void update() {
		_treeView.setRoot(null);
		
		if (_nodeTreeP.get() == null) return;
		
		TreeNode rootItem = addNode(_nodeTreeP.get());

		filterNode(rootItem);
		
		_treeView.setRoot(rootItem);
	}
}