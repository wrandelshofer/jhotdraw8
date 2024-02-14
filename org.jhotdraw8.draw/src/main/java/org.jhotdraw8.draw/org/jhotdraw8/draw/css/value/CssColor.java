/*
 * @(#)CssColor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.value;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.converter.DoubleCssConverter;
import org.jhotdraw8.draw.css.converter.ColorCssConverter;
import org.jhotdraw8.draw.render.RenderContext;

import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;

/**
 * Abstract base class for a color specified in a specific color system.
 * <p>
 * FIXME - make this class abstract and implement subclasses for each color system
 * <p>
 * References:
 * <dl>
 *     <dt>CSS Color Module Level 4, Editor’s Draft, 30 March 2023</dt>
 *     <dd><a href="https://drafts.csswg.org/css-color-4/">csswg.org</a></dd>
 * </dl>
 *
 * @author Werner Randelshofer
 */
public class CssColor implements Paintable {

    private static final DoubleCssConverter num = new DoubleCssConverter(false);

    private final @NonNull String name;
    private final @NonNull Color color;


    public CssColor(@NonNull Color color) {
        this(null, color);
    }

    public CssColor(@Nullable String name) {
        Color computedColor = DefaultSystemColorConverter.LIGHT_SYSTEM_COLORS.get(name);
        if (computedColor == null && name != null) {
            try {
                computedColor = Color.web(name);
            } catch (IllegalArgumentException e) {
                computedColor = Color.BLACK;
            }
        } else {
            computedColor = Color.BLACK;
        }
        this.color = computedColor;
        this.name = name == null ? toName(computedColor) : name;
    }

    public CssColor(@Nullable String name, @NonNull Color color) {
        this.name = name == null ? toName(color) : name;
        this.color = color;
    }

    public @NonNull String getName() {
        return name;
    }

    public @NonNull Color getColor(SystemColorConverter converter) {
        return converter.convert(this);
    }

    public @NonNull Color getColor() {
        return color;
    }

    @Override
    public @NonNull Color getPaint() {
        return color;
    }

    @Override
    public @Nullable Paint getPaint(RenderContext ctx) {
        return color;
    }

    public static @NonNull String toName(@NonNull Color c) {
        if (c.getOpacity() == 1.0) {
            int r = (int) Math.round(c.getRed() * 255.0);
            int g = (int) Math.round(c.getGreen() * 255.0);
            int b = (int) Math.round(c.getBlue() * 255.0);
            return String.format("#%02x%02x%02x", r, g, b);

        } else if (c.equals(Color.TRANSPARENT)) {
            return "transparent";
        } else {
            int r = (int) Math.round(c.getRed() * 255.0);
            int g = (int) Math.round(c.getGreen() * 255.0);
            int b = (int) Math.round(c.getBlue() * 255.0);
            int o = (int) Math.round(c.getOpacity() * 255.0);
            return String.format("#%02x%02x%02x%02x", r, g, b, o);
        }
    }



    @Override
    public int hashCode() {
        return Objects.hash(this.color, this.name);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!CssColor.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final CssColor other = (CssColor) obj;
        return Objects.equals(this.color, other.color)
                && Objects.equals(this.name, other.name);
    }

    @Override
    public @NonNull String toString() {
        return "CssColor{" + getName() + ","
                + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getOpacity()
                + "}";
    }

    private static final @NonNull ColorCssConverter converter = new ColorCssConverter();

    public static @NonNull CssColor valueOf(@NonNull String value) {
        try {
            return converter.fromString(value);
        } catch (ParseException | IOException e) {
            return new NamedCssColor(value, Color.BLACK);
        }
    }

    public static @Nullable CssColor ofColor(@Nullable Color c) {
        return c == null ? null : new CssColor(c);
    }

    public static @Nullable Color toColor(@Nullable CssColor c) {
        return c == null ? null : c.getColor();
    }

}
