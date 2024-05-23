/*
 * @(#)PolynomialTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.collection.primitive.DoubleArrayList;
import org.jhotdraw8.geom.Polynomial;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.sqrt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * PolynomialTest.
 *
 * @author Werner Randelshofer
 */
public class PolynomialTest {


    @TestFactory
    public List<DynamicTest> dynamicTestsGetRoots() {
        return Arrays.asList(
                dynamicTest("1", () -> testGetRoots(
                        new Polynomial(1, -1.61900826446281, 0.882231404958678, -0.163453828290417), new double[]{0.635379615995478})),
                dynamicTest("1", () -> testGetRoots(new Polynomial(false, 2330400.0, -1.49088E7, 3.76128E7, -4.6464E7, 2.3232E7), new double[]{0.405180683762359,
                        0.722769898622671})),
                dynamicTest("1", () -> testGetRoots(new Polynomial(1, 6, -5, -10, -3), new double[]{(-7 - sqrt(37)) / 2, (-7 + sqrt(37)) / 2, (1 - sqrt(5)) / 2, (1 + sqrt(5)) / 2})),
                dynamicTest("1", () -> testGetRoots(new Polynomial(5), new double[]{})),
                dynamicTest("1", () -> testGetRoots(new Polynomial(2, 1), new double[]{-0.5})),
                dynamicTest("1", () -> testGetRoots(new Polynomial(-3, 0, 2, 0, 5), new double[]{1.2909944487358056, -1.2909944487358056}))
        );
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsGetRootsInInterval() {
        return Arrays.asList(
                dynamicTest("1", () -> testGetRootsInInterval(new Polynomial(1, -1.61900826446281, 0.882231404958678, -0.163453828290417), 0.0, 1.0, new double[]{0.635379615995478})),
                dynamicTest("1", () -> testGetRootsInInterval(new Polynomial(5), -5.0, 5.0, new double[]{})),
                dynamicTest("1", () -> testGetRootsInInterval(new Polynomial(2, 1), -5.0, 5.0, new double[]{-0.5})),
                dynamicTest("1", () -> testGetRootsInInterval(new Polynomial(-3, 0, 2, 0, 5), -5.0, 5.0, new double[]{1.2909944487358056, -1.2909944487358056})),
                dynamicTest("1", () -> testGetRootsInInterval(new Polynomial(false, 2330400.0, -1.49088E7, 3.76128E7, -4.6464E7, 2.3232E7), 0.0, 1.0, new double[]{0.405180683762359,
                        0.722769898622671})),
                dynamicTest("1", () -> testGetRootsInInterval(new Polynomial(false, -288000.0, 2330400.0, -7454400.0, 1.25376E7, -1.1616E7, 4646400.0), -5.0, 5.0, new double[]{0.327910033575923,
                        0.838094098688656,
                        0.5}))
        );
    }

    public static void testGetRoots(Polynomial instance, double[] expected) {
        Arrays.sort(expected);
        double[] actual = instance.getRoots();
        Arrays.sort(actual);
        Arrays.sort(actual);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(actual[i], expected[i], 1e-6, "root #" + i);
        }
    }

    public static void testGetRootsInInterval(Polynomial instance, double from, double to, double[] expected) {
        Arrays.sort(expected);
        DoubleArrayList actual = instance.getRootsInInterval(from, to);
        actual.sort();
        for (int i = 0; i < expected.length; i++) {
            assertEquals(actual.get(i), expected[i], 1e-6, "root #" + i);
        }
    }


}
