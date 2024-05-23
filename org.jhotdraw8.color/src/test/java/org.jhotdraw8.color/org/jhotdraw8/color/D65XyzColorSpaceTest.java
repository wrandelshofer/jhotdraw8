package org.jhotdraw8.color;

import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class D65XyzColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected D65XyzColorSpace getInstance() {
        return new D65XyzColorSpace();
    }
}