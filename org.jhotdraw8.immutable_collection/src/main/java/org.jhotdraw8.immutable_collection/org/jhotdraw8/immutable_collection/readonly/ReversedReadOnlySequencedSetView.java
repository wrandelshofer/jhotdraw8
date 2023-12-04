package org.jhotdraw8.immutable_collection.readonly;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.immutable_collection.sequenced.SequencedSet;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Supplier;

/**
 * Provides a reversed view on a {@link SequencedSet}.
 *
 * @param <E> the element type
 */
public class ReversedReadOnlySequencedSetView<E> extends AbstractReadOnlySet<E> implements ReadOnlySequencedSet<E> {
    private final @NonNull ReadOnlySequencedSet<E> src;
    private final @NonNull Supplier<Iterator<E>> reverseIterator;
    private final @NonNull Supplier<Spliterator<E>> reverseSpliterator;

    /**
     * Constructs a new instance.
     *
     * @param src                the source set
     * @param reverseIterator    the reverse iterator
     * @param reverseSpliterator the reverse spliterator
     */
    public ReversedReadOnlySequencedSetView(@NonNull ReadOnlySequencedSet<E> src,
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
    public boolean containsAll(@NonNull Iterable<?> c) {
        return src.containsAll(c);
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
    public Iterator<E> iterator() {
        return reverseIterator.get();
    }

    @Override
    public int size() {
        return src.size();
    }


    @Override
    public @NonNull ReadOnlySequencedSet<E> readOnlyReversed() {
        return src;
    }
}
