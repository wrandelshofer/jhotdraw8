/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.tmp.GenericGammaCorrectedRGBColorSpace;
import org.jhotdraw8.color.tmp.NamedColorSpace;


public class LinearDisplayP3ColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected @NonNull NamedColorSpace getInstance() {
        return GenericGammaCorrectedRGBColorSpace.LINEAR_DISPLAY_P3_INSTANCE;
    }
}