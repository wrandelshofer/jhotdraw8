package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class D65XyzColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull D65XyzColorSpace getInstance() {
        return new D65XyzColorSpace();
    }
}