package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class ParametricLchColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull ParametricLchColorSpace getInstance() {
        return new ParametricLchColorSpace("CIE LCH", new CieLabColorSpace());
    }


}