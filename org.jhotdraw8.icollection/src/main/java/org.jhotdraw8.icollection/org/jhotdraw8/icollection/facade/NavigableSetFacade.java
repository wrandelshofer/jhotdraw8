package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.impl.iteration.MappedIterator;
import org.jhotdraw8.icollection.impl.iteration.MappedSpliterator;
import org.jhotdraw8.icollection.navigable.DescendingNavigableSetView;
import org.jhotdraw8.icollection.navigable.SubsetNavigableSetView;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class NavigableSetFacade<E> extends SequencedSetFacade<E> implements NavigableSet<E> {
    private final @NonNull IntSupplier modCount = () -> 0;


    @SuppressWarnings("ReturnOfNull")
    private final @NonNull Supplier<Comparator<? super E>> comparatorSupplier = () -> null;
    private final Function<E, E> higherFunction;
    private final Function<E, E> lowerFunction;
    private final Function<E, E> floorFunction;
    private final Function<E, E> ceilingFunction;


    public NavigableSetFacade(@NonNull Supplier<Iterator<E>> iteratorFunction, @NonNull Supplier<Spliterator<E>> spliteratorFunction, @NonNull Supplier<Iterator<E>> reverseIteratorFunction, @NonNull Supplier<Spliterator<E>> reverseSpliteratorFunction, @NonNull IntSupplier sizeFunction, @NonNull Predicate<Object> containsFunction, @Nullable Runnable clearFunction, @Nullable Predicate<Object> removeFunction, @Nullable Supplier<E> getFirstFunction, @Nullable Supplier<E> getLastFunction, @Nullable Predicate<E> addFunction, @Nullable Predicate<E> reversedAddFunction, @Nullable Consumer<E> addFirstFunction, @Nullable Consumer<E> addLastFunction,
                              @NonNull Function<E, E> higherFunction,
                              @NonNull Function<E, E> lowerFunction,
                              @NonNull Function<E, E> floorFunction,
                              @NonNull Function<E, E> ceilingFunction) {
        super(iteratorFunction, spliteratorFunction, reverseIteratorFunction, reverseSpliteratorFunction, sizeFunction, containsFunction, clearFunction, removeFunction, getFirstFunction, getLastFunction, addFunction, reversedAddFunction, addFirstFunction, addLastFunction);
        this.higherFunction = higherFunction;
        this.lowerFunction = lowerFunction;
        this.floorFunction = floorFunction;
        this.ceilingFunction = ceilingFunction;
    }

    @SuppressWarnings({"SuspiciousMethodCalls", "ReturnOfNull"})
    public static <K, V> @NonNull NavigableSet<K> createKeySet(@NonNull NavigableMap<K, V> m) {
        return new NavigableSetFacade<>(
                () -> new MappedIterator<>(m.sequencedEntrySet().iterator(), Map.Entry::getKey),
                () -> new MappedSpliterator<>(m.sequencedEntrySet().spliterator(), Map.Entry::getKey,
                        Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.ORDERED, null),
                () -> new MappedIterator<>(m.reversed().sequencedEntrySet().iterator(), Map.Entry::getKey),
                () -> new MappedSpliterator<>(m.reversed().sequencedEntrySet().spliterator(), Map.Entry::getKey,
                        Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.ORDERED, null),
                m::size,
                m::containsKey,
                m::clear,
                o -> {
                    if (m.containsKey(o)) {
                        m.remove(o);
                        return true;
                    }
                    return false;
                },
                () -> {
                    Map.Entry<K, V> e = m.firstEntry();
                    if (e == null) throw new NoSuchElementException();
                    return e.getKey();
                },
                () -> {
                    Map.Entry<K, V> e = m.lastEntry();
                    if (e == null) throw new NoSuchElementException();
                    return e.getKey();
                },
                null, null, null, null,
                e -> {
                    var entry = m.higherEntry(e);
                    return entry == null ? null : entry.getKey();
                },
                e -> {
                    var entry = m.lowerEntry(e);
                    return entry == null ? null : entry.getKey();
                },
                e -> {
                    var entry = m.floorEntry(e);
                    return entry == null ? null : entry.getKey();
                },
                e -> {
                    var entry = m.ceilingEntry(e);
                    return entry == null ? null : entry.getKey();
                }
        );
    }

    @Nullable
    @Override
    public E ceiling(E e) {
        return ceilingFunction.apply(e);
    }

    @Nullable
    @Override
    public Comparator<? super E> comparator() {
        return comparatorSupplier.get();
    }

    @NonNull
    @Override
    public Iterator<E> descendingIterator() {
        return reversed().iterator();
    }

    @NonNull
    @Override
    public NavigableSet<E> descendingSet() {
        return reversed();
    }

    @Override
    public E first() {
        return getFirst();
    }

    @Nullable
    @Override
    public E floor(E e) {
        return floorFunction.apply(e);
    }

    @NonNull
    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new SubsetNavigableSetView<>(this, modCount,
                true, null, true, false, toElement, inclusive, true);
    }

    @NonNull
    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Nullable
    @Override
    public E higher(E e) {
        return higherFunction.apply(e);
    }

    @Override
    public E last() {
        return getLast();
    }

    @Nullable
    @Override
    public E lower(E e) {
        return lowerFunction.apply(e);
    }

    @Nullable
    @Override
    public E pollFirst() {
        return isEmpty() ? null : getFirst();
    }

    @Nullable
    @Override
    public E pollLast() {
        return isEmpty() ? null : getLast();
    }

    @NonNull
    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new SubsetNavigableSetView<>(this, modCount,
                false, fromElement, fromInclusive, false, toElement, toInclusive, true);
    }

    @NonNull
    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @NonNull
    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new SubsetNavigableSetView<>(this, modCount,
                false, fromElement, inclusive, true, null, true, true);
    }

    @NonNull
    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public @NonNull NavigableSet<E> reversed() {
        return new DescendingNavigableSetView<>(this, modCount);
    }
}
