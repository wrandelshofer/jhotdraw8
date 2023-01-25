/*
 * @(#)AbstractCompositeFigure.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.fxbase.tree.ChildList;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.geom.FXTransforms;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * This base class can be used to implement figures which support child figures.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractCompositeFigure extends AbstractFigure {
    private final ChildList<Figure> children = new ChildList<>(this);

    public AbstractCompositeFigure() {
    }

    @Override
    public @NonNull ObservableList<Figure> getChildren() {
        return children;
    }

    @Override
    public final boolean isAllowsChildren() {
        return true;
    }

    @Override
    public @NonNull Bounds getLayoutBounds() {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Figure child : getChildren()) {
            Bounds b = child.getLayoutBoundsInParent();
            minX = min(minX, b.getMinX());
            maxX = max(maxX, b.getMaxX());
            minY = min(minY, b.getMinY());
            maxY = max(maxY, b.getMaxY());
        }
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public @NonNull Bounds getBoundsInLocal() {
        ObservableList<Figure> children = getChildren();
        if (children.isEmpty()) {
            return new BoundingBox(0, 0, 0, 0);
        }
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Figure child : children) {
            Bounds b = child.getBoundsInParent();
            minX = min(minX, b.getMinX());
            maxX = max(maxX, b.getMaxX());
            minY = min(minY, b.getMinY());
            maxY = max(maxY, b.getMaxY());
        }
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public @NonNull CssRectangle2D getCssLayoutBounds() {
        return new CssRectangle2D(getLayoutBounds());
    }

    @Override
    public @NonNull Bounds getLayoutBoundsInParent() {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        Transform t = getLocalToParent();

        for (Figure child : getChildren()) {
            Bounds b = FXTransforms.transform(t, child.getLayoutBoundsInParent());
            minX = min(minX, b.getMinX());
            maxX = max(maxX, b.getMaxX());
            minY = min(minY, b.getMinY());
            maxY = max(maxY, b.getMaxY());
        }
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public void firePropertyChangeEvent(@NonNull FigurePropertyChangeEvent event) {
        final Figure source = event.getSource();
        if (source.getParent() == this) {
            children.fireItemUpdated(children.indexOf(source));
        }
        super.firePropertyChangeEvent(event);
    }

    @Override
    public <T> void firePropertyChangeEvent(@Nullable Figure source, Key<T> key, T oldValue, T newValue) {
        if (children.hasChangeListeners()) {
            children.fireItemUpdated(children.indexOf(source));
        }
        super.firePropertyChangeEvent(source, key, oldValue, newValue);
    }
}
