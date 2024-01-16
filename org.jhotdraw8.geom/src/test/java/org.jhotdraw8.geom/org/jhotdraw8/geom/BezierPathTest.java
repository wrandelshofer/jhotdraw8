package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
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
    public @NonNull List<DynamicTest> dynamicTestsShouldBuildSubPathAtArcLength() {
        return Arrays.asList(
                dynamicTest("diagonal", () -> shouldEvalFirstAndLast("0,0 1,1",
                        new PointAndDerivative(0, 0, 1, 1),
                        new PointAndDerivative(1, 1, -1, -1)
                ))
        );
    }

    private void shouldEvalFirstAndLast(@NonNull String input, @NonNull PointAndDerivative expectedFirst, @NonNull PointAndDerivative expectedLastInReverse) throws ParseException {
        var path = SvgPaths.buildFromSvgString(new BezierPathBuilder(), input).build();
        var actualFirst = path.evalFirst();
        var actualLastInReverse = path.evalLastInReverse();
        var actualReverseFirst = path.reverse().evalFirst();
        assertEquals(expectedFirst, actualFirst);
        assertEquals(expectedLastInReverse, actualLastInReverse);
        assertEquals(expectedLastInReverse, actualReverseFirst);
    }
}