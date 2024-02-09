/*
 * @(#)SelectorGroup.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.readonly.ReadOnlyList;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A "selector group" matches an element if one of its selectors matches the
 * element.
 *
 * @author Werner Randelshofer
 */
public class SelectorGroup extends Selector {

    private final @NonNull ReadOnlyList<Selector> selectors;

    public SelectorGroup(@Nullable SourceLocator sourceLocator, @NonNull Selector selector) {
        super(sourceLocator);
        this.selectors = VectorList.of(selector);
    }

    public SelectorGroup(@Nullable SourceLocator sourceLocator, @NonNull List<Selector> selectors) {
        super(sourceLocator);
        this.selectors = VectorList.copyOf(selectors);
    }

    @Override
    public @NonNull String toString() {
        StringBuilder buf = new StringBuilder("( ");
        boolean first = true;
        for (Selector s : selectors) {
            if (first) {
                first = false;
            } else {
                buf.append(" || ");
            }
            buf.append(s);
        }
        buf.append(" )");
        return buf.toString();
    }

    @Override
    public int getSpecificity() {
        return selectors.stream().mapToInt(Selector::getSpecificity).sum();
    }

    /**
     * Returns true if the rule matches the element.
     *
     * @param <T>     the element type
     * @param model   The helper is used to access properties of the element and
     *                parent or sibling elements in the document.
     * @param element the element
     * @return true on match
     */
    @Override
    public <T> boolean matches(SelectorModel<T> model, T element) {
        return match(model, element) != null;
    }

    @Override
    protected @Nullable <T> T match(SelectorModel<T> model, T element) {
        for (Selector s : selectors) {
            T result = s.match(model, element);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the last selector with highest specificity that
     * matches the specified element or null.
     * <p>
     * If multiple selectors match, then this method returns the selector
     * with the highest specificity value.
     * <p>
     * If multiple matching selectors have the highest specificity, then this
     * method returns the last one.
     *
     * @param <T>     the element type
     * @param model   The helper is used to access properties of the element and
     *                parent or sibling elements in the document.
     * @param element the element
     * @return the last selector with highest specificity that matches the specified element,
     * returns null if no selector matches
     */
    public @Nullable <T> Selector matchSelector(@NonNull SelectorModel<T> model, @NonNull T element) {
        int maxSpecificity = 0;
        Selector found = null;// selector with maxSpecificity or last

        for (Selector s : selectors) {
            T result = s.match(model, element);
            if (result != null) {
                final int specificity = s.getSpecificity();
                if (found == null || specificity >= maxSpecificity) {
                    found = s;
                    maxSpecificity = specificity;
                }
            }
        }

        return found;
    }

    @Override
    public void produceTokens(@NonNull Consumer<CssToken> consumer) {
        boolean first = true;
        for (Selector s : selectors) {
            if (first) {
                first = false;
            } else {
                consumer.accept(new CssToken(CssTokenType.TT_COMMA));
                consumer.accept(new CssToken(CssTokenType.TT_S, "\n"));
            }
            s.produceTokens(consumer);

        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SelectorGroup that = (SelectorGroup) o;
        return selectors.equals(that.selectors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selectors);
    }

    /**
     * This selector matches only on a specific type, if all its selectors
     * match on the same type.
     */
    @Override
    public @Nullable TypeSelector matchesOnlyOnASpecificType() {
        TypeSelector typeSelector = null;
        for (int i = 0, n = selectors.size(); i < n; i++) {
            if (i == 0) {
                typeSelector = selectors.get(i).matchesOnlyOnASpecificType();
                if (typeSelector == null) {
                    return null;
                }
            } else {
                if (!typeSelector.equals(selectors.get(i).matchesOnlyOnASpecificType())) {
                    return null;
                }
            }
        }
        return typeSelector;
    }

}
