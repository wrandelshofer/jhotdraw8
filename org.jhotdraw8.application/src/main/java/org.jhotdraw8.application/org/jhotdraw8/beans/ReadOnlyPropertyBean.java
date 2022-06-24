/*
 * @(#)ReadOnlyPropertyBean.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.beans;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.typesafekey.MapAccessor;
import org.jhotdraw8.collection.typesafekey.NonNullMapAccessor;

public interface ReadOnlyPropertyBean {
    /**
     * Gets a property value.
     *
     * @param <T> the value type
     * @param key the key
     * @return the value
     */
    @Nullable <T> T get(@NonNull MapAccessor<T> key);

    /**
     * Gets a nonnull property value.
     *
     * @param <T> the value type
     * @param key the key
     * @return the value
     */
    @NonNull <T> T getNonNull(@NonNull NonNullMapAccessor<T> key);
}
