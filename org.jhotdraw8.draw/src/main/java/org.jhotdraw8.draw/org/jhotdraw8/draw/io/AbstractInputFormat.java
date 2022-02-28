package org.jhotdraw8.draw.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.Key;
import org.jhotdraw8.collection.ReadOnlyMap;
import org.jhotdraw8.collection.ReadOnlyMapWrapper;

import java.util.LinkedHashMap;

public abstract class AbstractInputFormat implements InputFormat {
    private @NonNull ReadOnlyMap<Key<?>, Object> options = new ReadOnlyMapWrapper<>(new LinkedHashMap<>());

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
