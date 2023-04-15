package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;

public class ParametricHlsColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull ParametricHlsColorSpace getInstance() {
        return new ParametricHlsColorSpace("HSL", new SrgbColorSpace());
    }
}