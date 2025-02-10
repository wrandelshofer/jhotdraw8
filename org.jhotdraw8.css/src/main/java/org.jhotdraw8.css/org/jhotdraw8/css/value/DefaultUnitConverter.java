/*
 * @(#)DefaultUnitConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.value;


/**
 * DefaultUnitConverter.
 *
 */
public class DefaultUnitConverter implements UnitConverter {

    static final DefaultUnitConverter instance = new DefaultUnitConverter(96);

    public static DefaultUnitConverter getInstance() {
        return instance;
    }

    private final double viewportWidth;
    private final double viewportHeight;
    private final double dpi;
    private final double percentageFactor;

    public DefaultUnitConverter(double dpi) {
        this(dpi, 100.0, 1024, 768);
    }

    public DefaultUnitConverter(double dpi, double percentageFactor) {
        this(dpi, percentageFactor, 1024, 768);
    }

    public DefaultUnitConverter(double dpi, double percentageFactor, double viewportWidth, double viewportHeight) {
        this.dpi = dpi;
        this.percentageFactor = percentageFactor;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public DefaultUnitConverter() {
        this(96.0);
    }

    @Override
    public double getDpi() {
        return dpi;
    }

    @Override
    public double getPercentageFactor() {
        return percentageFactor;
    }

    @Override
    public double getViewportWidth() {
        return viewportWidth;
    }

    @Override
    public double getViewportHeight() {
        return viewportHeight;
    }
}
