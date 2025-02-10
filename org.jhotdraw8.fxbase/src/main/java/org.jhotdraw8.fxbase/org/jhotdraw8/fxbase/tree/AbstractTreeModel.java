/*
 * @(#)AbstractTreeModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.tree;

import javafx.beans.InvalidationListener;
import org.jhotdraw8.base.event.Listener;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AbstractTreeModel.
 *
 * @param <N> the node type
 */
public abstract class AbstractTreeModel<N> implements TreeModel<N> {

    private final CopyOnWriteArrayList<Listener<TreeModelEvent<N>>> treeModelListeners = new CopyOnWriteArrayList<>();

    private final CopyOnWriteArrayList<InvalidationListener> invalidationListeners = new CopyOnWriteArrayList<>();

    public AbstractTreeModel() {
    }

    @Override
    public final CopyOnWriteArrayList<Listener<TreeModelEvent<N>>> getTreeModelListeners() {
        return treeModelListeners;
    }

    @Override
    public final CopyOnWriteArrayList<InvalidationListener> getInvalidationListeners() {
        return invalidationListeners;
    }
}
