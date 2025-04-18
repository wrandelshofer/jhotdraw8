package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableSequencedSetFacade;
import org.jhotdraw8.icollection.impl.iteration.MappedIterator;
import org.jhotdraw8.icollection.impl.redblack.RedBlackTree;
import org.jhotdraw8.icollection.persistent.PersistentCollection;
import org.jhotdraw8.icollection.persistent.PersistentNavigableSet;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSequencedSet;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jhotdraw8.icollection.serialization.SortedSetSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;


/**
 * Implements the {@link PersistentNavigableSet} interface using a Red-Black tree.
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
public class RedBlackSet<E> implements PersistentNavigableSet<E>, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    final transient RedBlackTree<E, Void> root;
    @SuppressWarnings({"serial", "RedundantSuppression"})//Conditionally serializable
    final Comparator<E> comparator;

    /**
     * Creates a new instance with the provided privateData data object.
     * <p>
     * This constructor is intended to be called from a constructor
     * of the subclass, that is called from method {@link #newInstance(PrivateData)}.
     *
     * @param privateData an privateData data object
     */
    @SuppressWarnings("unchecked")
    protected RedBlackSet(PrivateData privateData) {
        this(((Map.Entry<Comparator<E>, ?>) privateData.get()).getKey(), ((Map.Entry<?, RedBlackTree<E, Void>>) privateData.get()).getValue());
    }

    /**
     * Creates a new instance with the provided privateData object as its internal data structure.
     * <p>
     * Subclasses must override this method, and return a new instance of their subclass!
     *
     * @param privateData the internal data structure needed by this class for creating the instance.
     * @return a new instance of the subclass
     */
    protected RedBlackSet<E> newInstance(PrivateData privateData) {
        return new RedBlackSet<>(privateData);
    }

    private RedBlackSet<E> newInstance(Comparator<E> comparator, RedBlackTree<E, Void> root) {
        return newInstance(new PrivateData(new AbstractMap.SimpleImmutableEntry<>(comparator, root)));
    }

    RedBlackSet(Comparator<E> comparator, RedBlackTree<E, Void> root) {
        this.root = root;
        this.comparator = comparator;
    }

    /**
     * Returns an persistent set that contains the provided elements, sorted according to the
     * specified comparator.
     *
     * @param comparator a comparator, if {@code null} the natural ordering of the elements is used
     * @param c          an iterable
     * @param <E>        the element type
     * @return an persistent set of the provided elements
     */
    @SuppressWarnings("unchecked")
    public static <E> RedBlackSet<E> copyOf(@Nullable Comparator<E> comparator, Iterable<? extends E> c) {
        if (comparator == null) {
            comparator = NaturalComparator.instance();
        }
        if (c instanceof RedBlackSet<? extends E> r && r.comparator.equals(comparator)) {
            return (RedBlackSet<E>) r;
        }
        if (c instanceof MutableRedBlackSet<? extends E> r && r.comparator.equals(comparator)) {
            return (RedBlackSet<E>) r.toPersistent();
        }
        return RedBlackSet.sortedOf(comparator).addAll(c);
    }

    /**
     * Returns an persistent set that contains the provided elements sorted according to the
     * <i>natural ordering</i> of its elements.
     *
     * @param c   an iterable
     * @param <E> the element type
     * @return an persistent set of the provided elements
     */
    public static <E> RedBlackSet<E> copyOf(Iterable<? extends E> c) {
        return RedBlackSet.copyOf(NaturalComparator.instance(), c);
    }

    /**
     * Returns an empty persistent set, sorted according to the
     * specified comparator.
     *
     * @param comparator a comparator, if {@code null} the natural ordering of the elements is used
     * @param <E>        the element type
     * @return an empty persistent set
     */
    public static <E> RedBlackSet<E> sortedOf(@Nullable Comparator<E> comparator) {
        if (comparator == null) {
            comparator = NaturalComparator.instance();
        }
        return new RedBlackSet<>(comparator, RedBlackTree.of(comparator));
    }

    /**
     * Returns an persistent set that contains the provided elements, sorted according to the
     * specified comparator.
     *
     * @param comparator a comparator, if {@code null} the natural ordering of the elements is used
     * @param elements   elements
     * @param <E>        the element type
     * @return an persistent set of the provided elements
     */
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <E> RedBlackSet<E> sortedOf(@Nullable Comparator<E> comparator, E @Nullable ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (comparator == null) {
            comparator = NaturalComparator.instance();
        }
        return RedBlackSet.sortedOf(comparator).addAll(Arrays.asList(elements));
    }

    /**
     * Returns an empty persistent set, sorted according to the
     * <i>natural ordering</i> of its elements.
     *
     * @param <E> the element type
     * @return an empty persistent set
     */
    public static <E> RedBlackSet<E> of() {
        return new RedBlackSet<>(NaturalComparator.instance(), RedBlackTree.of(NaturalComparator.instance()));
    }

    /**
     * Returns an persistent set that contains the provided elements, sorted according to the
     * <i>natural ordering</i> of its elements.
     *
     * @param elements elements
     * @param <E>      the element type
     * @return an iterable of elements
     */
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <E> RedBlackSet<E> of(E @Nullable ... elements) {
        return sortedOf(NaturalComparator.instance(), elements);
    }

    @Override
    public RedBlackSet<E> add(E element) {
        RedBlackTree<E, Void> newRoot = root.insert(element, null, comparator);
        return newRoot == root ? this : newInstance(comparator, newRoot);
    }

    @Override
    public E getFirst() {
        return root.min().getKey();
    }

    @Override
    public E getLast() {
        return root.max().getKey();
    }

    @Override
    public RedBlackSet<E> addAll(Iterable<? extends E> c) {
        return (RedBlackSet<E>) PersistentNavigableSet.super.addAll(c);
    }

    @Override
    public @Nullable E ceiling(E e) {
        return root.ceiling(e, comparator).keyOrNull();
    }

    @Override
    public <T> RedBlackSet<T> empty() {
        return of();
    }

    @Override
    public <T> PersistentCollection<T> empty(@Nullable Comparator<T> comparator) {
        return sortedOf(comparator);
    }

    @Override
    public @Nullable Comparator<? super E> comparator() {
        return comparator == NaturalComparator.instance() ? null : comparator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return root.contains((E) o, comparator);
    }

    @Override
    public @Nullable E floor(E e) {
        return root.floor(e, comparator).keyOrNull();
    }

    @Override
    public @Nullable E higher(E e) {
        return root.higher(e, comparator).keyOrNull();
    }

    @Override
    public Iterator<E> iterator() {
        return new MappedIterator<>(root.iterator(), Map.Entry::getKey);
    }

    @Override
    public Spliterator<E> spliterator() {
        return new Spliterators.AbstractSpliterator<>(size(),
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED | Spliterator.IMMUTABLE) {
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
                return comparator();
            }
        };
    }

    @Override
    public @Nullable E lower(E e) {
        return root.lower(e, comparator).keyOrNull();
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public ReadableSequencedSet<E> readOnlyReversed() {
        return new ReadableSequencedSetFacade<>(
                this::reverseIterator,
                this::iterator,
                this::size,
                this::contains,
                this::getLast,
                this::getFirst,
                Spliterator.IMMUTABLE);
    }

    @Override
    public RedBlackSet<E> remove(E element) {
        RedBlackTree<E, Void> newRoot = root.delete(element, comparator);
        return newRoot.size() == root.size() ? this : newInstance(comparator, newRoot);
    }

    @Override
    public RedBlackSet<E> removeAll(Iterable<?> c) {
        return (RedBlackSet<E>) PersistentNavigableSet.super.removeAll(c);
    }

    @Override
    public RedBlackSet<E> retainAll(Iterable<?> c) {
        return (RedBlackSet<E>) PersistentNavigableSet.super.retainAll(c);
    }

    Iterator<E> reverseIterator() {
        return new MappedIterator<>(root.reverseIterator(), Map.Entry::getKey);
    }

    @Override
    public int size() {
        return root.size();
    }

    @Override
    public int hashCode() {
        return ReadableSet.iteratorToHashCode(iterator());
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return ReadableSet.setEquals(this, other);
    }

    @Override
    public MutableRedBlackSet<E> toMutable() {
        return new MutableRedBlackSet<>(comparator, root);
    }

    @Override
    public String toString() {
        return ReadableCollection.iterableToString(this);
    }

    @Serial
    private Object writeReplace() {
        return new RedBlackSet.SerializationProxy<>(this.toMutable());
    }

    private static class SerializationProxy<E> extends SortedSetSerializationProxy<E> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(SortedSet<E> target) {
            super(target);
        }

        @Serial
        @Override
        protected Object readResolve() {
            return RedBlackSet.copyOf(deserializedComparator, deserializedElements);
        }
    }
}
