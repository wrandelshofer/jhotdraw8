/*
 * @(#)LinearRgbColorSpaceTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.tmp.NamedColorSpace;
import org.jhotdraw8.color.tmp.SrgbColorSpace;


public class SrgbColorSpaceTest extends AbstractNamedColorSpaceTest {

    @Override
    protected @NonNull NamedColorSpace getInstance() {
        return SrgbColorSpace.getInstance();
    }
}