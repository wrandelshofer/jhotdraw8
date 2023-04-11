package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.tmp.HlsuvColorSpace;

public class HlsuvColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull HlsuvColorSpace getInstance() {
        return HlsuvColorSpace.getInstance();
    }
}