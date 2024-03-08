/*
 * @(#)BezierFit.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import javafx.geometry.Point2D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.primitive.IntArrayList;
import org.jhotdraw8.geom.intersect.IntersectLinePoint;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * References:
 * <pre>
 *     GraphicsGems.c
 *     2d and 3d Vector C Library
 *     by Andrew Glassner
 *     from "Graphics Gems", Academic Press, 1990
 * </pre>
 */
public class PolylineToCubicCurve {

    /**
     * Prevent instance creation.
     */
    private PolylineToCubicCurve() {
    }

    public static void main(String[] args) {
        ArrayList<Point2D> d = new ArrayList<>();
        d.add(new Point2D(0, 0));
        d.add(new Point2D(5, 1));
        d.add(new Point2D(10, 0));
        d.add(new Point2D(10, 10));
        d.add(new Point2D(0, 10));
        d.add(new Point2D(0, 0));
        ArrayList<ArrayList<Point2D>> segments = (splitAtCorners(d, 45 / 180d * Math.PI, 2d));
        for (ArrayList<Point2D> seg : segments) {
            for (int i = 0; i < 2; i++) {
                seg = reduceNoise(seg, 0.8);
            }
        }
    }

    /**
     * Fits a bezier path to the specified list of digitized points.
     * <p>
     * This is a convenience method for calling {@link #fitBezierPath}
     *
     * @param builder         the builder for the bezier path
     * @param digitizedPoints digited points.
     * @param error           the maximal allowed error between the bezier path and the
     *                        digitized points.
     */
    public static void fitBezierPath(@NonNull PathBuilder<?> builder, Point2D[] digitizedPoints, double error) {
        fitBezierPath(builder, Arrays.asList(digitizedPoints), error);
    }

    /**
     * Fits a bezier path to the specified list of digitized points.
     *
     * @param builder         the builder for the bezier path
     * @param digitizedPoints digited points.
     * @param error           the maximal allowed error between the bezier path and the
     *                        digitized points.
     */
    public static void fitBezierPath(@NonNull PathBuilder<?> builder, @NonNull List<Point2D> digitizedPoints, double error) {
        // Split into segments at corners
        ArrayList<ArrayList<Point2D>> segments;
        segments = splitAtCorners(digitizedPoints, 77 / 180d * Math.PI, error * error);

        // Clean up the data in the segments
        for (int i = 0, n = segments.size(); i < n; i++) {
            ArrayList<Point2D> seg = segments.get(i);
            seg = removeClosePoints(seg, error * 2);
            seg = reduceNoise(seg, 0.8);

            segments.set(i, seg);
        }

        // Quickly deal with empty dataset
        boolean isEmpty = false;
        for (ArrayList<Point2D> seg : segments) {
            if (seg.isEmpty()) {
                isEmpty = false;
                break;
            }
        }
        if (!isEmpty) {
            // Process each segment of digitized points
            double errorSquared = error * error;
            boolean first = true;
            for (ArrayList<Point2D> seg : segments) {
                switch (seg.size()) {
                case 0:
                    break;
                case 1:
                    if (first) {
                        builder.moveTo(seg.getFirst().getX(), seg.getFirst().getY());
                        first = false;
                    } else {
                        builder.lineTo(seg.getFirst().getX(), seg.getFirst().getY());
                    }
                    break;
                case 2:
                    if (first) {
                        builder.moveTo(seg.getFirst().getX(), seg.getFirst().getY());
                        first = false;
                    }
                    builder.lineTo(seg.get(1).getX(), seg.get(1).getY());
                    break;
                default:
                    if (first) {
                        builder.moveTo(seg.getFirst().getX(), seg.getFirst().getY());
                        first = false;
                    }
                    /*  Unit tangent vectors at endpoints */
                    Point2D tHat1;
                    Point2D tHat2;
                    tHat1 = computeLeftTangent(seg, 0);
                    tHat2 = computeRightTangent(seg, seg.size() - 1);

                    fitCubic(builder, seg, 0, seg.size() - 1, tHat1, tHat2, errorSquared);
                    break;
                }
            }
        }
    }

    /**
     * Fits a bezier path to the specified list of digitized points.
     * <p>
     * This is a convenience method for calling {@link #fitBezierPath}.
     *
     * @param builder         the builder for the bezier path
     * @param digitizedPoints digited points.
     * @param error           the maximal allowed error between the bezier path and the
     *                        digitized points.
     */
    public static void fitBezierPath(@NonNull PathBuilder<?> builder, @NonNull BezierPath digitizedPoints, double error) {
        List<Point2D> d = new ArrayList<>();
        for (BezierNode n : digitizedPoints) {
            d.add(new Point2D(n.pointX(), n.pointY()));
        }
        fitBezierPath(builder, d, error);
    }

    /**
     * Removes points which are closer together than the specified minimal
     * distance.
     * <p>
     * The minimal distance should be chosen dependent on the size and
     * resolution of the display device, and on the sampling rate. A good value
     * for mouse input on a display with 100% Zoom factor is 2.
     * <p>
     * The purpose of this method, is to remove points, which add no additional
     * information about the shape of the curve from the list of digitized
     * points.
     * <p>
     * The cleaned up set of digitized points gives better results, when used as
     * input for method {@link #splitAtCorners}.
     *
     * @param digitizedPoints Digitized points
     * @param minDistance     minimal distance between two points. If minDistance is
     *                        0, this method only removes sequences of coincident points.
     * @return Digitized points with a minimal distance.
     */
    public static @NonNull ArrayList<Point2D> removeClosePoints(@NonNull List<Point2D> digitizedPoints, double minDistance) {
        if (minDistance == 0) {
            return removeCoincidentPoints(digitizedPoints);
        } else {

            double squaredDistance = minDistance * minDistance;
            ArrayList<Point2D> cleaned = new ArrayList<>();
            if (!digitizedPoints.isEmpty()) {
                Point2D prev = digitizedPoints.getFirst();
                cleaned.add(prev);
                for (Point2D p : digitizedPoints) {
                    if (v2SquaredDistanceBetween2Points(prev, p) > squaredDistance) {
                        cleaned.add(p);
                        prev = p;
                    }
                }
                if (!prev.equals(digitizedPoints.getLast())) {
                    cleaned.set(cleaned.size() - 1, digitizedPoints.getLast());
                }
            }
            return cleaned;
        }
    }

    /**
     * Removes sequences of coincident points.
     * <p>
     * The purpose of this method, is to clean up a list of digitized points for
     * later processing using method {@link #splitAtCorners}.
     * <p>
     * Use this method only, if you know that the digitized points contain no
     * quantization errors - which is never the case, unless you want to debug
     * the curve fitting algorithm of this class.
     *
     * @param digitizedPoints Digitized points
     * @return Digitized points without subsequent duplicates.
     */
    private static @NonNull ArrayList<Point2D> removeCoincidentPoints(@NonNull List<Point2D> digitizedPoints) {
        ArrayList<Point2D> cleaned = new ArrayList<>();
        if (!digitizedPoints.isEmpty()) {
            Point2D prev = digitizedPoints.getFirst();
            cleaned.add(prev);
            for (Point2D p : digitizedPoints) {
                if (!prev.equals(p)) {
                    cleaned.add(p);
                    prev = p;
                }
            }
        }
        return cleaned;
    }

    /**
     * Splits the digitized points into multiple segments at each corner point.
     * <p>
     * Corner points are both contained as the last point of a segment and the
     * first point of a subsequent segment.
     *
     * @param digitizedPoints Digitized points
     * @param maxAngle        maximal angle in radians between the current point and
     *                        its predecessor and successor up to which the point does not break the
     *                        digitized list into segments. Recommended value 44° = 44 * 180d / Math.PI
     * @param minDistance     the minimal distance
     * @return Segments of digitized points, each segment having less than
     * maximal angle between points.
     */
    public static @NonNull ArrayList<ArrayList<Point2D>> splitAtCorners(@NonNull List<Point2D> digitizedPoints, double maxAngle, double minDistance) {
        IntArrayList cornerIndices = findCorners(digitizedPoints, maxAngle, minDistance);
        ArrayList<ArrayList<Point2D>> segments = new ArrayList<>(cornerIndices.size() + 1);

        if (cornerIndices.isEmpty()) {
            segments.add(new ArrayList<>(digitizedPoints));
        } else {
            segments.add(new ArrayList<>(digitizedPoints.subList(0, cornerIndices.getAsInt(0) + 1)));
            for (int i = 1; i < cornerIndices.size(); i++) {
                segments.add(new ArrayList<>(digitizedPoints.subList(cornerIndices.getAsInt(i - 1), cornerIndices.getAsInt(i) + 1)));
            }
            segments.add(new ArrayList<>(digitizedPoints.subList(cornerIndices.getAsInt(cornerIndices.size() - 1), digitizedPoints.size())));
        }

        return segments;
    }

    /**
     * Finds corners in the provided point list, and returns their indices.
     *
     * @param digitizedPoints List of digitized points.
     * @param minAngle        Minimal angle for corner points
     * @param minDistance     Minimal distance between a point and adjacent points
     *                        for corner detection
     * @return list of corner indices.
     */
    public static @NonNull IntArrayList findCorners(@NonNull List<Point2D> digitizedPoints, double minAngle, double minDistance) {
        IntArrayList cornerIndices = new IntArrayList();

        double squaredDistance = minDistance * minDistance;

        int previousCorner = -1;
        double previousCornerAngle = 0;

        for (int i = 1, n = digitizedPoints.size(); i < n - 1; i++) {
            Point2D p = digitizedPoints.get(i);

            // search for a preceding point for corner detection
            Point2D prev = null;
            boolean intersectsPreviousCorner = false;
            for (int j = i - 1; j >= 0; j--) {
                if (j == previousCorner || v2SquaredDistanceBetween2Points(digitizedPoints.get(j), p) >= squaredDistance) {
                    prev = digitizedPoints.get(j);
                    intersectsPreviousCorner = j < previousCorner;
                    break;
                }
            }
            if (prev == null) {
                continue;
            }

            // search for a succeeding point for corner detection
            Point2D next = null;
            for (int j = i + 1; j < n; j++) {
                if (v2SquaredDistanceBetween2Points(digitizedPoints.get(j), p) >= squaredDistance) {
                    next = digitizedPoints.get(j);
                    break;
                }
            }
            if (next == null) {
                continue;
            }

            double aPrev = Angles.atan2(prev.getY() - p.getY(), prev.getX() - p.getX());
            double aNext = Angles.atan2(next.getY() - p.getY(), next.getX() - p.getX());
            double angle = Math.abs(aPrev - aNext);
            if (angle < Math.PI - minAngle || angle > Math.PI + minAngle) {
                if (intersectsPreviousCorner) {
                    cornerIndices.setAsInt(cornerIndices.size() - 1, i);
                } else {
                    cornerIndices.addAsInt(i);
                }
                previousCorner = i;
                previousCornerAngle = angle;
            }
        }
        return cornerIndices;
    }

    /**
     * Reduces noise from the digitized points, by applying an approximation of
     * a gaussian filter to the data.
     * <p>
     * The filter does the following for each point P, with weight 0.5:
     * <p>
     * x[i] = 0.5*x[i] + 0.25*x[i-1] + 0.25*x[i+1]; y[i] = 0.5*y[i] +
     * 0.25*y[i-1] + 0.25*y[i+1];
     *
     * @param digitizedPoints Digitized points
     * @param weight          Weight of the current point
     * @return Digitized points with reduced noise.
     */
    public static @NonNull ArrayList<Point2D> reduceNoise(@NonNull List<Point2D> digitizedPoints, double weight) {
        ArrayList<Point2D> cleaned = new ArrayList<>();
        if (!digitizedPoints.isEmpty()) {
            Point2D prev = digitizedPoints.getFirst();
            cleaned.add(prev);
            double pnWeight = (1d - weight) / 2d; // weight of previous and next
            for (int i = 1, n = digitizedPoints.size() - 1; i < n; i++) {
                Point2D cur = digitizedPoints.get(i);
                Point2D next = digitizedPoints.get(i + 1);
                cleaned.add(new Point2D(
                        cur.getX() * weight + pnWeight * prev.getX() + pnWeight * next.getX(),
                        cur.getY() * weight + pnWeight * prev.getY() + pnWeight * next.getY()));
                prev = cur;
            }
            if (digitizedPoints.size() > 1) {
                cleaned.add(digitizedPoints.getLast());
            }
        }
        return cleaned;
    }

    /**
     * Fit one or multiple subsequent cubic bezier curves to a (sub)set of
     * digitized points. The digitized points represent a smooth curve without
     * corners.
     *
     * @param d            Array of digitized points. Must not contain subsequent
     *                     coincident points.
     * @param first        Indice of first point in d.
     * @param last         Indice of last point in d.
     * @param tHat1        Unit tangent vectors at start point.
     * @param tHat2        Unit tangent vector at end point.
     * @param errorSquared User-defined errorSquared squared.
     * @param builder      Path to which the bezier curve segments are added.
     */
    private static void fitCubic(@NonNull PathBuilder<?> builder, @NonNull ArrayList<Point2D> d, int first, int last,
                                 Point2D tHat1, Point2D tHat2,
                                 double errorSquared) {

        Point2D[] bezCurve;
        /*Control points of fitted BezierFit curve*/
        double[] u;
        /*  Parameter values for point  */
        double maxError;
        /*  Maximum fitting errorSquared	 */
        int[] splitPoint = new int[1];
        /*  Point to split point set at.
        This is an array of size one, because we need it as an input/output parameter.
         */
        int nPts;
        /*  Number of points in subset  */
        double iterationError;
        /* Error below which you try iterating  */
        int maxIterations = 4;
        /*  Max times to try iterating  */
        Point2D tHatCenter;
        /* Unit tangent vector at splitPoint */
        int i;

        iterationError = errorSquared * errorSquared;
        nPts = last - first + 1;

        /*  Use heuristic if region only has two points in it */
        if (nPts == 2) {
            double dist = v2DistanceBetween2Points(d.get(last), d.get(first)) / 3.0;

            Point2D bezCurve0 = d.get(first);
            Point2D bezCurve3 = d.get(last);
            Point2D bezCurve1 = v2Add(bezCurve0, tHat1 = v2Scale(tHat1, dist));
            Point2D bezCurve2 = v2Add(bezCurve3, tHat2 = v2Scale(tHat2, dist));

            builder.curveTo(
                    bezCurve1.getX(),
                    bezCurve1.getY(),
                    bezCurve2.getX(),
                    bezCurve2.getY(),
                    bezCurve3.getX(),
                    bezCurve3.getY()
            );
            return;
        }

        /*  Parameterize points, and attempt to fit curve */
        u = chordLengthParameterize(d, first, last);
        bezCurve = generateBezier(d, first, last, u, tHat1, tHat2);

        /*  Find max deviation of points to fitted curve */
        maxError = computeMaxError(d, first, last, bezCurve, u, splitPoint);
        if (maxError < errorSquared) {
            addCurveTo(builder, bezCurve, errorSquared, first == 0 && last == d.size() - 1);
            return;
        }


        /*  If errorSquared not too large, try some reparameterization  */
        /*  and iteration */
        if (maxError < iterationError) {
            double[] uPrime;
            /*  Improved parameter values */
            for (i = 0; i < maxIterations; i++) {
                uPrime = reparameterize(d, first, last, u, bezCurve);
                bezCurve = generateBezier(d, first, last, uPrime, tHat1, tHat2);
                maxError = computeMaxError(d, first, last, bezCurve, uPrime, splitPoint);
                if (maxError < errorSquared) {
                    addCurveTo(builder, bezCurve, errorSquared, first == 0 && last == d.size() - 1);
                    return;
                }
                u = uPrime;
            }
        }

        /* Fitting failed -- split at max errorSquared point and fit recursively */
        tHatCenter = computeCenterTangent(d, splitPoint[0]);
        if (first < splitPoint[0]) {
            fitCubic(builder, d, first, splitPoint[0], tHat1, tHatCenter, errorSquared);
        } else {
            builder.lineTo(d.get(splitPoint[0]).getX(), d.get(splitPoint[0]).getY());
            //   System.err.println("Can't split any further " + first + ".." + splitPoint[0]);
        }
        tHatCenter = v2Negate(tHatCenter);
        if (splitPoint[0] < last) {
            fitCubic(builder, d, splitPoint[0], last, tHatCenter, tHat2, errorSquared);
        } else {
            builder.lineTo(d.get(last).getX(), d.get(last).getY());
            //  System.err.println("Can't split any further " + splitPoint[0] + ".." + last);
        }
    }

    /**
     * Adds the curve to the bezier path.
     *
     * @param bezCurve
     * @param builder
     */
    private static void addCurveTo(@NonNull PathBuilder<?> builder, Point2D[] bezCurve, double errorSquared, boolean connectsCorners) {
        java.awt.geom.Point2D.Double lastNode = builder.getLastPoint();
        double error = Math.sqrt(errorSquared);
        if (connectsCorners && IntersectLinePoint.lineContainsPoint(lastNode.getX(), lastNode.getY(), bezCurve[3].getX(), bezCurve[3].getY(), bezCurve[1].getX(), bezCurve[1].getY(), error)
                && IntersectLinePoint.lineContainsPoint(lastNode.getX(), lastNode.getY(), bezCurve[3].getX(), bezCurve[3].getY(), bezCurve[2].getX(), bezCurve[2].getY(), error)) {
            builder.lineTo(
                    bezCurve[3].getX(), bezCurve[3].getY());

        } else {
            builder.curveTo(
                    bezCurve[1].getX(), bezCurve[1].getY(),
                    bezCurve[2].getX(), bezCurve[2].getY(),
                    bezCurve[3].getX(), bezCurve[3].getY());
        }
    }

    /**
     * Approximate unit tangents at "left" endpoint of digitized curve.
     *
     * @param d   Digitized points.
     * @param end Index to "left" end of region.
     */
    private static Point2D computeLeftTangent(@NonNull ArrayList<Point2D> d, int end) {
        Point2D tHat1;
        tHat1 = v2SubII(d.get(end + 1), d.get(end));
        tHat1 = v2Normalize(tHat1);
        return tHat1;
    }

    /**
     * Approximate unit tangents at "right" endpoint of digitized curve.
     *
     * @param d   Digitized points.
     * @param end Index to "right" end of region.
     */
    private static Point2D computeRightTangent(@NonNull ArrayList<Point2D> d, int end) {
        Point2D tHat2;
        tHat2 = v2SubII(d.get(end - 1), d.get(end));
        tHat2 = v2Normalize(tHat2);
        return tHat2;
    }

    /**
     * Approximate unit tangents at "center" of digitized curve.
     *
     * @param d      Digitized points.
     * @param center Index to "center" end of region.
     */
    private static Point2D computeCenterTangent(@NonNull ArrayList<Point2D> d, int center) {
        Point2D V1, V2,
                tHatCenter;

        V1 = v2SubII(d.get(center - 1), d.get(center));
        V2 = v2SubII(d.get(center), d.get(center + 1));
        tHatCenter = new Point2D((V1.getX() + V2.getX()) / 2.0,
                (V1.getY() + V2.getY()) / 2.0);
        tHatCenter = v2Normalize(tHatCenter);
        return tHatCenter;
    }

    /**
     * Assign parameter values to digitized points using relative distances
     * between points.
     *
     * @param d     Digitized points.
     * @param first Indice of first point of region in d.
     * @param last  Indice of last point of region in d.
     */
    private static double[] chordLengthParameterize(@NonNull ArrayList<Point2D> d, int first, int last) {
        int i;
        double[] u;
        /*  Parameterization		*/

        u = new double[last - first + 1];

        u[0] = 0.0;
        for (i = first + 1; i <= last; i++) {
            u[i - first] = u[i - first - 1]
                    + v2DistanceBetween2Points(d.get(i), d.get(i - 1));
        }

        for (i = first + 1; i <= last; i++) {
            u[i - first] = u[i - first] / u[last - first];
        }

        return (u);
    }

    /**
     * Given set of points and their parameterization, try to find a better
     * parameterization.
     *
     * @param d        Array of digitized points.
     * @param first    Indice of first point of region in d.
     * @param last     Indice of last point of region in d.
     * @param u        Current parameter values.
     * @param bezCurve Current fitted curve.
     */
    private static double[] reparameterize(@NonNull ArrayList<Point2D> d, int first, int last, double[] u, Point2D[] bezCurve) {
        int nPts = last - first + 1;
        int i;
        double[] uPrime;
        /*  New parameter values	*/

        uPrime = new double[nPts];
        for (i = first; i <= last; i++) {
            uPrime[i - first] = newtonRaphsonRootFind(bezCurve, d.get(i), u[i - first]);
        }
        return (uPrime);
    }

    /**
     * Use Newton-Raphson iteration to find better root.
     *
     * @param Q Current fitted bezier curve.
     * @param P Digitized point.
     * @param u Parameter value for P.
     */
    private static double newtonRaphsonRootFind(Point2D[] Q, @NonNull Point2D P, double u) {
        double numerator, denominator;
        Point2D[] Q1 = new Point2D[3], Q2 = new Point2D[2];
        /*  Q' and Q''			*/
        Point2D Q_u, Q1_u, Q2_u;
        /*u evaluated at Q, Q', & Q''	*/
        double uPrime;
        /*  Improved u	*/
        int i;

        /* Compute Q(u)	*/
        Q_u = bezierII(3, Q, u);

        /* Generate control points for Q'	*/
        for (i = 0; i <= 2; i++) {
            Q1[i] = new Point2D(
                    (Q[i + 1].getX() - Q[i].getX()) * 3.0,
                    (Q[i + 1].getY() - Q[i].getY()) * 3.0);
        }

        /* Generate control points for Q'' */
        for (i = 0; i <= 1; i++) {
            Q2[i] = new Point2D(
                    (Q1[i + 1].getX() - Q1[i].getX()) * 2.0,
                    (Q1[i + 1].getY() - Q1[i].getY()) * 2.0);
        }

        /* Compute Q'(u) and Q''(u)	*/
        Q1_u = bezierII(2, Q1, u);
        Q2_u = bezierII(1, Q2, u);

        /* Compute f(u)/f'(u) */
        numerator = (Q_u.getX() - P.getX()) * (Q1_u.getX()) + (Q_u.getY() - P.getY()) * (Q1_u.getY());
        denominator = (Q1_u.getX()) * (Q1_u.getX()) + (Q1_u.getY()) * (Q1_u.getY())
                + (Q_u.getX() - P.getX()) * (Q2_u.getX()) + (Q_u.getY() - P.getY()) * (Q2_u.getY());

        /* u = u - f(u)/f'(u) */
        uPrime = u - (numerator / denominator);
        return (uPrime);
    }

    /**
     * Find the maximum squared distance of digitized points to fitted curve.
     *
     * @param d          Digitized points.
     * @param first      Indice of first point of region in d.
     * @param last       Indice of last point of region in d.
     * @param bezCurve   Fitted BezierFit curve
     * @param u          Parameterization of points
     * @param splitPoint Point of maximum error (input/output parameter, must be
     *                   an array of 1)
     */
    private static double computeMaxError(@NonNull ArrayList<Point2D> d, int first, int last, Point2D[] bezCurve, double[] u, int[] splitPoint) {
        int i;
        double maxDist;
        /*  Maximum error */
        double dist;
        /*  Current error */
        Point2D P;
        /*  Point on curve */
        Point2D v;
        /*  Vector from point to curve */

        splitPoint[0] = (last - first + 1) / 2;
        maxDist = 0.0;
        for (i = first + 1; i < last; i++) {
            P = bezierII(3, bezCurve, u[i - first]);
            v = v2SubII(P, d.get(i));
            dist = v2SquaredLength(v);
            if (dist >= maxDist) {
                maxDist = dist;
                splitPoint[0] = i;
            }
        }
        return (maxDist);
    }

    /**
     * Use least-squares method to find BezierFit control points for region.
     *
     * @param d      Array of digitized points.
     * @param first  Indice of first point in d.
     * @param last   Indice of last point in d.
     * @param uPrime Parameter values for region .
     * @param tHat1  Unit tangent vectors at start point.
     * @param tHat2  Unit tangent vector at end point.
     * @return A cubic bezier curve consisting of 4 control points.
     */
    private static Point2D[] generateBezier(@NonNull ArrayList<Point2D> d, int first, int last, double[] uPrime, @NonNull Point2D tHat1, @NonNull Point2D tHat2) {
        Point2D[] bezCurve;

        bezCurve = new Point2D[4];
        for (int i = 0; i < bezCurve.length; i++) {
        }


        /*  Use the Wu/Barsky heuristic*/
        double dist = v2DistanceBetween2Points(d.get(last), d.get(first)) / 3.0;

        bezCurve[0] = d.get(first);
        bezCurve[3] = d.get(last);
        bezCurve[1] = v2Add(bezCurve[0], v2Scale(tHat1, dist));
        bezCurve[2] = v2Add(bezCurve[3], v2Scale(tHat2, dist));
        return (bezCurve);
    }

    /**
     * Evaluate a BezierFit curve at a particular parameter value.
     *
     * @param degree The degree of the bezier curve.
     * @param V      Array of control points.
     * @param t      Parametric value to find point for.
     */
    private static Point2D bezierII(int degree, Point2D[] V, double t) {
        int i, j;
        Point2D q;
        /* Point on curve at parameter t	*/
        Point2D[] vTemp;
        /* Local copy of control points		*/

        /* Copy array	*/
        vTemp = new Point2D[degree + 1];
        for (i = 0; i <= degree; i++) {
            vTemp[i] = V[i];
        }

        /* Triangle computation	*/
        for (i = 1; i <= degree; i++) {
            for (j = 0; j <= degree - i; j++) {
                vTemp[j] = new Point2D((1.0 - t) * vTemp[j].getX() + t * vTemp[j + 1].getX(),
                        (1.0 - t) * vTemp[j].getY() + t * vTemp[j + 1].getY());
            }
        }

        q = vTemp[0];
        return q;
    }


    /**
     * Return the distance between two points
     */
    private static double v2DistanceBetween2Points(@NonNull Point2D a, @NonNull Point2D b) {
        return Math.sqrt(v2SquaredDistanceBetween2Points(a, b));
    }

    /**
     * Return the distance between two points
     */
    private static double v2SquaredDistanceBetween2Points(@NonNull Point2D a, @NonNull Point2D b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return (dx * dx) + (dy * dy);
    }

    /**
     * Scales the input vector to the new length and returns it.
     */
    private static @NonNull Point2D v2Scale(@NonNull Point2D v, double newlen) {
        double len = v2Length(v);
        double x = v.getX(), y = v.getY();
        if (len != 0.0) {
            x *= newlen / len;
            y *= newlen / len;
        }

        return new Point2D(x, y);
    }


    /**
     * Returns length of input vector.
     */
    private static double v2Length(@NonNull Point2D a) {
        return Math.sqrt(v2SquaredLength(a));
    }

    /**
     * Returns squared length of input vector.
     */
    private static double v2SquaredLength(@NonNull Point2D a) {
        return (a.getX() * a.getX()) + (a.getY() * a.getY());
    }

    /**
     * Return vector sum c = a+b.
     */
    private static @NonNull Point2D v2Add(@NonNull Point2D a, @NonNull Point2D b) {
        return new Point2D(a.getX() + b.getX(),
                a.getY() + b.getY());
    }

    /**
     * Negates the input vector and returns it.
     */
    private static @NonNull Point2D v2Negate(@NonNull Point2D v) {
        return new Point2D(-v.getX(),
                -v.getY());
    }


    /**
     * Normalizes the input vector and returns it.
     */
    private static @NonNull Point2D v2Normalize(@NonNull Point2D v) {
        double len = v2Length(v);
        if (len != 0.0) {
            return new Point2D(v.getX() / len,
                    v.getY() / len);
        }

        return v;
    }

    /**
     * Subtract Vector a from Vector b.
     *
     * @param a Vector a - the value is not changed by this method
     * @param b Vector b - the value is not changed by this method
     * @return Vector a subtracted by Vector v.
     */
    private static @NonNull Point2D v2SubII(@NonNull Point2D a, @NonNull Point2D b) {
        Point2D c = new Point2D(a.getX() - b.getX(),
                a.getY() - b.getY());
        return (c);
    }
}
