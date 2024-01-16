package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class QuadCurvesTest {
    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsShouldBuildSubPathAtArcLength() {
        return Arrays.asList(
                dynamicTest("colinear", () -> shouldComputeArcLength(110, 50, 180, 50, 220, 50, 220 - 110))
        );
    }

    private void shouldComputeArcLength(double x0, double y0, double x1, double y1, double x2, double y2, double expectedArcLength) {
        double actualArcLength = QuadCurves.arcLength(new double[]{x0, y0, x1, y1, x2, y2}, 0);
        assertEquals(expectedArcLength, actualArcLength);
    }
}