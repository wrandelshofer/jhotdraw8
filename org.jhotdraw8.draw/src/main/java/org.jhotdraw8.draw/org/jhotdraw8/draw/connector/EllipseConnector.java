/*
 * @(#)EllipseConnector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.connector;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.locator.BoundsLocator;
import org.jhotdraw8.draw.locator.Locator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.FXRectangles;
import org.jhotdraw8.geom.intersect.IntersectEllipseLine;
import org.jhotdraw8.geom.intersect.IntersectionPointEx;
import org.jhotdraw8.geom.intersect.IntersectionResultEx;
import org.jspecify.annotations.Nullable;

import static org.jhotdraw8.draw.figure.StrokableFigure.STROKE;
import static org.jhotdraw8.draw.figure.StrokableFigure.STROKE_TYPE;
import static org.jhotdraw8.draw.figure.StrokableFigure.STROKE_WIDTH;

/**
 * EllipseConnector.
 *
 * @author Werner Randelshofer
 */
public class EllipseConnector extends LocatorConnector {

    public EllipseConnector() {
        super(BoundsLocator.CENTER);
    }

    public EllipseConnector(Locator locator) {
        super(locator);
    }

    @Override
    public @Nullable IntersectionPointEx intersect(RenderContext ctx, Figure connection, Figure target, Point2D start, Point2D end) {
        Point2D s = target.worldToLocal(start);
        Point2D e = target.worldToLocal(end);
        Bounds bounds = target.getLayoutBounds();

        if (target.getStyled(STROKE) != null) {
            double grow = switch (target.getStyledNonNull(STROKE_TYPE)) {
                default -> target.getStyledNonNull(STROKE_WIDTH).getConvertedValue() / 2d;
                case OUTSIDE -> target.getStyledNonNull(STROKE_WIDTH).getConvertedValue();
                case INSIDE -> 0d;
            };
            bounds = FXRectangles.grow(bounds, grow, grow);
        }

        IntersectionResultEx i = IntersectEllipseLine.intersectLineEllipseEx(s.getX(), s.getY(), e.getX(), e.getY(),
                bounds.getMinX() + bounds.getWidth() * 0.5, bounds.getMinY() + bounds.getHeight() * 0.5, bounds.getWidth() * 0.5, bounds.getHeight() * 0.5);
        return i.intersections().peekLast();
    }
}
