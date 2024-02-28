package org.jhotdraw8.icollection.sequenced;

import org.jhotdraw8.annotation.NonNull;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.SequencedSet;
import java.util.Spliterator;
import java.util.function.Supplier;

/**
 * Provides a reversed view on a {@link SequencedSet}.
 *
 * @param <E> the element type
 */
public class ReversedSequencedSetView<E> extends AbstractSet<E> implements SequencedSet<E> {
    private final @NonNull SequencedSet<E> src;
    private final @NonNull Supplier<Iterator<E>> reverseIterator;
    private final @NonNull Supplier<Spliterator<E>> reverseSpliterator;

    /**
     * Constructs a new instance.
     *
     * @param src                the source set
     * @param reverseIterator    the reverse iterator
     * @param reverseSpliterator the reverse spliterator
     */
    public ReversedSequencedSetView(@NonNull SequencedSet<E> src,
                                    @NonNull Supplier<Iterator<E>> reverseIterator,
                                    @NonNull Supplier<Spliterator<E>> reverseSpliterator) {
        this.src = src;
        this.reverseIterator = reverseIterator;
        this.reverseSpliterator = reverseSpliterator;
    }

    @Override
    public boolean equals(Object o) {
        return src.equals(o);
    }

    @Override
    public int hashCode() {
        return src.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return src.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return src.contains(o);
    }

    @Override
    public boolean add(E e) {
        if (src.contains(e)) {
            return false;
        }
        src.addFirst(e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return src.remove(o);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return src.containsAll(c);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return src.retainAll(c);
    }

    @Override
    public void clear() {
        src.clear();
    }

    @Override
    public Spliterator<E> spliterator() {
        return reverseSpliterator.get();
    }

    @Override
    public E getFirst() {
        return src.getLast();
    }

    @Override
    public E getLast() {
        return src.getFirst();
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return reverseIterator.get();
    }

    @Override
    public int size() {
        return src.size();
    }

    @Override
    public void addFirst(E e) {
        src.addLast(e);
    }

    @Override
    public void addLast(E e) {
        src.addFirst(e);
    }

    @Override
    public E removeFirst() {
        return src.removeLast();
    }

    @Override
    public E removeLast() {
        return src.removeFirst();
    }

    @Override
    public @NonNull SequencedSet<E> reversed() {
        return src;
    }
}
