package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.stream.Stream;

/// Builder for readable collections.
public interface ListBuilder<E, C extends ReadableCollection<E>> {
    /// Adds the specified item.
    ListBuilder<E, C> add(@Nullable E elem);

    /// Adds all specified items.
    default ListBuilder<E, C> addAll(Iterable<? extends E> elements) {
        for (E elem : elements) {
            add(elem);
        }
        return this;
    }

    /// Adds all remaining elements in the specified iterator.
    default ListBuilder<E, C> addIterator(Iterator<? extends E> it) {
        while (it.hasNext()) {
            add(it.next());
        }
        return this;
    }

    /// Adds all the specified elements.
    @SuppressWarnings("unchecked")
    default ListBuilder<E, C> addArray(E @Nullable ... elements) {
        for (E elem : elements) {
            add(elem);
        }
        return this;
    }

    /// Adds all the specified elements.
    @SuppressWarnings("unchecked")
    default ListBuilder<E, C> addStream(Stream<E> elements) {
        elements.forEach(this::add);
        return this;
    }

    /// Builds the collection and returns it.
    C build();
}
