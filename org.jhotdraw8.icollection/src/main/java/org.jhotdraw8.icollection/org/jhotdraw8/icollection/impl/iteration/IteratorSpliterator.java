package org.jhotdraw8.icollection.impl.iteration;

import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public class IteratorSpliterator<E> extends Spliterators.AbstractSpliterator<E> {
    private final Iterator<E> iterator;
    private final @Nullable Comparator<E> comparator;

    /**
     * Creates a spliterator reporting the given estimated size and
     * additionalCharacteristics.
     *
     * @param est                       the estimated size of this spliterator if known, otherwise
     *                                  {@code Long.MAX_VALUE}.
     * @param additionalCharacteristics properties of this spliterator's
     *                                  source or elements.  If {@code SIZED} is reported then this
     *                                  spliterator will additionally report {@code SUBSIZED}.
     */
    public IteratorSpliterator(Iterator<E> iterator, long est, int additionalCharacteristics, @Nullable Comparator<E> comparator) {
        super(est, additionalCharacteristics);
        this.iterator = iterator;
        this.comparator = comparator;
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        if (iterator.hasNext()) {
            action.accept(iterator.next());
            return true;
        }
        return false;
    }

    @Override
    public Comparator<? super E> getComparator() {
        return comparator;
    }
}
