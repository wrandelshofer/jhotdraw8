/*
 * @(#)Figure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.handle.AnchorOutlineHandle;
import org.jhotdraw8.draw.handle.BoundsInLocalOutlineHandle;
import org.jhotdraw8.draw.handle.BoundsInTranslationOutlineHandle;
import org.jhotdraw8.draw.handle.Handle;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.handle.MoveHandle;
import org.jhotdraw8.draw.handle.ResizeHandleKit;
import org.jhotdraw8.draw.handle.RotateHandle;
import org.jhotdraw8.draw.handle.TransformHandleKit;
import org.jhotdraw8.draw.locator.BoundsLocator;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxbase.styleable.StyleableBean;
import org.jhotdraw8.fxbase.styleable.StyleablePropertyBean;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxbase.tree.TreeNode;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.FXRectangles;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.icollection.ChampSet;
import org.jhotdraw8.icollection.immutable.ImmutableSet;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A <em>figure</em> is a graphical (figurative) element of a {@link Drawing}.
 * <p>
 * <b>Rendering.</b> A figure can render a JavaFX scene graph (see {@link Node})
 * with the help of a {@link RenderContext}. The contents of the scene graph
 * depends on the class of the figure, the state of the figure, and the state of
 * the render context.
 * <p>
 * <b>State.</b> The state of a figure is defined by its property values. The
 * state consists of genuine property values and of computed property
 * values.<br>
 * Genuine property values typically describe the shape and the style of a
 * figure.<br>
 * Computed property values typically describe the layout of the figure or
 * cached values. Such as cached CSS properties and cached transformation
 * matrices. Computed property values often depend on the state of other
 * figures.
 * <p>
 * <b>Tree Structure.</b> A figure can be composed of other figures in a tree
 * structure. The composition is implemented with the {@code children} property
 * and the {@code parent} property.<br>
 * The composition can be restricted. Typically the parent of {@code Layer}
 * objects is restricted to instances of {@code Drawing}, and the parent of all
 * other figures is restricted to non-instances of {@code Drawing}.
 * <p>
 * <b>Local Coordinate Systems.</b> A figure may introduce a local coordinate
 * system which affects the graphical representation of itself and of its
 * descendants.<br>
 * The Figure interface provides methods which allow to transform between the
 * local coordinate system of a figure, the coordinate system of its parent, and
 * the world coordinate system.</p>
 * <p>
 * <b>Dependent Figures.</b> The state of a figure may depend on the state of
 * other figures. These dependencies are made explicit by parent/child
 * relationships and provider/dependant relationships.<br>
 * The parent/child relationships are strictly hierarchical, the
 * provider/dependant relationships may include cycles.<br>
 * The parent/child relationships are typically used for grouping figures into
 * {@code Layer}s, {@code Group}s and into layout hierarchies.<br>
 * The provider/dependant relationships are typically used for the creation of
 * line connections between figures, such as with
 * {@link LineConnectionFigure}. The strategy for updating the state of
 * dependent figures is implement in {@link DrawingModel}.
 * <p>
 * <b>Handles.</b> A figure can produce {@code Handle}s which allow to
 * graphically change the state of the figure in a drawing view.</p>
 * <p>
 * <b>Map Accessors.</b> A figure has an open ended set of property values. The
 * property values are accessed using {@code FigureMapAccessor}s.
 * <p>
 * <b>Styling.</b> Some property values of a figure can be styled using CSS. The
 * corresponding property key must implement the interface
 * {@link WritableStyleableMapAccessor}.</p>
 * <p>
 * <b>Update Strategy.</b> A figure does not automatically update its computed
 * property values. The update strategy is factored out into
 * {@link DrawingModel}.
 *
 * @author Werner Randelshofer
 */
public interface Figure extends StyleablePropertyBean, TreeNode<Figure> {

    // ----
    // various declarations
    // ----
    /**
     * To avoid name clashes in the stylesheet, all styleable JHotDraw
     * getProperties use the prefix {@code "-jhotdraw-"}.
     * <p>
     * XXX mapping of css attribute names to keys should be done elsewhere!
     */
    String JHOTDRAW_CSS_PREFIX = "";
    // ----
    // key declarations
    // ----

    // ----
    // property names
    // ----
    /**
     * The name of the parent property.
     */
    String PARENT_PROPERTY = "parent";

    /**
     * Computes the union of the bounds of the provided figures in world
     * coordinates.
     *
     * @param selection a set of figures
     * @return bounds
     */
    static Bounds bounds(Collection<? extends Figure> selection) {
        double minx = Double.POSITIVE_INFINITY;
        double miny = Double.POSITIVE_INFINITY;
        double maxx = Double.NEGATIVE_INFINITY;
        double maxy = Double.NEGATIVE_INFINITY;

        for (Figure f : selection) {
            Bounds fb = FXTransforms.transform(f.getLocalToWorld(), f.getLayoutBounds());
            double v = fb.getMaxX();
            if (v > maxx) {
                maxx = v;
            }
            v = fb.getMaxY();
            if (v > maxy) {
                maxy = v;
            }
            v = fb.getMinX();
            if (v < minx) {
                minx = v;
            }
            v = fb.getMinY();
            if (v < miny) {
                miny = v;
            }
        }
        return new BoundingBox(minx, miny, maxx - minx, maxy - miny);
    }

    /**
     * FIXME should be private!
     */
    Map<Class<?>, ImmutableSet<MapAccessor<?>>> declaredAndInheritedKeys = new ConcurrentHashMap<>();

    static ImmutableSet<MapAccessor<?>> getDeclaredMapAccessors(Class<?> clazz, ImmutableSet<MapAccessor<?>> keys) {
        try {
            for (Field f : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())
                    && MapAccessor.class.isAssignableFrom(f.getType())) {
                    MapAccessor<?> k = (MapAccessor<?>) f.get(null);
                    if (k == null) {
                        throw new RuntimeException(clazz + " has null value for key: " + f);
                    }
                    keys = keys.add(k);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException(clazz + " has non-public keys", ex);
        }
        return keys;
    }

    static void getDeclaredKeys(Class<?> clazz, Collection<Key<?>> keys) {
        try {
            for (Field f : clazz.getDeclaredFields()) {
                if (Key.class.isAssignableFrom(f.getType())) {
                    Key<?> k = (Key<?>) f.get(null);
                    keys.add(k);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException("class can not read its own keys");
        }
    }

    /**
     * Returns all keys declared in this class and inherited from parent
     * classes.
     *
     * @param clazz A figure class.
     * @return an unmodifiable set of the keys
     */
    static ImmutableSet<MapAccessor<?>> getDeclaredAndInheritedMapAccessors(Class<?> clazz) {
        ImmutableSet<MapAccessor<?>> keys = declaredAndInheritedKeys.get(clazz);
        if (keys == null) {
            keys = ChampSet.of();
            ArrayDeque<Class<?>> todo = new ArrayDeque<>();
            Set<Class<?>> done = new HashSet<>();
            todo.add(clazz);
            while (!todo.isEmpty()) {
                Class<?> c = todo.removeFirst();
                keys = getDeclaredMapAccessors(c, keys);
                if (c.getSuperclass() != null) {
                    todo.add(c.getSuperclass());
                }
                for (Class<?> i : c.getInterfaces()) {
                    if (done.add(i)) {
                        todo.add(i);
                    }
                }

            }
            declaredAndInheritedKeys.put(clazz, keys);
        }
        return keys;
    }

    /**
     * Computes the union of the visual bounds of the provided figures in world
     * coordinates.
     *
     * @param selection a set of figures
     * @return bounds
     */
    static @Nullable Bounds visualBounds(Collection<Figure> selection) {
        Bounds b = null;

        for (Figure f : selection) {
            Bounds fb;
            if (f instanceof Drawing) {
                fb = f.getLocalToWorld().transform(f.getLayoutBounds());
                if (b == null) {
                    b = fb;
                } else {
                    b = FXRectangles.union(b, fb);
                }
            } else {
                for (Figure ff : f.preorderIterable()) {
                    fb = ff.getLayoutBounds();
                    double grow = 0.0;
                    if (ff.get(StrokableFigure.STROKE) != null) {
                        switch (ff.getNonNull(StrokableFigure.STROKE_TYPE)) {
                            case CENTERED:
                                grow += ff.getNonNull(StrokableFigure.STROKE_WIDTH).getConvertedValue() * 0.5;
                                break;
                            case INSIDE:
                                break;
                            case OUTSIDE:
                                grow += ff.getNonNull(StrokableFigure.STROKE_WIDTH).getConvertedValue();
                                break;
                        }
                    }
                    if (ff.get(CompositableFigure.EFFECT) != null) {
                        grow += 10.0;
                    }
                    fb = FXRectangles.grow(fb, grow, grow);
                    fb = f.localToWorld(fb);
                    if (b == null) {
                        b = fb;
                    } else {
                        b = FXRectangles.union(b, fb);
                    }
                }
            }
        }
        return b;
    }

    // ----
    // convenience methods
    // ----

    /**
     * Adds a new child to the figure if it is a suitable child and this
     * figure is a suitable parent.
     *
     * @param newChild the new child
     * @return whether the child was added
     */
    default boolean addChild(Figure newChild) {
        return getChildren().add(newChild);
    }

    /**
     * Invoked by {@code DrawingModel} when the figure was added to a drawing.
     *
     * @param drawing the drawing to which this figure has been added
     */
    default void addedToDrawing(Drawing drawing) {
    }

    /**
     * Creates handles of the specified level and adds them to the provided
     * list.
     *
     * @param handleType The desired handle type
     * @param list       The handles.
     */
    default void createHandles(HandleType handleType, List<Handle> list) {
        if (handleType == HandleType.SELECT) {
            list.add(new BoundsInLocalOutlineHandle(this));
        } else if (handleType == HandleType.ANCHOR) {
            list.add(new AnchorOutlineHandle(this));
        } else if (handleType == HandleType.LEAD) {
            list.add(new AnchorOutlineHandle(this));
        } else if (handleType == HandleType.MOVE) {
            list.add(new BoundsInLocalOutlineHandle(this));
            list.add(new MoveHandle(this, BoundsLocator.NORTH_EAST));
            list.add(new MoveHandle(this, BoundsLocator.NORTH_WEST));
            list.add(new MoveHandle(this, BoundsLocator.SOUTH_EAST));
            list.add(new MoveHandle(this, BoundsLocator.SOUTH_WEST));
        } else if (handleType == HandleType.POINT) {
            list.add(new BoundsInLocalOutlineHandle(this));
            ResizeHandleKit.addCornerResizeHandles(this, list);
        } else if (handleType == HandleType.RESIZE) {
            list.add(new BoundsInLocalOutlineHandle(this));
            if (this instanceof ResizableFigure) {
                ResizeHandleKit.addCornerResizeHandles(this, list);
                ResizeHandleKit.addEdgeResizeHandles(this, list);
            } else {
                list.add(new MoveHandle(this, BoundsLocator.NORTH_EAST));
                list.add(new MoveHandle(this, BoundsLocator.NORTH_WEST));
                list.add(new MoveHandle(this, BoundsLocator.SOUTH_EAST));
                list.add(new MoveHandle(this, BoundsLocator.SOUTH_WEST));

            }
        } else if (handleType == HandleType.TRANSFORM) {
            list.add(new BoundsInTranslationOutlineHandle(this));
            list.add(new BoundsInLocalOutlineHandle(this));
            if (this instanceof TransformableFigure tf) {
                list.add(new RotateHandle(tf));
                TransformHandleKit.addTransformHandles(tf, list);
            }
        }
    }

    /**
     * This method is invoked by a {@code RenderContext}, when it needs a node
     * to create a JavaFX scene graph for a figure.
     * <p>
     * A typical implementation should look like this:
     * <pre>{@code
     * public Node createNode(RenderContext v) {
     * return new ...desired subclass of Node...();
     * }
     * }</pre>
     * <p>
     * A figure may be rendered with multiple {@code RenderContext}s
     * simultaneously. Each {@code RenderContext} uses this method to
     * instantiate a JavaFX node for the figure and associate it to the figure.
     * <p>
     * This method must create a new instance because returning an already
     * existing instance may cause undesired side effects on other
     * {@code RenderContext}s.
     * <p>
     * Note that by convention this method <b>may only</b> be invoked by a
     * {@code RenderContext} object.
     *
     * @param ctx the renderer which will use the node
     * @return the newly created node
     */
    Node createNode(RenderContext ctx);

    /**
     * This method is invoked on a figure by
     * {@link DrawingModel} when it determines that one
     * or more layout subjects have been added or removed.
     * <p>
     * The default implementation of this method is empty.
     */
    default void layoutSubjectChanged() {
    }

    /**
     * This method is invoked on a figure by
     * {@link DrawingModel} when it determines that a
     * property has been changed.
     * <p>
     * The default implementation of this method is empty.
     *
     * @param key      the key
     * @param oldValue the old value
     * @param newValue the new value
     * @param <T>      the value type
     */
    default <T> void propertyChanged(Key<T> key, @Nullable T oldValue, @Nullable T newValue) {
    }

    /**
     * This method is invoked on a figure by
     * {@link DrawingModel} when it determines that one
     * or more layout observers have been added or removed.
     * <p>
     * The default implementation of this method is empty.
     */
    default void layoutObserverChanged() {
    }

    /**
     * Disconnects all layout subjects and layout observers from this figure.
     * <p>
     * This method is called, when the figure is about to be removed from a
     * drawing.
     */
    default void disconnect() {
        for (Figure connectedFigure : getReadOnlyLayoutObservers().toArray(new Figure[0])) {
            connectedFigure.removeLayoutSubject(this);
        }
        removeAllLayoutSubjects();
    }

    /**
     * Fires a property change event.
     *
     * @param <T>        the value type
     * @param source     the event source
     * @param key        the property key
     * @param oldValue   the old property value
     * @param newValue   the new property value
     * @param wasAdded   true if the new property value was added
     * @param wasRemoved true if the old property value was removed
     */
    default <T> void firePropertyChangeEvent(Figure source, Key<T> key, @Nullable T oldValue, @Nullable T newValue, boolean wasAdded, boolean wasRemoved) {
        if (!wasRemoved && !wasAdded && Objects.equals(oldValue, newValue)) {
            return;
        }
        if (hasPropertyChangeListeners()) {
            firePropertyChangeEvent(new FigurePropertyChangeEvent(source, key, oldValue, newValue, wasAdded, wasRemoved));
        }
        Figure parent = getParent();
        if (parent != null) {
            parent.firePropertyChangeEvent(source, key, oldValue, newValue, wasAdded, wasRemoved);
        }
    }

    /**
     * Fires a property change event.
     *
     * @param event the event
     */
    default void firePropertyChangeEvent(FigurePropertyChangeEvent event) {
        if (hasPropertyChangeListeners()) {
            for (Listener<FigurePropertyChangeEvent> l : getPropertyChangeListeners()) {
                l.handle(event);
            }
        }
        Figure parent = getParent();
        if (parent != null) {
            parent.firePropertyChangeEvent(event);
        }
    }

    // ----
    // behavior methods
    // ----

    /**
     * The bounds that should be used for transformations of this figure.
     * <p>
     * The bounds are given in the untransformed local coordinate space of the
     * figure.
     * <p>
     * This method may use caching and return incorrect results if the cache is
     * stale. Invoke {@link #layout} if you are not sure that the cache is
     * valid.
     *
     * @return the local bounds
     */
    default Bounds getLayoutBounds() {
        return getCssLayoutBounds().getConvertedBoundsValue();
    }

    /**
     * The bounds that should be used for clipping and intersection tests
     * of this figure.
     *
     * @return the visual bounds
     */
    default Bounds getVisualBounds() {
        return getLayoutBounds();
    }

    /**
     * The bounds of this figure in local coordinates,
     * including space required for a non-zero stroke.
     *
     * @return the local bounds
     */
    Bounds getBoundsInLocal();

    CssRectangle2D getCssLayoutBounds();

    /**
     * The layout bounds of this figure in parent coordinates.
     * <p>
     * The bounds are given in the coordinate space of the parent figure.
     * <p>
     * This method may use caching and return incorrect results if the caches
     * are stale. Invoke {@link #invalidateTransforms} and {@link #layout} if
     * you are not sure that the cache is valid.
     *
     * @return the local bounds
     */
    default Bounds getLayoutBoundsInParent() {
        return FXTransforms.transformedBoundingBox(getLocalToParent(), getLayoutBounds());
    }

    /**
     * The bounds of this figure in parent coordinates including space
     * required for a non-zero stroke.
     * <p>
     * The bounds are given in the coordinate space of the parent figure.
     * <p>
     * This method may use caching and return incorrect results if the caches
     * are stale. Invoke {@link #invalidateTransforms} and {@link #layout} if
     * you are not sure that the cache is valid.
     *
     * @return the bounds in parent coordinates
     */
    default Bounds getBoundsInParent() {
        return FXTransforms.transformedBoundingBox(getLocalToParent(), getBoundsInLocal());
    }

    /**
     * The bounds of this figure in world coordinates including space
     * required for a non-zero stroke.
     * <p>
     * The bounds are given in the coordinate space of the world.
     * <p>
     * This method may use caching and return incorrect results if the caches
     * are stale. Invoke {@link #invalidateTransforms} and {@link #layout} if
     * you are not sure that the cache is valid.
     *
     * @return the bounds in world coordinates
     */
    default Bounds getBoundsInWorld() {
        return FXTransforms.transformedBoundingBox(getLocalToWorld(), getBoundsInLocal());
    }

    /**
     * Returns the layout bounds of the figure in world coordinates, including
     * space required for non-zero strokkes.
     *
     * @return the bounds in world coordinates
     */
    default Bounds getLayoutBoundsInWorld() {
        return FXTransforms.transformedBoundingBox(getLocalToWorld(), getLayoutBounds());
    }

    /**
     * Returns the visal bounds of the figure in world coordinates, including
     * space required for non-zero strokkes.
     *
     * @return the bounds in world coordinates
     */
    default Bounds getVisualBoundsInWorld() {
        return FXTransforms.transformedBoundingBox(getLocalToWorld(), getVisualBounds());
    }


    /**
     * Returns the center of the figure in the local coordinates of the figure.
     *
     * @return The center of the figure
     */
    default Point2D getCenterInLocal() {
        Bounds b = getLayoutBounds();
        return FXRectangles.center(b);
    }

    /**
     * Returns the center of the figure in the local coordinates of the figure.
     *
     * @return The center of the figure
     */
    default Point2D getCenterInParent() {
        Bounds b = getLayoutBoundsInParent();
        return FXRectangles.center(b);
    }

    /**
     * The child figures.
     * <p>
     * All changes on this list causes this figure to fire an invalidation
     * event.
     * <p>
     * If a child is added to this list, then this figure removes the child from
     * its former parent, and then sets itself as the parent of the child.</p>
     * <p>
     * If a child is removed from this list, then this figure sets the parent of
     * the child to null.</p>
     *
     * @return the children
     */
    @Override
    ObservableList<Figure> getChildren();

    /**
     * Returns all figures which observe the layout of this figure.
     * <p>
     * When the layout of this figure changes, then the layout of the observers
     * figures must be updated.
     * <p>
     * The update strategy is implemented in {@link DrawingModel}.
     * {@code DrawingMode} observes state changes in figures and updates
     * dependent figures. {@code DrawingModel} can coalesce multiple state
     * changes of an observed figure into a smaller number of layout calls on
     * the observers. {@code DrawingModel} can also detect cyclic layout
     * dependencies and prevent endless update loops.
     * <p>
     * This set must be synchronized, because it is accessed by other figures,
     * when there properties are changed.
     *
     * @return a list of dependent figures
     */
    Set<Figure> getLayoutObservers();

    ReadOnlySet<Figure> getReadOnlyLayoutObservers();

    /**
     * Returns the ancestor Drawing.
     *
     * @return the drawing or null if no ancestor is a drawing. Returns this, if
     * this figure is a drawing.
     */
    default @Nullable Drawing getDrawing() {
        return getAncestor(Drawing.class);
    }

    /**
     * Returns the ancestor Layer.
     *
     * @return the drawing or null if no ancestor is a layer. Returns this, if
     * this figure is a layer.
     */
    default @Nullable Layer getLayer() {
        return getAncestor(Layer.class);
    }

    /**
     * Returns the transformation from local coordinates into parent
     * coordinates.
     * <p>
     * This method may use caching and return incorrect results if the cache is
     * stale.
     *
     * @return the transformation
     */
    Transform getLocalToParent();

    /**
     * Returns the transformation from local coordinates into world coordinates.
     * <p>
     * This method may use caching and return incorrect results if the cache is
     * stale.
     *
     * @return the transformation
     */
    Transform getLocalToWorld();

    @Override
    default @Nullable Figure getParent() {
        return parentProperty().get();
    }

    @Override
    default void setParent(@Nullable Figure newValue) {
        parentProperty().set(newValue);
    }

    /**
     * Returns the transformation from parent coordinates into local
     * coordinates.
     * <p>
     * This method may use caching and return incorrect results if the cache is
     * stale.
     *
     * @return the transformation
     */
    Transform getParentToLocal();

    /**
     * Returns the transformation from world coordinates into drawing
     * coordinates.
     * <p>
     * This method may use caching and return incorrect results if the cache is
     * stale.
     *
     * @return the transformation
     */
    Transform getParentToWorld();

    /**
     * Returns the preferred aspect ratio of the figure. The aspect ratio is
     * defined as the height divided by the width of the figure. If a figure
     * does not have a preference it should return its current aspect ratio.
     *
     * @return the preferred aspect ratio of the figure.
     */
    default double getPreferredAspectRatio() {
        Bounds bounds = getLayoutBounds();
        return (bounds.getHeight() == 0 || bounds.getWidth() == 0) ? 1 : bounds.getHeight() / bounds.getWidth();
    }

    /**
     * List of property change listeners.
     *
     * @return a list of property change listeners
     */
    CopyOnWriteArrayList<Listener<FigurePropertyChangeEvent>> getPropertyChangeListeners();

    /**
     * Returns all figures which are subject to the layout of this figure.
     * <p>
     * When the layout of a layout subject changes, then the layout of this
     * figure needs to be updated.
     * <p>
     * See {@link #getLayoutObservers} for a description of the update strategy.
     * <p>
     * This default implementation returns an unmodifiable empty set.
     *
     * @return a list of layout subjects
     */
    default ReadOnlySet<Figure> getLayoutSubjects() {
        return ChampSet.of();
    }

    /**
     * Returns the root.
     *
     * @return the root
     */
    default @Nullable Figure getRoot() {
        Figure parent = this;
        while (parent.getParent() != null) {
            parent = parent.getParent();
        }
        return parent;
    }

    @Override
    default @Nullable StyleableBean getStyleableParent() {
        return getParent();
    }

    // ---
    // static methods
    // ---

    /**
     * Returns all supported map accessors of the figure.
     * <p>
     * The default implementation returns all declared and inherited map
     * accessors.
     *
     * @return an unmodifiable set of keys
     */
    default ImmutableSet<MapAccessor<?>> getSupportedKeys() {
        return Figure.getDeclaredAndInheritedMapAccessors(this.getClass());
    }

    /**
     * Returns the transformation from world coordinates into local coordinates.
     * <p>
     * This method may use caching and return incorrect results if the cache is
     * stale.
     *
     * @return the transformation
     */
    Transform getWorldToLocal();

    /**
     * Returns the transformation from world coordinates into parent
     * coordinates.
     * <p>
     * This method may use caching and return incorrect results if the cache is
     * stale.
     *
     * @return the transformation
     */
    Transform getWorldToParent();

    /**
     * Whether this figure has property change listeners.
     *
     * @return true if this figure has property change listeners
     */
    boolean hasPropertyChangeListeners();

    /**
     * Invalidates the transformation matrices of this figure.
     * <p>
     * This figure does not keep track of changes that cause the invalidation of
     * its transformation matrices. Use a
     * {@link DrawingModel} to manage the
     * transformation matrices of the figures in a drawing. Or call this method
     * on a figure and all its descendants, after you have performed a change
     * which invalidated the transform matrices of the figure.
     */
    void invalidateTransforms();

    /**
     * Whether children may be added to this figure.
     *
     * @return true if getChildren are allowed
     */
    boolean isAllowsChildren();

    /**
     * Whether the figure is decomposable by the user.
     *
     * @return true if the figure is decomposable
     */
    default boolean isDecomposable() {
        return true;
    }

    /**
     * Whether the figure is deletable by the user.
     *
     * @return true if the user may delete the figure
     */
    boolean isDeletable();

    /**
     * Whether the figure is editable by the user.
     *
     * @return true if the user may edit the figure.
     */
    boolean isEditable();

    /**
     * Whether the figure can be reshaped as a group together with other
     * figures.
     * <p>
     * If this figure uses one of the other figures for computing its position
     * or its layout, then it will return false.
     * <p>
     * The default implementation always returns true.
     *
     * @param others A set of figures.
     * @return true if the user may svgStringReshapeToBuilder this figure together with
     * those in the set.
     */
    default boolean isGroupReshapeableWith(Set<Figure> others) {
        return true;
    }

    /**
     * Whether the {@code layout} method of this figure does anything.
     * <p>
     * The default implementation returns false.
     *
     * @return true if the {@code layout} method is not empty.
     */
    default boolean isLayoutable() {
        return false;
    }

    /**
     * Whether the figure is selectable by the user.
     *
     * @return true if the user may select the figure
     */
    boolean isSelectable();

    /**
     * This method returns whether the provided figure is a suitable parent for this
     * figure.
     *
     * @param newParent The new parent figure.
     * @return true if {@code newParent} is an acceptable parent
     */
    @Override
    boolean isSuitableParent(Figure newParent);

    /**
     * This method returns whether the provided figure is a suitable child for this
     * figure.
     *
     * @param newChild The new child figure.
     * @return true if {@code newChild} is an acceptable child
     */
    @Override
    boolean isSuitableChild(Figure newChild);

    /**
     * Returns true if the specified key is supported by this figure.
     * <p>
     * The default implementation returns all declared and inherited map
     * accessors.
     *
     * @param key a key
     * @return whether the key is supported
     */
    default boolean isSupportedKey(MapAccessor<?> key) {
        return getSupportedKeys().contains(key);
    }

    /**
     * Returns true if the specified key is user editable.
     *
     * @param key a key
     * @return whether the key is user edtiable
     */
    default boolean isEditableKey(MapAccessor<?> key) {
        return isSupportedKey(key);
    }

    /**
     * Whether the figure and all its ancestors are visible.
     *
     * @return true if the user can see the figure
     */
    default boolean isShowing() {
        Figure node = this;
        while (node != null) {
            if (!node.getStyledNonNull(HideableFigure.VISIBLE)) {
                return false;
            }
            node = node.getParent();
        }
        return true;
    }

    /**
     * Whether the figure is visible.
     *
     * @return true if the user can see the figure
     */
    default boolean isVisible() {
        Figure node = this;
        return node.getStyledNonNull(HideableFigure.VISIBLE);
    }

    /**
     * Updates the layout of this figure, based on the layout of its children
     * and the layout of observed layout subjects.
     * <p>
     * If the layout of this figure depends on the layout of other figures, then
     * calling layout on this figure will only result in the correct result,
     * if layout of the other figures has been performed first.
     * <p>
     * A figure does not keep track of changes that require layout updates.
     * Use {@link DrawingModel} to manage layout updates.
     * <p>
     * The default implementation is empty.
     * <p>
     * To layout a drawing use {@link Drawing#layoutAll(RenderContext)}.
     *
     * @param ctx the render context (optional)
     */
    default void layout(RenderContext ctx) {

    }

    /**
     * This method is invoked on a figure by
     * {@link DrawingModel} when it determines that the
     * figure needs to be laid out.
     * <p>
     * The default implementation of this method calls {@link #layout}.
     *
     * @param ctx the render context (optional)
     */
    default void layoutChanged(RenderContext ctx) {
        layout(ctx);
    }

    /**
     * Transforms the specified point from local coordinates into world
     * coordinates.
     * <p>
     * This method may use caching and return incorrect results if the cache is
     * stale.
     *
     * @param p point in local coordinates
     * @return point in world coordinates
     */
    default Point2D localToWorld(Point2D p) {
        return FXTransforms.transform(getLocalToWorld(), p);
    }

    /**
     * Transforms the specified bounds from local coordinates into parent
     * coordinates.
     * <p>
     * This method may use caching and return incorrect results if the cache is
     * stale.
     *
     * @param p bounds in local coordinates
     * @return bounds in parent coordinates
     */
    default Bounds localToParent(Bounds p) {
        return getLocalToParent().transform(p);
    }

    /**
     * Transforms the specified bounds from local coordinates into world
     * coordinates.
     * <p>
     * This method may use caching and return incorrect results if the cache is
     * stale.
     *
     * @param p bounds in local coordinates
     * @return bounds in world coordinates
     */
    default Bounds localToWorld(Bounds p) {
        return getLocalToWorld().transform(p);
    }

    /**
     * The parent figure.
     * <p>
     * If this figure has not been added as a child to another figure, then this
     * variable will be null.
     * </p>
     * By convention the parent is set exclusively by a composite figure on its
     * child figures. The composite figure sets parent to itself on a child
     * immediately after the child figure has been added to the composite
     * figure. The composite figure sets parent to {@code null} on a child
     * immediately after the child figure has been removed from the composite
     * figure.
     *
     * @return the parent property, with {@code getBean()} returning this
     * figure, and {@code getName()} returning {@code PARENT_PROPERTY}.
     */
    ObjectProperty<Figure> parentProperty();

    /**
     * Removes a child from the figure.
     *
     * @param child a child of the figure
     */
    default void removeChild(Figure child) {
        getChildren().remove(child);
    }

    /**
     * Removes this figure from its parent if it has a parent.
     */
    default void removeFromParent() {
        Figure parent = getParent();
        if (parent != null) {
            parent.getChildren().remove(this);
        }
    }

    /**
     * Requests to removeChild all connection targets.
     */
    void removeAllLayoutSubjects();

    // ----
    // property fields
    // ----

    /**
     * Removes the specified connection target.
     *
     * @param targetFigure a Figure which is a connection target.
     */
    void removeLayoutSubject(Figure targetFigure);

    /**
     * Invoked by {@code DrawingModel} when the figure is removed from a
     * drawing.
     *
     * @param drawing the drawing from which this figure has been removed
     */
    default void removedFromDrawing(Drawing drawing) {
    }

    /**
     * Attempts to change the local bounds of the figure.
     * <p>
     * The figure may choose to only partially change its local bounds.
     *
     * @param transform the desired transformation in local coordinates
     */
    void reshapeInLocal(Transform transform);

    /**
     * Attempts to change the local bounds of the figure.
     * <p>
     * See {@link #reshapeInLocal(CssSize, CssSize, CssSize, CssSize)} for a description of this method.
     *
     * @param bounds the desired bounds
     */
    default void reshapeInLocal(Bounds bounds) {
        reshapeInLocal(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
    }

    /**
     * Attempts to change the local bounds of the figure.
     * <p>
     * See {#link #reshapeInLocal(Transform)} for a description of this method.
     * <p>
     * This is a convenience method which takes all parameters in pixel units.
     *
     * @param x      desired x-position in parent coordinates
     * @param y      desired y-position in parent coordinates
     * @param width  desired width in parent coordinates, may be negative
     * @param height desired height in parent coordinates, may be negative
     */
    default void reshapeInLocal(double x, double y, double width, double height) {
        reshapeInLocal(CssSize.of(x), CssSize.of(y), CssSize.of(width), CssSize.of(height));
    }

    /**
     * Attempts to change the local bounds of the figure.
     * <p>
     * See {#link #reshapeInLocal(Transform)} for a description of this method.
     * <p>
     * This method takes parameters as {@code CssSize}s. This can be used to avoid rounding
     * errors when the figure is reshaped in non-pixel units.
     * <p>
     * This method can forward a call to {@link #reshapeInLocal(Transform)}
     * using the following code:
     * <pre><code>
     * void reshapeInLocal(CssSize x, CssSize y, CssSize width, CssSize height) {
     *   Transform tx = Transforms.createReshapeTransform(getCssBoundsInLocal(), x, y, width, height);
     *   reshapeInLocal(tx);
     * }
     * </code></pre>
     *
     * @param x      desired x-position in parent coordinates
     * @param y      desired y-position in parent coordinates
     * @param width  desired width in parent coordinates, may be negative
     * @param height desired height in parent coordinates, may be negative
     */
    void reshapeInLocal(CssSize x, CssSize y, CssSize width, CssSize height);

    /**
     * Attempts to change the parent bounds of the figure.
     * <p>
     * The figure may choose to only partially change its parent bounds.
     * <p>
     * This method may also call
     * {@code reshapeInLocal} on child figures.
     *
     * @param transform the desired transformation in parent coordinates
     */
    void reshapeInParent(Transform transform);

    /**
     * Attempts to translate the parent bounds of the figure.
     *
     * @param t the translation in x and in y direction
     */
    default void translateInParent(CssPoint2D t) {
        if (FXTransforms.isIdentityOrNull(getParentToLocal())) {
            translateInLocal(t);
        } else {
            Point2D p = t.getConvertedValue();
            reshapeInParent(new Translate(p.getX(), p.getY()));
        }
    }

    /**
     * Attempts to translate the local bounds of the figure.
     *
     * @param t the translation in x and in y direction
     */
    default void translateInLocal(CssPoint2D t) {
        CssRectangle2D b = getCssLayoutBounds();
        reshapeInLocal(b.getMinX().add(t.getX()),
                b.getMinY().add(t.getY()),
                b.getWidth(), b.getHeight());
    }

    /**
     * This method is invoked on a figure by
     * {@link DrawingModel} when it determines that the
     * figure needs to apply its stylesheet again.
     * <p>
     * The default implementation of this method calls {@link #updateCss}.
     *
     * @param ctx the render context (optional)
     */
    default void stylesheetChanged(RenderContext ctx) {
        updateCss(ctx);
    }

    /**
     * Attempts to transform the figure.
     * <p>
     * The figure may choose to only partially change its transformation.
     *
     * @param transform the desired transformation in local coordinates
     */
    void transformInLocal(Transform transform);

    /**
     * Attempts to transform the figure.
     * <p>
     * The figure may choose to only partially change its transformation.
     *
     * @param transform the desired transformation in parent coordinates
     */
    void transformInParent(Transform transform);

    /**
     * This method is invoked on a figure and all its descendants by
     * {@link DrawingModel} when it determines that the
     * transformation of the figure has changed.
     * <p>
     * The default implementation of this method calls
     * {@link #invalidateTransforms}.
     */
    default void transformChanged() {
        invalidateTransforms();
    }

    /**
     * Updates the stylesheet cache of this figure depending on its property
     * values and on the and the property values of its ancestors.
     * <p>
     * This figure does not keep track of changes that require CSS updates. Use
     * a {@link DrawingModel} to manage CSS updates.
     *
     * @param ctx the render context
     */
    void updateCss(RenderContext ctx);

    /**
     * This method is invoked by a {@code RenderContext}, when it needs to
     * update the node which represents the scene graph in the figure.
     * <p>
     * A figure which is composed from child figures, must addChild the nodes of
     * its getChildren to its node. This ensures that coordinate space
     * transformations of the composed figure are properly propagated to its
     * getChildren.
     * </p>
     * <pre>
     * public void updateNode(RenderContext rc, Node n) {
     *     ObservableList&lt;Node&gt; group = ((Group) n).getChildren();
     * group.clear();
     * for (Figure child : children()) {
     * group.addChild(rc.getNode(child));
     * }
     * </pre>
     * <p>
     * A figure may be shown in multiple {@code RenderContext}s. Each
     * {@code RenderContext} uses this method to update the a JavaFX node for
     * the figure.
     * <p>
     * Note that the figure <b>must</b> retrieve the JavaFX node from other
     * figures from the render context by invoking {@code rc.getNode(child)}
     * rather than creating new nodes using {@code child.createNode(rc)}. This
     * convention allows to implement a cache in the render context for the Java
     * FX node. Also, render contexts like a drawing view need to associate
     * input events on Java FX nodes to the corresponding figure.
     * <p>
     * This figure does not keep track of changes that require node updates.
     * {@link DrawingModel} to manage node updates.
     *
     * @param ctx  the render context
     * @param node the node which was created with {@link #createNode}
     */
    void updateNode(RenderContext ctx, Node node);

    /**
     * Transforms the specified point from world coordinates into local
     * coordinates.
     * <p>
     * This method may use caching and return incorrect results if the cache is
     * stale.
     *
     * @param pointInWorld point in drawing coordinates
     * @return point in local coordinates
     */
    default Point2D worldToLocal(Point2D pointInWorld) {
        final Transform wtl = getWorldToLocal();
        return FXTransforms.isIdentityOrNull(wtl) ? pointInWorld : FXTransforms.transform(wtl, pointInWorld);
    }

    default CssPoint2D worldToLocal(CssPoint2D pointInWorld) {
        final Transform wtl = getWorldToLocal();
        return FXTransforms.isIdentityOrNull(wtl) ? pointInWorld
                : new CssPoint2D(FXTransforms.transform(wtl, pointInWorld.getConvertedValue()));
    }

    /**
     * Transforms the specified point from world coordinates into parent
     * coordinates.
     * <p>
     * This method may use caching and return incorrect results if the cache is
     * stale.
     *
     * @param pointInWorld point in drawing coordinates
     * @return point in local coordinates
     */
    default Point2D worldToParent(Point2D pointInWorld) {
        final Transform wtp = getWorldToParent();
        return FXTransforms.transform(wtp, pointInWorld);
    }

    default Point2D worldToParent(double x, double y) {
        final Transform wtp = getWorldToParent();
        return FXTransforms.transform(wtp, x, y);
    }

    /**
     * Returns true if this figure should be deleted if its last layout
     * subject is deleted.
     *
     * @return if the deletion of the layout subject should lead to a cascaded
     * delete
     */
    default boolean isDeletWithLastLayoutSubject() {
        return true;
    }

}
