/*
 * @(#)WriteableStyleableMapAccessor.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.styleable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;

/**
 * Interface for keys which support styled values from CSS.
 *
 * @param <T> The value type.
 * @author Werner Randelshofer
 */
public interface WritableStyleableMapAccessor<T> extends ReadOnlyStyleableMapAccessor<T> {

    long serialVersionUID = 1L;

    /**
     * Gets examples.
     *
     * @return a help text.
     */
    default @NonNull ImmutableList<String> getExamples() {
        return ImmutableLists.emptyList();
    }


}
