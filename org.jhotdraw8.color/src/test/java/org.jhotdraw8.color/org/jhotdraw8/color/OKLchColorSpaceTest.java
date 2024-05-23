package org.jhotdraw8.color;

import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class OKLchColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected OKLchColorSpace getInstance() {
        return new OKLchColorSpace();
    }
}