/*
 * @(#)AbstractInputFormat.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.champ.ChampMap;
import org.jhotdraw8.collection.immutable.ImmutableMap;
import org.jhotdraw8.fxcollection.typesafekey.Key;

public abstract class AbstractInputFormat implements InputFormat {
    private @NonNull ImmutableMap<Key<?>, Object> options = ChampMap.of();

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
