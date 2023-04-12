/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.linalg.Matrix3;
import org.jhotdraw8.color.linalg.Matrix3Double;
import org.junit.jupiter.api.Test;

import java.awt.color.ColorSpace;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class LinearSrgbColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected @NonNull ParametricLinearRgbColorSpace getInstance() {
        return (ParametricLinearRgbColorSpace) new SrgbColorSpace().getLinearColorSpace();
    }

    @Test
    public void shouldHaveExpectedMatrix() {
        ParametricLinearRgbColorSpace instance = getInstance();
        Matrix3 actual = instance.getToXyzMatrix();
        Matrix3Double expected = new Matrix3Double(0.4124, 0.3576, 0.1805, 0.2126, 0.7152, 0.0722, 0.0193, 0.1192, 0.9505);
        assertTrue(expected.equals(actual, 1e-4));
    }

    @Test
    public void fromRgbShouldBeEqualToJava() {
        ColorSpace reference = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        NamedColorSpace cs = getInstance();
        float[] rgbf = new float[3];
        float[] actualComponentf = new float[cs.getNumComponents()];
        int failures = 0;
        for (int rgb = 0; rgb < (1 << 24); rgb++) {
            RgbBitConverters.rgb24ToRgbFloat(rgb, rgbf);
            cs.fromRGB(rgbf, actualComponentf);
            float[] expectedComponentF = reference.fromRGB(rgbf);
            try {
                assertArrayEquals(expectedComponentF, actualComponentf, 1e-3f);
            } catch (AssertionError e) {
                if (failures < 10) {
                    String message =
                            "\ninitial rgbf: " + Arrays.toString(rgbf)
                                    + "\nexpectedComponentf: " + Arrays.toString(expectedComponentF)
                                    + "\n  actualComponentf: " + Arrays.toString(actualComponentf)
                                    + "\n";
                    System.out.println(message);
                }
                failures++;
            }
        }
        assertTrue(failures < 1, "too many failures=" + failures);
    }

    @Test
    public void toXYZShouldBeEqualToJava() {
        ColorSpace reference = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);

        NamedColorSpace cs = getInstance();
        float[] rgbf = new float[3];
        float[] actualXYZf = new float[cs.getNumComponents()];
        int failures = 0;
        for (int rgb = 0; rgb < (1 << 24); rgb++) {
            cs.toCIEXYZ(rgbf, actualXYZf);
            float[] expectedXYZ = reference.toCIEXYZ(rgbf);
            try {
                assertArrayEquals(expectedXYZ, actualXYZf, 1e-6f);
            } catch (AssertionError e) {
                if (failures < 10) {
                    String message =
                            "\ninitial rgbf: " + Arrays.toString(rgbf)
                                    + "\nexpectedXYZ: " + Arrays.toString(expectedXYZ)
                                    + "\n  actualXYZf: " + Arrays.toString(actualXYZf)
                                    + "\n";
                    System.out.println(message);
                }
                failures++;
            }
        }
        assertTrue(failures < 1, "too many failures=" + failures);
    }
}