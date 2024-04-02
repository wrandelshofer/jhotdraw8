package org.jhotdraw8.icollection.navigable;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.impl.IdentityObject;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.function.IntSupplier;

public class SubsetNavigableSetView<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final @NonNull NavigableSet<E> src;
    private final @NonNull IntSupplier modCount;
    private final boolean fromStart;
    private final @Nullable E fromElement;
    private final boolean fromInclusive;
    private final boolean toEnd;
    private final @Nullable E toElement;
    private final boolean toInclusive;
    private final boolean nullFirst;

    /**
     * Constructs a new instance.
     *
     * @param src the source set
     */
    public SubsetNavigableSetView(@NonNull NavigableSet<E> src, @NonNull IntSupplier modCount, boolean fromStart, @Nullable E fromElement, boolean fromInclusive, boolean toEnd, @Nullable E toElement, boolean toInclusive, boolean nullFirst) {
        this.src = src;
        this.modCount = modCount;
        this.fromStart = fromStart;
        this.fromElement = fromElement;
        this.fromInclusive = fromInclusive;
        this.toEnd = toEnd;
        this.toElement = toElement;
        this.toInclusive = toInclusive;
        this.nullFirst = nullFirst;
    }

    @SuppressWarnings("unchecked")
    private int compare(@Nullable E a, @Nullable E b) {
        Comparator<? super E> comparator = src.comparator();
        if (comparator == null) {
            if (a == null) {
                return (b == null) ? 0 : (nullFirst ? -1 : 1);
            } else if (b == null) {
                return nullFirst ? 1 : -1;
            } else {
                return ((Comparable<? super E>) a).compareTo(b);
            }
        }
        return comparator.compare(a, b);
    }

    private boolean tooLow(@Nullable E key) {
        if (!fromStart) {
            int c = compare(key, fromElement);
            if (c < 0 || (c == 0 && !fromInclusive)) {
                return true;
            }
        }
        return false;
    }

    private boolean tooHigh(@Nullable E key) {
        if (!toEnd) {
            int c = compare(key, toElement);
            if (c > 0 || (c == 0 && !toInclusive)) {
                return true;
            }
        }
        return false;
    }

    private @Nullable E lowest() {
        E e = (fromStart ? src.first() :
                (fromInclusive ? src.ceiling(fromElement) :
                        src.higher(fromElement)));
        return (e == null || tooHigh(e)) ? null : e;
    }

    private @Nullable E highest() {
        E e = (toEnd ? src.last() :
                (toInclusive ? src.floor(toElement) :
                        src.lower(toElement)));
        return (e == null || tooLow(e)) ? null : e;
    }

    @Nullable
    @Override
    public E lower(E e) {
        if (tooHigh(e)) {
            return highest();
        }
        e = src.lower(e);
        return (e == null || tooLow(e)) ? null : e;
    }

    @Nullable
    @Override
    public E floor(E e) {
        if (tooHigh(e)) {
            return highest();
        }
        e = src.floor(e);
        return (e == null || tooLow(e)) ? null : e;
    }

    @Nullable
    @Override
    public E ceiling(E e) {
        if (tooLow(e)) {
            return lowest();
        }
        e = src.ceiling(e);
        return (e == null || tooHigh(e)) ? null : e;
    }

    @Nullable
    @Override
    public E higher(E e) {
        if (tooLow(e)) {
            return lowest();
        }
        e = src.higher(e);
        return (e == null || tooHigh(e)) ? null : e;
    }

    @Nullable
    @Override
    public E pollFirst() {
        return lowest();
    }

    @Nullable
    @Override
    public E pollLast() {
        return highest();
    }

    @Override
    public int size() {
        return (fromStart && toEnd) ? src.size() : countElements();
    }

    private int countElements() {
        int size = 0;
        for (E e : this) {
            size++;
        }
        return size;
    }

    @SuppressWarnings("unchecked")
    private boolean inRange(Object key) {
        return !tooLow((E) key) && !tooHigh((E) key);
    }

    private @Nullable E iteratorHighFence() {
        return (toEnd ? null : (toInclusive ?
                src.higher(toElement) :
                src.ceiling(toElement)));
    }

    private @Nullable E iteratorLowFence() {
        return (fromStart ? null : (fromInclusive ?
                src.lower(fromElement) :
                src.floor(fromElement)));
    }
    @Override
    public boolean contains(Object o) {
        return inRange(o) && src.contains(o);
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return new SubsetIterator(lowest(), iteratorHighFence(), src.iterator());
    }


    @Override
    public boolean add(E e) {
        if (!inRange(e)) {
            throw new IllegalArgumentException("element out of range");
        }
        return src.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return inRange(o) && src.remove(o);
    }

    @Override
    public void clear() {
        if (fromStart && toEnd) {
            src.clear();
        } else {
            for (Object o : toArray()) {
                src.remove(o);
            }
        }
    }

    @NonNull
    @Override
    public NavigableSet<E> descendingSet() {
        return new DescendingNavigableSetView<>(this, modCount);
    }

    @NonNull
    @Override
    public Iterator<E> descendingIterator() {
        return new SubsetIterator(highest(), iteratorLowFence(), src.descendingIterator());
    }

    private boolean inClosedRange(E e) {
        return (fromStart || compare(e, fromElement) >= 0)
                && (toEnd || compare(toElement, e) >= 0);
    }

    private boolean inRange(E e, boolean inclusive) {
        return inclusive ? inRange(e) : inClosedRange(e);
    }
    @NonNull
    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        if (!inRange(fromElement, fromInclusive)) {
            throw new IllegalArgumentException("fromElement out of range");
        }
        if (!inRange(toElement, toInclusive)) {
            throw new IllegalArgumentException("toElement out of range");
        }
        return new SubsetNavigableSetView<>(src, modCount,
                false, fromElement, fromInclusive,
                false, toElement, toInclusive,
                nullFirst);
    }

    @NonNull
    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        if (!inRange(toElement, toInclusive)) {
            throw new IllegalArgumentException("toElement out of range");
        }
        return new SubsetNavigableSetView<>(src, modCount,
                fromStart, fromElement, fromInclusive,
                false, toElement, toInclusive,
                nullFirst);
    }

    @NonNull
    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        if (!inRange(fromElement, fromInclusive)) {
            throw new IllegalArgumentException("fromElement out of range");
        }
        return new SubsetNavigableSetView<>(src, modCount,
                false, fromElement, fromInclusive,
                toEnd, toElement, toInclusive,
                nullFirst);
    }

    @Nullable
    @Override
    public Comparator<? super E> comparator() {
        return src.comparator();
    }

    @NonNull
    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @NonNull
    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(fromElement, false);
    }

    @NonNull
    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public E first() {
        return lowest();
    }

    @Override
    public E last() {
        return highest();
    }

    private class SubsetIterator implements Iterator<E> {
        boolean hasLastReturned;
        E lastReturned;
        E next;
        boolean hasNext;
        final Object fenceKey;
        int expectedModCount;
        Iterator<E> srcIterator;

        SubsetIterator(@Nullable E first,
                       @Nullable E fence,
                       Iterator<E> srcIterator) {
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

        public final E next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            if (modCount.getAsInt() != expectedModCount) {
                throw new ConcurrentModificationException();
            }

            E e = next;

            hasNext = srcIterator.hasNext();
            if (hasNext) {
                next = srcIterator.next();
                if (next == fenceKey) {
                    hasNext = false;
                }
            }

            hasLastReturned = true;
            lastReturned = e;
            return e;
        }

        public void remove() {
            if (!hasLastReturned) {
                throw new IllegalStateException();
            }
            if (modCount.getAsInt() != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            // FIXME A call to src.remove() breaks the srcIterator
            src.remove(lastReturned);
            lastReturned = null;
            hasLastReturned = false;
            expectedModCount = modCount.getAsInt();
        }
    }
}
