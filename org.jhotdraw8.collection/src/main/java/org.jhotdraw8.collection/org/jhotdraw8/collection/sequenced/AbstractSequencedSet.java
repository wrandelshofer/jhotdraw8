package org.jhotdraw8.collection.sequenced;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.facade.SequencedSetFacade;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Spliterator;

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

    protected abstract Iterator<E> reverseIterator();

    protected abstract Spliterator<E> reverseSpliterator();

    protected boolean reverseAdd(@Nullable E e) {
        boolean didNotAlreadyContain = !contains(e);
        if (didNotAlreadyContain) {
            addFirst(e);
        }
        return didNotAlreadyContain;
    }
}
