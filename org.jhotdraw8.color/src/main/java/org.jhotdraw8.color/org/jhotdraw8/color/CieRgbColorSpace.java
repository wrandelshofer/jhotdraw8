/*
 * @(#)CieRgbColorSpace.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import javafx.geometry.Point2D;

/**
 * Linear CIE RGB Color Space.
 * <dl>
 *     <dt>Wikipedia: CIE 1931 color space</dt>
 *     <dd><a href="https://en.wikipedia.org/wiki/CIE_1931_color_space">wikipedia</a></dd>
 * </dl>
 */
public class CieRgbColorSpace extends ParametricLinearRgbColorSpace {

    public CieRgbColorSpace() {
        super("Linear CIE RGB", new Point2D(0.73474284, 0.26525716),
                new Point2D(0.27377903, 0.7174777),
                new Point2D(0.16655563, 0.00891073),
                ILLUMINANT_E_XYZ
        );
    }
}
