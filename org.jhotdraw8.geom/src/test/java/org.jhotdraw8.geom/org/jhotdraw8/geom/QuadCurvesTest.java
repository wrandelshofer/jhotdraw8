package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.geom.shape.SimplePathMetrics;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.awt.geom.QuadCurve2D;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class QuadCurvesTest {
    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsShouldComputeArcLength() {
        return Arrays.asList(
                dynamicTest("nice-curve", () -> shouldComputeArcLength(10, 10, 100, 10, 100, 200, 233.02908392127134)),
                dynamicTest("colinear", () -> shouldComputeArcLength(110, 50, 180, 50, 220, 50, 220 - 110))
        );
    }

    private void shouldComputeArcLength(double x0, double y0, double x1, double y1, double x2, double y2, double expectedArcLength) {
        double[] coords = {x0, y0, x1, y1, x2, y2};

        // check if arc length is computed correctly
        double integratedArcLength = QuadCurves.arcLength(coords, 0, 1, 0.125);
        assertEquals(expectedArcLength, integratedArcLength, 0.125);

        // check if flattened arc length is the same
        var expectedQuadCurve = new QuadCurve2D.Double(x0, y0, x1, y1, x2, y2);
        SimplePathMetrics pm = new SimplePathMetrics(expectedQuadCurve.getPathIterator(null, 0.125));
        assertEquals(pm.arcLength(), integratedArcLength, 0.125);

        // check if inverse arc length is computed correctly
        for (double expectedT = 0; expectedT <= 1.0; expectedT += 0.125) {
            double arcLengthAtExpectedT = QuadCurves.arcLength(coords, 0, expectedT, 0.125);
            double actualT = QuadCurves.invArcLength(coords, 0, arcLengthAtExpectedT, 1e-3);
            assertEquals(expectedT, actualT, 1e-3);
        }

    }
}