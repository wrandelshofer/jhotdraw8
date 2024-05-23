/*
 * @(#)AbstractNamedColorSpace.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;


import java.awt.color.ColorSpace;
import java.io.Serial;
import java.util.Objects;

/**
 * {@code AbstractNamedColorSpace}.
 *
 * @author Werner Randelshofer

 */
public abstract class AbstractNamedColorSpace extends ColorSpace implements NamedColorSpace {
    @Serial
    private static final long serialVersionUID = 1L;

    public AbstractNamedColorSpace(int type, int numcomponents) {
        super(type, numcomponents);
    }

    @Override
    public float[] fromCIEXYZ(float[] colorvalue) {
        return fromCIEXYZ(colorvalue, new float[getNumComponents()]);
    }

    @Override
    public float[] toRGB(float[] colorvalue) {
        return toRGB(colorvalue, new float[3]);
    }

    @Override
    public float[] fromRGB(float[] rgb) {
        return fromRGB(rgb, new float[getNumComponents()]);
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        return toCIEXYZ(colorvalue, new float[3]);
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return new SrgbColorSpace().toCIEXYZ(toRGB(colorvalue, xyz), xyz);
    }

    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] colorvalue) {
        return fromRGB(new SrgbColorSpace().fromCIEXYZ(xyz, colorvalue), colorvalue);
    }


    @Override
    public String toString() {
        return getName();
    }

    /**
     * Lazy-initialized names of components in the color space.
     */
    private transient volatile String[] compName;

    public String getName(int component) {
        Objects.checkIndex(component, getNumComponents());
        if (compName == null) {
            compName = switch (getType()) {
                case NamedColorSpace.TYPE_LCH -> new String[]{"Lightness", "Chroma", "Hue"};
                default -> {
                    String[] tmp = new String[getNumComponents()];
                    for (int i = 0; i < tmp.length; i++) {
                        tmp[i] = super.getName(i);
                    }
                    yield tmp;
                }
            };
        }
        return compName[component];
    }
}
