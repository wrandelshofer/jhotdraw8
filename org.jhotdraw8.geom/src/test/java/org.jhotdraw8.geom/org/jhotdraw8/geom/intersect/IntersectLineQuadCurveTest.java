/*
 * @(#)IntersectLineQuadCurveTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import javafx.scene.shape.Line;
import javafx.scene.shape.QuadCurve;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * IntersectionTest.
 *
 * @author Werner Randelshofer
 */
public class IntersectLineQuadCurveTest {

    @TestFactory
    public List<DynamicTest> dynamicTestsIntersectLineQuadraticCurve_5args() {
        return Arrays.asList(
                dynamicTest("1", () -> testIntersectLineQuadCurve(new Line(10, 40, 210, 175), new QuadCurve(125, 200, 250, 225, 275, 100), new double[]{}))
        );
    }

    /**
     * Test of intersectLineBezier2 method, of class Intersection.
     */
    public static void testIntersectLineQuadCurve(Line a, QuadCurve b, double[] expected) {
        Point2D b1 = new Point2D.Double(b.getStartX(), b.getEndX());
        Point2D b2 = new Point2D.Double(b.getControlX(), b.getControlY());
        Point2D b3 = new Point2D.Double(b.getEndX(), b.getEndX());
        Point2D a1 = new Point2D.Double(a.getStartX(), a.getStartY());
        Point2D a2 = new Point2D.Double(a.getEndX(), a.getEndY());

        IntersectionResult isec = IntersectLineQuadCurve.intersectLineQuadCurve(a1, a2, b1, b2, b3);

        double[] actual = new double[isec.intersections().size()];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = isec.getAllArgumentsA().get(i);
        }
        Arrays.sort(actual);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(actual[i], expected[i], 1e-6, "root #" + i);
        }
    }
}
