package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;

@Disabled("BROKEN")
public class OKHlsColorSpaceTest extends AbstractNamedColorSpaceTest {

    protected @NonNull OKHlsColorSpace getInstance() {
        return OKHlsColorSpace.getInstance();
    }
}