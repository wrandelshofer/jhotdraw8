/*
 * @(#)SimpleTreeModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.tree;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.event.Listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SimpleTreeModel.
 *
 * @param <N> the node type
 * @author Werner Randelshofer
 */
public class SimpleTreeModel<N> implements TreeModel<N> {
    public SimpleTreeModel() {
    }

    @Override
    public @NonNull N getChild(@NonNull N parent, int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getChildCount(@NonNull N node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public @NonNull List<N> getChildren(@NonNull N node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public @NonNull CopyOnWriteArrayList<InvalidationListener> getInvalidationListeners() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public @NonNull CopyOnWriteArrayList<Listener<TreeModelEvent<N>>> getTreeModelListeners() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void insertChildAt(@NonNull N child, @NonNull N parent, int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeFromParent(@NonNull N child) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public @NonNull ObjectProperty<N> rootProperty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
