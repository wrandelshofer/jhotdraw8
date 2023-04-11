package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;

import java.awt.color.ColorSpace;

@Disabled("BROKEN")
public class JavaPyccColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull NamedColorSpace getInstance() {
        return new NamedColorSpaceAdapter("PYCC", ColorSpace.getInstance(ColorSpace.CS_PYCC));
    }
}