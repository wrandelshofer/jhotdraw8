package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;

public class ParametricHsvColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected @NonNull ParametricHsvColorSpace getInstance() {
        return new ParametricHsvColorSpace("HSV", new SrgbColorSpace());
    }
}