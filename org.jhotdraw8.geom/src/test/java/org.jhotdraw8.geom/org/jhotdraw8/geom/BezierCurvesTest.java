/*
 * @(#)BezierCurvesTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.primitive.DoubleArrayList;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class BezierCurvesTest {
    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsCharacterization() {
        return Arrays.asList(
                dynamicTest("plainCurve", () -> testCurveCharacteristics(new double[]{160, 200, 75, 240, 260, 300, 260, 80},
                        BezierCurveCharacteristics.Characteristics.PLAIN_CURVE)),
                dynamicTest("loopAtT0", () -> testCurveCharacteristics(new double[]{160, 200, 75, 240, 260, 300, 104, 123},
                        BezierCurveCharacteristics.Characteristics.LOOP_AT_T_0)),
                dynamicTest("loopAtT1", () -> testCurveCharacteristics(new double[]{228, 127, 75, 240, 260, 300, 200, 150},
                        BezierCurveCharacteristics.Characteristics.LOOP_AT_T_1)),
                dynamicTest("loop", () -> testCurveCharacteristics(new double[]{230, 120, 75, 240, 260, 300, 150, 120},
                        BezierCurveCharacteristics.Characteristics.LOOP)),
                dynamicTest("cusp", () -> testCurveCharacteristics(new double[]{230, 120, 166, 223, 260, 300, 150, 120},
                        BezierCurveCharacteristics.Characteristics.CUSP)),
                dynamicTest("doubleInflection", () -> testCurveCharacteristics(new double[]{260, 120, 166, 223, 260, 300, 150, 120},
                        BezierCurveCharacteristics.Characteristics.DOUBLE_INFLECTION)),
                dynamicTest("singleInflection", () -> testCurveCharacteristics(new double[]{260, 120, 220, 190, 260, 300, 150, 120},
                        BezierCurveCharacteristics.Characteristics.SINGLE_INFLECTION)),
                dynamicTest("collinearMonotonicMotion", () -> testCurveCharacteristics(new double[]{0, 0, 10, 0, 20, 0, 30, 0},
                        BezierCurveCharacteristics.Characteristics.COLLINEAR)),
                dynamicTest("collinearOneFold", () -> testCurveCharacteristics(new double[]{0, 0, 20, 0, 30, 0, 10, 0},
                        BezierCurveCharacteristics.Characteristics.COLLINEAR)),
                dynamicTest("collinearTwoFold", () -> testCurveCharacteristics(new double[]{10, 0, 0, 0, 30, 0, 20, 0},
                        BezierCurveCharacteristics.Characteristics.COLLINEAR)),
                dynamicTest("collinearLoop", () -> testCurveCharacteristics(new double[]{10, 0, 30, 0, 0, 0, 20, 0},
                        BezierCurveCharacteristics.Characteristics.COLLINEAR))
        );
    }

    public void testCurveCharacteristics(double[] b, BezierCurveCharacteristics.Characteristics expected) {
        BezierCurveCharacteristics.Characteristics actual = new BezierCurveCharacteristics().characteristics(b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7]);
        assertEquals(expected, actual);
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsInflections() {
        return Arrays.asList(
                dynamicTest("1", () -> testInflections(new double[]{135, 25, 25, 135, 215, 75, 215, 240}, new double[]{0.5059963709191709})),
                dynamicTest("2", () -> testInflections(new double[]{80, 40, 210, 40, 190, 110, 190, 20}, new double[]{0.4746000729159903, 0.8484087766415317,}))
        );
    }

    /**
     * References:
     * <dl>
     *    <dt>Zhiyi Zhang, Min Chen , Xian Zhang, Zepeng Wang.
     *    Analysis of Inflection Points for Planar Cubic Bé́zier Curve</dt>
     *    <dd><a href="https://cie.nwsuaf.edu.cn/docs/20170614173651207557.pdf">cie.nwsuaf.edu.cn</a></dd>
     * </dl>
     */
    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsInflectionsPaper() {
        return Arrays.asList(
                dynamicTest("Fig. 3", () -> testInflections(
                        new double[]{16, 467, 185, 95, 673, 545, 810, 17}, new double[]{0.45659003534192677})),
                dynamicTest("Fig. 4", () -> testInflections(
                        new double[]{859, 676, 13, 422, 781, 12, 266, 425}, new double[]{0.6810755244969373, 0.7052992722985928})),
                dynamicTest("Fig. 5", () -> testInflections(
                        new double[]{872, 686, 11, 423, 779, 13, 220, 376}, new double[]{0.5880709423503425, 0.8868629954101945})),
                dynamicTest("Fig. 6", () -> testInflections(
                        new double[]{819, 566, 43, 18, 826, 18, 25, 533}, new double[]{0.4761686268954743, 0.539295336904044}, 0.507732)),
                dynamicTest("Fig. 7", () -> testInflections(
                        new double[]{884, 576, 135, 14, 678, 14, 14, 566}, new double[]{0.32125653593447834, 0.682645677331897}, 0.501564))
        );
    }

    private void testInflections(double[] b, double[] expected) {
        new BezierCurveCharacteristics();
        DoubleArrayList inflections = BezierCurveCharacteristics.inflectionPoints(b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7]);
        assertArrayEquals(expected, inflections.toArray());
    }

    private void testInflections(double[] b, double[] expected, double expectedSingularPoint) {
        DoubleArrayList inflections = BezierCurveCharacteristics.inflectionPoints(b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7]);
        assertArrayEquals(expected, inflections.toArray());
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsAlign() {
        return Arrays.asList(
                dynamicTest("1", () -> testAlign(new double[]{135, 25, 25, 135, 215, 75, 215, 240},
                        new double[]{0, 0,
                                64.73369529397579, 141.4551119386878,
                                74.75978951459155, -57.54106248353403,
                                229.40139493908922, -1.4210854715202004e-14}
                ))
        );
    }

    private void testAlign(double[] b, double[] expected) {
        double[] actual = new BezierCurveCharacteristics().align(b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7]);
        assertArrayEquals(expected, actual);
    }
}