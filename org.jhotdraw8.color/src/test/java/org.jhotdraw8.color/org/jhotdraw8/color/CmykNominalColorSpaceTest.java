package org.jhotdraw8.color;

import org.junit.jupiter.api.Disabled;

@Disabled("TEST RUN NEEDS TOO MUCH TIME")

public class CmykNominalColorSpaceTest extends AbstractNamedColorSpaceTest {

    protected CmykNominalColorSpace getInstance() {
        return new CmykNominalColorSpace();
    }
}