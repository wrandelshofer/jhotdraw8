/*
 * @(#)PointAndTangentBuilderTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import org.junit.jupiter.api.Test;

import java.awt.geom.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PointAndTangentBuilderTest {
    @Test
    public void testPointAndTangentAtStraightLine() {
        Line2D.Double line = new Line2D.Double(0, 0, 1, 0);
        PointAndDerivativeBuilder instance = new PointAndDerivativeBuilder(line.getPathIterator(null), 0.125);

        double actualLength = instance.getLength();
        assertEquals(1.0, actualLength);

        PointAndDerivative actualStartPAndD = instance.getPointAndDerivativeAt(0);
        assertEquals(new Point2D.Double(0, 0), actualStartPAndD.getPoint(Point2D.Double::new));
        assertEquals(new Point2D.Double(1, 0), actualStartPAndD.getDerivative(Point2D.Double::new));

        PointAndDerivative actualEndPAndD = instance.getPointAndDerivativeAt(1.0);
        assertEquals(new Point2D.Double(1, 0), actualEndPAndD.getPoint(Point2D.Double::new));
        assertEquals(new Point2D.Double(1, 0), actualEndPAndD.getDerivative(Point2D.Double::new));

        PointAndDerivative actualMiddlePAndD = instance.getPointAndDerivativeAt(0.5);
        assertEquals(new Point2D.Double(0.5, 0), actualMiddlePAndD.getPoint(Point2D.Double::new));
        assertEquals(new Point2D.Double(1, 0), actualMiddlePAndD.getDerivative(Point2D.Double::new));

    }

    @Test
    public void testPointAndTangentAtQuadCurve() {
        QuadCurve2D.Double quadCurve = new QuadCurve2D.Double(0, 0, 1, 0, 1, 1);
        PointAndDerivativeBuilder instance = new PointAndDerivativeBuilder(quadCurve.getPathIterator(null), 0.125);
        double actualLength = instance.getLength();
        assertEquals(1.612752463338847, actualLength);

        PointAndDerivative actualStartPAndD = instance.getPointAndDerivativeAt(0);
        assertEquals(new Point2D.Double(0, 0), actualStartPAndD.getPoint(Point2D.Double::new));
        assertEquals(new Point2D.Double(1, 0), actualStartPAndD.getDerivative(Point2D.Double::new));

        PointAndDerivative actualEndPAndD = instance.getPointAndDerivativeAt(1.0);
        assertEquals(new Point2D.Double(1, 1), actualEndPAndD.getPoint(Point2D.Double::new));
        assertEquals(new Point2D.Double(0, 1), actualEndPAndD.getDerivative(Point2D.Double::new));

        PointAndDerivative actualMiddlePAndD = instance.getPointAndDerivativeAt(0.5);
        assertEquals(new Point2D.Double(0.75, 0.25), actualMiddlePAndD.getPoint(Point2D.Double::new));
        assertEquals(new Point2D.Double(0.5, 0.5), actualMiddlePAndD.getDerivative(Point2D.Double::new));

    }

    @Test
    public void testPointAndTangentAtCubicCurve() {
        CubicCurve2D.Double cubicCurve = new CubicCurve2D.Double(0, 0, 1, 0, 1, 0, 1, 1);
        PointAndDerivativeBuilder instance = new PointAndDerivativeBuilder(cubicCurve.getPathIterator(null), 0.125);
        double actualLength = instance.getLength();
        assertEquals(1.7677669529663689, actualLength);

        PointAndDerivative actualStartPAndD = instance.getPointAndDerivativeAt(0);
        assertEquals(new Point2D.Double(0, 0), actualStartPAndD.getPoint(Point2D.Double::new));
        assertEquals(new Point2D.Double(1, 0), actualStartPAndD.getDerivative(Point2D.Double::new));

        PointAndDerivative actualEndPAndD = instance.getPointAndDerivativeAt(1.0);
        assertEquals(new Point2D.Double(1, 1), actualEndPAndD.getPoint(Point2D.Double::new));
        assertEquals(new Point2D.Double(0, 1), actualEndPAndD.getDerivative(Point2D.Double::new));

        PointAndDerivative actualMiddlePAndD = instance.getPointAndDerivativeAt(0.5);
        assertEquals(new Point2D.Double(0.875, 0.125), actualMiddlePAndD.getPoint(Point2D.Double::new));
        assertEquals(new Point2D.Double(0.25, 0.25), actualMiddlePAndD.getDerivative(Point2D.Double::new));

    }

    @Test
    public void testPointAndTangentAtSquare() {
        Rectangle2D.Double rectangle = new Rectangle2D.Double(0, 0, 1, 1);
        PointAndDerivativeBuilder instance = new PointAndDerivativeBuilder(rectangle, 0.125);
        double actualLength = instance.getLength();
        assertEquals(4.0, actualLength);

        PointAndDerivative actualStartPAndD = instance.getPointAndDerivativeAt(0);
        assertEquals(new Point2D.Double(0, 0), actualStartPAndD.getPoint(Point2D.Double::new));
        assertEquals(new Point2D.Double(1, 0), actualStartPAndD.getDerivative(Point2D.Double::new));

        PointAndDerivative actualEndPAndD = instance.getPointAndDerivativeAt(1.0);
        assertEquals(new Point2D.Double(0, 0), actualEndPAndD.getPoint(Point2D.Double::new));
        assertEquals(new Point2D.Double(0, -1), actualEndPAndD.getDerivative(Point2D.Double::new));

        PointAndDerivative actualMiddlePAndD = instance.getPointAndDerivativeAt(0.5);
        assertEquals(new Point2D.Double(1, 1), actualMiddlePAndD.getPoint(Point2D.Double::new));
        assertEquals(new Point2D.Double(-1, 0), actualMiddlePAndD.getDerivative(Point2D.Double::new));

    }

}