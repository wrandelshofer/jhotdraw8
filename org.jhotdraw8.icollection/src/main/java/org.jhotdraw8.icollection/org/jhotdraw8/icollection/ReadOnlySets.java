package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadOnlySetFacade;
import org.jhotdraw8.icollection.immutable.ImmutableSet;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides factory methods for immutable lists.
 */
public class ReadOnlySets {
    /**
     * Don't let anyone instantiate this class.
     */
    private ReadOnlySets() {
    }

    /**
     * Returns an empty read-only set.
     *
     * @param <E> the element type
     * @return empty read-only set
     */
    public static <E> ReadOnlySet<E> of() {
        return ChampSet.of();
    }

    /**
     * Returns a read-only set with the specified elements.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return read-only set with the specified elements
     */
    @SafeVarargs
    public static <E> ReadOnlySet<E> of(E... elements) {
        return new ReadOnlySetFacade<>(new HashSet<>(Arrays.asList(elements)));
    }

    /**
     * Returns a new read-only set with the specified elements.
     * <p>
     * If the set can be cast to {@link ImmutableSet},
     * it will be cast, otherwise a new set will be created.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return read-only set with the specified elements
     */
    public static <E> ReadOnlySet<E> copyOf(Iterable<E> elements) {
        if (elements instanceof ImmutableSet<?>) return (ReadOnlySet<E>) elements;
        if (elements instanceof Collection<?>) return new ReadOnlySetFacade<>(new HashSet<>((Collection<E>) elements));
        var s = new HashSet<E>();
        for (var e : elements) {
            s.add(e);
        }
        return new ReadOnlySetFacade<>(s);
    }

    /**
     * Returns the same set wrapped or cast into a read-only set interface.
     * <p>
     * If the set can be cast to a {@link ReadOnlySet}, it will be cast.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return read-only set with the specified elements
     */
    @SuppressWarnings("unchecked")
    public static <E> ReadOnlySet<E> asReadOnly(Set<E> elements) {
        return elements instanceof ReadOnlySet<?> ? (ReadOnlySet<E>) elements : new ReadOnlySetFacade<>(elements);
    }
}
