/*
 * @(#)CssPoint3D.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.value;

import javafx.geometry.Point3D;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a point with x, y, z values specified as {@link CssSize}s.
 *
 */
public class CssPoint3D {

    public static final CssPoint3D ZERO = new CssPoint3D();

    private final CssSize x;
    private final CssSize y;
    private final CssSize z;

    public CssPoint3D(CssSize x, CssSize y, CssSize z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public CssPoint3D(double x, double y, double z, String units) {
        this(CssSize.of(x, units), CssSize.of(y, units), CssSize.of(z, units));
    }

    public CssPoint3D() {
        this(CssSize.ZERO, CssSize.ZERO, CssSize.ZERO);
    }

    public CssPoint3D(double x, double y, double z) {
        this(x, y, z, null);
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
        final CssPoint3D other = (CssPoint3D) obj;
        if (!Objects.equals(this.x, other.x)) {
            return false;
        }
        if (!Objects.equals(this.y, other.y)) {
            return false;
        }
        return Objects.equals(this.z, other.z);
    }

    public CssSize getX() {
        return x;
    }

    public CssSize getY() {
        return y;
    }

    public CssSize getZ() {
        return z;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.x);
        hash = 89 * hash + Objects.hashCode(this.y);
        hash = 89 * hash + Objects.hashCode(this.z);
        return hash;
    }

    @Override
    public String toString() {
        return "CssPoint3D{" + x +
                ", " + y +
                ", " + z +
                '}';
    }

    public Point3D getConvertedValue() {
        return new Point3D(x.getConvertedValue(), y.getConvertedValue(), z.getConvertedValue());

    }
}
