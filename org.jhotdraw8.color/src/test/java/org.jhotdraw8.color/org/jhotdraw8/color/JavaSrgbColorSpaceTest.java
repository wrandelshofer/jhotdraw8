package org.jhotdraw8.color;

import org.junit.jupiter.api.Disabled;

import java.awt.color.ColorSpace;

/**
 * This test exists only for comparison with {@link SrgbColorSpace}.
 */
@Disabled("this test succeeds - the java implementation is okay")
public class JavaSrgbColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected NamedColorSpace getInstance() {
        return new NamedColorSpaceAdapter("sRGB", ColorSpace.getInstance(ColorSpace.CS_sRGB));
    }
}