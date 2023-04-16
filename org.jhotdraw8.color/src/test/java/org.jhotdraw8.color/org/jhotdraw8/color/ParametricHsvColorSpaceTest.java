package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class ParametricHsvColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull ParametricHsvColorSpace getInstance() {
        return new ParametricHsvColorSpace("HSV", new SrgbColorSpace());
    }
}