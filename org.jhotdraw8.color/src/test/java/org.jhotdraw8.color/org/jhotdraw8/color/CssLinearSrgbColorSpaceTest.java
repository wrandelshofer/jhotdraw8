/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class CssLinearSrgbColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected @NonNull ParametricLinearRgbColorSpace getInstance() {
        return (ParametricLinearRgbColorSpace) new SrgbColorSpace().getLinearColorSpace();
    }


}