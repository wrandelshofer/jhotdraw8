/*
 * @(#)OKLABColorSpaceTest.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OKLabColorSpaceTest extends AbstractNamedColorSpaceTest {
    @Override
    protected @NonNull OKLabColorSpace getInstance() {
        return OKLabColorSpace.getInstance();
    }

    /**
     * References:
     * <dl>
     *     <dt>Björn Ottosson, A perceptual color space for image processing</dt>
     *     <dd><a href="https://bottosson.github.io/posts/oklab/">github.io</a></dd>
     * </dl>
     *
     * @param x X value
     * @param y Y value
     * @param z Z value
     * @param l L value
     * @param a a value
     * @param b b value
     */
    @ParameterizedTest
    @CsvSource({
            // X Y Z L a b
            "0.950,1.000,1.089,	1.000,0.000,0.000",
            "1.000,0.000,0.000,	0.450,1.236,-0.019",
            "0.000,1.000,0.000,	0.922,-0.671,0.263",
            "0.000,0.000,1.000,	0.153,-1.415,-0.449"
    })
    public void shouldConvertFromXYZ(float x, float y, float z, float l, float a, float b) {
        OKLabColorSpace instance = OKLabColorSpace.getInstance();
        float[] actualLab = instance.fromCIEXYZ(new float[]{x, y, z});
        assertEquals(l, actualLab[0], 1e-2f, "L");
        assertEquals(a, actualLab[1], 1e-2f, "a");
        assertEquals(b, actualLab[2], 1e-2f, "b");

        float[] actualXYZ = instance.toCIEXYZ(new float[]{l, a, b});
        assertEquals(x, actualXYZ[0], 1e-2f, "X");
        assertEquals(y, actualXYZ[1], 1e-2f, "Y");
        assertEquals(z, actualXYZ[2], 1e-2f, "Z");
    }

    /**
     * References:
     * <dl>
     *     <dt>CSS Color Module Level 4, 9.4. Specifying Oklab and Oklch: the oklab() and oklch() functional notations,
     *     Example 23</dt>
     *     <dd><a href="https://drafts.csswg.org/css-color-4/#ex-oklab-samples">drafts.csswg.org</a></dd>
     * </dl>
     *
     * @param R R value
     * @param G G value
     * @param B B value
     * @param l L value
     * @param a a value
     * @param b b value
     */
    @Disabled("BROKEN Check correctness of CSV table with Björn Ottosson")
    @ParameterizedTest
    @CsvSource({
            // R G B L a b
            "0.4906, 0.1387, 0.159, 0.40101, 0.1147, 0.0453",
            "0.7761, 0.3634, 0.0245, 0.59686, 0.1009, 0.1192",
            "0.6165, 0.5751, 0.0928, 0.65125, -0.0320, 0.1274",
            "0.4073, 0.6512, 0.2235, 0.66016, -0.1084, 0.1114",
            "0.3829, 0.6727, 0.9385, 0.72322, -0.0465, -0.1150",
            "0.502, 0.0, 0.502, 0.421, 0.41,-0.25",
    })
    public void shouldConvertFromRGB(float R, float G, float B, float l, float a, float b) {
        OKLabColorSpace instance = OKLabColorSpace.getInstance();
        float[] actualLab = instance.fromRGB(new float[]{R, G, B});
        assertEquals(l, actualLab[0], 1e-2f, "L");
        assertEquals(a, actualLab[1], 1e-2f, "a");
        assertEquals(b, actualLab[2], 1e-2f, "b");

        float[] actualRGB = instance.toRGB(new float[]{l, a, b});
        assertEquals(R, actualRGB[0], 1e-2f, "R");
        assertEquals(G, actualRGB[1], 1e-2f, "G");
        assertEquals(B, actualRGB[2], 1e-2f, "B");
    }


}