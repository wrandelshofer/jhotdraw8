/*
 * @(#)AbstractInputFormat.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.io;

import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.icollection.PersistentHashMap;
import org.jhotdraw8.icollection.persistent.PersistentMap;

public abstract class AbstractInputFormat implements InputFormat {
    private PersistentMap<Key<?>, Object> options = PersistentHashMap.of();

    public AbstractInputFormat() {
    }

    @Override
    public PersistentMap<Key<?>, Object> getOptions() {
        return options;
    }

    @Override
    public void setOptions(PersistentMap<Key<?>, Object> options) {
        this.options = options;
    }
}
