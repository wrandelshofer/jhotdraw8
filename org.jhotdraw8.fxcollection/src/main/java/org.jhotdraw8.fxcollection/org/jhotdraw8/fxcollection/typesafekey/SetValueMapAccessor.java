/*
 * @(#)SetValueMapAccessor.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.immutable.ImmutableMap;
import org.jhotdraw8.collection.immutable.ImmutableSet;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;

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
    private final @NonNull MapAccessor<ImmutableSet<E>> setAccessor;
    private final @Nullable E value;
    private final boolean defaultValue;
    private final @NonNull String name;
    private final boolean isTransient;

    public SetValueMapAccessor(@NonNull String name, boolean isTransient, @NonNull MapAccessor<ImmutableSet<E>> setAccessor, @Nullable E value, boolean defaultValue) {
        Objects.requireNonNull(value, "value");
        this.setAccessor = setAccessor;
        this.value = value;
        this.defaultValue = defaultValue;
        this.name = name;
        this.isTransient = isTransient;
    }

    public SetValueMapAccessor(@NonNull String name, @NonNull MapAccessor<ImmutableSet<E>> setAccessor, E value) {
        this(name, false, setAccessor, value, false);
    }

    @Override
    public Boolean get(@NonNull Map<? super Key<?>, Object> a) {
        ImmutableSet<E> es = setAccessor.get(a);
        return es != null && es.contains(value);
    }

    @Override
    public Boolean get(@NonNull ReadOnlyMap<? super Key<?>, Object> a) {
        ImmutableSet<E> es = setAccessor.get(a);
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
        ImmutableSet<E> set = setAccessor.get(a);
        assert set != null;
        boolean oldValue = set.contains(this.value);
        if (value != null && value) {
            set = set.add(this.value);
        } else {
            set = set.remove(this.value);
        }
        setAccessor.put(a, set);
        return oldValue;
    }

    @Override
    public @NonNull ImmutableMap<Key<?>, Object> put(@NonNull ImmutableMap<Key<?>, Object> a, @Nullable Boolean value) {
        ImmutableSet<E> set = setAccessor.get(a);
        assert set != null;
        if (value != null && value) {
            set = set.add(this.value);
        } else {
            set = set.remove(this.value);
        }
        return setAccessor.put(a, set);
    }

    @Override
    public Boolean remove(@NonNull Map<? super Key<?>, Object> a) {
        return put(a, false);
    }

    @Override
    public @NonNull ImmutableMap<Key<?>, Object> remove(@NonNull ImmutableMap<Key<?>, Object> a) {
        return setAccessor.remove(a);
    }
}
