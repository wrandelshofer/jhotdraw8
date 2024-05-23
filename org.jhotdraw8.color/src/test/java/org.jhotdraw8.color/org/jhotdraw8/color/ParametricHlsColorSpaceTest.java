package org.jhotdraw8.color;

import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class ParametricHlsColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected ParametricHlsColorSpace getInstance() {
        return new ParametricHlsColorSpace("HSL", new SrgbColorSpace());
    }
}