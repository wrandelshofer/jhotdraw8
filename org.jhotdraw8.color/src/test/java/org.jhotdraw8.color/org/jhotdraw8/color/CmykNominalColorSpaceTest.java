package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.tmp.CmykNominalColorSpace;

public class CmykNominalColorSpaceTest extends AbstractNamedColorSpaceTest {

    protected @NonNull CmykNominalColorSpace getInstance() {
        return CmykNominalColorSpace.getInstance();
    }
}