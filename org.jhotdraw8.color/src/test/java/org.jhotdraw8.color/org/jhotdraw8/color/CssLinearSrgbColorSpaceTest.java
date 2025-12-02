/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.junit.jupiter.api.Disabled;


public class CssLinearSrgbColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected ParametricLinearRgbColorSpace getInstance() {
        return (ParametricLinearRgbColorSpace) new SrgbColorSpace().getLinearColorSpace();
    }


}