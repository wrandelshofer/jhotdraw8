/*
 * @(#)TreeModelUndoAdapter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.tree;

import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.fxbase.beans.NonNullObjectProperty;
import org.jhotdraw8.fxbase.text.ResourceBundleStub;
import org.jhotdraw8.fxbase.undo.FXUndoManager;
import org.jspecify.annotations.Nullable;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This adapter can be bound to a {@link javafx.scene.control.TextInputControl}
 * to support undo/redo with a {@link FXUndoManager}.
 * <p>
 * This text filter can be added to multiple tree models.
 * If you do this, make sure that you add the {@link FXUndoManager}
 * only once as a listener.
 *
 * @param <E> the element type of the tree
 */
public class TreeModelUndoAdapter<E> {
    public static final String RESOURCE_BUNDLE_PROPERTY = "resourceBundle";
    private final CopyOnWriteArrayList<UndoableEditListener> listeners = new CopyOnWriteArrayList<>();
    private final Listener<TreeModelEvent<E>> treeModelListener = event -> {
        UndoableEdit edit = switch (event.getEventType()) {
            case ROOT_CHANGED -> new RootChangedEdit<>(event.getSource(), event.getOldRoot(), event.getNewRoot());
            case SUBTREE_NODES_CHANGED,
                    NODE_ADDED_TO_TREE,
                    NODE_REMOVED_FROM_TREE,
                    NODE_CHANGED -> null;
            case NODE_ADDED_TO_PARENT ->
                    new NodeAddedEdit<>(event.getSource(), event.getParent(), event.getChildIndex(), event.getChild());
            case NODE_REMOVED_FROM_PARENT ->
                    new NodeRemovedEdit<>(event.getSource(), event.getParent(), event.getChildIndex(), event.getChild());

        };
        if (edit != null) {
            fireUndoableEdit(event.getSource(), edit);
        }
    };
    @SuppressWarnings("this-escape")
    final private NonNullObjectProperty<ResourceBundle> resourceBundle = new NonNullObjectProperty<>(this, RESOURCE_BUNDLE_PROPERTY, new ResourceBundleStub());

    public TreeModelUndoAdapter() {
    }

    @SuppressWarnings("this-escape")
    public TreeModelUndoAdapter(TreeModel<E> model) {
        bind(model);
    }

    public void addUndoEditListener(UndoableEditListener listener) {
        listeners.add(listener);
    }

    public void bind(TreeModel<E> model) {
        unbind(model);
        model.addTreeModelListener(treeModelListener);
    }

    protected void fireUndoableEdit(Object source, UndoableEdit edit) {
        final UndoableEditEvent editEvent = new UndoableEditEvent(source, edit);
        listeners.forEach(e -> e.undoableEditHappened(editEvent));
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle.get();
    }

    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle.set(resourceBundle);
    }

    public void removeUndoEditListener(UndoableEditListener listener) {
        listeners.remove(listener);
    }

    public NonNullObjectProperty<ResourceBundle> resourceBundleProperty() {
        return resourceBundle;
    }

    public void unbind(TreeModel<E> model) {
        model.removeTreeModelListener(treeModelListener);
    }

    @SuppressWarnings({"serial", "RedundantSuppression"})
    class RootChangedEdit<EE> extends AbstractUndoableEdit {
        private final TreeModel<EE> model;
        private final @Nullable EE oldRoot;
        private final @Nullable EE newRoot;

        public RootChangedEdit(TreeModel<EE> model, @Nullable EE oldRoot, @Nullable EE newRoot) {
            this.model = model;
            this.oldRoot = oldRoot;
            this.newRoot = newRoot;
        }

        @Override
        public String getPresentationName() {
            return getResourceBundle().getString("edit.addElement");
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            model.setRoot(newRoot);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            model.setRoot(oldRoot);
        }
    }

    @SuppressWarnings({"serial", "RedundantSuppression"})
    class NodeAddedEdit<EE> extends AbstractUndoableEdit {
        private final TreeModel<EE> model;
        private final EE parent;
        private final int childIndex;
        private final EE child;

        public NodeAddedEdit(TreeModel<EE> model, EE parent, int childIndex, EE child) {
            this.model = model;
            this.parent = parent;
            this.childIndex = childIndex;
            this.child = child;
        }

        @Override
        public String getPresentationName() {
            return getResourceBundle().getString("edit.addElement");
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            model.insertChildAt(child, parent, childIndex);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            model.removeFromParent(child);
        }
    }

    @SuppressWarnings({"serial", "RedundantSuppression"})
    class NodeRemovedEdit<EE> extends AbstractUndoableEdit {
        private final TreeModel<EE> model;
        private final EE parent;
        private final int childIndex;
        private final EE child;

        public NodeRemovedEdit(TreeModel<EE> model, EE parent, int childIndex, EE child) {
            this.model = model;
            this.parent = parent;
            this.childIndex = childIndex;
            this.child = child;
        }

        @Override
        public String getPresentationName() {
            return getResourceBundle().getString("edit.removeElement");
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            model.removeFromParent(child);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            model.insertChildAt(child, parent, childIndex);
        }
    }
}
