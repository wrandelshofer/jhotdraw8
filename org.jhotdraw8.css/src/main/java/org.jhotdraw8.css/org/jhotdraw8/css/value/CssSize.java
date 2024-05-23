/*
 * @(#)CssSize.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.value;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a size specified in a particular unit.
 * <p>
 * A CssSize can be used to hold the value of a CSS {@code number-token},
 * {@code percentage-token} or {@code dimension-token}.
 * <p>
 * Unlike {@link javafx.css.Size} this class supports an open-ended
 * set of units.
 * <p>
 * References:
 * <dl>
 * <dt>CSS Syntax Module Level 3, Chapter 4. Tokenization</dt>
 * <dd><a href="https://www.w3.org/TR/2019/CR-css-syntax-3-20190716/#tokenization">w3.org</a></dd>
 * </dl>
 *
 * @author Werner Randelshofer
 */
public class CssSize {

    public static final CssSize ZERO = new CssSize(0);
    public static final CssSize ONE = new CssSize(1);
    private final double value;

    CssSize(double value) {
        this.value = value;
    }

    public static CssSize of(double value, @Nullable String units) {
        boolean hasDefaultUnits = units == null || units.equals(UnitConverter.DEFAULT);
        if (hasDefaultUnits) {
            if (value == 0.0) {
                return CssSize.ZERO;
            }
            if (value == 1.0) {
                return CssSize.ONE;
            }
        }
        return hasDefaultUnits ? new CssSize(value) : new CssSizeWithUnits(value, units);
    }

    public static CssSize of(double value) {
        return of(value, null);
    }

    public static CssSize max(CssSize a, CssSize b) {
        return (a.getConvertedValue() >= b.getConvertedValue()) ? a : b;
    }

    public static CssSize min(CssSize a, CssSize b) {
        return (a.getConvertedValue() <= b.getConvertedValue()) ? a : b;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CssSize other)) {
            return false;
        }
        if (Double.doubleToLongBits(this.value) != Double.doubleToLongBits(other.value)) {
            return false;
        }
        return Objects.equals(this.getUnits(), other.getUnits());
    }

    public double getConvertedValue() {
        return DefaultUnitConverter.getInstance().convert(this, UnitConverter.DEFAULT);
    }

    public double getConvertedValue(UnitConverter converter) {
        return converter.convert(this, UnitConverter.DEFAULT);
    }

    public double getConvertedValue(UnitConverter converter, String units) {
        return converter.convert(this, units);
    }

    public String getUnits() {
        return UnitConverter.DEFAULT;
    }

    public double getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.getUnits());
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return "CssSize{" + value + getUnits() + '}';
    }

    public CssSize subtract(CssSize that) {
        return subtract(that, DefaultUnitConverter.getInstance());
    }

    public CssSize add(CssSize that) {
        return add(that, DefaultUnitConverter.getInstance());
    }

    public CssSize subtract(CssSize that, UnitConverter unitConverter) {
        return of(this.value - unitConverter.convert(that, this.getUnits()), this.getUnits());
    }

    public CssSize add(CssSize that, UnitConverter unitConverter) {
        return of(this.value + unitConverter.convert(that, this.getUnits()), this.getUnits());
    }

    public CssSize abs() {
        return value >= 0 ? this : of(Math.abs(value), getUnits());
    }

    public CssSize multiply(double factor) {
        return of(value * factor, getUnits());
    }

    public CssSize divide(double divisor) {
        return of(value / divisor, getUnits());
    }
}
