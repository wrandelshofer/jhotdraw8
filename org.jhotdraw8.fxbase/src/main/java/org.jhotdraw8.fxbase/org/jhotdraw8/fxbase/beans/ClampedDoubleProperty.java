/*
 * @(#)ClampedDoubleProperty.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.beans;

import javafx.beans.property.SimpleDoubleProperty;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * ClampedDoubleProperty.
 *
 */
public class ClampedDoubleProperty extends SimpleDoubleProperty {

    private final double minValue;
    private final double maxValue;

    public ClampedDoubleProperty(Object bean, String name, double initialValue, double minValue, double maxValue) {
        super(bean, name, initialValue);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public void set(double newValue) {
        super.set(max(minValue, min(newValue, maxValue)));
    }

    @Override
    public double get() {
        // note we must override get too, so that values are still clamped,
        // when we are bound to another property
        return max(minValue, min(super.get(), maxValue));
    }
}
