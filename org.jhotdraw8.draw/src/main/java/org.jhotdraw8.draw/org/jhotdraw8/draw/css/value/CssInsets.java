/*
 * @(#)CssInsets.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.value;

import javafx.geometry.Insets;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.css.value.UnitConverter;

import java.util.Objects;

/**
 * Represents a set of inside offsets specified as {@link CssSize}s.
 *
 * @author Werner Randelshofer
 */
public class CssInsets {

    public static final @NonNull CssInsets ZERO = new CssInsets();

    private final @NonNull CssSize bottom;
    private final @NonNull CssSize left;
    private final @NonNull CssSize right;
    private final @NonNull CssSize top;

    public CssInsets(@NonNull CssSize top, @NonNull CssSize right, @NonNull CssSize bottom, @NonNull CssSize left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public CssInsets(double top, double right, double bottom, double left, String units) {
        this(CssSize.of(top, units), CssSize.of(right, units), CssSize.of(bottom, units), CssSize.of(left, units));
    }

    public CssInsets() {
        this(CssSize.ZERO, CssSize.ZERO, CssSize.ZERO, CssSize.ZERO);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CssInsets other = (CssInsets) obj;
        if (!Objects.equals(this.top, other.top)) {
            return false;
        }
        if (!Objects.equals(this.right, other.right)) {
            return false;
        }
        if (!Objects.equals(this.bottom, other.bottom)) {
            return false;
        }
        return Objects.equals(this.left, other.left);
    }

    public CssSize getBottom() {
        return bottom;
    }

    /**
     * Converts values using the specified width and heights for converting
     * percentages in the insets.
     *
     * @param width  the width for computing percentages for left and right
     *               insets
     * @param height the height for computing percentages for top and bottom
     *               insets
     * @return the converted value
     */
    public @NonNull Insets getConvertedValue(double width, double height) {
        final UnitConverter heightConverter = new DefaultUnitConverter(72.0, height);
        final UnitConverter widthConverter = new DefaultUnitConverter(72.0, width);
        return new Insets(heightConverter.convert(top, UnitConverter.DEFAULT), widthConverter.convert(right, UnitConverter.DEFAULT),
                heightConverter.convert(bottom, UnitConverter.DEFAULT), widthConverter.convert(left, UnitConverter.DEFAULT));
    }

    public @NonNull Insets getConvertedValue() {
        return new Insets(top.getConvertedValue(), right.getConvertedValue(),
                bottom.getConvertedValue(), left.getConvertedValue());
    }

    public @NonNull Insets getConvertedValue(UnitConverter converter, @NonNull String units) {
        return new Insets(top.getConvertedValue(converter, units), right.getConvertedValue(converter, units),
                bottom.getConvertedValue(converter, units), left.getConvertedValue(converter, units));
    }

    public @NonNull Insets getConvertedValue(@NonNull UnitConverter converter) {
        return getConvertedValue(converter, UnitConverter.DEFAULT);
    }

    public CssSize getLeft() {
        return left;
    }

    public CssSize getRight() {
        return right;
    }

    public CssSize getTop() {
        return top;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.top);
        hash = 89 * hash + Objects.hashCode(this.right);
        hash = 89 * hash + Objects.hashCode(this.bottom);
        hash = 89 * hash + Objects.hashCode(this.left);
        return hash;
    }

    @Override
    public @NonNull String toString() {
        return "CssInsets{" + bottom +
                ", " + left +
                ", " + right +
                ", " + top +
                '}';
    }
}
