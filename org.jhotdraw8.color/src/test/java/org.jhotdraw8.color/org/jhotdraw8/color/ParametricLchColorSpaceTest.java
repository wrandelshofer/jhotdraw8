package org.jhotdraw8.color;

import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class ParametricLchColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected ParametricLchColorSpace getInstance() {
        return new ParametricLchColorSpace("CIE LCH", new CieLabColorSpace());
    }


}