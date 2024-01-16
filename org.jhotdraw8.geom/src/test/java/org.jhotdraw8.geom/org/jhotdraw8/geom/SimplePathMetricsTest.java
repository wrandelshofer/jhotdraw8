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
                dynamicTest("quadto", () -> shouldReversePath("M0,0 Q1,0,1,1", "M1,1 Q1,0,0,0")),
                dynamicTest("moveto-lineto-quadto", () -> shouldReversePath("M0,0 0,2 Q1,0,1,1", "M1,1 Q1,0,0,2 L0,0")),
                dynamicTest("two-quadtos", () -> shouldReversePath("M0,0 Q1,0,1,1 2,1,2,0", "M2,0 Q2,1,1,1 1,0,0,0")),
                dynamicTest("diagonal-line", () -> shouldReversePath("M0,0 1,1", "M1,1 0,0")),
                dynamicTest("two-diagonal-lines", () -> shouldReversePath("M0,0 1,1 2,2", "M2,2 1,1 0,0")),
                dynamicTest("rectangle", () -> shouldReversePath("M0,0 1,0 1,1 0,1 0,0", "M0,0 0,1 1,1 1,0 0,0")),
                dynamicTest("closed-rectangle", () -> shouldReversePath("M0,0 1,0 1,1 0,1 0,0Z", "M0,0 0,1 1,1 1,0 0,0 Z")),
                dynamicTest("two-closed-rectangles", () -> shouldReversePath("M0,0 1,0 1,1 0,1 0,0Z M2,0 3,0 3,1 2,1 2,0Z", "M2,0 2,1 3,1 3,0 2,0 Z M0,0 0,1 1,1 1,0 0,0 Z")),
                dynamicTest("closed-rectangle-closed-with-gap", () -> shouldReversePath("M0,0 1,0 1,1 0,1Z", "M0,0 0,1 1,1 1,0 0,0 Z")),
                dynamicTest("two-lines", () -> shouldReversePath("M0,0 1,0 M1,1 0,1", "M0,1 1,1 M1,0 0,0")),
                dynamicTest("closed-path-with-moveto", () -> shouldReversePath("M0,0 1,0 M1,1 0,1 1,1Z", "M1,1 0,1 1,1 Z M1,0 0,0"))

        );
    }

    private void shouldReversePath(String input, String expected) throws Exception {
        var metrics = SvgPaths.buildFromSvgString(new PathMetricsBuilder(), input).build();
        PathMetrics reversedMetrics = metrics.reverse();
        var actual = SvgPaths.doubleSvgStringFromAwt(reversedMetrics.getPathIterator(null));
        assertEquals(expected, actual, "should reverse path operations");
        assertEquals(metrics.getArcLength(), reversedMetrics.getArcLength(), "should have identical arc length");

    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsShouldBuildSubPathAtArcLength() {
        return Arrays.asList(
                dynamicTest("quadTo-complicated-1-no-cut", () -> shouldBuildSubPathAtArcLength("M37,195 110,50 Q180,50,220,50 320,40,244,195", 0, Integer.MAX_VALUE, "M37,195 110,50 Q180,50,220,50 320,40,244,195")),
                dynamicTest("quadTo-complicated-1-cut-to-single-point", () -> shouldBuildSubPathAtArcLength("M37,195 110,50 Q180,50,220,50 320,40,244,195", 7, 7, "M40.14773113166062,188.74765734122204 40.14773113166062,188.74765734122204")),
                dynamicTest("quadTo-complicated-2-cut-to-single-point", () -> shouldBuildSubPathAtArcLength("M37,195 110,50 Q180,60,220,60 320,50,244,195", 7, 7, "M40.14773113166062,188.74765734122204 40.14773113166062,188.74765734122204")),
                dynamicTest("quadTo-complicated-1", () -> shouldBuildSubPathAtArcLength("M37,195 110,50 Q180,50,220,50 320,40,244,195", 7, 464.144222523906 - 7, "M40.14773113166062,188.74765734122204 110,50 Q180,50,220,50 317.9623118772454,40.20376881227546,247.0242077038002,188.75167767207336")),
                dynamicTest("quadTo-complicated-2", () -> shouldBuildSubPathAtArcLength("M37,195 110,50 Q180,60,220,60 320,50,244,195", 7, 455.87069527285587 - 7, "M40.14773113166062,188.74765734122204 110,50 Q180,60,220,60 317.84983522276104,50.21501647772389,247.1868819905839,188.83618187883093")),
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