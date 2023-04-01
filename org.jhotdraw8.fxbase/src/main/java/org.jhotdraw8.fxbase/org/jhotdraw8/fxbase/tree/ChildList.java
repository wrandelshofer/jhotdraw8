/*
 * @(#)ChildList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.tree;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.fxcollection.indexedset.AbstractIndexedArrayObservableSet;

/**
 * A child list for implementations of the {@link TreeNode} interface.
 * <p>
 * This list maintains the parent of tree nodes that are added/removed
 * from the child list, as described in {@link TreeNode#getChildren()}.
 *
 * @param <E> the node type
 */
public class ChildList<E extends TreeNode<E>> extends AbstractIndexedArrayObservableSet<E> {

    private final E parent;

    public ChildList(E parent) {
        this.parent = parent;

    }

    @Override
    protected Boolean onContains(E e) {
        return e.getParent() == parent;
    }

    @Override
    protected void onAdded(@NonNull E e) {
        E oldParent = e.getParent();
        if (oldParent != null && oldParent != parent) {
            oldParent.getChildren().remove(e);
        }
        e.setParent(parent);
    }

    @Override
    protected void onRemoved(@NonNull E e) {
        e.setParent(null);
    }


    @Override
    protected boolean mayBeAdded(@NonNull E element) {
        return parent.isSuitableChild(element) &&
                element.isSuitableParent(parent);
    }
}
