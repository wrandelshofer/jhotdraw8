package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.tmp.CieLabColorSpace;

public class CieLabColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull CieLabColorSpace getInstance() {
        return CieLabColorSpace.getInstance();
    }
}