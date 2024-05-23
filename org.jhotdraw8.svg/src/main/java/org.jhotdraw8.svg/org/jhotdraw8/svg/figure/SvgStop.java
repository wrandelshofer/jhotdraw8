/*
 * @(#)SvgStop.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.figure;

import org.jhotdraw8.css.value.CssDefaultableValue;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.svg.css.SvgDefaultablePaint;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * CssStop.
 *
 * @author Werner Randelshofer
 */
public record SvgStop(double offset, SvgDefaultablePaint<CssColor> color, CssDefaultableValue<CssSize> opacity) {

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
    public String toString() {
        return "CssStop{" + "offset=" + offset + ", " + color + '}';
    }

}
