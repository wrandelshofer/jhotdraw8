/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;


public class CssLinearSrgbColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected @NonNull ParametricLinearRgbColorSpace getInstance() {
        return (ParametricLinearRgbColorSpace) new SrgbColorSpace().getLinearColorSpace();
    }


}