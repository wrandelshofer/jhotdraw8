/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.awt.color.ColorSpace;
import java.util.Arrays;
import java.util.List;


public class LinearRgbColorSpaceTest {
    @TestFactory
    public @NonNull List<DynamicTest> shouldConvertToRgb() {
        return Arrays.asList(
                DynamicTest.dynamicTest("black", () -> doShouldConvertToRgb(0, 0, 0)),
                DynamicTest.dynamicTest("white", () -> doShouldConvertToRgb(1, 1, 1))
        );
    }

    private void doShouldConvertToRgb(float lr, float lg, float lb) {
        ColorSpace reference = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        NamedColorSpaceAdapter instance = new NamedColorSpaceAdapter("test", reference);
        float[] lrgb = {lr, lg, lb};
        float[] expected = reference.toRGB(lrgb);
        float[] actual = instance.toRGB(lrgb);
        Assertions.assertArrayEquals(expected, actual, Arrays.toString(expected) + " == " + Arrays.toString(actual));
    }

    @TestFactory
    public @NonNull List<DynamicTest> shouldConvertFromRgb() {
        return Arrays.asList(
                DynamicTest.dynamicTest("black", () -> doShouldConvertFromRgb(0, 0, 0)),
                DynamicTest.dynamicTest("white", () -> doShouldConvertFromRgb(1, 1, 1))
        );
    }

    private void doShouldConvertFromRgb(float sr, float sg, float sb) {
        ColorSpace reference = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        NamedColorSpaceAdapter instance = new NamedColorSpaceAdapter("test", reference);
        float[] srgb = {sr, sg, sb};
        float[] expected = reference.fromRGB(srgb);
        float[] actual = instance.fromRGB(srgb);
        Assertions.assertArrayEquals(expected, actual, Arrays.toString(expected) + " == " + Arrays.toString(actual));
    }

}