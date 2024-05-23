/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class LinearDisplayP3ColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected NamedColorSpace getInstance() {
        return new DisplayP3ColorSpace().getLinearColorSpace();
    }
}