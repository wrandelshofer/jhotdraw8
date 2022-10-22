/*
 * @(#)ImmutableArrayList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.facade.ReadOnlyListFacade;
import org.jhotdraw8.collection.readonly.AbstractReadOnlyList;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Implements an immutable list using an {@link ArrayList}.
 * <p>
 * Features:
 * <ul>
 *     <li>allows null elements</li>
 *     <li>is immutable</li>
 *     <li>is thread-safe</li>
 *     <li>iterates in the order of the list</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>copyAdd: O(n)</li>
 *     <li>copySet: O(n)</li>
 *     <li>copyRemove: O(n)</li>
 *     <li>contains: O(1)</li>
 *     <li>toMutable: O(n)</li>
 *     <li>clone: O(n)</li>
 *     <li>iterator.next(): O(1)</li>
 * </ul>
 * <p>
 * XXX Replace this class, it is too inefficient.
 *
 * @param <E> the element type
 */
public class ImmutableArrayList<E> extends AbstractReadOnlyList<E> implements ImmutableList<E> {
    public static final ImmutableArrayList<Object> EMPTY = new ImmutableArrayList<>(new ArrayList<>(), ArrayList::new);
    private final @NonNull List<E> list;
    private final @NonNull Function<List<E>, List<E>> cloneFunction;

    public ImmutableArrayList(@NonNull List<? extends E> list, @NonNull Function<List<E>, List<E>> cloneFunction) {
        this.list = new ArrayList<>(list);
        this.cloneFunction = cloneFunction;
    }

    public ImmutableArrayList(@NonNull Iterable<? extends E> list) {
        this.list = new ArrayList<>();
        for (E e : list) {
            this.list.add(e);
        }
        this.cloneFunction = ArrayList::new;
    }

    /**
     * Returns an empty immutable list.
     *
     * @param <E> the element type
     * @return an empty immutable list
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull ImmutableArrayList<E> of() {
        return (ImmutableArrayList<E>) EMPTY;
    }

    /**
     * Returns an immutable list that contains the provided elements.
     *
     * @param elements elements
     * @param <E>      the element type
     * @return an immutable list of the provided elements
     */
    @SuppressWarnings({"unchecked", "varargs"})
    @SafeVarargs
    public static <E> @NonNull ImmutableArrayList<E> of(E @NonNull ... elements) {
        if (elements.length == 0) {
            return (ImmutableArrayList<E>) EMPTY;
        } else {
            return new ImmutableArrayList<>(Arrays.asList(elements));
        }
    }

    /**
     * Returns an immutable list that contains the provided elements.
     *
     * @param iterable an iterable
     * @param <E>      the element type
     * @return an immutable list of the provided elements
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull ImmutableArrayList<E> copyOf(Iterable<? extends E> iterable) {
        if (iterable instanceof ImmutableArrayList<?>) {
            return (ImmutableArrayList<E>) iterable;
        }
        if (iterable instanceof ReadOnlyCollection<?>) {
            ReadOnlyCollection<E> c = (ReadOnlyCollection<E>) iterable;
            return c.isEmpty() ? (ImmutableArrayList<E>) EMPTY : new ImmutableArrayList<>(c.asCollection());
        }
        if (iterable instanceof Collection<?>) {
            Collection<E> c = (Collection<E>) iterable;
            return c.isEmpty() ? (ImmutableArrayList<E>) EMPTY : new ImmutableArrayList<>(c);
        }
        ArrayList<E> a = new ArrayList<>();
        iterable.forEach(a::add);
        return copyOf(a);
    }

    @Override
    public @NonNull ImmutableArrayList<E> clear() {
        if (list.isEmpty()) {
            return this;
        }
        List<E> c = cloneFunction.apply(list);
        c.clear();
        return new ImmutableArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull ImmutableArrayList<E> add(@NonNull E element) {
        List<E> c = cloneFunction.apply(list);
        c.add(element);
        return new ImmutableArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull ImmutableList<E> add(int index, @NonNull E element) {
        List<E> c = cloneFunction.apply(list);
        c.add(index, element);
        return new ImmutableArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull ImmutableArrayList<E> addAll(@NonNull Iterable<? extends E> s) {
        List<E> c = cloneFunction.apply(list);
        boolean changed = false;
        for (E e : s) {
            changed |= c.add(e);
        }
        return changed ? new ImmutableArrayList<>(c, cloneFunction) : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ImmutableList<E> addAll(int index, @NonNull Iterable<? extends E> it) {
        List<E> c = cloneFunction.apply(list);
        if (it instanceof Collection<?>) {
            c.addAll(index, ((Collection<E>) it));
        } else {
            for (E e : it) {
                c.add(index++, e);
            }
        }
        return new ImmutableArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull ImmutableArrayList<E> remove(@NonNull E element) {
        if (!list.contains(element)) {
            return this;
        }
        List<E> c = cloneFunction.apply(list);
        c.remove(element);
        return new ImmutableArrayList<>(c, cloneFunction);

    }

    @Override
    public @NonNull ImmutableList<E> removeAt(int index) {
        List<E> c = cloneFunction.apply(list);
        c.remove(index);
        return new ImmutableArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull ImmutableList<E> removeRange(int fromIndex, int toIndex) {
        List<E> c = cloneFunction.apply(list);
        c.subList(fromIndex, toIndex).clear();
        return new ImmutableArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull ImmutableArrayList<E> removeAll(@NonNull Iterable<? extends E> s) {
        List<E> c = cloneFunction.apply(list);
        boolean changed = false;
        for (E e : s) {
            changed |= c.remove(e);
        }
        return changed ? new ImmutableArrayList<>(c, cloneFunction) : this;
    }

    @Override
    public @NonNull ImmutableArrayList<E> retainAll(@NonNull Collection<? extends E> s) {
        List<E> c = cloneFunction.apply(list);
        boolean changed = false;
        for (Iterator<E> iterator = c.iterator(); iterator.hasNext(); ) {
            E e = iterator.next();
            if (!s.contains(e)) {
                changed = true;
                iterator.remove();
            }
        }
        return changed ? new ImmutableArrayList<>(c, cloneFunction) : this;
    }

    @Override
    public @NonNull ImmutableList<E> set(int index, @NonNull E element) {
        List<E> c = cloneFunction.apply(list);
        c.set(index, element);
        return new ImmutableArrayList<>(c, cloneFunction);
    }

    @Override
    public E get(int index) {
        return list.get(index);
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return Collections.unmodifiableList(list).iterator();
    }

    @Override
    public @NonNull ReadOnlySequencedCollection<E> readOnlyReversed() {
        return new ReadOnlyListFacade<>(
                this::size,
                i -> get(size() - i)
        );
    }

    @Override
    public @NonNull ImmutableList<E> readOnlySubList(int fromIndex, int toIndex) {
        return new ImmutableArrayList<>(list.subList(fromIndex, toIndex));
    }

    @Override
    public @NonNull List<E> toMutable() {
        return new ArrayList<>(list);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean contains(@Nullable Object e) {
        return list.contains(e);
    }


    @Override
    public int hashCode() {
        return list.hashCode();
    }
}
