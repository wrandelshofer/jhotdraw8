package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadOnlyListFacade;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jhotdraw8.icollection.readonly.ReadOnlyList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Provides factory methods for read-only lists.
 */
public class ReadOnlyLists {
    /**
     * Don't let anyone instantiate this class.
     */
    private ReadOnlyLists() {
    }

    /**
     * Returns an empty read-only list.
     *
     * @param <E> the element type
     * @return empty read-only list
     */
    public static <E> ReadOnlyList<E> of() {
        return VectorList.of();
    }

    /**
     * Returns a new read-only list with the specified elements.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return read-only list with the specified elements
     */
    @SafeVarargs
    public static <E> ReadOnlyList<E> of(E... elements) {
        return new ReadOnlyListFacade<>(Arrays.asList(elements));
    }

    /**
     * Returns a read-only list with the specified elements.
     * <p>
     * If the list can be cast to {@link org.jhotdraw8.icollection.immutable.ImmutableList},
     * it will be cast, otherwise a new list will be created.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return read-only list with the specified elements
     */
    @SuppressWarnings("unchecked")
    public static <E> ReadOnlyList<E> copyOf(Iterable<E> elements) {
        if (elements instanceof ImmutableList<?>) return (ReadOnlyList<E>) elements;
        if (elements instanceof Collection<?>)
            return new ReadOnlyListFacade<>(new ArrayList<>((Collection<E>) elements));
        ArrayList<E> l = new ArrayList<>();
        for (var e : elements) {
            l.add(e);
        }
        return new ReadOnlyListFacade<>(l);
    }

    /**
     * Returns the same list wrapped or cast into a read-only list interface.
     * <p>
     * If the list can be cast to a {@link ReadOnlyList}, it will be cast.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return read-only list with the specified elements
     */
    @SuppressWarnings("unchecked")
    public static <E> ReadOnlyList<E> asReadOnly(List<E> elements) {
        return elements instanceof ReadOnlyList<?> ? (ReadOnlyList<E>) elements : new ReadOnlyListFacade<>(elements);
    }
}
