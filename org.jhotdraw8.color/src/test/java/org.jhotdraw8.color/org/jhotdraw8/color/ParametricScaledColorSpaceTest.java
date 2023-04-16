/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class ParametricScaledColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected @NonNull NamedColorSpace getInstance() {
        return new ParametricScaledColorSpace("sRGB*255", 255f, new SrgbColorSpace());
    }
}