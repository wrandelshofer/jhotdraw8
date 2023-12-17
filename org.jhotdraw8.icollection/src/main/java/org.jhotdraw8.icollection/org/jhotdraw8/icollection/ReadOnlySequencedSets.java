package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadOnlySequencedSetFacade;
import org.jhotdraw8.icollection.immutable.ImmutableSequencedSet;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.SequencedSet;

/**
 * Provides factory methods for read-only sequenced sets.
 */
public class ReadOnlySequencedSets {
    /**
     * Don't let anyone instantiate this class.
     */
    private ReadOnlySequencedSets() {
    }

    /**
     * Returns an empty read-only sequenced set.
     *
     * @param <E> the element type
     * @return empty read-only sequenced set
     */
    public static <E> ReadOnlySequencedSet<E> of() {
        return VectorSet.of();
    }

    /**
     * Returns a read-only sequenced set with the specified elements.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return read-only sequenced set with the specified elements
     */
    @SafeVarargs
    public static <E> ReadOnlySequencedSet<E> of(E... elements) {
        return new ReadOnlySequencedSetFacade<>(new LinkedHashSet<>(Arrays.asList(elements)));
    }

    /**
     * Returns a new read-only sequenced set with the specified elements.
     * <p>
     * If the set can be cast to {@link ImmutableSequencedSet},
     * it will be cast, otherwise a new set will be created.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return read-only sequenced set with the specified elements
     */
    public static <E> ReadOnlySequencedSet<E> copyOf(Iterable<E> elements) {
        if (elements instanceof ImmutableSequencedSet<?>) return (ReadOnlySequencedSet<E>) elements;
        if (elements instanceof Collection<?>)
            return new ReadOnlySequencedSetFacade<>(new LinkedHashSet<>((Collection<E>) elements));
        var s = new LinkedHashSet<E>();
        for (var e : elements) {
            s.add(e);
        }
        return new ReadOnlySequencedSetFacade<>(s);

    }

    /**
     * Returns the same set wrapped or cast into a read-only sequenced set interface.
     * <p>
     * If the set can be cast to a {@link ReadOnlySequencedSet}, it will be cast.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return read-only sequenced set with the specified elements
     */
    public static <E> ReadOnlySequencedSet<E> asReadOnly(SequencedSet<E> elements) {
        return elements instanceof ReadOnlySequencedSet<?> ? (ReadOnlySequencedSet<E>) elements : new ReadOnlySequencedSetFacade<>(elements);
    }
}
