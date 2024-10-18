/*
 * @(#)WritableStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.styleable;

import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;

/**
 * Interface for keys which support styled values from CSS.
 *
 * @param <T> The value type.
 * @author Werner Randelshofer
 */
public interface WritableStyleableMapAccessor<T> extends ReadOnlyStyleableMapAccessor<T> {

    long serialVersionUID = 1L;

    /**
     * Gets example values.
     *
     * @return a help text.
     */
    default PersistentList<String> getExamples() {
        return VectorList.of();
    }


}
