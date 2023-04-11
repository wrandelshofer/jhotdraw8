package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.tmp.HlsColorSpace;

public class HlsColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull HlsColorSpace getInstance() {
        return HlsColorSpace.getInstance();
    }
}