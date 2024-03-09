/*
 * @(#)Arc.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.biarc;

import org.jhotdraw8.annotation.NonNull;

import java.awt.geom.Point2D;

/**
 * Definition of an Arc. It contains redundant information.
 *
 * @param c          Center point.
 * @param r          Radius.
 * @param startAngle Start angle in radian.
 * @param sweepAngle Sweep angle in radian.
 * @param p1         Start point of the arc.
 * @param p2         End point of the arc.
 */
public record Arc(Point2D.@NonNull Double c, double r, double startAngle, double sweepAngle, Point2D.@NonNull Double p1,
                  Point2D.@NonNull Double p2) {

    /**
     * Orientation of the arc.
     */
    public boolean isClockwise() {
        return sweepAngle > 0;
    }

    /**
     * Implements the parametric equation.
     *
     * @param t Parameter of the curve. Must be in [0,1]
     * @return the point at t
     */
    public Point2D.Double pointAt(double t) {
        double x = c.getX() + r * Math.cos(startAngle + t * sweepAngle);
        double y = c.getY() + r * Math.sin(startAngle + t * sweepAngle);
        return new Point2D.Double(x, y);
    }

    public double length() {
        return r * Math.abs(sweepAngle);
    }
}