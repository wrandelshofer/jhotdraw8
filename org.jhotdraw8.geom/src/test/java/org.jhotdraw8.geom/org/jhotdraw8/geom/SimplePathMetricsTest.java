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
                dynamicTest("horizontal-line", () -> shouldMeasureArcLength("M0,0 1,0", 1.0)),
                dynamicTest("degenerated-smoothQuadTo", () -> shouldMeasureArcLength("M36.16707689026681,195.13717962388827 Q65,75,100,75 320,75,250,75 T223.30609011754328,195.29469477095358", 475.24631578037315))

        );
    }

    private void shouldMeasureArcLength(String input, double expected) throws Exception {
        var metrics = SvgPaths.buildFromSvgString(new PathMetricsBuilder(), input).build();
        assertEquals(expected, metrics.arcLength());

        var flattenedMetrics = new SimplePathMetrics(SvgPaths.buildFromSvgString(new AwtPathBuilder(), input).build().getPathIterator(null, 0.125));
        assertEquals(expected, flattenedMetrics.arcLength(), 1.5);
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
        assertEquals(metrics.arcLength(), reversedMetrics.arcLength(), "should have identical arc length");

    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsShouldBuildSubPathAtArcLength() {
        return Arrays.asList(
                dynamicTest("quadTo-moveTo-lineTo-cut-end-before-moveTo", () -> shouldIterateSubPath("M435,195 Q450,110,490,110 M530,110 592,195", 0, 100, "M435,195 Q448.20371138917096,120.17896879469775,480.77831105040013,111.2189577143933")),
                dynamicTest("quadTo-moveTo-lineTo-cut-end-after-moveTo", () -> shouldIterateSubPath("M435,195 Q450,110,490,110 M530,110 592,195", 0, 120, "M435,195 Q450,110,490,110 M530,110 536.2831377179671,118.613979129471")),
                dynamicTest("quadTo-moveTo-lineTo-cut-start-inside-quadTo", () -> shouldIterateSubPath("M435,195 Q450,110,490,110 M530,110 592,195", 100, 215, "M480.77831105040013,111.2189577143933 Q485.2098970377893,110,490,110 M530,110 592,195")),
                dynamicTest("quadTo-moveTo-2*lineTo-cut-start-inside-quadTo", () -> shouldIterateSubPath("M435,195 Q450,110,490,110 M530,110 592,195 600,195", 100, 215 + 8, "M480.77831105040013,111.2189577143933 Q485.2098970377893,110,490,110 M530,110 592,195 600,195")),
                dynamicTest("quadTo-3*lineTo-cut-start-inside-quadTo", () -> shouldIterateSubPath("M435,195 Q450,110,490,110 L530,110 592,195 600,195", 100, 300, "M480.77831105040013,111.2189577143933 Q485.2098970377893,110,490,110 L530,110 592,195 600,195")),
                dynamicTest("quadTo-moveTo-lineTo-cut-start-after-moveTo", () -> shouldIterateSubPath("M435,195 Q450,110,490,110 M530,110 592,195", 120, 215, "M536.2831377179671,118.613979129471 592,195")),
                dynamicTest("quadTo-complicated-1-no-cut", () -> shouldIterateSubPath("M37,195 110,50 Q180,50,220,50 320,40,244,195", 0, Integer.MAX_VALUE, "M37,195 110,50 Q180,50,220,50 320,40,244,195")),
                dynamicTest("quadTo-complicated-1-cut-to-single-point", () -> shouldIterateSubPath("M37,195 110,50 Q180,50,220,50 320,40,244,195", 7, 7, "M40.14773113166062,188.74765734122204 40.14773113166062,188.74765734122204")),
                dynamicTest("quadTo-complicated-2-cut-to-single-point", () -> shouldIterateSubPath("M37,195 110,50 Q180,60,220,60 320,50,244,195", 7, 7, "M40.14773113166062,188.74765734122204 40.14773113166062,188.74765734122204")),
                dynamicTest("quadTo-complicated-1", () -> shouldIterateSubPath("M37,195 110,50 Q180,50,220,50 320,40,244,195", 7, 464.144222523906 - 7, "M40.14773113166062,188.74765734122204 110,50 Q180,50,220,50 317.9610285106768,40.20389714893232,247.0260663404481,188.74778556121353")),
                dynamicTest("quadTo-complicated-2", () -> shouldIterateSubPath("M37,195 110,50 Q180,60,220,60 320,50,244,195", 7, 455.87069527285587 - 7, "M40.14773113166062,188.74765734122204 110,50 Q180,60,220,60 317.8497029105922,50.215029708940776,247.18707309062006,188.83580699309448")),
                dynamicTest("horizontal-line-not-cropped", () -> shouldIterateSubPath("M0,0 1,0", 0, 1, "M0,0 1,0")),
                dynamicTest("horizontal-line-cropped-at-end", () -> shouldIterateSubPath("M0,0 1,0", 0, 0.75, "M0,0 0.75,0")),
                dynamicTest("horizontal-line-cropped-at-start", () -> shouldIterateSubPath("M0,0 1,0", 0.25, 1, "M0.25,0 1,0")),
                dynamicTest("horizontal-line-cropped-at-start-and-end", () -> shouldIterateSubPath("M0,0 1,0", 0.25, 0.75, "M0.25,0 0.75,0")),
                dynamicTest("two-horizontal-lines-not-cropped", () -> shouldIterateSubPath("M0,0 1,0 2,0", 0, 2, "M0,0 1,0 2,0")),
                dynamicTest("two-horizontal-lines-cropped-at-end", () -> shouldIterateSubPath("M0,0 1,0 2,0", 0, 1.75, "M0,0 1,0 1.75,0")),
                dynamicTest("two-horizontal-lines-cropped-at-end-of-second-last-element", () -> shouldIterateSubPath("M0,0 1,0 2,0", 0, 1, "M0,0 1,0")),
                dynamicTest("two-horizontal-lines-cropped-at-end-inside-middle-element", () -> shouldIterateSubPath("M0,0 1,0 2,0", 0, 0.75, "M0,0 0.75,0")),
                dynamicTest("two-horizontal-lines-cropped-at-start-inside-first-element", () -> shouldIterateSubPath("M0,0 1,0 2,0", 0.25, 2, "M0.25,0 1,0 2,0")),
                dynamicTest("two-horizontal-lines-cropped-at-start-of-second-element", () -> shouldIterateSubPath("M0,0 1,0 2,0", 1, 2, "M1,0 2,0")),
                dynamicTest("many-horizontal-lines-cropped-at-third-start-segment", () -> shouldIterateSubPath("M1680,225 1685,225 1690,225 1700,225, 1705,225 1748,225", 7, 68 - 7, "M1687,225 1690,225 1700,225 1705,225 1741,225")),
                dynamicTest("rectangle", () -> shouldIterateSubPath("M0,0 1,0 1,1 0,1 0,0", 0, 4, "M0,0 1,0 1,1 0,1 0,0")),
                dynamicTest("rectangle-closed", () -> shouldIterateSubPath("M0,0 1,0 1,1 0,1 0,0Z", 0, 4, "M0,0 1,0 1,1 0,1 0,0 Z")),
                dynamicTest("rectangle-closed-with-gap", () -> shouldIterateSubPath("M0,0 1,0 1,1 0,1Z", 0, 4, "M0,0 1,0 1,1 0,1 0,0 Z"))
        );
    }

    private void shouldIterateSubPath(@NonNull String input, double s0, double s1, @NonNull String expected) throws Exception {
        var metrics = SvgPaths.buildFromSvgString(new PathMetricsBuilder(), input).build();

        // should getSubPathIteratorAtArcLength
        var actual = SvgPaths.doubleSvgStringFromAwt(metrics.getSubPathIteratorAtArcLength(s0, s1, null));
        assertEquals(expected, actual);

        // sub path should have expected length
        SimplePathMetrics subMetrics = new SimplePathMetrics(metrics.getSubPathIteratorAtArcLength(s0, s1, null));
        assertEquals(Math.min(metrics.arcLength(), s1) - Math.max(0, s0), subMetrics.arcLength(), 0.125);
    }

}