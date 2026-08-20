package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;

/// Builder for readable sets.
public interface SetBuilder<E, C extends ReadableCollection<E>> {
    /// Adds the specified item.
    SetBuilder<E, C> add(@Nullable E elem);

    /// Adds all specified items.
    default SetBuilder<E, C> addAll(Iterable<? extends E> elements) {
        for (var e : elements) {
            add(e);
        }
        return this;
    }

    /// Adds all remaining elements from the specified iterator.
    default SetBuilder<E, C> addIterator(Iterator<? extends E> it) {
        while (it.hasNext()) {
            add(it.next());
        }
        return this;
    }

    /// Adds all remaining elements from the specified spliterator.
    default SetBuilder<E, C> addSpliterator(Spliterator<? extends E> it) {
        while (it.tryAdvance(this::add)) {
        }
        return this;
    }

    /// Adds all elements from the specified array.
    @SuppressWarnings("unchecked")
    default SetBuilder<E, C> addArray(E @Nullable ... elements) {
        for (E elem : elements) {
            add(elem);
        }
        return this;
    }

    /// Adds all remaining elements from the specified stream.
    @SuppressWarnings("unchecked")
    default SetBuilder<E, C> addStream(Stream<E> elements) {
        elements.forEach(this::add);
        return this;
    }

    /// Builds the collection and returns it.
    C build();
}
