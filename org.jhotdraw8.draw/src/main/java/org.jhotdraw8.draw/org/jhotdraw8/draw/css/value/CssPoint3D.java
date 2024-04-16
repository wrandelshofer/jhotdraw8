/*
 * @(#)CssPoint3D.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.value;

import javafx.geometry.Point3D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.value.CssSize;

import java.util.Objects;

/**
 * Represents a point with x, y, z values specified as {@link CssSize}s.
 *
 * @author Werner Randelshofer
 */
public class CssPoint3D {

    public static final @NonNull CssPoint3D ZERO = new CssPoint3D();

    private final @NonNull CssSize x;
    private final @NonNull CssSize y;
    private final @NonNull CssSize z;

    public CssPoint3D(@NonNull CssSize x, @NonNull CssSize y, @NonNull CssSize z) {
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
    public @NonNull String toString() {
        return "CssPoint3D{" + x +
                ", " + y +
                ", " + z +
                '}';
    }

    public @NonNull Point3D getConvertedValue() {
        return new Point3D(x.getConvertedValue(), y.getConvertedValue(), z.getConvertedValue());

    }
}
