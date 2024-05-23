/*
 * @(#)SimpleTreeModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.tree;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
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
    public N getChild(N parent, int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getChildCount(N node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<N> getChildren(N node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CopyOnWriteArrayList<InvalidationListener> getInvalidationListeners() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CopyOnWriteArrayList<Listener<TreeModelEvent<N>>> getTreeModelListeners() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void insertChildAt(N child, N parent, int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeFromParent(N child) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectProperty<N> rootProperty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
