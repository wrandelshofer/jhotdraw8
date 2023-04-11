package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.tmp.HsvPhysiologicColorSpace;

public class HsvPhysiologicalColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull HsvPhysiologicColorSpace getInstance() {
        return HsvPhysiologicColorSpace.getInstance();
    }
}