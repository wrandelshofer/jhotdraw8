/*
 * @(#)SetValueMapAccessor.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.CompositeMapAccessor;
import org.jhotdraw8.collection.MapAccessor;
import org.jhotdraw8.collection.PersistentSet;
import org.jhotdraw8.collection.ReadOnlyMap;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * SetValueMapAccessor.
 *
 * @author Werner Randelshofer
 */
public class SetValueMapAccessor<E> implements CompositeMapAccessor<Boolean> {
    private static final long serialVersionUID = 1L;
    private final @NonNull MapAccessor<PersistentSet<E>> setAccessor;
    private final @Nullable E value;
    private boolean defaultValue;
    private final @NonNull String name;
    private final boolean isTransient;

    public SetValueMapAccessor(@NonNull String name, boolean isTransient, @NonNull MapAccessor<PersistentSet<E>> setAccessor, @Nullable E value, boolean defaultValue) {
        Objects.requireNonNull(value, "value");
        this.setAccessor = setAccessor;
        this.value = value;
        this.defaultValue = defaultValue;
        this.name = name;
        this.isTransient = isTransient;
    }

    public SetValueMapAccessor(@NonNull String name, @NonNull MapAccessor<PersistentSet<E>> setAccessor, E value) {
        this(name, false, setAccessor, value, false);
    }

    @Override
    public Boolean get(@NonNull Map<? super Key<?>, Object> a) {
        PersistentSet<E> es = setAccessor.get(a);
        return es != null && es.contains(value);
    }

    @Override
    public Boolean get(@NonNull ReadOnlyMap<? super Key<?>, Object> a) {
        PersistentSet<E> es = setAccessor.get(a);
        return es != null && es.contains(value);
    }

    @Override
    public Boolean getDefaultValue() {
        return defaultValue;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public @NonNull Set<MapAccessor<?>> getSubAccessors() {
        return Collections.singleton(setAccessor);
    }

    @Override
    public @NonNull Class<Boolean> getValueType() {
        return Boolean.class;
    }

    @Override
    public boolean isTransient() {
        return isTransient;
    }

    @Override
    public Boolean put(@NonNull Map<? super Key<?>, Object> a, @Nullable Boolean value) {
        PersistentSet<E> set = setAccessor.get(a);
        assert set != null;
        boolean oldValue = set.contains(this.value);
        if (value != null && value) {
            set = set.copyAdd(this.value);
        } else {
            set = set.copyRemove(this.value);
        }
        setAccessor.put(a, set);
        return oldValue;
    }

    @Override
    public Boolean remove(@NonNull Map<? super Key<?>, Object> a) {
        return put(a, false);
    }

}
