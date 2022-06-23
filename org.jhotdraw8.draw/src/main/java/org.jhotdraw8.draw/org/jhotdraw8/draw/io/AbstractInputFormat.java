/*
 * @(#)AbstractInputFormat.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.facade.ReadOnlyMapFacade;
import org.jhotdraw8.collection.key.Key;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;

import java.util.LinkedHashMap;

public abstract class AbstractInputFormat implements InputFormat {
    private @NonNull ReadOnlyMap<Key<?>, Object> options = new ReadOnlyMapFacade<>(new LinkedHashMap<>());

    public AbstractInputFormat() {
    }

    @NonNull
    @Override
    public ReadOnlyMap<Key<?>, Object> getOptions() {
        return options;
    }

    @Override
    public void setOptions(@NonNull ReadOnlyMap<Key<?>, Object> options) {
        this.options = options;
    }
}
