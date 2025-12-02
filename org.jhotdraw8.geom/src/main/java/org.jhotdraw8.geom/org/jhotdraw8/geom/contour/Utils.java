/*
 * @(#)Utils.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.contour;

import org.jhotdraw8.geom.Angles;
import org.jhotdraw8.geom.Lines;
import org.jhotdraw8.geom.Points2D;
import org.jhotdraw8.geom.Scalars;

import java.awt.geom.Point2D;
import java.util.List;

public class Utils {
    /**
     * absolute threshold to be used for reals in common geometric computation (e.g. to check for
     * singularities).
     */
    public static final double realPrecision = 1e-5;
    /**
     * absolute threshold to be used for joining slices together at end points
     */
    public static final double sliceJoinThreshold = 1e-4;

    public static final double tau = 2.0 * Math.PI;
    // absolute threshold to be used for pruning invalid slices for offset
    public static final double offsetThreshold = 1e-4;

    /**
     * Don't let anyone instantiate this class.
     */
    private Utils() {
    }

    /**
     * Test if a point is within an arc sweep angle region defined by center, start, end, and bulge.
     */
    static boolean pointWithinArcSweepAngle(final Point2D.Double center, final Point2D.Double arcStart,
                                            final Point2D.Double arcEnd, double bulge, final Point2D.Double point) {
        assert Math.abs(bulge) > Scalars.REAL_THRESHOLD : "expected arc";
        assert Math.abs(bulge) <= 1.0 : "bulge should always be between -1 and 1";

        if (bulge > 0.0) {
            return isLeftOrCoincident(center, arcStart, point) &&
                    isRightOrCoincident(center, arcEnd, point);
        }

        return isRightOrCoincident(center, arcStart, point) && isLeftOrCoincident(center, arcEnd, point);
    }

    /**
     * Returns true if point is left or fuzzy coincident with the line pointing in the direction of the
     * vector (p1 - p0).
     */
    static boolean isLeftOrCoincident(final Point2D.Double p0, final Point2D.Double p1,
                                      final Point2D.Double point) {
        return isLeftOrCoincident(p0, p1, point, Scalars.REAL_THRESHOLD);
    }

    static boolean isLeftOrCoincident(final Point2D.Double p0, final Point2D.Double p1,
                                      final Point2D.Double point, double epsilon) {
        return (p1.getX() - p0.getX()) * (point.getY() - p0.getY()) - (p1.getY() - p0.getY()) * (point.getX() - p0.getX()) >
                -epsilon;
    }

    /**
     * Returns true if point is right or fuzzy coincident with the line pointing in the direction of
     * the vector (p1 - p0).
     */
    static boolean isRightOrCoincident(final Point2D.Double p0, final Point2D.Double p1,
                                       final Point2D.Double point) {
        return isRightOrCoincident(p0, p1, point, Scalars.REAL_THRESHOLD);
    }

    static boolean isRightOrCoincident(final Point2D.Double p0, final Point2D.Double p1,
                                       final Point2D.Double point, double epsilon) {
        return (p1.getX() - p0.getX()) * (point.getY() - p0.getY()) - (p1.getY() - p0.getY()) * (point.getX() - p0.getX()) <
                epsilon;
    }


    /**
     * Return the point on the segment going from p0 to p1 at parametric value t.
     */
    public static Point2D.Double pointFromParametric(final Point2D.Double p0, final Point2D.Double p1, double t) {
        return Lines.lerp(p0, p1, t);
        //return Points2D.add(p0,Points2D.multiply(Points2D.subtract(p1,p0),t));
    }

    /**
     * Counter-clockwise angle of the vector going from p0 to p1.
     */
    public static double angle(final Point2D.Double p0, final Point2D.Double p1) {
        return Angles.atan2(p1.getY() - p0.getY(), p1.getX() - p0.getX());
    }

    /**
     * Returns the smaller difference between two angles, result is negative if
     * {@literal angle2 < angle1}.
     */
    public static double deltaAngle(double angle1, double angle2) {
        double diff = normalizeRadians(angle2 - angle1);
        if (diff > Math.PI) {
            diff -= tau;
        }

        return diff;
    }

    /**
     * Normalize radians to be between 0 and 2PI, e.g. -PI/4 becomes 7PI/8 and 5PI becomes PI.
     */
    public static double normalizeRadians(double angle) {
        if (angle >= 0.0 && angle < tau) {
            return angle;
        }

        return angle - Math.floor(angle / tau) * tau;
    }

    /**
     * Normalized perpendicular vector to v (rotating counter clockwise).
     */
    public static Point2D.Double unitPerp(Point2D.Double v) {
        Point2D.Double result = new Point2D.Double(-v.getY(), v.getX());
        return Points2D.normalize(result);
    }

    static <T> int nextWrappingIndex(int index, List<T> container) {
        return index == container.size() - 1 ? 0 : index + 1;

    }

    static <T> int prevWrappingIndex(int index, List<T> container) {
        return index == 0 ? container.size() - 1 : index - 1;

    }

    static boolean angleIsWithinSweep(double startAngle, double sweepAngle, double testAngle) {
        return angleIsWithinSweep(startAngle, sweepAngle, testAngle, Scalars.REAL_THRESHOLD);
    }

    static boolean angleIsWithinSweep(double startAngle, double sweepAngle, double testAngle,
                                      double epsilon) {
        double endAngle = startAngle + sweepAngle;
        if (sweepAngle < 0.0) {
            return angleIsBetween(endAngle, startAngle, testAngle, epsilon);
        }

        return angleIsBetween(startAngle, endAngle, testAngle, epsilon);
    }


    static boolean angleIsBetween(double startAngle, double endAngle, double testAngle,
                                  double epsilon) {
        double endSweep = normalizeRadians(endAngle - startAngle);
        double midSweep = normalizeRadians(testAngle - startAngle);

        return midSweep < endSweep + epsilon;
    }

    /// Returns the closest point that lies on the line segment from p0 to p1 to the point given.

    static Point2D.Double closestPointOnLineSeg(Point2D.Double p0, Point2D.Double p1,
                                                Point2D.Double point) {
        // Dot product used to find angles
        // See: http://geomalgorithms.com/a02-_lines.html
        Point2D.Double v = Points2D.subtract(p1, p0);
        Point2D.Double w = Points2D.subtract(point, p0);
        double c1 = Points2D.dotProduct(w, v);
        if (c1 < Scalars.REAL_THRESHOLD) {
            return p0;
        }

        double c2 = Points2D.dotProduct(v, v);
        if (c2 < c1 + Scalars.REAL_THRESHOLD) {
            return p1;
        }

        double b = c1 / c2;
        return Points2D.add(p0, Points2D.multiply(v, b));
    }
}
