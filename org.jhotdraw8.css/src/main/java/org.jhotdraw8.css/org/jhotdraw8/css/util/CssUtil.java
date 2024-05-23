/*
 * @(#)CssUtil.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.css.util;

import org.jhotdraw8.css.ast.Selector;
import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssParser;

import java.text.ParseException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Provides utility methods for CSS.
 */
public class CssUtil {
    /**
     * Don't let anyone instantiate this class.
     */
    private CssUtil() {
    }

    /**
     * Returns the selected elements.
     */
    public static <E> Stream<E> select(String selector, Iterable<E> elements, SelectorModel<E> model, boolean parallel) throws ParseException {
        CssParser parser = new CssParser();
        Selector s = parser.parseSelector(selector);
        return StreamSupport.stream(elements.spliterator(), parallel)
                .filter(e -> s.matches(model, e));
    }
}
