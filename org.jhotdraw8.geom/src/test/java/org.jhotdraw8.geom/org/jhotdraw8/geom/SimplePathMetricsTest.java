package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class SimplePathMetricsTest {
    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsShouldMeasureArcLength() {
        return Arrays.asList(
                dynamicTest("1", () -> shouldMeasureArcLength("M0,0 1,0", 1.0))

        );
    }

    private void shouldMeasureArcLength(String input, double expected) throws Exception {
        var metrics = SvgPaths.buildFromSvgString(new PathMetricsBuilder(), input).build();
        assertEquals(expected, metrics.getArcLength());

    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsShouldReversePath() {
        return Arrays.asList(
                dynamicTest("diagonal-line", () -> shouldReversePath("M0,0 1,1", "M1,1 0,0")),
                dynamicTest("two-diagonal-lines", () -> shouldReversePath("M0,0 1,1 2,2", "M2,2 1,1 0,0")),
                dynamicTest("rectangle", () -> shouldReversePath("M0,0 1,0 1,1 0,1 0,0", "M0,0 0,1 1,1 1,0 0,0")),
                dynamicTest("rectangle-closed", () -> shouldReversePath("M0,0 1,0 1,1 0,1 0,0Z", "M0,0 0,1 1,1 1,0 0,0 Z")),
                dynamicTest("rectangle-closed-with-gap", () -> shouldReversePath("M0,0 1,0 1,1 0,1Z", "M0,0 0,1 1,1 1,0 0,0 Z"))

        );
    }

    private void shouldReversePath(String input, String expected) throws Exception {
        var metrics = SvgPaths.buildFromSvgString(new PathMetricsBuilder(), input).build();
        var actual = SvgPaths.doubleSvgStringFromAwt(metrics.getReversePathIterator(null));
        assertEquals(expected, actual);

    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsShouldBuildSubPathAtArcLength() {
        return Arrays.asList(
                dynamicTest("horizontal-line-not-cropped", () -> shouldBuildSubPathAtArcLength("M0,0 1,0", 0, 1, "M0,0 1,0")),
                dynamicTest("horizontal-line-cropped-at-end", () -> shouldBuildSubPathAtArcLength("M0,0 1,0", 0, 0.75, "M0,0 0.75,0")),
                dynamicTest("horizontal-line-cropped-at-start", () -> shouldBuildSubPathAtArcLength("M0,0 1,0", 0.25, 1, "M0.25,0 1,0")),
                dynamicTest("horizontal-line-cropped-at-start-and-end", () -> shouldBuildSubPathAtArcLength("M0,0 1,0", 0.25, 0.75, "M0.25,0 0.75,0")),
                dynamicTest("two-horizontal-lines-not-cropped", () -> shouldBuildSubPathAtArcLength("M0,0 1,0 2,0", 0, 2, "M0,0 1,0 2,0")),
                dynamicTest("two-horizontal-lines-cropped-at-end", () -> shouldBuildSubPathAtArcLength("M0,0 1,0 2,0", 0, 1.75, "M0,0 1,0 1.75,0")),
                dynamicTest("two-horizontal-lines-cropped-at-end-of-second-last-element", () -> shouldBuildSubPathAtArcLength("M0,0 1,0 2,0", 0, 1, "M0,0 1,0")),
                dynamicTest("two-horizontal-lines-cropped-at-end-inside-middle-element", () -> shouldBuildSubPathAtArcLength("M0,0 1,0 2,0", 0, 0.75, "M0,0 0.75,0")),
                dynamicTest("two-horizontal-lines-cropped-at-start-inside-middle-element", () -> shouldBuildSubPathAtArcLength("M0,0 1,0 2,0", 0.25, 2, "M0.25,0 1,0 2,0")),
                dynamicTest("two-horizontal-lines-cropped-at-start-of-second-element", () -> shouldBuildSubPathAtArcLength("M0,0 1,0 2,0", 1, 2, "M1,0 2,0")),
                dynamicTest("rectangle", () -> shouldBuildSubPathAtArcLength("M0,0 1,0 1,1 0,1 0,0", 0, 4, "M0,0 1,0 1,1 0,1 0,0")),
                dynamicTest("rectangle-closed", () -> shouldBuildSubPathAtArcLength("M0,0 1,0 1,1 0,1 0,0Z", 0, 4, "M0,0 1,0 1,1 0,1 0,0 Z")),
                dynamicTest("rectangle-closed-with-gap", () -> shouldBuildSubPathAtArcLength("M0,0 1,0 1,1 0,1Z", 0, 4, "M0,0 1,0 1,1 0,1 0,0 Z"))

        );
    }

    private void shouldBuildSubPathAtArcLength(String input, double s0, double s1, String expected) throws Exception {
        var metrics = SvgPaths.buildFromSvgString(new PathMetricsBuilder(), input).build();

        // should buildSubPathAtArcLength
        var actual = SvgPaths.doubleSvgStringFromAwt(metrics.buildSubPathAtArcLength(s0, s1, new AwtPathBuilder()).build());
        assertEquals(expected, actual);

        // should getSubPathIteratorAtArcLength
        actual = SvgPaths.doubleSvgStringFromAwt(metrics.getSubPathIteratorAtArcLength(s0, s1, null));
        assertEquals(expected, actual);

    }

}