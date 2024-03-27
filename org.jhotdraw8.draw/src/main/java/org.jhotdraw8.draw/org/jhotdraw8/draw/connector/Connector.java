/*
 * @(#)Connector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.connector;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.Lines;
import org.jhotdraw8.geom.PointAndDerivative;
import org.jhotdraw8.geom.intersect.IntersectLineRectangle;
import org.jhotdraw8.geom.intersect.IntersectionPointEx;
import org.jhotdraw8.geom.intersect.IntersectionResultEx;

import java.awt.geom.Rectangle2D;

/**
 * A <em>connector</em> encapsulates a strategy for locating a connection point
 * for a connection figure on a target figure.
 *
 * @author Werner Randelshofer
 */
public interface Connector {

    /**
     * Returns a point and derivative on the target figure for the specified
     * connection figure in local coordinates.
     *
     * @param connection a connection figure
     * @param target     the target
     * @return A point and derivative on the target figure in local coordinates of the target
     * figure.
     */
    @NonNull PointAndDerivative getPointAndDerivativeInLocal(@NonNull Figure connection,
                                                             @NonNull Figure target);

    /**
     * Returns a point and derivative on the target figure for the specified
     * connection figure in world coordinates.
     *
     * @param connection a connection figure
     * @param target     the target
     * @return A point and derivative on the target figure in world coordinates
     */
    default @NonNull PointAndDerivative getPointAndDerivativeInWorld(Figure connection, Figure target) {
        PointAndDerivative inLocal = getPointAndDerivativeInLocal(connection, target);
        Transform localToWorld = target.getLocalToWorld();
        Point2D pointInWorld = FXTransforms.transform(localToWorld, inLocal.getPoint(Point2D::new));
        Point2D derivativeInWorld = FXTransforms.deltaTransform(localToWorld, inLocal.getDerivative(Point2D::new));
        return new PointAndDerivative(pointInWorld.getX(), pointInWorld.getY(),
                derivativeInWorld.getX(), derivativeInWorld.getY());
    }

    /**
     * Clips the start of the provided line at the bounds of the target figure.
     * The line must be given in world coordinates.
     *
     * @param ctx        the render context
     * @param connection a connection figure
     * @param target     the target
     * @param sx         x-coordinate at the start of the line
     * @param sy         x-coordinate at the start of the line
     * @param ex         x-coordinate at the end of the line
     * @param ey         y-coordinate at the end of the line
     * @return the new start point in world coordinates
     */
    default IntersectionPointEx chopStart(@NonNull RenderContext ctx, Figure connection, @NonNull Figure target, double sx, double sy, double ex, double ey) {
        return chopStart(ctx, connection, target, new Point2D(sx, sy), new Point2D(ex, ey));
    }


    /**
     * Clips the start of the provided line at the bounds of the target figure.
     * The line must be given in world coordinates.
     *
     * @param ctx        the render context
     * @param connection a connection figure
     * @param target     the target
     * @param start      the start of the line, should be inside the target figure
     * @param end        the end of the line, should be outside the target figure
     * @return the new start point in world coordinates
     */
    default IntersectionPointEx chopStart(@NonNull RenderContext ctx, Figure connection, @NonNull Figure target, @NonNull Point2D start, @NonNull Point2D end) {
        IntersectionPointEx ip = intersect(ctx, connection, target, start, end);
        Point2D derivative = end.subtract(start);
        return ip == null ? new IntersectionPointEx(start.getX(), start.getY(), 0, derivative.getX(), derivative.getY(), 0, derivative.getX(), derivative.getY()) :
                new IntersectionPointEx(Lines.lerp(start.getX(), start.getY(), end.getX(), end.getY(), ip.argumentA()), ip.argumentA(), ip.getDerivativeA(), ip.getArgumentB(), ip.getDerivativeB());
    }

    /**
     * Returns the intersection of the line going from start to end with the
     * target figure. The line must be given in world coordinates.
     *
     * @param ctx the render context
     * @param connection the connection figure
     * @param target     the target figure
     * @param start      the start point of the line in world coordinates, should be
     *                   inside the target figure
     * @param end        the end point of the line in world coordinates, should be
     *                   outside the target figure
     * @return the intersection in the interval [0,1], null if no intersection.
     * In case of multiple intersections returns the largest value.
     */
    default @Nullable IntersectionPointEx intersect(@NonNull RenderContext ctx, Figure connection, @NonNull Figure target, @NonNull Point2D start, @NonNull Point2D end) {
        Point2D s = target.worldToLocal(start);
        Point2D e = target.worldToLocal(end);
        Bounds b = target.getLayoutBounds();
        IntersectionResultEx i = IntersectLineRectangle.intersectLineRectangleEx(
                new java.awt.geom.Point2D.Double(s.getX(), s.getY()),
                new java.awt.geom.Point2D.Double(e.getX(), e.getY()),
                new Rectangle2D.Double(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight()));
        return i.intersections().peekLast();
    }
}
