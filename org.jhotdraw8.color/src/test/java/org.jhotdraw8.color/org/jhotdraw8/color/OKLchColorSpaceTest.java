package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class OKLchColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull OKLchColorSpace getInstance() {
        return new OKLchColorSpace();
    }
}