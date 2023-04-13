/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;


public class CssSrgbColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected @NonNull NamedColorSpace getInstance() {
        return new CssLegacySrgbColorSpace();
    }
}