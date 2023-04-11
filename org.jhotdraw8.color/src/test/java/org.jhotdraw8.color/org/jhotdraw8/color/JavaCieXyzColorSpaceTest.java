package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;

import java.awt.color.ColorSpace;

@Disabled("this test succeeds - the java implementation is okay")
public class JavaCieXyzColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull NamedColorSpace getInstance() {
        return new NamedColorSpaceAdapter("CIE XYZ", ColorSpace.getInstance(ColorSpace.CS_CIEXYZ));
    }
}