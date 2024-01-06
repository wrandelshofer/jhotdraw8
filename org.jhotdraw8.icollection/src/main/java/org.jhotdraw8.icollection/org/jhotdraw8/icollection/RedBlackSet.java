package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.facade.ReadOnlySequencedSetFacade;
import org.jhotdraw8.icollection.immutable.ImmutableNavigableSet;
import org.jhotdraw8.icollection.impl.redblack.RedBlackTree;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;
import org.jhotdraw8.icollection.serialization.SortedSetSerializationProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;


/**
 * Implements the {@link ImmutableNavigableSet} interface using a Red-Black tree.
 * <p>
 * References:
 * <p>
 * For a similar design, see 'TreeSet.java' in vavr. The internal data structure of
 * this class is licensed from vavr.
 * <dl>
 *     <dt>TreeSet.java. Copyright 2023 (c) vavr. <a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/LICENSE">MIT License</a>.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/TreeSet.java">github.com</a></dd>
 * </dl>
 *
 * @param <E> the element type
 */
public class RedBlackSet<E> implements ImmutableNavigableSet<E>, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    final @NonNull RedBlackTree<E> root;
    final @NonNull Comparator<E> comparator;

    RedBlackSet(@NonNull Comparator<E> comparator, @NonNull RedBlackTree<E> root) {
        this.root = root;
        this.comparator = comparator;
    }

    /**
     * Returns an immutable set that contains the provided elements, sorted according to the
     * specified comparator.
     *
     * @param c   an iterable
     * @param <E> the element type
     * @return an immutable set of the provided elements
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull RedBlackSet<E> copyOf(@NonNull Comparator<E> comparator, @NonNull Iterable<? extends E> c) {
        if (c instanceof RedBlackSet<? extends E> r && r.comparator.equals(comparator)) {
            return (RedBlackSet<E>) r;
        }
        if (c instanceof MutableRedBlackSet<? extends E> r && r.comparator.equals(comparator)) {
            return (RedBlackSet<E>) r.toImmutable();
        }
        return RedBlackSet.<E>sortedOf(comparator).addAll(c);
    }

    /**
     * Returns an immutable set that contains the provided elements sorted according to the
     * <i>natural ordering</i> of its elements.
     *
     * @param c   an iterable
     * @param <E> the element type
     * @return an immutable set of the provided elements
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull RedBlackSet<E> copyOf(@NonNull Iterable<? extends E> c) {
        return RedBlackSet.<E>copyOf(NaturalComparator.instance(), c);
    }

    /**
     * Returns an empty immutable set, sorted according to the
     * specified comparator.
     *
     * @param <E> the element type
     * @return an empty immutable set
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull RedBlackSet<E> sortedOf(@NonNull Comparator<E> comparator) {
        return ((RedBlackSet<E>) new RedBlackSet<>(comparator, RedBlackTree.of(comparator, RedBlackSet::updateElement)));
    }

    /**
     * Returns an immutable set that contains the provided elements, sorted according to the
     * specified comparator.
     *
     * @param elements elements
     * @param <E>      the element type
     * @return an immutable set of the provided elements
     */
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <E> @NonNull RedBlackSet<E> sortedOf(@NonNull Comparator<E> comparator, @NonNull E @Nullable ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return RedBlackSet.<E>sortedOf(comparator).addAll(Arrays.asList(elements));
    }

    /**
     * Returns an empty immutable set, sorted according to the
     * <i>natural ordering</i> of its elements.
     *
     * @param <E> the element type
     * @return an empty immutable set
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull RedBlackSet<E> of() {
        return ((RedBlackSet<E>) new RedBlackSet<>(NaturalComparator.instance(), RedBlackTree.of(NaturalComparator.instance(), RedBlackSet::updateElement)));
    }

    /**
     * Returns an immutable set that contains the provided elements, sorted according to the
     * <i>natural ordering</i> of its elements.
     *
     * @param elements elements
     * @param <E>      the element type
     * @return an immutable set of the provided elements
     */
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <E> @NonNull RedBlackSet<E> of(@NonNull E @Nullable ... elements) {
        return sortedOf(NaturalComparator.instance(), elements);
    }

    @Override
    public @NonNull RedBlackSet<E> add(E element) {
        RedBlackTree<E> newRoot = root.insert(element, comparator, RedBlackSet::updateElement);
        return newRoot == root ? this : new RedBlackSet<>(comparator, newRoot);
    }

    @Override
    public E getFirst() {
        return root.min().orThrow();
    }

    @Override
    public E getLast() {
        return root.max().orThrow();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull RedBlackSet<E> addAll(@NonNull Iterable<? extends E> c) {
        if (c instanceof MutableRedBlackSet<? extends E> m) {
            c = m.toImmutable();
        }
        if (c instanceof RedBlackSet<? extends E> that && that.comparator == this.comparator) {
            RedBlackTree<E> newRoot = root.addAll((RedBlackTree<E>) that.root, comparator, RedBlackSet::updateElement);
            return newRoot.size() == root.size() ? this : new RedBlackSet<>(comparator, newRoot);
        }
        return (RedBlackSet<E>) ImmutableNavigableSet.super.addAll(c);
    }

    @Override
    public @Nullable E ceiling(E e) {
        return root.ceiling(e, comparator).orNull();
    }

    @Override
    public @NonNull RedBlackSet<E> clear() {
        return isEmpty() ? this : sortedOf(comparator);
    }

    @Override
    public @Nullable Comparator<? super E> comparator() {
        return comparator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return root.contains((E) o, comparator);
    }

    @Override
    public @Nullable E floor(E e) {
        return root.floor(e, comparator).orNull();
    }

    @Override
    public @Nullable E higher(E e) {
        return root.higher(e, comparator).orNull();
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return root.iterator();
    }

    @Override
    public Spliterator<E> spliterator() {
        return new Spliterators.AbstractSpliterator<>(size(), Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED | Spliterator.IMMUTABLE) {
            private final Iterator<E> iterator = iterator();

            @Override
            public boolean tryAdvance(Consumer<? super E> action) {
                if (iterator.hasNext()) {
                    action.accept(iterator.next());
                }
                return false;
            }

            @Override
            public Comparator<? super E> getComparator() {
                return comparator;
            }
        };
    }

    @Override
    public @Nullable E lower(E e) {
        return root.lower(e, comparator).orNull();
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public @NonNull ReadOnlySequencedSet<E> readOnlyReversed() {
        return new ReadOnlySequencedSetFacade<>(
                this::reverseIterator,
                this::iterator,
                this::size,
                this::contains,
                this::getLast,
                this::getFirst,
                Spliterator.IMMUTABLE);
    }

    @Override
    public @NonNull RedBlackSet<E> remove(E element) {
        RedBlackTree<E> newRoot = root.delete(element, comparator);
        return newRoot.size() == root.size() ? this : new RedBlackSet<>(comparator, newRoot);
    }

    @Override
    public @NonNull RedBlackSet<E> removeAll(@NonNull Iterable<?> c) {
        return (RedBlackSet<E>) ImmutableNavigableSet.super.removeAll(c);
    }

    @Override
    public @NonNull RedBlackSet<E> retainAll(@NonNull Iterable<?> c) {
        return (RedBlackSet<E>) ImmutableNavigableSet.super.retainAll(c);
    }

    @NonNull Iterator<E> reverseIterator() {
        return root.reverseIterator();
    }

    @Override
    public int size() {
        return root.size();
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(iterator());
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return ReadOnlySet.setEquals(this, other);
    }

    @Override
    public @NonNull MutableRedBlackSet<E> toMutable() {
        return new MutableRedBlackSet<>(comparator, root);
    }

    @Override
    public String toString() {
        return ReadOnlyCollection.iterableToString(this);
    }

    @Serial
    private @NonNull Object writeReplace() {
        return new RedBlackSet.SerializationProxy<>(this.toMutable());
    }

    private static class SerializationProxy<E> extends SortedSetSerializationProxy<E> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(@NonNull SortedSet<E> target) {
            super(target);
        }

        @Serial
        @Override
        protected @NonNull Object readResolve() {
            return RedBlackSet.copyOf(deserializedComparator, deserializedElements);
        }
    }

    /**
     * Update function for a set: we always keep the old element.
     *
     * @param oldElement the old element
     * @param newElement the new element
     * @param <E>        the element type
     * @return always returns the old element
     */
    static <E> E updateElement(E oldElement, E newElement) {
        return oldElement;
    }
}
