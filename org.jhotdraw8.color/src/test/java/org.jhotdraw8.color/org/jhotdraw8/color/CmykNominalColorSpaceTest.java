package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class CmykNominalColorSpaceTest extends AbstractNamedColorSpaceTest {

    protected @NonNull CmykNominalColorSpace getInstance() {
        return new CmykNominalColorSpace();
    }
}