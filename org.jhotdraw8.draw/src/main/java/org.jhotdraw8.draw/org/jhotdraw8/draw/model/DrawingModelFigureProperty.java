/*
 * @(#)DrawingModelFigureProperty.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.model;

import javafx.beans.property.ReadOnlyObjectWrapper;
import org.jspecify.annotations.Nullable;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxbase.event.SimpleWeakListener;
import org.jhotdraw8.fxcollection.typesafekey.Key;

import java.lang.ref.WeakReference;

/**
 * This property is weakly bound to a property of a figure in the DrawingModel.
 * <p>
 * If the key is not declared by the figure, then the value will always be null.
 *
 * @param <T> the value type of the property
 * @author Werner Randelshofer
 */
public class DrawingModelFigureProperty<T> extends ReadOnlyObjectWrapper<T> {

    private final DrawingModel model;
    protected final @Nullable WeakReference<Figure> figure;
    private final @Nullable Key<T> key;
    private final @Nullable Listener<DrawingModelEvent> modelListener;
    private final @Nullable SimpleWeakListener<DrawingModelEvent> weakListener;
    private final boolean isDeclaredKey;

    public DrawingModelFigureProperty(DrawingModel model, Figure figure, Key<T> key) {
        this(model, figure, key, false);
    }

    public DrawingModelFigureProperty(DrawingModel model, @Nullable Figure figure, @Nullable Key<T> key, boolean allKeys) {
        this.model = model;
        this.key = key;
        this.figure = new WeakReference<>(figure);
        this.isDeclaredKey = figure != null && Figure.getDeclaredAndInheritedMapAccessors(figure.getClass()).contains(key);

        if (key != null) {
            this.modelListener = (event) -> {
                DrawingModelEvent.EventType eventType = event.getEventType();
                if (event.getEventType() == DrawingModelEvent.EventType.PROPERTY_VALUE_CHANGED
                        && this.figure.get() == event.getNode()) {
                    if (this.key == event.getKey()) {
                        @SuppressWarnings("unchecked")
                        T newValue = event.getNewValue();
                        if (super.get() != newValue) {
                            set(newValue);
                        }
                    } else if (allKeys) {
                        updateValue();
                    }
                }
            };

            model.addDrawingModelListener(weakListener = new SimpleWeakListener<>(modelListener, model::removeDrawingModelListener));
        } else {
            modelListener = null;
            weakListener = null;
        }
    }

    @Override
    public @Nullable T getValue() {
        Figure f = figure.get();
        @SuppressWarnings("unchecked")
        T temp = isDeclaredKey && f != null && key != null ? f.get(key) : null;
        return temp;
    }

    @Override
    public void setValue(@Nullable T value) {
        if (isDeclaredKey && figure != null && key != null) {
            if (value != null && !key.isAssignable(value)) {
                throw new IllegalArgumentException("value is not assignable " + value);
            }
            Figure f = this.figure.get();
            if (f != null) {
                model.set(f, key, value);
            }
        }
        // Note: super must be called after "put", so that listeners
        //       can be properly informed.
        super.setValue(value);
    }

    @Override
    public void unbind() {
        super.unbind();
        model.removeDrawingModelListener(weakListener);
    }

    /**
     * This implementation is empty.
     */
    protected void updateValue() {
    }
}
