package org.jhotdraw8.color;

public class ParametricHsvColorSpaceTest extends AbstractNamedColorSpaceTest {
    protected ParametricHsvColorSpace getInstance() {
        return new ParametricHsvColorSpace("HSV", new SrgbColorSpace());
    }
}