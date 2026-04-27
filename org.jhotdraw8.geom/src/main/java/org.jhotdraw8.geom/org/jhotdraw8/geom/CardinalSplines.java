/*
 * @(#)CardinalSplines.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

/// Provides conversion functions from cardinal splines (c-splines) to
/// bezier curves.
public class CardinalSplines {
    /// Don't let anyone instantiate this class.
    private CardinalSplines() {
    }

    /// Converts a cardinal spline into a bezier curve.
    ///
    /// Cardinal spline from B to E with control points A, B, E, F and
    /// tension parameter c:
    /// <pre>
    ///       B         F
    ///     /   \     /
    ///   /      \   /
    /// A          E
    /// </pre>
    /// The tangent vectors of the cardinal spline are:
    ///
    ///   - Tb = c * (E - A)
    ///   - Te = c * (F - B)
    ///
    ///
    /// Cubic bezier curve from B to E with control points B, C, D, E.
    /// <pre>
    ///       B----C   F
    ///     /   \     /
    ///   /      \   /
    /// A     D----E
    /// </pre>
    /// The tangent vectors of the cubic bezier curve are:
    ///
    ///   - Tb = 3 * (C - B)
    ///   - Te = 3 * (E - D)
    ///
    /// Therefore, we can compute C and D from the cardinal spline as follows:
    ///
    ///   - C = B + (E - A) * c / 3
    ///   - D = E - (F - B) * c/ 3
    ///
    /// References:
    /// <dl>
    ///     <dt>Stackoverflow. Converting a Cubic Bezier Curves into a Cardinal Spline and back.
    ///     Copyright MBo. CC BY-SA 4.0 license.</dt>
    ///     <dd><a href="https://stackoverflow.com/questions/31274246/converting-a-cubic-bezier-curves-into-a-cardinal-spline-and-back">stackoverflow.com</a></dd>
    /// </dl>
    ///
    /// @param p the points of the cardinal spline
    /// @param c the tension of the cardinal spline
    /// @return the cubic bezier curves (first point is `moveTo`,
    /// subsequent triples of points are `curveTo`s.
    public static Point2D[] cardinalSplineToBezier(Point2D[] p, double c) {
        List<Point2D> b = new ArrayList<>();
        for (int k = 1, n = p.length; k < n - 2; k++) {
            Point2D A = p[k - 1];
            Point2D B = p[k];
            Point2D E = p[k + 1];
            Point2D F = p[k + 2];
            Point2D C = B.add(E.subtract(A).multiply(c / 3));
            Point2D D = E.subtract(F.subtract(B).multiply(c / 3));

            if (b.isEmpty()) {
                b.add(B);
            }
            b.add(C);
            b.add(D);
            b.add(E);
        }

        return b.toArray(new Point2D[0]);
    }

    public static Point2D[] cardinalSplineToBezier(double[] p, double c) {
        Point2D[] points = new Point2D[p.length / 2];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point2D(p[i * 2], p[i * 2 + 1]);
        }
        return cardinalSplineToBezier(points, c);
    }

    public static Point2D[] cardinalSplineToBezier(List<Double> p, double c) {
        Point2D[] points = new Point2D[p.size() / 2];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point2D(p.get(i * 2), p.get(i * 2 + 1));
        }
        return cardinalSplineToBezier(points, c);
    }
}
