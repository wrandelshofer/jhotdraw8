package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;

public class ParametricLchColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull ParametricLchColorSpace getInstance() {
        return new ParametricLchColorSpace("CIE LCH", new CieLabColorSpace());
    }


}