package gui;

import java.util.ArrayList;
import java.util.List;

import core.structures.TNode;
import core.structures.syntax.SyntaxNode;
import core.structures.syntax.SyntaxNodeTerminal;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import javax.annotation.Nonnull;

public class SyntaxTreeView extends NodeTreeView<SyntaxNode> {
	private final CheckBox _checkBox_filterEps;

	public SyntaxTreeView(@Nonnull TreeView<SyntaxNode> treeView, @Nonnull ObjectProperty<SyntaxNode> syntaxTree, @Nonnull CheckBox checkBox_filterEps) {
		super(treeView, syntaxTree);

		_checkBox_filterEps = checkBox_filterEps;

		_checkBox_filterEps.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) {
				update();
			}
		});
	}

	@Override
	protected boolean filter(@Nonnull TreeItem<SyntaxNode> nodeItem) {
		if (_checkBox_filterEps.isSelected()) return false;

		return ((TreeNode) nodeItem).getReqChildren() <= 0;
	}

	@Override
	protected NodeTreeView.TreeNode createNode(@Nonnull SyntaxNode node) {
		return new TreeNode(node);
	}

	private class TreeNode extends NodeTreeView.TreeNode {
		private int _reqChildren = 0;
		
		public int getReqChildren() {
			return _reqChildren;
		}
		
		private boolean updateReqChildren() {
			if (_refNode instanceof SyntaxNodeTerminal) {
				if (((SyntaxNodeTerminal) _refNode).getToken() == null) {
					_reqChildren = 0;
					
					return false;
				}
				
				_reqChildren = 1;
				
				return true;
			}
			
			int reqChildren = 0;

			List<TreeItem<SyntaxNode>> children = new ArrayList<>(getChildren());

			for (TreeItem<SyntaxNode> child : children) {
				TreeNode childItem = ((TreeNode) child);
				
				if (childItem.updateReqChildren()) reqChildren++;
			}

			_reqChildren = reqChildren;

			return (reqChildren > 0);
		}

		@Override
		protected Background getBackground() {
			if (_reqChildren > 0) {
				return new Background(new BackgroundFill(Color.ALICEBLUE, CornerRadii.EMPTY, Insets.EMPTY));
			} else {
				return new Background(new BackgroundFill(Color.BISQUE, CornerRadii.EMPTY, Insets.EMPTY));
			}
		}

		@Override
		public void addChild(@Nonnull NodeTreeView.TreeNode child) {
			super.addChild(child);

			updateReqChildren();
		}

		public TreeNode(@Nonnull SyntaxNode refNode) {
			super(refNode);
		}
	}
}