/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.junit.jupiter.api.Disabled;

public class LinearRec2020ColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected NamedColorSpace getInstance() {
        return new Rec2020ColorSpace().getLinearColorSpace();
    }

}