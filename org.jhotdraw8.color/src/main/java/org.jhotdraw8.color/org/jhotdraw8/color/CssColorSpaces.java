package org.jhotdraw8.color;

import java.util.Map;

/**
 * References:
 * <dl>
 *     <dt>CSS Color Module Level 4. 4. Representing Colors: the <color> type.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-type">w3.org</a></dd>
 *
 *     <dt>CSS Color Module Level 4. 4. Representing Colors: the <color> type.  4.1 The <color> syntax.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-syntax">w3.org</a></dd>
 * </dl>
 */
public class CssColorSpaces {
    /**
     * Don't let anyone instantiate this class.
     */
    private CssColorSpaces() {

    }

    /**
     * Map of CSS color spaces.
     */
    public final static Map<String, NamedColorSpace> COLOR_SPACES;

    static {
        CieXyzColorSpace cieXyzColorSpace = new CieXyzColorSpace();
        COLOR_SPACES = Map.of(
                "srgb", new SrgbColorSpace(),
                "srgb-linear", new LinearSrgbColorSpace(),
                "display-p3", new DisplayP3ColorSpace(),
                "a98-rgb", new A98RgbColorSpace(),
                "prophoto-rgb", new ProPhotoRgbColorSpace(),
                "rec2020", new Rec2020ColorSpace(),
                "xyz", cieXyzColorSpace,
                "xyz-d50", new D50XyzColorSpace(),
                "xyz-d65", cieXyzColorSpace
        );
    }
}
