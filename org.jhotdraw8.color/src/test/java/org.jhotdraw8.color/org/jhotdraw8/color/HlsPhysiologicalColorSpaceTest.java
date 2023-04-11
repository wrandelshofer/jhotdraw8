package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.tmp.HlsPhysiologicColorSpace;

public class HlsPhysiologicalColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull HlsPhysiologicColorSpace getInstance() {
        return HlsPhysiologicColorSpace.getInstance();
    }
}