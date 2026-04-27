/*
 * @(#)AbstractTreePresentationModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.tree;

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
public abstract class AbstractTreePresentationModel<N> implements TreePresentationModel<N> {
    /// Holds the underlying model.
    private final NonNullObjectProperty<TreeModel<N>> treeModel //
            = new NonNullObjectProperty<>(this, MODEL_PROPERTY, new SimpleTreeModel<>()) {
        private @Nullable TreeModel<N> oldValue = null;

        @Override
        protected void fireValueChangedEvent() {
            TreeModel<N> newValue = get();
            super.fireValueChangedEvent();
            onTreeModelChanged(oldValue, newValue);
            oldValue = newValue;
        }
    };

    public AbstractTreePresentationModel() {
    }

    @Override
    public NonNullObjectProperty<TreeModel<N>> treeModelProperty() {
        return treeModel;
    }


    protected abstract void onTreeModelChanged(TreeModel<N> oldValue, TreeModel<N> newValue);
}
