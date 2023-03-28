/*
 * @(#)DrawingModel.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.model;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxbase.tree.TreeModel;
import org.jhotdraw8.fxbase.tree.TreeModelEvent;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@code DrawingModel} provides {@code DrawingModelEvent}s about a
 * {@code Drawing}.
 * <p>
 * {@code DrawingModel} is used by {@code DrawingView} to get change events from
 * a drawing without having to register listeners on all figures.</p>
 * <p>
 * The {@code DrawingModelEvent}s that a {@code DrawingModel} fires are based on
 * assumptions that it makes about the figures contained in the drawing. If the
 * assumptions are wrong, then the drawing view will not properly update its
 * view!</p>
 * <p>
 * {@code DrawingModel} invokes {@code addNotify()} and {@code removeNotify()}
 * methods on a {@code Figure} when it detects that the figure has been added or
 * removed from a {@code Drawing}.
 * </p>
 * <p>
 * A {@code DrawingView} will only be updated properly, if all {@code Tool}s,
 * {@code Handle}s and inspectors update the drawing using the
 * {@code DrawingModel}.
 * </p>
 *
 * @author Werner Randelshofer
 */
public interface DrawingModel extends Observable, TreeModel<Figure> {

    /**
     * Name of the drawing property.
     */
    String DRAWING_PROPERTY = "drawing";

    /**
     * List of drawing model listeners.
     *
     * @return a list of drawing model listeners
     */
    @NonNull CopyOnWriteArrayList<Listener<DrawingModelEvent>> getDrawingModelListeners();

    /**
     * List of invalidation listeners.
     *
     * @return a list of drawing model listeners
     */
    @Override
    @NonNull CopyOnWriteArrayList<InvalidationListener> getInvalidationListeners();

    /**
     * The root of the drawing model.
     *
     * @return the root
     */
    @NonNull ObjectProperty<Drawing> drawingProperty();

    /**
     * Adds a listener for {@code DrawingModelEvent}s.
     *
     * @param l the listener
     */
    default void addDrawingModelListener(Listener<DrawingModelEvent> l) {
        getDrawingModelListeners().add(l);
    }

    /**
     * Removes a listener for {@code DrawingModelEvent}s.
     *
     * @param l the listener
     */
    default void removeDrawingModelListener(Listener<DrawingModelEvent> l) {
        getDrawingModelListeners().remove(l);
    }

    @Override
    default void addListener(InvalidationListener l) {
        getInvalidationListeners().add(l);
    }

    @Override
    default void removeListener(InvalidationListener l) {
        getInvalidationListeners().remove(l);
    }

    /**
     * Gets the root of the tree.
     *
     * @return the drawing
     */
    default Drawing getDrawing() {
        return drawingProperty().get();
    }

    /**
     * Sets the root of the tree and fires appropriate
     * {@code DrawingModelEvent}s.
     *
     * @param root the new root
     */
    default void setDrawing(Drawing root) {
        drawingProperty().set(root);
    }

    /**
     * Gets the getChildren of the specified figure.
     *
     * @param figure the figure.
     * @return the getChildren.
     */
    @Override
    default List<Figure> getChildren(@NonNull Figure figure) {
        return figure.getChildren();
    }

    /**
     * Gets the child count of the specified figure.
     *
     * @param figure the parent.
     * @return the number of getChildren
     */
    @Override
    default int getChildCount(@NonNull Figure figure) {
        return getChildren(figure).size();
    }

    /**
     * Gets the child at the given index from the parent.
     *
     * @param parent the parent.
     * @param index  the index.
     * @return the child
     */
    @Override
    default Figure getChild(@NonNull Figure parent, int index) {
        return getChildren(parent).get(index);
    }

    /**
     * Removes the specified child from its parent and fires appropriate
     * {@code DrawingModelEvent}s.
     *
     * @param child the figure
     */
    @Override
    void removeFromParent(Figure child);

    /**
     * Removes the specified child from its parent and fires appropriate
     * {@code DrawingModelEvent}s.
     *
     * @param parent the parent figure
     * @param index  the index
     */
    Figure removeFromParent(Figure parent, int index);

    /**
     * Adds the specified child to a parent and fires appropriate
     * {@code DrawingModelEvent}s.
     *
     * @param child  the new child
     * @param parent the parent.
     * @param index  the index
     */
    @Override
    void insertChildAt(Figure child, Figure parent, int index);

    /**
     * Adds the specified child to a parent and fires appropriate
     * {@code DrawingModelEvent}s.
     *
     * @param child  the new child
     * @param parent the parent.
     */
    @Override
    default void addChildTo(Figure child, @NonNull Figure parent) {
        insertChildAt(child, parent, getChildCount(parent));
    }

    /**
     * Sets the specified property on the figure and fires appropriate
     * {@code DrawingModelEvent}s.
     *
     * @param <T>      the value type
     * @param figure   the figure
     * @param key      the key
     * @param newValue the new value
     * @return the old value
     */
    @Nullable <T> T set(@NonNull Figure figure, MapAccessor<T> key, @Nullable T newValue);

    @Nullable <T> T remove(@NonNull Figure figure, MapAccessor<T> key);

    /**
     * Sets the specified property on the figure and fires appropriate
     * {@code DrawingModelEvent}s.
     *
     * @param <T>      the value type
     * @param figure   the figure
     * @param key      the key
     * @param newValue the new value
     * @return the old value
     */
    @NonNull <T> T setNonNull(@NonNull Figure figure, @NonNull NonNullMapAccessor<T> key, @NonNull T newValue);

    /**
     * Gets the specified property from the figure.
     *
     * @param <T>    the value type
     * @param figure the figure
     * @param key    the key
     * @return the value
     */
    default @Nullable <T> T get(@NonNull Figure figure, @NonNull MapAccessor<T> key) {
        return figure.get(key);
    }

    /**
     * Gets the specified property from the figure.
     *
     * @param <T>    the value type
     * @param figure the figure
     * @param key    the key
     * @return the value
     */
    default @NonNull <T> T getNonNull(@NonNull Figure figure, @NonNull NonNullMapAccessor<T> key) {
        return figure.getNonNull(key);
    }

    /**
     * Attempts to change the local bounds of the figure and fires appropriate
     * {@code DrawingModelEvent}s.
     *
     * @param f         the figure
     * @param transform the desired transformation in the local coordinate
     *                  system
     */
    void reshapeInLocal(Figure f, Transform transform);

    /**
     * Attempts to change the parent bounds of the figure and fires appropriate
     * {@code DrawingModelEvent}s.
     *
     * @param f         the figure
     * @param transform the desired transformation in the parent coordinate
     *                  system
     */
    void reshapeInParent(Figure f, Transform transform);

    void translateInParent(Figure f, CssPoint2D tx);

    /**
     * Attempts to change the local bounds of the figure and fires appropriate
     * {@code DrawingModelEvent}s.
     *
     * @param f      the figure
     * @param x      desired x-position in the local coordinate system
     * @param y      desired y-position in the local coordinate system
     * @param width  desired width in the local coordinate system, may be
     *               negative
     * @param height desired height in the local coordinat system, may be
     *               negative
     */
    void reshapeInLocal(Figure f, double x, double y, double width, double height);

    void reshapeInLocal(Figure f, CssSize x, CssSize y, CssSize width, CssSize height);

    /**
     * Invokes the layout method of the figure and fires appropriate /
     * {@code DrawingModelEvent}s.
     *
     * @param f   the figure
     * @param ctx the render context (optional)
     */
    void layout(Figure f, RenderContext ctx);

    /**
     * Invokes the disconnect method of the figure and fires appropriate
     * {@code DrawingModelEvent}s.
     *
     * @param f the figure
     */
    void disconnect(Figure f);

    /**
     * Invokes the updateCss method of the figure and fires appropriate
     * {@code DrawingModelEvent}s.
     *
     * @param f the figure
     */
    void updateCss(Figure f);

    /**
     * Fires the specified event.
     *
     * @param event the event
     */
    default void fireDrawingModelEvent(DrawingModelEvent event) {
        for (Listener<DrawingModelEvent> l : getDrawingModelListeners()) {
            l.handle(event);
        }
    }

    /**
     * Validates the model. This method is invoked by {@code DrawingView} each
     * time before it renders the model.
     *
     * @param ctx the render context (optional)
     */
    void validate(RenderContext ctx);

    // ---
    // convenience methods
    // ---

    /**
     * Fires "node invalidated" event for the specified figure.
     *
     * @param f the figure
     */
    @Override
    default void fireNodeInvalidated(Figure f) {
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    /**
     * Fires "node invalidated" event for the specified figure.
     *
     * @param <T>        the value type
     * @param f          the figure
     * @param key        the property key
     * @param oldValue   the old value
     * @param newValue   the new value
     * @param wasAdded
     * @param wasRemoved
     */
    default <T> void firePropertyValueChanged(Figure f, Key<T> key, @Nullable T oldValue, @Nullable T newValue, boolean wasAdded, boolean wasRemoved) {
        fireDrawingModelEvent(DrawingModelEvent.propertyValueChanged(this, f, key, oldValue, newValue, wasAdded, wasRemoved));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    /**
     * Fires "node invalidated" event for the specified figure.
     *
     * @param f the figure
     */
    default void fireTransformInvalidated(Figure f) {
        fireDrawingModelEvent(DrawingModelEvent.transformChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    /**
     * Fires "node invalidated" event for the specified figure.
     *
     * @param f the figure
     */
    default void fireLayoutInvalidated(Figure f) {
        fireDrawingModelEvent(DrawingModelEvent.layoutChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    /**
     * Fires "style invalidated" event for the specified figure.
     *
     * @param f the figure
     */
    default void fireStyleInvalidated(Figure f) {
        fireDrawingModelEvent(DrawingModelEvent.styleInvalidated(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    /**
     * Fires an "invalidated" event.
     */
    default void fireDrawingModelInvalidated() {
        for (InvalidationListener l : new ArrayList<>(getInvalidationListeners())) {
            l.invalidated(this);
        }
    }

    /**
     * Invokes "transformInParent" on the specified figure.
     *
     * @param figure    a figure
     * @param transform the desired transformation
     */
    void transformInParent(Figure figure, Transform transform);

    /**
     * Invokes "transformInLocal" on the specified figure.
     *
     * @param figure    a figure
     * @param transform the desired transformation
     */
    void transformInLocal(Figure figure, Transform transform);

    /**
     * Removes the specified key from the figure.
     *
     * @param <T>    the value type
     * @param f      a figure
     * @param remove a key
     * @return the old value
     */
    @Nullable <T> T remove(Figure f, Key<T> remove);

    /**
     * Creates a property for the specified figure and property key.
     *
     * @param f   a figure
     * @param key a property key
     * @param <T> the property type
     * @return a property
     */

    @NonNull <T> Property<T> propertyAt(Figure f, Key<T> key);
}
