package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.awt.geom.CubicCurve2D;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class CubicCurvesTest {
    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsShouldComputeArcLength() {
        return Arrays.asList(
                dynamicTest("nice-curve", () -> shouldComputeArcLength(10, 10, 100, 10, 100, 200, 90, 100, 188.31346554689188)),
                dynamicTest("colinear", () -> shouldComputeArcLength(10, 10, 20, 10, 30, 10, 40, 10, 30))
        );
    }

    private void shouldComputeArcLength(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, double expectedArcLength) {
        double[] coords = {x0, y0, x1, y1, x2, y2, x3, y3};

        // check if arc length is computed correctly
        double actualArcLength = CubicCurves.arcLength(coords, 0, 1e-7);
        assertEquals(expectedArcLength, actualArcLength);

        // check if flattened arc length is the same
        var expectedCubicCurve = new CubicCurve2D.Double(x0, y0, x1, y1, x2, y2, x3, y3);
        SimplePathMetrics pm = new SimplePathMetrics(expectedCubicCurve.getPathIterator(null, 0.125));
        assertEquals(pm.arcLength(), actualArcLength, 0.125);

        // check if inverse arc length is computed correctly
        for (double expectedT = 0; expectedT <= 1.0; expectedT += 0.125) {
            double arcLengthAtExpectedT = CubicCurves.arcLength(coords, 0, expectedT, 0.125);
            double actualT = CubicCurves.invArcLength(coords, 0, arcLengthAtExpectedT, 1e-3);
            assertEquals(expectedT, actualT, 1e-3);
        }
    }
}