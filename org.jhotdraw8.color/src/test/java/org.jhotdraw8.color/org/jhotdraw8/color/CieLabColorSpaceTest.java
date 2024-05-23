package org.jhotdraw8.color;

import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class CieLabColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected CieLabColorSpace getInstance() {
        return new CieLabColorSpace();
    }
}