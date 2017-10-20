package gui;

import core.structures.TNode;
import core.structures.semantics.SemanticNode;
import core.structures.syntax.SyntaxNode;
import core.structures.syntax.SyntaxNodeTerminal;
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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SemanticTreeView extends NodeTreeView<SemanticNode> {
	public SemanticTreeView(@Nonnull TreeView<SemanticNode> treeView, @Nonnull ObjectProperty<SemanticNode> semanticTree) {
		super(treeView, semanticTree);
	}

	@Override
	protected boolean filter(@Nonnull TreeItem<SemanticNode> nodeItem) {
		return false;
	}

	private class TreeNode extends NodeTreeView.TreeNode {
		public TreeNode(@Nonnull TNode refNode) {
			super(refNode);
		}

		@Override
		protected Background getBackground() {
			return new Background(new BackgroundFill(Color.BISQUE, CornerRadii.EMPTY, Insets.EMPTY));
		}
	}

	@Override
	protected TreeNode createNode(@Nonnull SemanticNode refNode) {
		return new TreeNode(refNode);
	}
}