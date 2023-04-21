/*
 * @(#)SvgStop.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.figure;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.value.CssDefaultableValue;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.svg.css.SvgDefaultablePaint;

import java.util.Objects;

/**
 * CssStop.
 *
 * @author Werner Randelshofer
 */
public class SvgStop {

    private final Double offset;
    private final CssDefaultableValue<CssSize> opacity;
    private final SvgDefaultablePaint<CssColor> color;

    public SvgStop(Double offset, SvgDefaultablePaint<CssColor> color, CssDefaultableValue<CssSize> opacity) {
        this.offset = offset;
        this.color = color;
        this.opacity = opacity;
    }

    public Double getOffset() {
        return offset;
    }

    public CssDefaultableValue<CssSize> getOpacity() {
        return opacity;
    }

    public SvgDefaultablePaint<CssColor> getColor() {
        return color;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
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
        final SvgStop other = (SvgStop) obj;
        if (!Objects.equals(this.offset, other.offset)) {
            return false;
        }
        return Objects.equals(this.color, other.color);
    }

    @Override
    public @NonNull String toString() {
        return "CssStop{" + "offset=" + offset + ", " + color + '}';
    }

}
