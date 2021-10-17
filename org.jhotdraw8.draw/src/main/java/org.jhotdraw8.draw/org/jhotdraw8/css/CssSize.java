/*
 * @(#)CssSize.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Objects;

/**
 * Represents a size specified in a particular unit.
 * <p>
 * A CssSize can be used to hold the value of a CSS {@code number-token},
 * {@code percentage-token} or {@code dimension-token}.
 * <p>
 * Unlike {@link javafx.css.Size} this class supports an open ended
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

    public static final @Nullable CssSize ZERO = new CssSize(0);
    public static final CssSize ONE = new CssSize(1);
    private final double value;

    public CssSize(double value) {
        this.value = value;
    }

    public static CssSize from(double value, @Nullable String units) {
        if (units == null || units.equals(UnitConverter.DEFAULT)) {
            if (value == 0.0) {
                return CssSize.ZERO;
            }
            if (value == 1.0) {
                return CssSize.ONE;
            }
        }
        return units == null || UnitConverter.DEFAULT.equals(units) ? new CssSize(value) : new CssSizeWithUnits(value, units);
    }

    public static CssSize from(double value) {
        return from(value, null);
    }

    public static @NonNull CssSize max(@NonNull CssSize a, @NonNull CssSize b) {
        return (a.getConvertedValue() >= b.getConvertedValue()) ? a : b;
    }

    public static @NonNull CssSize min(@NonNull CssSize a, @NonNull CssSize b) {
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
        if (!(obj instanceof CssSize)) {
            return false;
        }
        final CssSize other = (CssSize) obj;
        if (Double.doubleToLongBits(this.value) != Double.doubleToLongBits(other.value)) {
            return false;
        }
        return Objects.equals(this.getUnits(), other.getUnits());
    }

    public double getConvertedValue() {
        return DefaultUnitConverter.getInstance().convert(this, UnitConverter.DEFAULT);
    }

    public double getConvertedValue(@NonNull UnitConverter converter) {
        return converter.convert(this, UnitConverter.DEFAULT);
    }

    public double getConvertedValue(@NonNull UnitConverter converter, @NonNull String units) {
        return converter.convert(this, units);
    }

    public @NonNull String getUnits() {
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
    public @NonNull String toString() {
        return "CssSize{" + value + "\"" + getUnits() + "\"" + '}';
    }

    public @NonNull CssSize subtract(@NonNull CssSize that) {
        return subtract(that, DefaultUnitConverter.getInstance());
    }

    public @NonNull CssSize add(@NonNull CssSize that) {
        return add(that, DefaultUnitConverter.getInstance());
    }

    public @NonNull CssSize subtract(@NonNull CssSize that, @NonNull UnitConverter unitConverter) {
        return from(this.value - unitConverter.convert(that, this.getUnits()), this.getUnits());
    }

    public @NonNull CssSize add(@NonNull CssSize that, @NonNull UnitConverter unitConverter) {
        return from(this.value + unitConverter.convert(that, this.getUnits()), this.getUnits());
    }

    public @NonNull CssSize abs() {
        return value >= 0 ? this : from(Math.abs(value), getUnits());
    }

    public @NonNull CssSize multiply(double factor) {
        return from(value * factor, getUnits());
    }

    public @NonNull CssSize divide(double divisor) {
        return from(value / divisor, getUnits());
    }
}
