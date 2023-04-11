/* @(#)CIELCHabColorSpace.java
 * Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color.tmp;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * The 1976 CIE L*CHa*b* color space (CIELCH).
 * <p>
 * The {@code L*} coordinate of an object is the lightness intensity as measured on a
 * scale from 0 to 100, where 0 represents black and 100 represents white.
 * <p>
 * The {@code C} and {@code H} coordinates are projections of the {@code a*} and {@code b*}
 * colors of the CIE {@code L*a*b*} color space into polar coordinates.
 * <pre>
 * a = C * cos(H)
 * b = C * sin(H)
 * </pre>
 *
 * @author Werner Randelshofer
 */
public class CieLchColorSpace extends AbstractLchColorSpace {
    private static @Nullable CieLchColorSpace instance;

    public static @NonNull CieLchColorSpace getInstance() {
        if (instance == null) {
            instance = new CieLchColorSpace();
        }
        return instance;
    }


    public CieLchColorSpace() {
        super("CIELCH", new CieLabColorSpace());
    }
}
