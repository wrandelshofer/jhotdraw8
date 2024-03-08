/*
 * @(#)StylesheetsManager.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.manager;

import javafx.css.StyleOrigin;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.function.Consumer3;
import org.jhotdraw8.css.ast.StyleRule;
import org.jhotdraw8.css.ast.Stylesheet;
import org.jhotdraw8.css.model.SelectorModel;

import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.StreamSupport;

/**
 * StylesheetsManager.
 *
 * @param <E> the element type that can be styled by this style manager
 * @author Werner Randelshofer
 */
public interface StylesheetsManager<E> {

    /**
     * Adds a stylesheet with the specified origin.
     *
     * @param origin the style origin
     * @param url    the stylesheet url
     */
    default void addStylesheet(@NonNull StyleOrigin origin, @NonNull URI url) {
        addStylesheet(origin, url, url);
    }

    /**
     * Adds a stylesheet with the specified origin.
     *
     * @param origin        the style origin
     * @param stylesheetUri the stylesheet url
     * @param documentHome  the document Home url
     */
    void addStylesheet(@NonNull StyleOrigin origin, @NonNull URI stylesheetUri, @NonNull URI documentHome);

    /**
     * Adds a stylesheet with the specified origin.
     *
     * @param origin     the style origin
     * @param stylesheet the stylesheet
     */
    void addStylesheet(@NonNull StyleOrigin origin, @NonNull Stylesheet stylesheet);

    /**
     * Adds a stylesheet with the specified origin.
     *
     * @param origin     the style origin
     * @param stylesheet the stylesheet given as a literal string
     */
    void addStylesheet(@NonNull StyleOrigin origin, @NonNull String stylesheet, @Nullable URI documentHome);

    default void applyStylesheetsTo(@NonNull Iterable<E> iterable) {
        StreamSupport.stream(iterable.spliterator(), false).toList()
                .stream()
                .parallel()
                .forEach(this::applyStylesheetsTo);
    }

    /**
     * Removes all stylesheets with the specified origin.
     *
     * @param origin the style origin
     */
    void clearStylesheets(StyleOrigin origin);

    /**
     * Sets a list of stylesheets with the specified origin.
     *
     * @param <T>         type of the list elements
     * @param origin      the origin
     * @param stylesheets list elements can be Strings or URIs.
     */
    default <T> void setStylesheets(StyleOrigin origin, List<T> stylesheets) {
        setStylesheets(origin, null, stylesheets);
    }

    /**
     * Sets a list of stylesheets with the specified origin.
     *
     * @param <T>          type of the list elements
     * @param origin       the origin
     * @param documentHome the document home
     * @param stylesheets  list elements can be Strings or URIs.
     */
    <T> void setStylesheets(StyleOrigin origin, URI documentHome, List<T> stylesheets);

    /**
     * Applies all managed stylesheets to the specified element.
     *
     * @param e The element
     */
    void applyStylesheetsTo(E e);

    /**
     * Returns the selector model of the style manager.
     *
     * @return the selector model
     */
    @NonNull
    SelectorModel<E> getSelectorModel();

    /**
     * Applies the provided stylesheet.
     *
     * @param styleOrigin            the style origin to be used when setting attribute
     *                               values
     * @param s                      the stylesheet
     * @param element                the element
     * @param suppressParseException if parse exceptions should be suppressed
     * @return true if an element was selected
     * @throws ParseException on parse exception
     */
    boolean applyStylesheetTo(StyleOrigin styleOrigin, Stylesheet s, E element, boolean suppressParseException) throws ParseException;

    /**
     * Returns true if the provided stylesheet has selectors which match the
     * specified element.
     *
     * @param s    the stylesheet
     * @param elem the element
     * @return true the element was selected
     */
    default boolean matchesElement(@NonNull Stylesheet s, E elem) {
        SelectorModel<E> selectorModel = getSelectorModel();
        for (StyleRule r : s.getStyleRules()) {
            if (r.getSelectorGroup().matches(selectorModel, elem)) {
                return true;
            }
        }

        return false;
    }

    default List<StyleRule> getMatchingRulesForElement(@NonNull Stylesheet s, E elem) {
        List<StyleRule> matchingRules = new ArrayList<>();
        SelectorModel<E> selectorModel = getSelectorModel();
        for (StyleRule r : s.getStyleRules()) {
            if (r.getSelectorGroup().matches(selectorModel, elem)) {
                matchingRules.add(r);
            }
        }

        return matchingRules;
    }

    /**
     * Returns the logger.
     *
     * @return the logger
     * @see #setLogger(Consumer3)
     */
    @NonNull Consumer3<Level, String, Throwable> getLogger();

    /**
     * Sets the logger.
     * <p>
     * By default, this class does not log anything.
     * <p>
     * A good logger value would be:
     * <pre>
     * (l, s,t)->Logger.getLogger(SimpleStylesheetsManager.class.getName()).log(l,s,t);
     * </pre>
     *
     * @param logger a logger
     */
    void setLogger(@NonNull Consumer3<Level, String, Throwable> logger);

    /**
     * Returns a localized help text.
     *
     * @return the help text
     */
    String getHelpText();

    List<StylesheetInfo> getStylesheets();

    interface StylesheetInfo {
        URI getUri();

        StyleOrigin getOrigin();

        Stylesheet getStylesheet();
    }

    default boolean hasStylesheets() {
        return !getStylesheets().isEmpty();
    }
}
