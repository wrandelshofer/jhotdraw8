/*
 * @(#)ContourPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.geom.contour.ContourBuilder;
import org.jhotdraw8.geom.contour.PolyArcPath;
import org.jhotdraw8.geom.contour.PolyArcPathBuilder;

/**
 * Builds a contour path around a path.
 *
 * @param <T> the product type
 */
public class ContourPathBuilder<T> extends AbstractPathBuilder<T> {
    private final double offset;

    private final @NonNull PathBuilder<T> consumer;
    private final @NonNull PolyArcPathBuilder papb = new PolyArcPathBuilder();
    private final double eps2;

    public ContourPathBuilder(@NonNull PathBuilder<T> consumer, double offset) {
        this(consumer, offset, 0);
    }

    public ContourPathBuilder(@NonNull PathBuilder<T> consumer, double offset, double epsilon) {
        this.offset = offset;
        this.consumer = consumer;
        this.eps2 = epsilon * epsilon;
    }

    @Override
    protected void doClosePath() {
        papb.closePath();
    }

    @Override
    protected void doPathDone() {
        papb.pathDone();
        ContourBuilder contourBuilder = new ContourBuilder();
        for (PolyArcPath path : papb.build()) {
            for (PolyArcPath contourPath : contourBuilder.parallelOffset(path, -offset)) {
                SvgPaths.buildFromPathIterator(consumer, contourPath.getPathIterator(null), false);
            }
        }
        consumer.pathDone();
    }

    @Override
    protected void doCurveTo(double x1, double y1, double x2, double y2, double x, double y) {
        papb.curveTo(x1, y1, x2, y2, x, y);
    }

    @Override
    protected void doLineTo(double x, double y) {
        if (eps2 == 0 || (lastX - x) * (lastX - x) + (lastY - y) * (lastY - y) >= eps2) {
            papb.lineTo(x, y);
        }
    }

    @Override
    protected void doMoveTo(double x, double y) {
        papb.moveTo(x, y);
    }

    @Override
    protected void doQuadTo(double x1, double y1, double x, double y) {
        papb.quadTo(x1, y1, x, y);
    }

    @Override
    public @Nullable T build() {
        return consumer.build();
    }
}
