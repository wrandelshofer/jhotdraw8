/*
 * @(#)KeyMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.collection.key.Key;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Keys are instances of {@link Key}.
 * Values are integers {@literal >= 0}.
 */
public class KeyMap implements Map<Key<?>, Integer> {
    private int size;
    /**
     * Stores stuffed values which are value + 1,
     * so that we can initialize the table with 0.
     */
    private int[] table = new int[0];
    private final Map<Key<?>, Integer> backingMap;

    public KeyMap(int initialCapacity) {
        this.table = new int[initialCapacity];
        backingMap = new LinkedHashMap<>(initialCapacity * 2);
    }


    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return table[((Key) key).ordinal()] != 0;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer get(Object key) {
        int index = ((Key) key).ordinal();
        int stuffedValue = index >= table.length ? 0 : table[index];
        return stuffedValue == 0 ? null : stuffedValue - 1;
    }

    @Override
    public Integer put(Key<?> key, Integer value) {
        int index = ((Key) key).ordinal();
        if (index >= table.length) {
            resize(index + 1);
        }
        int oldStuffedValue = table[index];
        int newStuffedValue = value == null ? 0 : value + 1;
        table[index] = newStuffedValue;

        if (oldStuffedValue == 0) {
            if (newStuffedValue != 0) {
                size++;
            }
        } else {
            if (newStuffedValue == 0) {
                size--;
            }
        }
        backingMap.put(key, value);
        return oldStuffedValue == 0 ? null : oldStuffedValue - 1;
    }

    private void resize(int newSize) {
        int[] old = table;
        table = new int[newSize];
        System.arraycopy(old, 0, table, 0, Math.min(old.length, newSize));
    }

    @Override
    public Integer remove(Object key) {
        int index = ((Key) key).ordinal();
        int oldStuffedValue = table[index];
        table[index] = 0;
        if (oldStuffedValue != 0) {
            size--;
        }
        return oldStuffedValue == 0 ? null : oldStuffedValue - 1;
    }

    @Override
    public void putAll(Map<? extends Key<?>, ? extends Integer> m) {
        for (Entry<? extends Key<?>, ? extends Integer> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        Arrays.fill(table, 0);
        size = 0;
    }

    @Override
    public Set<Key<?>> keySet() {
        return backingMap.keySet();
    }

    @Override
    public Collection<Integer> values() {
        return backingMap.values();
    }

    @Override
    public Set<Entry<Key<?>, Integer>> entrySet() {
        return backingMap.entrySet();
    }
}
