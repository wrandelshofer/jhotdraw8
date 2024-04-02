package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.geom.shape.BezierPathBuilder;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class BezierPathTest {
    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsShouldIterate() {
        return Arrays.asList(
                dynamicTest("squircle-with-cubicto", () -> shouldIterate("M200,100 C250,100,300,150,300,200 S250,300,200,300 100,250,100,200 150,100,200,100 Z", null)),
                dynamicTest("squircle-with-quadto", () -> shouldIterate("M200,100 Q300,100,300,200 T200,300 100,200 200,100 Z", null)),
                dynamicTest("single-moveTo", () -> shouldIterate("M0,0", null)),
                dynamicTest("line", () -> shouldIterate("M0,0 1,1", null)),
                dynamicTest("line-moveTo-line", () -> shouldIterate("M0,0 1,1 M2,0 3,1", null)),
                dynamicTest("square-closeWithGap", () -> shouldIterate("M0,0 1,0 1,1 0,1 Z", null)),
                dynamicTest("square-closeWithoutGap", () -> shouldIterate("M0,0 1,0 1,1 0,1 Z", null)),
                dynamicTest("square-closeWithGap-moveTo-square-closeWithGap", () -> shouldIterate("M0,0 1,0 1,1 0,1 Z M2,0 3,0 3,1 2,1 Z", null)),
                dynamicTest("square-closeWithoutGap-moveTo-square-closeWithoutGap", () -> shouldIterate("M0,0 1,0 1,1 0,1 0,0 Z M2,0 3,0 3,1 2,1 2,0 Z", "M0,0 1,0 1,1 0,1 Z M2,0 3,0 3,1 2,1 Z")),
                dynamicTest("quadTo", () -> shouldIterate("M37,195 110,50 Q180,50,220,50 320,40,244,195", null))
        );
    }

    private void shouldIterate(@NonNull String input, @Nullable String expected) throws ParseException {
        if (expected == null) expected = input;
        var path = SvgPaths.buildSvgString(new BezierPathBuilder(), input).build();
        String actual = SvgPaths.awtPathIteratorToDoubleSvgString(path.getPathIterator(null));
        assertEquals(expected, actual);
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsShouldBuildSubPathAtArcLength() {
        return Arrays.asList(
                dynamicTest("diagonal", () -> shouldEvalFirstAndLast("0,0 1,1",
                        new PointAndDerivative(0, 0, 1, 1),
                        new PointAndDerivative(1, 1, -1, -1)
                ))
        );
    }

    private void shouldEvalFirstAndLast(@NonNull String input, @NonNull PointAndDerivative expectedFirst, @NonNull PointAndDerivative expectedLastInReverse) throws ParseException {
        var path = SvgPaths.buildSvgString(new BezierPathBuilder(), input).build();
        var actualFirst = path.evalFirst();
        var actualLastInReverse = path.evalLastInReverse();
        var actualReverseFirst = path.reverse().evalFirst();
        assertEquals(expectedFirst, actualFirst);
        assertEquals(expectedLastInReverse, actualLastInReverse);
        assertEquals(expectedLastInReverse, actualReverseFirst);
    }
}