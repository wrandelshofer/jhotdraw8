/*
 * @(#)AbstractInputFormat.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.icollection.SimpleImmutableMap;
import org.jhotdraw8.icollection.immutable.ImmutableMap;

public abstract class AbstractInputFormat implements InputFormat {
    private @NonNull ImmutableMap<Key<?>, Object> options = SimpleImmutableMap.of();

    public AbstractInputFormat() {
    }

    @NonNull
    @Override
    public ImmutableMap<Key<?>, Object> getOptions() {
        return options;
    }

    @Override
    public void setOptions(@NonNull ImmutableMap<Key<?>, Object> options) {
        this.options = options;
    }
}
