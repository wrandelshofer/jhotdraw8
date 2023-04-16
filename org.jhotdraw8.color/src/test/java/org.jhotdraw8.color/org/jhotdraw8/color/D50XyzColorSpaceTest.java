package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class D50XyzColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull D50XyzColorSpace getInstance() {
        return new D50XyzColorSpace();
    }


}