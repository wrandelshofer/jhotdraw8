package org.jhotdraw8.color;

public class ParametricHlsColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected ParametricHlsColorSpace getInstance() {
        return new ParametricHlsColorSpace("HSL", new SrgbColorSpace());
    }
}