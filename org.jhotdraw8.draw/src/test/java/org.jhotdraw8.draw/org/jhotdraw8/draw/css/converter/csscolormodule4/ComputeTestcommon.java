package org.jhotdraw8.draw.css.converter.csscolormodule4;

import javafx.scene.paint.Color;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.NumberConverter;
import org.jhotdraw8.draw.css.converter.CssColorConverter;
import org.jhotdraw8.draw.css.value.CssColor;

import java.io.IOException;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComputeTestcommon {
    private final static CssColorConverter colorConverter = new CssColorConverter();

    public static void test_computed_value(@NonNull String property, @NonNull String specified, @NonNull String computed) {
        test_computed_value(property, specified, computed, null);
    }

    /**
     * <dl>
     *     <dt>web-platform-tests / wpt</dt>
     *     <dd><a href="https://github.com/web-platform-tests/wpt/blob/f69cc2c952a97e745446a6026559292a96340fd8/css/support/computed-testcommon.js">github.com</a></dd>
     * </dl>
     *
     * @param property   The name of the CSS property being tested.
     * @param specified  @param {string} specified A specified value for the property.
     * @param computed   The expected computed value,
     *                   or an array of permitted computed value.
     *                   If omitted, defaults to {@code specified}.
     * @param titleExtra Optional extra title.
     */
    public static void test_computed_value(@NonNull String property, @NonNull String specified, @NonNull String computed, @Nullable String titleExtra) {
        String message = "specified=\"" + specified + (titleExtra == null ? "\"" : "\" " + titleExtra);
        try {
            CssColor parsed = colorConverter.fromString(specified);
            String actualComputed = colorConverter.toString(parsed);
            assertEquals(computed, actualComputed, message);
        } catch (ParseException | IOException e) {
            throw new AssertionError(message, e);
        }
    }

    private final static NumberConverter number = new NumberConverter();

    /**
     * <dl>
     *     <dt>web-platform-tests / wpt</dt>
     *     <dd><a href="https://github.com/web-platform-tests/wpt/blob/f69cc2c952a97e745446a6026559292a96340fd8/css/support/computed-testcommon.js">github.com</a></dd>
     * </dl>
     *
     * @param property   The name of the CSS property being tested.
     * @param specified  @param {string} specified A specified value for the property.
     * @param computed   The expected computed value,
     *                   or an array of permitted computed value.
     *                   If omitted, defaults to {@code specified}.
     * @param titleExtra Optional extra title.
     */
    public static void test_computed_value_to_rgb(@NonNull String property, @NonNull String specified, @NonNull String computed, @Nullable String titleExtra) {
        String message = "specified=\"" + specified + (titleExtra == null ? "\"" : "\" " + titleExtra);
        try {
            CssColor parsed = colorConverter.fromString(specified);
            Color c = parsed.getColor();
            String actualComputed = (computed.startsWith("rgba(") ? "rgba(" : "rgb(")
                    + number.toString(255 * c.getRed())
                    + ", " + number.toString(255 * c.getGreen())
                    + ", " + number.toString(255 * c.getBlue())
                    + (c.getOpacity() != 1 ? ", " + number.toString(c.getOpacity()) : "")
                    + ")";
            assertEquals(computed, actualComputed, message);
        } catch (ParseException | IOException e) {
            throw new AssertionError(message, e);
        }
    }
}