package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableSequencedSetFacade;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.icollection.impl.iteration.MappedIterator;
import org.jhotdraw8.icollection.impl.redblack.RedBlackTree;
import org.jhotdraw8.icollection.navigable.DescendingNavigableSetView;
import org.jhotdraw8.icollection.navigable.SubsetNavigableSetView;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableNavigableSet;
import org.jhotdraw8.icollection.readable.ReadableSequencedSet;
import org.jhotdraw8.icollection.serialization.SortedSetSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.stream.Stream;

/**
 * Implements the {@link NavigableSet} interface using a Red-Black tree.
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
public class MutableRedBlackSet<E> extends AbstractSet<E> implements NavigableSet<E>, Serializable, Cloneable, ReadableNavigableSet<E> {
    @Serial
    private static final long serialVersionUID = 0L;
    @SuppressWarnings({"serial", "RedundantSuppression"})// Conditionally serializable
    final Comparator<E> comparator;
    /**
     * The number of times this set has been structurally modified.
     */
    protected transient int modCount;
    transient RedBlackTree<E, Void> root;
    /**
     * Constructs a new, empty set, sorted according to the
     * specified comparator.
     *
     * @param comparator a comparator, if {@code null} the natural ordering of the elements is used
     */
    public MutableRedBlackSet(@Nullable Comparator<E> comparator) {
        this.comparator = comparator == null ? NaturalComparator.instance() : comparator;
        this.root = RedBlackTree.empty();
    }
    /**
     * Constructs a new tree set containing the elements in the specified
     * collection, sorted according to the specified comparator.
     *
     * @param comparator a comparator, if {@code null} the natural ordering of the elements is used
     * @param c          the collection
     */
    @SuppressWarnings("this-escape")
    public MutableRedBlackSet(@Nullable Comparator<E> comparator, Collection<? extends E> c) {
        this.comparator = comparator == null ? NaturalComparator.instance() : comparator;
        this.root = RedBlackTree.empty();
        this.addAll(c);
    }

    /**
     * Constructs a new, empty set, sorted according to the
     * <i>natural ordering</i> of its elements.
     */
    @SuppressWarnings("this-escape")
    public MutableRedBlackSet() {
        this.comparator = NaturalComparator.instance();
        this.root = RedBlackTree.empty();
    }

    /**
     * Constructs a new tree set containing the elements in the specified
     * collection, sorted according to the <i>natural ordering</i> of its
     * elements.
     *
     * @param c the collection
     */
    @SuppressWarnings("this-escape")
    public MutableRedBlackSet(Iterable<? extends E> c) {
        this.comparator = NaturalComparator.instance();
        this.root = RedBlackTree.empty();
        this.addAll(c);
    }

    MutableRedBlackSet(@Nullable Comparator<E> comparator, RedBlackTree<E, Void> root) {
        this.comparator = comparator == null ? NaturalComparator.instance() : comparator;
        this.root = root;
    }

    @Override
    public boolean add(E e) {
        RedBlackTree<E, Void> newRoot = root.insert(e, null, comparator);
        if (newRoot.size() != root.size()) {
            root = newRoot;
            modCount++;
            return true;
        }
        return false;
    }

    public boolean addAll(Iterable<? extends E> c) {
        boolean modified = false;
        for (E e : c) {
            if (add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    @Nullable
    @Override
    public E ceiling(E e) {
        return root.ceiling(e, comparator).keyOrNull();
    }

    @Override
    public void clear() {
        if (!isEmpty()) {
            modCount++;
            root = RedBlackTree.empty();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public MutableRedBlackSet<E> clone() {
        try {
            return (MutableRedBlackSet<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Nullable
    @Override
    public Comparator<? super E> comparator() {
        return comparator == NaturalComparator.instance() ? null : comparator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return root.contains((E) o, comparator);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new FailFastIterator<>(
                new MappedIterator<>(root.reverseIterator(), Map.Entry::getKey),
                this::iteratorRemove, this::getModCount
        );
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new DescendingNavigableSetView<>(this, this::getModCount);
    }

    @Override
    public E first() {
        return root.min().getKey();
    }

    @Nullable
    @Override
    public E floor(E e) {
        return root.floor(e, comparator).keyOrNull();
    }

    @Override
    public E getFirst() {
        return first();
    }

    @Override
    public E getLast() {
        return last();
    }

    /**
     * Returns the current value of the modification counter.
     *
     * @return value of modification counter
     */
    protected int getModCount() {
        return modCount;
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new SubsetNavigableSetView<>(this, this::getModCount,
                true, null, true, false, toElement, inclusive, true);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Nullable
    @Override
    public E higher(E e) {
        return root.higher(e, comparator).keyOrNull();
    }

    @Override
    public boolean isEmpty() {
        return root.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return new FailFastIterator<>(
                new MappedIterator<>(root.iterator(), Map.Entry::getKey),
                this::iteratorRemove, this::getModCount
        );
    }

    private void iteratorRemove(E e) {
        remove(e);
    }

    @Override
    public E last() {
        return root.max().getKey();
    }

    @Nullable
    @Override
    public E lower(E e) {
        return root.lower(e, comparator).keyOrNull();
    }

    @Nullable
    @Override
    public E pollFirst() {
        E value = root.min().keyOrNull();
        root = root.delete(value, comparator);
        return value;
    }

    @Nullable
    @Override
    public E pollLast() {
        E value = root.max().keyOrNull();
        root = root.delete(value, comparator);
        return value;
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

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        RedBlackTree<E, Void> newRoot = root.delete((E) o, comparator);
        if (newRoot.size() != root.size()) {
            root = newRoot;
            modCount++;
            return true;
        }
        return false;
    }

    Iterator<E> reverseIterator() {
        return new FailFastIterator<>(
                new MappedIterator<>(root.reverseIterator(), Map.Entry::getKey),
                this::iteratorRemove, this::getModCount
        );
    }

    @Override
    public int size() {
        return root.size();
    }

    @Override
    public Spliterator<E> spliterator() {
        return new FailFastSpliterator<>(
                NavigableSet.super.spliterator(),
                this::getModCount,
                comparator == NaturalComparator.instance() ? null : comparator);
    }

    @Override
    public Stream<E> stream() {
        return super.stream();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new SubsetNavigableSetView<>(this, this::getModCount,
                false, fromElement, fromInclusive, false, toElement, toInclusive, true);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new SubsetNavigableSetView<>(this, this::getModCount,
                false, fromElement, inclusive, true, null, true, true);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }


    /**
     * Returns an persistent copy of this set.
     *
     * @return an persistent copy
     */
    public RedBlackSet<E> toPersistent() {
        return new RedBlackSet<>(comparator, root);
    }

    @Override
    public String toString() {
        return ReadableCollection.iterableToString(this);
    }

    @Serial
    private Object writeReplace() {
        return new MutableRedBlackSet.SerializationProxy<>(this);
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
            return new MutableRedBlackSet<>(deserializedComparator, deserializedElements);
        }
    }

}
