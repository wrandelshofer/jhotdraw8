/*
 * @(#)SetValueMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.icollection.ChampVectorSet;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.persistent.PersistentSequencedSet;
import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jhotdraw8.icollection.readable.ReadableMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * SetValueMapAccessor.
 *
 * @param <E> the value type
 */
public class SetValueMapAccessor<E> implements CompositeMapAccessor<Boolean> {

    private final MapAccessor<PersistentSet<E>> setAccessor;
    private final @Nullable E value;
    private final boolean defaultValue;
    private final String name;
    private final boolean isTransient;

    public SetValueMapAccessor(String name, boolean isTransient, MapAccessor<PersistentSet<E>> setAccessor, @Nullable E value, boolean defaultValue) {
        Objects.requireNonNull(value, "value");
        this.setAccessor = setAccessor;
        this.value = value;
        this.defaultValue = defaultValue;
        this.name = name;
        this.isTransient = isTransient;
    }

    public SetValueMapAccessor(String name, MapAccessor<PersistentSet<E>> setAccessor, E value) {
        this(name, false, setAccessor, value, false);
    }

    @Override
    public Boolean get(Map<? super Key<?>, Object> a) {
        PersistentSet<E> es = setAccessor.get(a);
        return es != null && es.contains(value);
    }

    @Override
    public Boolean get(ReadableMap<? super Key<?>, Object> a) {
        PersistentSet<E> es = setAccessor.get(a);
        return es != null && es.contains(value);
    }

    @Override
    public Boolean getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PersistentSequencedSet<MapAccessor<?>> getSubAccessors() {
        return ChampVectorSet.of(setAccessor);
    }

    @Override
    public Class<Boolean> getValueType() {
        return Boolean.class;
    }

    @Override
    public boolean isTransient() {
        return isTransient;
    }

    @Override
    public Boolean put(Map<? super Key<?>, Object> a, @Nullable Boolean value) {
        PersistentSet<E> set = setAccessor.get(a);
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
    public PersistentMap<Key<?>, Object> put(PersistentMap<Key<?>, Object> a, @Nullable Boolean value) {
        PersistentSet<E> set = setAccessor.get(a);
        assert set != null;
        if (value != null && value) {
            set = set.add(this.value);
        } else {
            set = set.remove(this.value);
        }
        return setAccessor.put(a, set);
    }

    @Override
    public Boolean remove(Map<? super Key<?>, Object> a) {
        return put(a, false);
    }

    @Override
    public PersistentMap<Key<?>, Object> remove(PersistentMap<Key<?>, Object> a) {
        return setAccessor.remove(a);
    }
}
