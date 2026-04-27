/*
 * @(#)TreePresentationModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.tree;

import javafx.scene.control.TreeItem;
import org.jhotdraw8.fxbase.beans.NonNullObjectProperty;
import org.jspecify.annotations.Nullable;

/// The `TreePresentationModel` can be used to present a `TreeModel`
/// in a `TreeView` or a `TreeTableView`.
///
/// Maps `TreeModel` to a `TreeItem&lt;E&gt;` hierarchy.
///
/// Note: for performance reasons we do not expand the tree nodes by default.
///
/// @param <N> the node type
public interface TreePresentationModel<N> {

    /// The name of the model property.
    String MODEL_PROPERTY = "model";

    default TreeModel<N> getTreeModel() {
        return treeModelProperty().get();
    }

    default void setTreeModel(TreeModel<N> newValue) {
        treeModelProperty().set(newValue);
    }

    NonNullObjectProperty<TreeModel<N>> treeModelProperty();

    TreeItem<N> getRoot();

    boolean isUpdating();

    /// Returns the tree item associated to the specified node.
    ///
    /// @param value the node value
    /// @return a TreeItem. Returns null if no tree item has been associated to
    /// the node because the tree is not expanded yet.
    @Nullable
    TreeItem<N> getTreeItem(N value);

}
