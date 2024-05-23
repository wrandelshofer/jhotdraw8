/*
 * @(#)IntersectEllipseLineTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
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
public class IntersectEllipseLineTest {


    @TestFactory
    public List<DynamicTest> dynamicTestsLineEllipse() {
        return Arrays.asList(
                dynamicTest("1", () -> testIntersectLineEllipse_5args(
                        new Line(10, 40, 200, 40), new Ellipse(100, 100, 60, 60), new double[]{0.47368421052631576})),
                dynamicTest("1", () -> testIntersectLineEllipse_5args(
                        new Line(10, 40, 200, 40), new Ellipse(100, 100, 50, 60), new double[]{0.47368421052631576}))
        );
    }

    public static void testIntersectLineEllipse_5args(Line a, Ellipse b, double[] expected) {
        Point2D bc = new Point2D.Double(b.getCenterX(), b.getCenterX());
        double brx = b.getRadiusX();
        double bry = b.getRadiusY();
        Point2D a1 = new Point2D.Double(a.getStartX(), a.getStartY());
        Point2D a2 = new Point2D.Double(a.getEndX(), a.getEndY());
        IntersectionResult isec = IntersectEllipseLine.intersectLineEllipse(a1, a2, bc, brx, bry);
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
