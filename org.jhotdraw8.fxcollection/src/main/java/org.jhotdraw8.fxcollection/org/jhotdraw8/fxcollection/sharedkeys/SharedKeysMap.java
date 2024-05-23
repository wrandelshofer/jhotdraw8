/*
 * @(#)SharedKeysMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.sharedkeys;

import javafx.beans.InvalidationListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.jspecify.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An observable map which stores its values in an array, and which shares
 * its immutable keys with other {@code SharedKeysMap} instances.
 *
 * @param <K> key type
 * @param <V> value type
 * @author Werner Randelshofer
 */
public class SharedKeysMap<K, V> extends AbstractMap<K, V> implements ObservableMap<K, V> {

    private static final Object NULL_VALUE = new Object();
    private CopyOnWriteArrayList<MapChangeListener<? super K, ? super V>> changeListenerList;
    private CopyOnWriteArrayList<InvalidationListener> invalidationListenerList;
    private final Map<K, Integer> keyMap;
    private int size;

    private final Object[] values;

    /**
     * Creates a new instance.
     *
     * @param keyMap a map which maps from keys to indices. The indices must be
     *               in the range {@code [0,keyMap.size()-1]}.
     *               <p>
     *               This map must be immutable.
     */
    public SharedKeysMap(Map<K, Integer> keyMap) {
        this.keyMap = keyMap;
        this.values = new Object[keyMap.size()];
    }

    @Override
    public void addListener(InvalidationListener listener) {
        if (invalidationListenerList == null) {
            invalidationListenerList = new CopyOnWriteArrayList<>();
        }
        invalidationListenerList.add(listener);
    }

    @Override
    public void addListener(MapChangeListener<? super K, ? super V> observer) {
        if (changeListenerList == null) {
            changeListenerList = new CopyOnWriteArrayList<>();
        }
        changeListenerList.add(observer);
    }

    private boolean hasObservers() {
        return changeListenerList != null || invalidationListenerList != null;
    }

    protected void callObservers(MapChangeListener.Change<K, V> change) {
        if (changeListenerList != null) {
            for (MapChangeListener<? super K, ? super V> l : changeListenerList) {
                l.onChanged(change);
            }
        }
        if (invalidationListenerList != null) {
            for (InvalidationListener l : invalidationListenerList) {
                l.invalidated(this);
            }
        }
    }

    @Override
    public void clear() {
        Arrays.fill(values, null);
        size = 0;
    }

    @Override
    public boolean containsKey(Object key) {
        Integer index = keyMap.get(key);

        boolean result = index != null
                && index < values.length
                && values[index] != null;
        return result;
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        if (value == null) {
            for (Object o : values) {
                if (o == NULL_VALUE) {
                    return true;
                }
            }
        } else {
            for (Object o : values) {
                if (Objects.equals(o, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable V get(Object key) {
        Integer index = keyMap.get(key);
        return index == null ? null : getValue(index, (K) key);
    }

    @SuppressWarnings("unchecked")
    private @Nullable V getValue(int index, K key) {
        Object value = index < values.length ? values[index] : null;
        return value == NULL_VALUE ? null : (V) value;
    }

    private boolean hasValue(int index) {
        return index < values.length && values[index] != null;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    @Override
    public @Nullable V put(K key, V value) {
        Integer index = keyMap.get(key);
        if (index == null) {
            throw new UnsupportedOperationException("Cannot put key=" + key + " value=" + value);
        }
        return setValue(index, key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable V remove(Object key) {
        Integer index = keyMap.get(key);
        if (index == null) {
            throw new UnsupportedOperationException("cannot remove key=" + key);
        }
        return removeValue(index, (K) key);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        if (invalidationListenerList != null) {
            invalidationListenerList.remove(listener);
        }
    }

    @Override
    public void removeListener(MapChangeListener<? super K, ? super V> observer) {
        if (changeListenerList != null) {
            changeListenerList.remove(observer);
        }
    }

    @SuppressWarnings("unchecked")
    private @Nullable V removeValue(int index, K key) {

        Object oldValue = index < values.length ? values[index] : null;
        if (oldValue == null) {
            return null;
        } else {
            values[index] = null;
            size--;
            V returnValue = (V) (oldValue == NULL_VALUE ? null : oldValue);
            if (hasObservers()) {
                @SuppressWarnings("unchecked")
                ChangeEvent change = new ChangeEvent(key, returnValue, null, false, true);
                callObservers(change);
            }
            return returnValue;
        }

    }

    @SuppressWarnings("unchecked")
    private @Nullable V setValue(int index, K key, V newValue) {
        V oldValue = (V) values[index];
        if (oldValue == null) {
            size++;
        }
        values[index] = newValue;

        V returnValue = oldValue == NULL_VALUE ? null : oldValue;
        if (hasObservers() && (oldValue == null || !Objects.equals(oldValue, newValue))) {
            ChangeEvent change = new ChangeEvent(key, returnValue, newValue, true, oldValue != null);
            callObservers(change);
        }

        return returnValue;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Collection<V> values() {
        return new ValueCollection();
    }

    private class ChangeEvent extends MapChangeListener.Change<K, V> {

        private final K key;
        private final V old;
        private final V added;
        private final boolean wasAdded;
        private final boolean wasRemoved;

        public ChangeEvent(K key, V old, V added, boolean wasAdded, boolean wasRemoved) {
            super(SharedKeysMap.this);
            assert (wasAdded || wasRemoved);
            this.key = key;
            this.old = old;
            this.added = added;
            this.wasAdded = wasAdded;
            this.wasRemoved = wasRemoved;
        }

        @Override
        public String toString() {
            return "ChangeEvent{" + "key=" + key + ", old=" + old + ", added=" + added + ", wasAdded=" + wasAdded + ", wasRemoved=" + wasRemoved + '}';
        }

        @Override
        public boolean wasAdded() {
            return wasAdded;
        }

        @Override
        public boolean wasRemoved() {
            return wasRemoved;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValueAdded() {
            return added;
        }

        @Override
        public V getValueRemoved() {
            return old;
        }

    }

    private class EntrySet extends AbstractSet<Entry<K, V>> {

        @Override
        public void clear() {
            SharedKeysMap.this.clear();
        }

        @Override
        public int size() {
            return SharedKeysMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return SharedKeysMap.this.isEmpty();
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<K, V> e = (Entry<K, V>) o;
            return SharedKeysMap.this.containsKey(e.getKey())
                    && Objects.equals(SharedKeysMap.this.get(e.getKey()), e.getValue());
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new Iterator<>() {
                private final Iterator<Entry<K, Integer>> entryIt = SharedKeysMap.this.keyMap.entrySet().iterator();
                private boolean hasNext;
                private K nextKey;
                private K lastKey;
                private int nextValue;
                private int lastValue;

                {
                    advance();
                }

                private void advance() {
                    while (entryIt.hasNext()) {
                        Entry<K, Integer> entry = entryIt.next();
                        if (hasValue(entry.getValue())) {
                            nextKey = entry.getKey();
                            nextValue = entry.getValue();
                            hasNext = true;
                            return;
                        }
                    }
                    hasNext = false;
                }

                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public Entry<K, V> next() {
                    lastKey = nextKey;
                    lastValue = nextValue;
                    advance();
                    return new MapEntry(lastKey, lastValue);
                }

                @Override
                public void remove() {
                    SharedKeysMap.this.removeValue(lastValue, lastKey);
                }

            };
        }

        @Override
        public boolean add(Entry<K, V> e) {
            boolean added = !SharedKeysMap.this.containsKey(e.getKey())
                    || Objects.equals(SharedKeysMap.this.get(e.getKey()), e.getValue());
            if (added) {
                SharedKeysMap.this.put(e.getKey(), e.getValue());
            }
            return added;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean remove(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<K, V> e = (Entry<K, V>) o;
            boolean removed = SharedKeysMap.this.containsKey(e.getKey())
                    && Objects.equals(SharedKeysMap.this.get(e.getKey()), e.getValue());
            if (removed) {
                SharedKeysMap.this.remove(e.getKey());
            }
            return removed;
        }

    }

    private class KeySet extends AbstractSet<K> {

        @Override
        public int size() {
            return SharedKeysMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return SharedKeysMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return SharedKeysMap.this.containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            return new Iterator<>() {
                private final Iterator<Entry<K, Integer>> entryIt = SharedKeysMap.this.keyMap.entrySet().iterator();
                private boolean hasNext;
                private K nextKey;
                private K currentKey;

                {
                    advance();
                }

                private void advance() {
                    while (entryIt.hasNext()) {
                        Entry<K, Integer> entry = entryIt.next();
                        if (hasValue(entry.getValue())) {
                            nextKey = entry.getKey();
                            hasNext = true;
                            return;
                        }
                    }
                    hasNext = false;
                }

                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public K next() {
                    currentKey = nextKey;
                    advance();
                    return currentKey;
                }

                @Override
                public void remove() {
                    SharedKeysMap.this.remove(currentKey);
                }

            };
        }

        @Override
        public boolean remove(Object o) {
            boolean removed = SharedKeysMap.this.containsKey(o);
            if (removed) {
                SharedKeysMap.this.remove(o);
            }
            return removed;
        }

        @Override
        public void clear() {
            SharedKeysMap.this.clear();
        }
    }

    private class MapEntry implements Entry<K, V> {

        private final K key;
        private final int index;

        public MapEntry(K key, int index) {
            this.key = key;
            this.index = index;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public @Nullable V getValue() {
            return SharedKeysMap.this.getValue(index, key);
        }

        @Override
        public @Nullable V setValue(V value) {
            V oldValue = SharedKeysMap.this.getValue(index, key);
            SharedKeysMap.this.setValue(index, key, value);
            return oldValue;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            @SuppressWarnings("unchecked")
            Map.Entry<K, V> e = (Map.Entry<K, V>) o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (Objects.equals(k1, k2)) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                return Objects.equals(v1, v2);
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return (getKey() == null ? 0 : getKey().hashCode())
                    ^ (getValue() == null ? 0 : getValue().hashCode());
        }

        @Override
        public final @Nullable String toString() {
            return getKey() + "=" + getValue();
        }

    }

    private class ValueCollection extends AbstractCollection<V> {

        public ValueCollection() {
        }

        @Override
        public int size() {
            return SharedKeysMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return SharedKeysMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return SharedKeysMap.this.containsValue(o);
        }

        @Override
        public Iterator<V> iterator() {
            return new Iterator<>() {

                private final Iterator<Entry<K, Integer>> entryIt = keyMap.entrySet().iterator();
                private boolean hasNext;
                private K nextKey;
                private K currentKey;
                private @Nullable V nextValue;
                private @Nullable V currentValue;

                {
                    advance();
                }

                private void advance() {
                    while (entryIt.hasNext()) {
                        Entry<K, Integer> entry = entryIt.next();
                        if (SharedKeysMap.this.hasValue(entry.getValue())) {
                            nextKey = entry.getKey();
                            nextValue = SharedKeysMap.this.getValue(entry.getValue(), nextKey);
                            hasNext = true;
                            return;
                        }
                    }
                    hasNext = false;
                }

                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public @Nullable V next() {
                    currentKey = nextKey;
                    currentValue = nextValue;
                    advance();
                    return currentValue;
                }

                @Override
                public void remove() {
                    SharedKeysMap.this.remove(currentKey);
                }

            };
        }

        @Override
        public void clear() {
            SharedKeysMap.this.clear();
        }

    }

}
