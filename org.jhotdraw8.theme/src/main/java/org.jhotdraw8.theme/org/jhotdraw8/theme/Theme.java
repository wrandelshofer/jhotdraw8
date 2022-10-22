package org.jhotdraw8.theme;

import javafx.application.Application;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.os.Appearance;

import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Encapsulates a customizable user agent stylesheet.
 */
public interface Theme {
    /**
     * Gets the name of the theme.
     *
     * @return the name of the theme.
     */
    @NonNull String getName();

    /**
     * Generates a user agent stylesheet that can be set on
     * {@link Application#setUserAgentStylesheet(String)}.
     * <p>
     * The stylesheet must be a URL. If the stylesheet is generated
     * dynamically, the stylesheet can be a String that is converted
     * into a Data URL. See {@link #toDataUrl(String)}.
     *
     * @param params parameters for generating the stylesheet
     * @return a stylesheet URL
     */
    @Nullable
    String createUserAgentStylesheet(@NonNull ThemeParameters params);

    /**
     * Gets the appearance of the theme.
     *
     * @return the appearance
     */
    @NonNull
    Appearance getAppearance();


    /**
     * Converts a String into a data url that contains the String.
     */
    static String toDataUrl(String data) {
        String dataUrl = data == null ? null : "data:text/css;base64,"
                + Base64.getEncoder().encodeToString(data.getBytes(UTF_8));
        return dataUrl;
    }
}
