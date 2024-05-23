/*
 * @(#)DefaultableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.figure;

import javafx.css.StyleOrigin;
import org.jhotdraw8.css.value.CssDefaultableValue;
import org.jhotdraw8.css.value.CssDefaulting;
import org.jhotdraw8.draw.key.DefaultableStyleableMapAccessor;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public interface DefaultableFigure extends Figure {
    /**
     * Returns the styled value.
     *
     * @param <T> The value type
     * @param key The property key
     * @return The styled value.
     */
    default @Nullable <T> T getDefaultableStyled(DefaultableStyleableMapAccessor<T> key) {
        return getDefaultableStyled(StyleOrigin.INLINE, key);
    }

    default @Nullable <T> T getDefaultableStyled(StyleOrigin origin, DefaultableStyleableMapAccessor<T> key) {
        // FIXME REVERT does not work this way, must use getStyled(origin,key) for _starting a search at the specified origin_ value
        CssDefaultableValue<T> dv = Objects.requireNonNull(getStyled(origin == StyleOrigin.INLINE ? null : origin, key));
        if (dv.getDefaulting() == null) {
            return dv.getValue();
        }
        switch (dv.getDefaulting()) {
        case INITIAL:
            return key.getInitialValue();
        case INHERIT:
            if (getParent() instanceof DefaultableFigure) {
                return ((DefaultableFigure) getParent()).getDefaultableStyled(key);
            } else {
                return key.getInitialValue();
            }
        case UNSET:
            CssDefaultableValue<T> defaultValue = key.getDefaultValue();
            if (defaultValue.getDefaulting() == CssDefaulting.INHERIT) {
                if (getParent() instanceof DefaultableFigure) {
                    return ((DefaultableFigure) getParent()).getDefaultableStyled(key);
                } else {
                    return key.getInitialValue();
                }
            } else {
                return key.getInitialValue();
            }
        case REVERT:
            return switch (origin) {
                case USER_AGENT -> key.getInitialValue();
                case USER -> getDefaultableStyled(StyleOrigin.USER_AGENT, key);
                case AUTHOR -> getDefaultableStyled(StyleOrigin.USER, key);
                case INLINE -> getDefaultableStyled(StyleOrigin.AUTHOR, key);
            };
        default:
            throw new UnsupportedOperationException("unsupported defaulting: " + dv.getDefaulting());
        }
    }

    /**
     * Returns the styled value.
     *
     * @param <T> The value type
     * @param key The property key
     * @return The styled value.
     */
    default <T> T getDefaultableStyledNonNull(DefaultableStyleableMapAccessor<T> key) {
        return Objects.requireNonNull(getDefaultableStyled(key));
    }
}
