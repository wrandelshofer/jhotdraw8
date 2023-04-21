/*
 * @(#)SimpleTreeNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.tree;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.List;

public class SimpleTreeNode<V> implements TreeNode<SimpleTreeNode<V>> {
    private ChildList<SimpleTreeNode<V>> children;
    private @Nullable SimpleTreeNode<V> parent;
    private V value;

    public SimpleTreeNode() {
    }

    public SimpleTreeNode(V value) {
        this.value = value;
    }

    public void addChild(@NonNull SimpleTreeNode<V> child) {
        if (child.parent != null) {
            child.parent.removeChild(child);
        }
        getChildren().add(child);
        child.parent = this;
    }

    public void removeChild(@NonNull SimpleTreeNode<V> child) {
        if (children != null) {
            children.remove(child);
            child.parent = null;
        }
    }

    @Override
    public @NonNull List<SimpleTreeNode<V>> getChildren() {
        if (children == null) {
            children = new ChildList<>(this);
        }
        return children;
    }

    @Override
    public @Nullable SimpleTreeNode<V> getParent() {
        return null;
    }

    @Override
    public void setParent(@Nullable SimpleTreeNode<V> newValue) {
        this.parent = newValue;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
