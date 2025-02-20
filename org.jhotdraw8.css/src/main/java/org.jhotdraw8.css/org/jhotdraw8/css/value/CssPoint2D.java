/*
 * @(#)CssPoint2D.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.value;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a point with x, y values specified as {@link CssSize}s.
 *
 */
public class CssPoint2D {

    public static final CssPoint2D ZERO = new CssPoint2D();

    private final CssSize x;
    private final CssSize y;

    public CssPoint2D(CssSize x, CssSize y) {
        this.x = x;
        this.y = y;
    }

    public CssPoint2D(double x, double y, String units) {
        this(CssSize.of(x, units), CssSize.of(y, units));
    }

    public CssPoint2D() {
        this(CssSize.ZERO, CssSize.ZERO);
    }

    public CssPoint2D(double x, double y) {
        this(x, y, UnitConverter.DEFAULT);
    }

    public CssPoint2D(Point2D p) {
        this(p.getX(), p.getY());
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
        final CssPoint2D other = (CssPoint2D) obj;
        if (!Objects.equals(this.x, other.x)) {
            return false;
        }
        return Objects.equals(this.y, other.y);
    }

    public CssSize getX() {
        return x;
    }

    public CssSize getY() {
        return y;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.x);
        hash = 89 * hash + Objects.hashCode(this.y);
        return hash;
    }

    @Override
    public String toString() {
        return "CssPoint2D{" + x +
                ", " + y +
                '}';
    }

    public Point2D getConvertedValue() {
        return new Point2D(x.getConvertedValue(), y.getConvertedValue());

    }

    public CssPoint2D subtract(CssPoint2D that) {
        return new CssPoint2D(x.subtract(that.x), y.subtract(that.y));
    }

    public CssPoint2D add(CssPoint2D that) {
        return new CssPoint2D(x.add(that.x), y.add(that.y));
    }

    /**
     * Gets a point that was given in relative coordinates to a bounds.
     * <p>
     * If the x- or y-coordinate of the point is given as a percentage,
     * then the returned point is {@code bounds.minX + p.x/100 * bounds.width},
     * {@code bounds.minY + p.y/100 * bounds.height}.
     * <p>
     * If the x- or y-coordinate of the point is given with default units,
     * then the returned point is {@code bounds.minX + p.x * bounds.width},
     * {@code bounds.minY + p.y * bounds.height}.
     * <p>
     * Otherwise the returned point is {@code bounds.minX + p.x},
     * {@code bounds.minY + p.y}.
     *
     * @param p      point in relative coordinates
     * @param bounds the bounds
     * @return point in absolute coordinates
     */
    public static Point2D getPointInBounds(CssPoint2D p, Bounds bounds) {
        final double x, y;
        final CssSize px = p.getX();
        final CssSize py = p.getY();
        x = switch (px.getUnits()) {
            case UnitConverter.PERCENTAGE -> Math.fma(bounds.getWidth(), px.getValue() / 100.0, bounds.getMinX());
            case UnitConverter.DEFAULT -> Math.fma(bounds.getWidth(), px.getValue(), bounds.getMinX());
            default -> bounds.getMinX() + px.getConvertedValue();
        };
        y = switch (py.getUnits()) {
            case UnitConverter.PERCENTAGE -> Math.fma(bounds.getHeight(), py.getValue() / 100.0, bounds.getMinY());
            case UnitConverter.DEFAULT -> Math.fma(bounds.getHeight(), py.getValue(), bounds.getMinY());
            default -> bounds.getMinY() + py.getConvertedValue();
        };
        return new Point2D(x, y);
    }
}
