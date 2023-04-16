package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class ParametricHlsColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull ParametricHlsColorSpace getInstance() {
        return new ParametricHlsColorSpace("HSL", new SrgbColorSpace());
    }
}