/*
 * @(#)PlineVertex.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.contour;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.function.QuadConsumer;
import org.jhotdraw8.base.function.TriFunction;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.geom.AABB;
import org.jhotdraw8.geom.Points;
import org.jhotdraw8.geom.Points2D;
import org.jhotdraw8.geom.Rectangles;
import org.jhotdraw8.geom.intersect.IntersectionResult;
import org.jhotdraw8.geom.intersect.IntersectionResultEx;

import java.awt.geom.Point2D;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.jhotdraw8.geom.contour.BulgeConversionFunctions.arcRadiusAndCenter;
import static org.jhotdraw8.geom.contour.Utils.*;

public class PlineVertex implements Cloneable {
    private final double x;
    private final double y;
    private double bulge;

    public PlineVertex(Point2D.Double p, double bulge) {
        this(p.getX(), p.getY(), bulge);
    }

    public PlineVertex(double x, double y) {
        this(x, y, 0.0);
    }

    public PlineVertex(double x, double y, double bulge) {
        this.x = x;
        this.y = y;
        this.bulge = bulge;
    }

    public boolean bulgeIsNeg() {
        return bulge < 0;
    }

    public boolean bulgeIsPos() {
        return bulge > 0;
    }

    public boolean bulgeIsZero() {
        return bulgeIsZero(Utils.realPrecision);
    }

    public boolean bulgeIsZero(double epsilon) {
        return Math.abs(bulge) < epsilon;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double bulge() {
        return bulge;
    }

    public void bulge(double bulge) {
        this.bulge = bulge;
    }

    public Point2D.Double pos() {
        return new Point2D.Double(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlineVertex that = (PlineVertex) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.bulge, bulge) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, bulge);
    }

    /**
     * Computes a fast approximate AABB of a segment described by v1 to v2, bounding box may be larger
     * than the true bounding box for the segment
     */
    static @NonNull AABB createFastApproxBoundingBox(final @NonNull PlineVertex v1, final @NonNull PlineVertex v2) {
        if (v1.bulgeIsZero()) {
            return new AABB(
                    min(v1.getX(), v2.getX()),
                    min(v1.getY(), v2.getY()),
                    max(v1.getX(), v2.getX()),
                    max(v1.getY(), v2.getY()));
        }

        // For arcs we don't compute the actual extents which is slower, instead we create an approximate
        // bounding box from the rectangle formed by extending the chord by the sagitta, NOTE: this
        // approximate bounding box is always equal to or bigger than the true bounding box
        double b = v1.bulge();
        double offsX = b * (v2.getY() - v1.getY()) / 2.0;
        double offsY = -b * (v2.getX() - v1.getX()) / 2.0;

        double pt1X = v1.getX() + offsX;
        double pt2X = v2.getX() + offsX;
        double pt1Y = v1.getY() + offsY;
        double pt2Y = v2.getY() + offsY;

        double endPointXMin, endPointXMax;
        if (v1.getX() < v2.getX()) {
            endPointXMin = v1.getX();
            endPointXMax = v2.getX();
        } else {
            endPointXMin = v2.getX();
            endPointXMax = v1.getX();
        }

        double ptXMin, ptXMax;
        if (pt1X < pt2X) {
            ptXMin = pt1X;
            ptXMax = pt2X;
        } else {
            ptXMin = pt2X;
            ptXMax = pt1X;
        }

        double endPointYMin, endPointYMax;
        if (v1.getY() < v2.getY()) {
            endPointYMin = v1.getY();
            endPointYMax = v2.getY();
        } else {
            endPointYMin = v2.getY();
            endPointYMax = v1.getY();
        }

        double ptYMin, ptYMax;
        if (pt1Y < pt2Y) {
            ptYMin = pt1Y;
            ptYMax = pt2Y;
        } else {
            ptYMin = pt2Y;
            ptYMax = pt1Y;
        }

        return new AABB(
                min(endPointXMin, ptXMin),
                min(endPointYMin, ptYMin),
                max(endPointXMax, ptXMax),
                max(endPointYMax, ptYMax)
        );
    }

    /**
     * Split the segment defined by v1 to v2 at some point defined along it.
     */
    static SplitResult splitAtPoint(final PlineVertex v1, final PlineVertex v2,
                                    final Point2D.Double point) {
        SplitResult result = new SplitResult();
        if (v1.bulgeIsZero()) {
            result.updatedStart = v1;
            result.splitVertex = new PlineVertex(point, 0.0);
        } else if (Points.almostEqual(v1.pos(), v2.pos(), Utils.realPrecision) ||
                Points.almostEqual(v1.pos(), point, Utils.realPrecision)) {
            result.updatedStart = new PlineVertex(point, 0.0);
            result.splitVertex = new PlineVertex(point, v1.bulge());
        } else if (Points.almostEqual(v2.pos(), point, Utils.realPrecision)) {
            result.updatedStart = v1;
            result.splitVertex = new PlineVertex(v2.pos(), 0.0);
        } else {
            BulgeConversionFunctions.ArcRadiusAndCenter radiusAndCenter = arcRadiusAndCenter(v1, v2);
            Point2D.Double arcCenter = radiusAndCenter.center;
            double a = angle(arcCenter, point);
            double arcStartAngle = angle(arcCenter, v1.pos());
            double theta1 = Utils.deltaAngle(arcStartAngle, a);
            double bulge1 = Math.tan(theta1 / 4.0);
            double arcEndAngle = angle(arcCenter, v2.pos());
            double theta2 = Utils.deltaAngle(a, arcEndAngle);
            double bulge2 = Math.tan(theta2 / 4.0);

            result.updatedStart = new PlineVertex(v1.pos(), bulge1);
            result.splitVertex = new PlineVertex(point, bulge2);
        }

        return result;
    }

    /// Calculate the path length for the segment defined from v1 to v2.
    public static double segLength(PlineVertex v1, PlineVertex v2) {
        if (Points.almostEqual(v1.pos(), v2.pos())) {
            return 0.0;
        }

        if (v1.bulgeIsZero()) {
            return v1.pos().distance(v2.pos());
        }

        BulgeConversionFunctions.ArcRadiusAndCenter arc = arcRadiusAndCenter(v1, v2);
        double startAngle = angle(arc.center, v1.pos());
        double endAngle = angle(arc.center, v2.pos());
        return Math.abs(arc.radius * Utils.deltaAngle(startAngle, endAngle));
    }

    /// Return the mid point along a segment path.
    public static Point2D.Double segMidpoint(PlineVertex v1, PlineVertex v2) {
        if (v1.bulgeIsZero()) {
            return midpoint(v1.pos(), v2.pos());
        }

        BulgeConversionFunctions.ArcRadiusAndCenter arc = arcRadiusAndCenter(v1, v2);
        double a1 = angle(arc.center, v1.pos());
        double a2 = angle(arc.center, v2.pos());
        double angleOffset = Math.abs(Utils.deltaAngle(a1, a2) / 2.0);
        // use arc direction to determine offset sign to robustly handle half circles
        double midAngle = v1.bulgeIsPos() ? a1 + angleOffset : a1 - angleOffset;
        return pointOnCircle(arc.radius, arc.center, midAngle);
    }

    /// Returns the midpoint between p0 and p1.
    public static Point2D.Double midpoint(Point2D.Double p0, Point2D.Double p1) {
        return new Point2D.Double((p0.getX() + p1.getX()) / 2.0, (p0.getY() + p1.getY()) / 2.0);
    }

    /// Computes the point on the circle with radius, center, and polar angle given.
    public static Point2D.Double pointOnCircle(double radius, Point2D.Double center, double angle) {
        return new Point2D.Double(center.getX() + radius * Math.cos(angle),
                center.getY() + radius * Math.sin(angle));
    }

    /// Compute the closest point on a segment defined by v1 to v2 to the point given.

    static Point2D.Double closestPointOnSeg(PlineVertex v1, PlineVertex v2,
                                            Point2D.Double point) {
        if (v1.bulgeIsZero()) {
            return closestPointOnLineSeg(v1.pos(), v2.pos(), point);
        }

        BulgeConversionFunctions.ArcRadiusAndCenter arc = arcRadiusAndCenter(v1, v2);

        if (Points.almostEqual(point, arc.center)) {
            // avoid normalizing zero length vector (point is at center, just return start point)
            return v1.pos();
        }

        if (pointWithinArcSweepAngle(arc.center, v1.pos(), v2.pos(), v1.bulge(), point)) {
            // closest point is on the arc
            Point2D.Double vToPoint = Points2D.normalize(Points2D.subtract(point, arc.center));
            return Points2D.add(Points2D.multiply(vToPoint, arc.radius), arc.center);
        }

        // else closest point is one of the ends
        double dist1 = v1.pos().distanceSq(point);
        double dist2 = v2.pos().distanceSq(point);
        if (dist1 < dist2) {
            return v1.pos();
        }

        return v2.pos();
    }

    IntrPlineSegsResult intrPlineSegs(PlineVertex v1, PlineVertex v2,
                                      PlineVertex u1, PlineVertex u2) {
        IntrPlineSegsResult result = new IntrPlineSegsResult();
        final boolean vIsLine = v1.bulgeIsZero();
        final boolean uIsLine = u1.bulgeIsZero();

        // helper function to process line arc intersect
        QuadConsumer<Point2D.Double, Point2D.Double, PlineVertex, PlineVertex>
                processLineArcIntr = /*[&result]*/(Point2D.Double p0, Point2D.Double p1,
                                                   PlineVertex a1, PlineVertex a2) -> {
            BulgeConversionFunctions.ArcRadiusAndCenter arc = arcRadiusAndCenter(a1, a2);
            IntersectionResult intrResult = ContourIntersections.intrLineSeg2Circle2(p0, p1, arc.radius, arc.center);

            // helper function to test and get point within arc sweep
            Function<Double, OrderedPair<Boolean, Point2D.Double>> pointInSweep = (Double t) -> {
                if (t + Rectangles.REAL_THRESHOLD < 0.0 ||
                        t > 1.0 + Rectangles.REAL_THRESHOLD) {
                    return new OrderedPair<>(false, new Point2D.Double(0, 0));
                }

                Point2D.Double p = pointFromParametric(p0, p1, t);
                boolean withinSweep = pointWithinArcSweepAngle(arc.center, a1.pos(), a2.pos(), a1.bulge(), p);
                return new OrderedPair<>(withinSweep, p);
            };

            if (intrResult.size() == 0) {
                result.intrType = PlineSegIntrType.NoIntersect;
            } else if (intrResult.size() == 1) {
                OrderedPair<Boolean, Point2D.Double> p = pointInSweep.apply(intrResult.getFirst().getArgumentA());
                if (p.first()) {
                    result.intrType = PlineSegIntrType.OneIntersect;
                    result.point1 = p.second();
                } else {
                    result.intrType = PlineSegIntrType.NoIntersect;
                }
            } else {
                assert intrResult.size() == 2 : "shouldn't get here without 2 intersects";
                OrderedPair<Boolean, Point2D.Double> p1_ = pointInSweep.apply(intrResult.getFirst().getArgumentA());
                OrderedPair<Boolean, Point2D.Double> p2_ = pointInSweep.apply(intrResult.getFirst().getArgumentA());

                if (p1_.first() && p2_.first()) {
                    result.intrType = PlineSegIntrType.TwoIntersects;
                    result.point1 = p1_.second();
                    result.point2 = p2_.second();
                } else if (p1_.first()) {
                    result.intrType = PlineSegIntrType.OneIntersect;
                    result.point1 = p1_.second();
                } else if (p2_.first()) {
                    result.intrType = PlineSegIntrType.OneIntersect;
                    result.point1 = p2_.second();
                } else {
                    result.intrType = PlineSegIntrType.NoIntersect;
                }
            }
        };

        if (vIsLine && uIsLine) {
            IntersectionResultEx intrResult = ContourIntersections.intrLineSeg2LineSeg2(v1.pos(), v2.pos(), u1.pos(), u2.pos());
            switch (intrResult.getStatus()) {
            case NO_INTERSECTION_PARALLEL:
                result.intrType = PlineSegIntrType.NoIntersect;
                break;
            case INTERSECTION:
                result.intrType = PlineSegIntrType.OneIntersect;
                result.point1 = intrResult.getFirst();
                break;
            case NO_INTERSECTION:
                result.intrType = PlineSegIntrType.SegmentOverlap;
                // build points from parametric parameters (using second segment as defined by the function)
                result.point1 = pointFromParametric(u1.pos(), u2.pos(), intrResult.getFirst().getArgumentA());
                result.point2 = pointFromParametric(u1.pos(), u2.pos(), intrResult.getFirst().getArgumentB());
                break;
            case NO_INTERSECTION_COINCIDENT:
                result.intrType = PlineSegIntrType.NoIntersect;
                break;
            }

        } else if (vIsLine) {
            processLineArcIntr.accept(v1.pos(), v2.pos(), u1, u2);
        } else if (uIsLine) {
            processLineArcIntr.accept(u1.pos(), u2.pos(), v1, v2);
        } else {
            BulgeConversionFunctions.ArcRadiusAndCenter arc1 = arcRadiusAndCenter(v1, v2);
            BulgeConversionFunctions.ArcRadiusAndCenter arc2 = arcRadiusAndCenter(u1, u2);

            TriFunction<Point2D.Double, Point2D.Double, Double, OrderedPair<Double, Double>> startAndSweepAngle = (Point2D.Double sp, Point2D.Double center, Double bulge) -> {
                double startAngle = Utils.normalizeRadians(angle(center, sp));
                double sweepAngle = 4.0 * Math.atan(bulge);
                return new OrderedPair<>(startAngle, sweepAngle);
            };

            Predicate<Point2D.Double> bothArcsSweepPoint = (Point2D.Double pt) -> {
                return pointWithinArcSweepAngle(arc1.center, v1.pos(), v2.pos(), v1.bulge(), pt) &&
                        pointWithinArcSweepAngle(arc2.center, u1.pos(), u2.pos(), u1.bulge(), pt);
            };

            IntersectionResult intrResult = ContourIntersections.intrCircle2Circle2(arc1.radius, arc1.center, arc2.radius, arc2.center);

            switch (intrResult.getStatus()) {
            case NO_INTERSECTION_INSIDE:
            case NO_INTERSECTION_OUTSIDE:
                result.intrType = PlineSegIntrType.NoIntersect;
                break;
            case INTERSECTION:
                if (intrResult.size() == 1) {
                    if (bothArcsSweepPoint.test(intrResult.getFirst())) {
                        result.intrType = PlineSegIntrType.OneIntersect;
                        result.point1 = intrResult.getFirst();
                    } else {
                        result.intrType = PlineSegIntrType.NoIntersect;
                    }
                } else {
                    assert intrResult.size() == 2 : "there must be 2 intersections";
                    final boolean pt1InSweep = bothArcsSweepPoint.test(intrResult.getFirst());
                    final boolean pt2InSweep = bothArcsSweepPoint.test(intrResult.getLast());
                    if (pt1InSweep && pt2InSweep) {
                        result.intrType = PlineSegIntrType.TwoIntersects;
                        result.point1 = intrResult.getFirst();
                        result.point2 = intrResult.getLast();
                    } else if (pt1InSweep) {
                        result.intrType = PlineSegIntrType.OneIntersect;
                        result.point1 = intrResult.getFirst();
                    } else if (pt2InSweep) {
                        result.intrType = PlineSegIntrType.OneIntersect;
                        result.point1 = intrResult.getLast();
                    } else {
                        result.intrType = PlineSegIntrType.NoIntersect;
                    }
                }
                break;
            case NO_INTERSECTION_COINCIDENT:
                // determine if arcs overlap along their sweep
                // start and sweep angles
                OrderedPair<Double, Double> arc1StartAndSweep = startAndSweepAngle.apply(v1.pos(), arc1.center, v1.bulge());
                // we have the arcs go the same direction to simplify checks
                OrderedPair<Double, Double> arc2StartAndSweep;
                if (v1.bulgeIsNeg() == u1.bulgeIsNeg()) {
                    arc2StartAndSweep = startAndSweepAngle.apply(u1.pos(), arc2.center, u1.bulge());
                } else {
                    arc2StartAndSweep = startAndSweepAngle.apply(u2.pos(), arc2.center, -u1.bulge());
                }
                // end angles (start + sweep)
                double arc1End = arc1StartAndSweep.first() + arc1StartAndSweep.second();
                double arc2End = arc2StartAndSweep.first() + arc2StartAndSweep.second();

                if (Points.almostEqual(arc1StartAndSweep.first(), arc2End)) {
                    // only end points touch at start of arc1
                    result.intrType = PlineSegIntrType.OneIntersect;
                    result.point1 = v1.pos();
                } else if (Points.almostEqual(arc2StartAndSweep.first(), arc1End)) {
                    // only end points touch at start of arc2
                    result.intrType = PlineSegIntrType.OneIntersect;
                    result.point1 = u1.pos();
                } else {
                    final boolean arc2StartsInArc1Sweep = Utils.angleIsWithinSweep(
                            arc1StartAndSweep.first(), arc1StartAndSweep.second(), arc2StartAndSweep.first());
                    final boolean arc2EndsInArc1Sweep =
                            Utils.angleIsWithinSweep(arc1StartAndSweep.first(), arc1StartAndSweep.second(), arc2End);
                    if (arc2StartsInArc1Sweep && arc2EndsInArc1Sweep) {
                        // arc2 is fully overlapped by arc1
                        result.intrType = PlineSegIntrType.ArcOverlap;
                        result.point1 = u1.pos();
                        result.point2 = u2.pos();
                    } else if (arc2StartsInArc1Sweep) {
                        // overlap from arc2 start to arc1 end
                        result.intrType = PlineSegIntrType.ArcOverlap;
                        result.point1 = u1.pos();
                        result.point2 = v2.pos();
                    } else if (arc2EndsInArc1Sweep) {
                        // overlap from arc1 start to arc2 end
                        result.intrType = PlineSegIntrType.ArcOverlap;
                        result.point1 = v1.pos();
                        result.point2 = u2.pos();
                    } else {
                        final boolean arc1StartsInArc2Sweep = Utils.angleIsWithinSweep(
                                arc2StartAndSweep.first(), arc2StartAndSweep.second(), arc1StartAndSweep.first());
                        if (arc1StartsInArc2Sweep) {
                            result.intrType = PlineSegIntrType.ArcOverlap;
                            result.point1 = v1.pos();
                            result.point2 = v2.pos();
                        } else {
                            result.intrType = PlineSegIntrType.NoIntersect;
                        }
                    }
                }

                break;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return "PlineVertex{" +
                "x=" + x +
                ", y=" + y +
                ", bulge=" + bulge +
                '}';
    }

    @Override
    protected PlineVertex clone() {
        try {
            return (PlineVertex) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
