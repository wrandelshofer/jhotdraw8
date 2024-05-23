/*
 * @(#)CssDimension2D.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.value;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.UnitConverter;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a dimension with width, height values specified as {@link CssSize}s.
 *
 * @author Werner Randelshofer
 */
public class CssDimension2D {

    public static final CssDimension2D ZERO = new CssDimension2D();

    private final CssSize width;
    private final CssSize height;

    public CssDimension2D(CssSize width, CssSize height) {
        this.width = width;
        this.height = height;
    }

    public CssDimension2D(double width, double height, String units) {
        this(CssSize.of(width, units), CssSize.of(height, units));
    }

    public CssDimension2D() {
        this(CssSize.ZERO, CssSize.ZERO);
    }

    public CssDimension2D(double width, double height) {
        this(width, height, UnitConverter.DEFAULT);
    }

    public CssDimension2D(Point2D p) {
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
        final CssDimension2D other = (CssDimension2D) obj;
        if (!Objects.equals(this.width, other.width)) {
            return false;
        }
        return Objects.equals(this.height, other.height);
    }

    public CssSize getWidth() {
        return width;
    }

    public CssSize getHeight() {
        return height;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.width);
        hash = 89 * hash + Objects.hashCode(this.height);
        return hash;
    }

    @Override
    public String toString() {
        return "CssPoint2D{" + width +
                ", " + height +
                '}';
    }

    public Dimension2D getConvertedValue() {
        return new Dimension2D(width.getConvertedValue(), height.getConvertedValue());

    }

    public CssDimension2D subtract(CssDimension2D that) {
        return new CssDimension2D(width.subtract(that.width), height.subtract(that.height));
    }

    public CssDimension2D add(CssDimension2D that) {
        return new CssDimension2D(width.add(that.width), height.add(that.height));
    }
}
