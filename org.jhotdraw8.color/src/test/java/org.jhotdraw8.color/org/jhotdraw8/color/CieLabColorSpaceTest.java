package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class CieLabColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull CieLabColorSpace getInstance() {
        return new CieLabColorSpace();
    }
}