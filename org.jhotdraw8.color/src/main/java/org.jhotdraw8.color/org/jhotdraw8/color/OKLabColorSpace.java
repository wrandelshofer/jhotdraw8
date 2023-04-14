/* @(#)CIELABColorSpace.java
 * Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;

import java.awt.color.ColorSpace;

/**
 * The OKLAB Color Space
 * <p>
 * References:
 * <dl>
 *     <dt>Börn Ottosson, A perceptual color space for image processing, Converting from linear sRGB to Oklab/dt>
 *     <dd><a href="https://bottosson.github.io/posts/oklab/#converting-from-linear-srgb-to-oklab">github.io</a></dd>
 * </dl>
 *
 * @author Werner Randelshofer

 */
public class OKLabColorSpace extends AbstractNamedColorSpace {
    private static final long serialVersionUID = 1L;
    private static @NonNull
    final NamedColorSpace linearSrgb = new SrgbColorSpace().getLinearColorSpace();

    public OKLabColorSpace() {
        super(ColorSpace.TYPE_Lab, 3);

    }


    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] colorvalue) {
        return fromLinearRGB(linearSrgb.fromCIEXYZ(xyz, colorvalue), colorvalue);
    }

    public float[] fromLinearRGB(float[] rgb, float[] colorvalue) {
        double cr = rgb[0];
        double cg = rgb[1];
        double cb = rgb[2];

        double l = 0.4122214708 * cr + 0.5363325363 * cg + 0.0514459929 * cb;
        double m = 0.2119034982 * cr + 0.6806995451 * cg + 0.1073969566 * cb;
        double s = 0.0883024619 * cr + 0.2817188376 * cg + 0.6299787005 * cb;

        double l_ = Math.cbrt(l);
        double m_ = Math.cbrt(m);
        double s_ = Math.cbrt(s);

        colorvalue[0] = (float) (0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_);
        colorvalue[1] = (float) (1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_);
        colorvalue[2] = (float) (0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_);
        return colorvalue;
    }

    @Override
    public float[] fromRGB(float[] srgbvalue, float[] colorvalue) {
        return fromLinearRGB(toLinear(srgbvalue, colorvalue), colorvalue);
    }

    protected float[] fromLinear(float linear[], float corrected[]) {
        corrected[0] = SrgbColorSpace.fromLinear(linear[0]);
        corrected[1] = SrgbColorSpace.fromLinear(linear[1]);
        corrected[2] = SrgbColorSpace.fromLinear(linear[2]);
        return corrected;
    }


    protected float[] toLinear(float corrected[], float linear[]) {
        linear[0] = SrgbColorSpace.toLinear(corrected[0]);
        linear[1] = SrgbColorSpace.toLinear(corrected[1]);
        linear[2] = SrgbColorSpace.toLinear(corrected[2]);
        return linear;
    }

    @Override
    public float getMaxValue(int component) {
        switch (component) {
            case 0:
                return 1f;
            case 1:
            case 2:
                return 0.4f;
        }
        throw new IllegalArgumentException("Illegal component:" + component);
    }

    @Override
    public float getMinValue(int component) {
        switch (component) {
            case 0:
                return 0f;
            case 1:
            case 2:
                return -0.4f;
        }
        throw new IllegalArgumentException("Illegal component:" + component);
    }

    @Override
    public String getName() {
        return "OKLAB";
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return linearSrgb.toCIEXYZ(toLinearRGB(colorvalue, xyz), xyz);
    }

    protected float[] toLinearRGB(float[] colorvalue, float[] rgb) {
        double cL = colorvalue[0];
        double ca = colorvalue[1];
        double cb = colorvalue[2];

        double l_ = cL + 0.3963377774 * ca + 0.2158037573 * cb;
        double m_ = cL - 0.1055613458 * ca - 0.0638541728 * cb;
        double s_ = cL - 0.0894841775 * ca - 1.2914855480 * cb;

        double l = l_ * l_ * l_;
        double m = m_ * m_ * m_;
        double s = s_ * s_ * s_;

        rgb[0] = (float) (4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s);
        rgb[1] = (float) (-1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s);
        rgb[2] = (float) (-0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s);
        return rgb;
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        return fromLinear(toLinearRGB(colorvalue, rgb), rgb);
    }

}
