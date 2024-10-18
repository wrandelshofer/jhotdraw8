package org.jhotdraw8.icollection.navigable;

import org.jhotdraw8.icollection.MutableMapEntry;
import org.jhotdraw8.icollection.facade.NavigableSetFacade;
import org.jhotdraw8.icollection.facade.SetFacade;
import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.MappedIterator;
import org.jhotdraw8.icollection.readable.ReadableMap;
import org.jspecify.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntSupplier;

public class SubsetNavigableMapView<K, V> extends AbstractMap<K, V> implements ReadableMap<K, V>, NavigableMap<K, V> {
    private final NavigableMap<K, V> src;
    private final IntSupplier modCount;
    private final boolean fromStart;
    private final @Nullable K fromKey;
    private final boolean fromInclusive;
    private final boolean toEnd;
    private final @Nullable K toKey;
    private final boolean toInclusive;
    private final boolean nullFirst;

    /**
     * Constructs a new instance.
     *
     * @param src the source set
     */
    public SubsetNavigableMapView(NavigableMap<K, V> src, IntSupplier modCount, boolean fromStart, @Nullable K fromKey, boolean fromInclusive, boolean toEnd, @Nullable K toKey, boolean toInclusive, boolean nullFirst) {
        this.src = src;
        this.modCount = modCount;
        this.fromStart = fromStart;
        this.fromKey = fromKey;
        this.fromInclusive = fromInclusive;
        this.toEnd = toEnd;
        this.toKey = toKey;
        this.toInclusive = toInclusive;
        this.nullFirst = nullFirst;
    }

    @Override
    public Map.@Nullable Entry<K, V> ceilingEntry(K k) {
        if (tooLow(k)) {
            return lowestEntry();
        }
        var e = src.ceilingEntry(k);
        return (e == null || tooHigh(e.getKey())) ? null : e;
    }

    @Override
    public @Nullable K ceilingKey(K key) {
        var e = ceilingEntry(key);
        return e == null ? null : e.getKey();
    }

    @Override
    public void clear() {
        if (fromStart && toEnd) {
            src.clear();
        } else {
            for (Object o : keySet().toArray()) {
                src.remove(o);
            }
        }
    }

    @Nullable
    @Override
    public Comparator<? super K> comparator() {
        return src.comparator();
    }

    @SuppressWarnings("unchecked")
    private int compare(@Nullable K a, @Nullable K b) {
        Comparator<? super K> comparator = src.comparator();
        if (comparator == null) {
            if (a == null) {
                return (b == null) ? 0 : (nullFirst ? -1 : 1);
            } else if (b == null) {
                return nullFirst ? 1 : -1;
            } else {
                return ((Comparable<? super K>) a).compareTo(b);
            }
        }
        return comparator.compare(a, b);
    }

    @Override
    public boolean containsKey(Object o) {
        return inRange(o) && src.containsKey(o);
    }

    private int countElements() {
        int size = 0;
        for (var e : this.entrySet()) {
            size++;
        }
        return size;
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return navigableKeySet().reversed();
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        return new DescendingNavigableMapView<>(this, modCount);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new SetFacade<>(
                this::iterator,
                () -> Spliterators.spliterator(iterator(), size(), Spliterator.DISTINCT | Spliterator.SIZED),
                this::size,
                this::containsEntry,
                this::clear,
                null,
                this::removeEntry
        );
    }

    @Override
    public Entry<K, V> firstEntry() {
        return src.firstEntry();
    }

    @Override
    public @Nullable K firstKey() {
        Entry<K, V> entry = lowestEntry();
        if (entry == null) {
            throw new NoSuchElementException();
        }
        return entry.getKey();
    }

    @Override
    public Map.@Nullable Entry<K, V> floorEntry(K k) {
        if (tooHigh(k)) {
            return highestEntry();
        }
        Map.@Nullable Entry<K, V> e = src.floorEntry(k);
        return (e == null || tooLow(e.getKey())) ? null : e;
    }

    @Override
    public @Nullable K floorKey(K key) {
        var e = floorEntry(key);
        return e == null ? null : e.getKey();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return super.getOrDefault(key, defaultValue);
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        if (!inRange(toKey, toInclusive)) {
            throw new IllegalArgumentException("toKey out of range");
        }
        return new SubsetNavigableMapView<>(src, modCount,
                fromStart, fromKey, fromInclusive,
                false, toKey, toInclusive,
                nullFirst);
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return headMap(fromKey, false);
    }

    @Override
    public Map.@Nullable Entry<K, V> higherEntry(K k) {
        if (tooLow(k)) {
            return lowestEntry();
        }
        var e = src.higherEntry(k);
        return (e == null || tooHigh(e.getKey())) ? null : e;
    }

    @Override
    public @Nullable K higherKey(K key) {
        var e = higherEntry(key);
        return e == null ? null : e.getKey();
    }

    private Map.@Nullable Entry<K, V> highestEntry() {
        Map.Entry<K, V> e = (toEnd ? src.lastEntry() :
                (toInclusive ? src.floorEntry(toKey) :
                        src.lowerEntry(toKey)));
        return (e == null || tooLow(e.getKey())) ? null : e;
    }

    private boolean inClosedRange(K e) {
        return (fromStart || compare(e, fromKey) >= 0)
                && (toEnd || compare(toKey, e) >= 0);
    }

    @SuppressWarnings("unchecked")
    private boolean inRange(Object key) {
        return !tooLow((K) key) && !tooHigh((K) key);
    }

    private boolean inRange(K e, boolean inclusive) {
        return inclusive ? inRange(e) : inClosedRange(e);
    }

    public Iterator<Entry<K, V>> iterator() {
        return new FailFastIterator<>(
                new MappedIterator<>(new SubsetIterator(lowestKey(), iteratorHighFence(), src.entrySet().iterator()),
                        e -> new MutableMapEntry<>(this::iteratorPutIfPresent, e.getKey(), e.getValue())),
                this::iteratorRemove, modCount
        );
    }

    private @Nullable K iteratorHighFence() {
        return (toEnd ? null : (toInclusive ?
                src.higherEntry(toKey) :
                src.ceilingEntry(toKey)).getKey());
    }

    private @Nullable K iteratorLowFence() {
        return (fromStart ? null : (fromInclusive ?
                src.lowerEntry(fromKey) :
                src.floorEntry(fromKey)).getKey());
    }

    private void iteratorPutIfPresent(@Nullable K k, @Nullable V v) {
        if (containsKey(k)) {
            put(k, v);
        }
    }

    private void iteratorRemove(Map.Entry<K, V> entry) {
        remove(entry.getKey());
    }

    @Override
    public Entry<K, V> lastEntry() {
        return highestEntry();
    }

    @Override
    public @Nullable K lastKey() {
        Entry<K, V> entry = highestEntry();
        if (entry == null) {
            throw new NoSuchElementException();
        }
        return entry.getKey();
    }

    @Override
    public Map.@Nullable Entry<K, V> lowerEntry(K k) {
        if (tooHigh(k)) {
            return highestEntry();
        }
        Map.@Nullable Entry<K, V> e = src.lowerEntry(k);
        return (e == null || tooLow(e.getKey())) ? null : e;
    }

    @Override
    public @Nullable K lowerKey(K key) {
        var e = lowerEntry(key);
        return e == null ? null : e.getKey();
    }

    private Map.@Nullable Entry<K, V> lowestEntry() {
        Map.Entry<K, V> e = (fromStart ? src.firstEntry() :
                (fromInclusive ? src.ceilingEntry(fromKey) :
                        src.higherEntry(fromKey)));
        return (e == null || tooHigh(e.getKey())) ? null : e;
    }

    private @Nullable K lowestKey() {
        var entry = lowestEntry();
        return entry == null ? null : entry.getKey();
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return NavigableSetFacade.createKeySet(this);
    }

    @Override
    public Map.@Nullable Entry<K, V> pollFirstEntry() {
        return lowestEntry();
    }

    @Override
    public Map.@Nullable Entry<K, V> pollLastEntry() {
        return highestEntry();
    }

    @Override
    public V put(K e, V v) {
        if (!inRange(e)) {
            throw new IllegalArgumentException("element out of range");
        }
        return src.put(e, v);
    }

    @Override
    public @Nullable V remove(Object o) {
        return inRange(o) ? src.remove(o) : null;
    }

    /**
     * Removes the specified entry from the map.
     *
     * @param o an entry (should be a {@link Map.Entry}).
     * @return true if the element was contained in the map
     */
    @SuppressWarnings("unchecked")
    private boolean removeEntry(@Nullable Object o) {
        if (containsEntry(o)) {
            assert o != null;
            remove(((Entry<K, V>) o).getKey());
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return (fromStart && toEnd) ? src.size() : countElements();
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        if (!inRange(fromKey, fromInclusive)) {
            throw new IllegalArgumentException("fromKey out of range");
        }
        if (!inRange(toKey, toInclusive)) {
            throw new IllegalArgumentException("toKey out of range");
        }
        return new SubsetNavigableMapView<>(src, modCount,
                false, fromKey, fromInclusive,
                false, toKey, toInclusive,
                nullFirst);
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        if (!inRange(fromKey, fromInclusive)) {
            throw new IllegalArgumentException("fromKey out of range");
        }
        return new SubsetNavigableMapView<>(src, modCount,
                false, fromKey, fromInclusive,
                toEnd, toKey, toInclusive,
                nullFirst);
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return tailMap(fromKey, true);
    }

    private boolean tooHigh(@Nullable K key) {
        if (!toEnd) {
            int c = compare(key, toKey);
            if (c > 0 || (c == 0 && !toInclusive)) {
                return true;
            }
        }
        return false;
    }

    private boolean tooLow(@Nullable K key) {
        if (!fromStart) {
            int c = compare(key, fromKey);
            if (c < 0 || (c == 0 && !fromInclusive)) {
                return true;
            }
        }
        return false;
    }

    private class SubsetIterator implements Iterator<Map.Entry<K, V>> {
        final Object fenceKey;
        Map.Entry<K, V> lastReturned;
        Map.Entry<K, V> next;
        boolean hasNext;
        int expectedModCount;
        Iterator<Map.Entry<K, V>> srcIterator;

        SubsetIterator(@Nullable K first,
                       @Nullable K fence,
                       Iterator<Map.Entry<K, V>> srcIterator) {
            expectedModCount = modCount.getAsInt();
            lastReturned = null;

            if (first == null) {
                hasNext = srcIterator.hasNext();
                next = srcIterator.next();
            } else {
                while (srcIterator.hasNext()) {
                    next = srcIterator.next();
                    if (next == first) {
                        hasNext = true;
                        break;
                    }
                }
            }

            fenceKey = fence == null ? new IdentityObject() : fence;
            if (next == fenceKey) {
                hasNext = false;
            }
        }

        public final boolean hasNext() {
            return hasNext;
        }

        public final Map.Entry<K, V> next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            if (modCount.getAsInt() != expectedModCount) {
                throw new ConcurrentModificationException();
            }

            Map.Entry<K, V> e = next;

            hasNext = srcIterator.hasNext();
            if (hasNext) {
                next = srcIterator.next();
                if (next == fenceKey) {
                    hasNext = false;
                }
            }

            lastReturned = e;
            return e;
        }

        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            if (modCount.getAsInt() != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            // FIXME A call to src.remove() breaks the srcIterator
            src.remove(lastReturned.getKey());
            lastReturned = null;
            expectedModCount = modCount.getAsInt();
        }
    }
}
