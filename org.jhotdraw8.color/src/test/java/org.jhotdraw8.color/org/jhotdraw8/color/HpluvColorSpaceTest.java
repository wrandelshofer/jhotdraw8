package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.tmp.HpluvColorSpace;

public class HpluvColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull HpluvColorSpace getInstance() {
        return HpluvColorSpace.getInstance();
    }
}