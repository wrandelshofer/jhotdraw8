/*
 * @(#)PathConnector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.connector;

import javafx.geometry.Point2D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.PathIterableFigure;
import org.jhotdraw8.draw.locator.BoundsLocator;
import org.jhotdraw8.draw.locator.Locator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.intersect.IntersectLinePathIterator;
import org.jhotdraw8.geom.intersect.IntersectionPointEx;
import org.jhotdraw8.geom.intersect.IntersectionResultEx;

import java.awt.geom.PathIterator;

import static org.jhotdraw8.draw.figure.StrokableFigure.STROKE;
import static org.jhotdraw8.draw.figure.StrokableFigure.STROKE_TYPE;

/**
 * PathConnector. The target of the connection must implement {@link PathIterableFigure}.
 *
 * @author Werner Randelshofer
 * $$
 */
public class PathConnector extends LocatorConnector {

    public PathConnector() {
        super(BoundsLocator.CENTER);
    }

    public PathConnector(Locator locator) {
        super(locator);
    }


    @Override
    public IntersectionPointEx intersect(RenderContext ctx, Figure connection, @NonNull Figure target, @NonNull Point2D start, @NonNull Point2D end) {
        if (!(target instanceof PathIterableFigure pif)) {
            return super.intersect(ctx, connection, target, start, end);
        }
        Point2D s = target.worldToLocal(start);
        Point2D e = target.worldToLocal(end);
        PathIterator pit;

        // FIXME does not take line join into account
        if (target.getStyled(STROKE) != null) {
            pit = switch (target.getStyledNonNull(STROKE_TYPE)) {
                default ->
                    // FIXME must stroke the path
                        pif.getPathIterator(ctx, null);
                case INSIDE -> pif.getPathIterator(ctx, null);
            };
        } else {
            pit = pif.getPathIterator(ctx, null);
        }

        IntersectionResultEx i = IntersectLinePathIterator.intersectLinePathIteratorEx(s.getX(), s.getY(), e.getX(), e.getY(), pit);
        return i.intersections().peekLast();
    }
}
