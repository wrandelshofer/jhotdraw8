/*
 * @(#)MarkerPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.awt.geom.Path2D;

/**
 * MarkerPathBuilder. Places markers at the start, end and middle of the path.
 * The path itself is not included by the builder.
 *
 * @param <T> the product type
 * @author Werner Randelshofer
 */
public class MarkerPathBuilder<T> extends AbstractPathBuilder<T> {

    private final Path2D.Double startMarker;
    private final Path2D.Double endMarker;
    private final Path2D.Double midMarker;// FIXME support midMarker
    private final @NonNull PathBuilder<T> out;
    private boolean needsStartMarker;
    private boolean needsEndMarker;
    private double tangentX, tangentY;

    public MarkerPathBuilder(PathBuilder<T> out, Path2D.Double startMarker, Path2D.Double endMarker, Path2D.Double midMarker) {
        this.startMarker = startMarker;
        this.endMarker = endMarker;
        this.midMarker = midMarker;
        this.out = out;
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x3, double y3) {
        doStartOrMidMarker(x1, y1);
        tangentX = x2;
        tangentY = y2;
        needsEndMarker = true;
    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        doStartOrMidMarker(x, y);
        tangentX = lastX;
        tangentY = lastY;
        needsEndMarker = true;
    }

    private void doStartOrMidMarker(double x, double y) {
        final Path2D.Double marker;
        if (needsStartMarker) {
            needsStartMarker = false;
            marker = startMarker;
        } else {
            marker = midMarker;
        }

        if (marker == null) {
            return;
        }
        final double x0 = getLastX();
        final double y0 = getLastY();
        final Transform tx = FXTransforms.rotate(x0 - x, y0 - y, 0, 0).createConcatenation(new Translate(x, y));
        AwtShapes.buildPathIterator(out, marker.getPathIterator(FXTransforms.toAwt(tx)));
    }

    private void doEndMarker() {
        if (needsEndMarker) {
            needsEndMarker = false;
            if (endMarker == null) {
                return;
            }
            double x = getLastX();
            double y = getLastY();
            double x0 = tangentX;
            double y0 = tangentY;
            Transform tx = FXTransforms.rotate(x0 - x, y0 - y, x, y).createConcatenation(new Translate(x, y));
            AwtShapes.buildPathIterator(out, startMarker.getPathIterator(FXTransforms.toAwt(tx)));
        }
    }

    @Override
    protected void doMoveTo(double x, double y) {
        needsStartMarker = true;
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x1, double y1, double x2, double y2) {
        doStartOrMidMarker(x1, y1);
        tangentX = x1;
        tangentY = y1;
        needsEndMarker = true;
    }

    @Override
    public @Nullable T build() {
        return out.build();
    }
}
