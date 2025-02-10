/*
 * @(#)NonTransformableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Bounds;
import javafx.scene.transform.Transform;
import org.jhotdraw8.geom.FXTransforms;

/**
 * Provides default implementations for figures which can not be transformed.
 *
 */
public interface NonTransformableFigure extends TransformCachingFigure {

    @Override
    default void transformInParent(Transform transform) {
        // transformInParent is the same as transportInLocal for non-transformable figures
        transformInLocal(transform);
    }

    @Override
    default void transformInLocal(Transform transform) {
        // empty because non-transformable figures can not be transformed
    }

    @Override
    default void reshapeInParent(Transform transform) {
        reshapeInLocal(FXTransforms.concat(getParentToLocal(), transform));
    }

    @Override
    default Transform getLocalToParent() {
        return FXTransforms.IDENTITY;
    }

    @Override
    default Transform getParentToLocal() {
        return FXTransforms.IDENTITY;
    }

    @Override
    default void reshapeInLocal(Transform transform) {
        Bounds b = getLayoutBounds();
        b = transform.transform(b);
        reshapeInLocal(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
    }
}
