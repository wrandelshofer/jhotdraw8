package org.jhotdraw8.collection.sequenced;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.facade.SequencedSetFacade;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Spliterator;

/**
 * Abstract base class for {@link SequencedSet}s.
 *
 * @param <E> the element type
 */
public abstract class AbstractSequencedSet<E> extends AbstractSet<E> implements SequencedSet<E> {
    @Override
    public @NonNull SequencedSet<E> _reversed() {
        return new SequencedSetFacade<>(
                this::reverseIterator,
                this::reverseSpliterator,
                this::iterator,
                this::spliterator,
                this::size,
                this::contains,
                this::clear,
                this::remove,
                this::getLast, this::getFirst,
                this::reverseAdd, this::add,
                this::addLast, this::addFirst
        );
    }

    /**
     * Returns an iterator that iterates in the reverse
     * sequence over the elements contained in this collection.
     *
     * @return an iterator that iterates in reverse
     */
    protected abstract Iterator<E> reverseIterator();

    /**
     * Returns a spliterator that iterates in the reverse
     * sequence over the elements contained in this collection.
     *
     * @return a spliterator that iterates in reverse
     */
    protected abstract Spliterator<E> reverseSpliterator();

    /**
     * Adds an element to the set. If the element was not in the set,
     * it is added at the front of this collection.
     *
     * @param e the element
     * @return true if the element was not in the set
     */
    protected boolean reverseAdd(@Nullable E e) {
        boolean didNotAlreadyContain = !contains(e);
        if (didNotAlreadyContain) {
            addFirst(e);
        }
        return didNotAlreadyContain;
    }
}
