/*
 * @(#)LocatorConnector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.connector;

import javafx.geometry.Point2D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.locator.Locator;
import org.jhotdraw8.geom.PointAndDerivative;

/**
 * LocatorConnector uses a {@link Locator} to compute its position.
 *
 * @author Werner Randelshofer
 */
public class LocatorConnector extends AbstractConnector {

    private final @NonNull Locator locator;

    /**
     * Creates a new instance
     *
     * @param locator the locator that should be used
     */
    public LocatorConnector(@NonNull Locator locator) {
        this.locator = locator;
    }

    /**
     * Returns the locator used to compute the position of the connector.
     *
     * @return the locator
     */
    public @NonNull Locator getLocator() {
        return locator;
    }

    @Override
    public @NonNull PointAndDerivative getPointAndDerivativeInLocal(@NonNull Figure connection, @NonNull Figure target) {
        return new PointAndDerivative(locator.locate(target).getX(), locator.locate(target).getY(), new Point2D(1, 0).getX(), new Point2D(1, 0).getY());
    }

    /*
    @Override
    public IntersectionPointEx chopStart(@NonNull RenderContext ctx, Figure connection, @NonNull Figure target, double startX, double startY, double endX, double endY) {
        final Bounds b = target.getLayoutBounds();
        Point2D center = new Point2D(b.getMinX() + b.getWidth() * 0.5, b.getMinY() + b.getHeight() * 0.5);
        Point2D location = locator.locate(target);
        Point2D direction = location.subtract(center);
        Point2D derivative1 = new Point2D(direction.getY(), -direction.getX());
        Point2D derivative2 = new Point2D(direction.getX(), direction.getY());
        if (FXGeom.squaredMagnitude(derivative1) < 1e-6) {
            derivative1 = new Point2D(1, 0);
            derivative2 = new Point2D(0, -1);
        }

        Transform localToWorld = target.getLocalToWorld();
        Point2D targetP = target.localToWorld(location);
        Point2D t1p = localToWorld == null ? derivative1 : localToWorld.deltaTransform(derivative1);
        Point2D t2p = localToWorld == null ? derivative2 : localToWorld.deltaTransform(derivative2);
        return new IntersectionPointEx(
                targetP.getX(), targetP.getY(),
                0, t1p.getX(), t1p.getY(),
                0, t2p.getX(), t2p.getY());
    }*/
}
