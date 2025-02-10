/*
 * @(#)AbstractFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.transform.Transform;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.css.manager.StylesheetsManager;
import org.jhotdraw8.css.value.CssDefaultableValue;
import org.jhotdraw8.css.value.CssDefaulting;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxbase.styleable.AbstractStyleablePropertyBean;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.icollection.ChampSet;
import org.jhotdraw8.icollection.facade.ReadableSetFacade;
import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AbstractFigure.
 *
 */
public abstract class AbstractFigure extends AbstractStyleablePropertyBean
        implements Figure, TransformCachingFigure {

    private Set<Figure> layoutObservers;
    private @Nullable Drawing drawing;
    private final ObjectProperty<Figure> parent = new SimpleObjectProperty<>(this, Figure.PARENT_PROPERTY);
    private CopyOnWriteArrayList<Listener<FigurePropertyChangeEvent>> propertyChangeListeners;
    private Transform cachedLocalToWorld;
    private Transform cachedWorldToParent;
    private Transform cachedParentToWorld;
    private Transform cachedParentToLocal;
    private Transform cachedLocalToParent;
    private Transform cachedWorldToLocal;

    public AbstractFigure() {
    }

    @Override
    protected Map<Key<?>, Integer> createKeyMap() {
        return keyMaps.computeIfAbsent(getClass(), k -> {
            int index = 0;

            PersistentSet<MapAccessor<?>> accessors = Figure.getDeclaredAndInheritedMapAccessors(getClass());
            Map<Key<?>, Integer> m = new IdentityHashMap<>(accessors.size());
            for (MapAccessor<?> accessor : accessors) {
                if (accessor instanceof Key<?>) {
                    m.put((Key<?>) accessor, index++);
                }
            }
            return m;
        });
    }

    /**
     * This method calls {@link #doAddedToDrawing}.
     *
     * @param drawing the drawing
     */
    @Override
    public final void addedToDrawing(Drawing drawing) {
        this.drawing = drawing;
        doAddedToDrawing(drawing);
    }

    /**
     * This method is called by {@link Figure#addedToDrawing}. The implementation of this
     * class is empty.
     *
     * @param drawing the drawing
     */
    protected void doAddedToDrawing(Drawing drawing) {

    }

    /**
     * This method is called by {@link #removedFromDrawing}. The implementation of
     * this class is empty.
     *
     * @param drawing the drawing
     */
    protected void doRemovedFromDrawing(Drawing drawing) {

    }


    @Override
    public final @Nullable Drawing getDrawing() {
        return drawing;
    }

    @Override
    public final Set<Figure> getLayoutObservers() {
        if (layoutObservers == null) {
            layoutObservers = Collections.newSetFromMap(new IdentityHashMap<>(1));
        }
        return layoutObservers;
    }

    @Override
    public ReadableSet<Figure> getReadOnlyLayoutObservers() {
        if (layoutObservers == null) {
            return ChampSet.of();
        }
        return new ReadableSetFacade<>(layoutObservers);
    }

    @Override
    public CopyOnWriteArrayList<Listener<FigurePropertyChangeEvent>> getPropertyChangeListeners() {
        if (propertyChangeListeners == null) {
            propertyChangeListeners = new CopyOnWriteArrayList<>();
        }
        return propertyChangeListeners;
    }

    @Override
    public boolean hasPropertyChangeListeners() {
        return propertyChangeListeners != null && !propertyChangeListeners.isEmpty();
    }

    @Override
    public ObjectProperty<Figure> parentProperty() {
        return parent;
    }

    /**
     * This implementation is empty.
     */
    @Override
    public void removeAllLayoutSubjects() {
        // empty
    }

    /**
     * This implementation is empty.
     *
     * @param connectedFigure the connected figure
     */
    @Override
    public void removeLayoutSubject(Figure connectedFigure) {
        // empty
    }

    /**
     * This method calls {@link #doAddedToDrawing}.
     */
    @Override
    public final void removedFromDrawing(Drawing drawing) {
        this.drawing = null;
        doRemovedFromDrawing(drawing);
    }

    @Override
    public @Nullable Transform getCachedLocalToWorld() {
        return cachedLocalToWorld;
    }

    @Override
    public void setCachedLocalToWorld(@Nullable Transform newValue) {
        this.cachedLocalToWorld = newValue;
    }

    @Override
    public @Nullable Transform getCachedWorldToParent() {
        return cachedWorldToParent;
    }

    @Override
    public void setCachedWorldToParent(@Nullable Transform newValue) {
        this.cachedWorldToParent = newValue;
    }

    @Override
    public @Nullable Transform getCachedParentToLocal() {
        return cachedParentToLocal;
    }

    @Override
    public void setCachedParentToLocal(@Nullable Transform newValue) {
        this.cachedParentToLocal = newValue;
    }

    @Override
    public @Nullable Transform getCachedLocalToParent() {
        return cachedLocalToParent;
    }

    @Override
    public void setCachedLocalToParent(@Nullable Transform newValue) {
        this.cachedLocalToParent = newValue;
    }

    @Override
    public @Nullable Transform getCachedWorldToLocal() {
        return cachedWorldToLocal;
    }

    @Override
    public void setCachedWorldToLocal(@Nullable Transform newValue) {
        this.cachedWorldToLocal = newValue;
    }

    @Override
    public @Nullable Transform getCachedParentToWorld() {
        return cachedParentToWorld;
    }

    @Override
    public void setCachedParentToWorld(@Nullable Transform newValue) {
        this.cachedParentToWorld = newValue;
    }


    @Override
    public void updateCss(RenderContext ctx) {
        Drawing d = getDrawing();
        if (d != null) {
            StylesheetsManager<Figure> styleManager = d.getStyleManager();
            if (styleManager != null) {
                styleManager.applyStylesheetsTo(this);
            }
        }
        //invalidateTransforms();
    }

    /**
     * Overrides of this method must call super!
     *
     * @param key        the changed key
     * @param oldValue   the old value
     * @param newValue   the new value
     * @param wasAdded   whether the value was added (hence oldValue does not matter)
     * @param wasRemoved whether the value was removed (hence newValue does not matter)
     * @param <T>        the value type of the property
     */
    @Override
    protected <T> void onPropertyChanged(Key<T> key, T oldValue, T newValue, boolean wasAdded, boolean wasRemoved) {
        firePropertyChangeEvent(this, key, oldValue, newValue, wasAdded, wasRemoved);
    }

    @Override
    public <T> T getStyledNonNull(NonNullMapAccessor<T> key) {
        T value = super.getStyledNonNull(key);
        if (value instanceof CssDefaultableValue<?>) {
            @SuppressWarnings("unchecked") CssDefaultableValue<T> dv = (CssDefaultableValue<T>) value;
            if (dv.getDefaulting() == CssDefaulting.INHERIT && getParent() != null) {
                value = getParent().getStyledNonNull(key);
            }
        }

        return value;
    }

}
