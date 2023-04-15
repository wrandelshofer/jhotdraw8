/* @(#)AbstractNamedColorSpace.java
 * Copyright © The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;

import java.awt.color.ColorSpace;
import java.util.Objects;

/**
 * {@code AbstractNamedColorSpace}.
 *
 * @author Werner Randelshofer

 */
public abstract class AbstractNamedColorSpace extends ColorSpace implements NamedColorSpace {
    private static final long serialVersionUID = 1L;

    public AbstractNamedColorSpace(int type, int numcomponents) {
        super(type, numcomponents);
    }

    @Override
    public float @NonNull [] fromCIEXYZ(float @NonNull [] colorvalue) {
        return fromCIEXYZ(colorvalue, new float[getNumComponents()]);
    }

    @Override
    public float @NonNull [] toRGB(float @NonNull [] colorvalue) {
        return toRGB(colorvalue, new float[3]);
    }

    @Override
    public float @NonNull [] fromRGB(float @NonNull [] rgb) {
        return fromRGB(rgb, new float[getNumComponents()]);
    }

    @Override
    public float @NonNull [] toCIEXYZ(float @NonNull [] colorvalue) {
        return toCIEXYZ(colorvalue, new float[3]);
    }

    @Override
    public float @NonNull [] toCIEXYZ(float @NonNull [] colorvalue, float @NonNull [] xyz) {
        return new SrgbColorSpace().toCIEXYZ(toRGB(colorvalue, xyz), xyz);
    }

    @Override
    public float @NonNull [] fromCIEXYZ(float @NonNull [] xyz, float @NonNull [] colorvalue) {
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

    public @NonNull String getName(int component) {
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
