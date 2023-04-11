/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.tmp.LinearSrgbColorSpace;
import org.jhotdraw8.color.tmp.NamedColorSpace;
import org.junit.jupiter.api.Test;

import java.awt.color.ColorSpace;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class LinearSrgbColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected @NonNull NamedColorSpace getInstance() {
        return LinearSrgbColorSpace.getInstance();
    }

    @Test
    public void fromRgbShouldBeEqualToJava() {
        ColorSpace reference = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        NamedColorSpace cs = getInstance();
        float[] rgbf = new float[3];
        float[] actualComponentf = new float[cs.getNumComponents()];
        int failures = 0;
        for (int rgb = 0; rgb < (1 << 24); rgb++) {
            cs.fromRgb24(rgb, rgbf, actualComponentf);
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