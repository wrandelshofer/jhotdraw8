/*
 * @(#)PersistentListWrapper.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class PersistentArrayList<E> extends AbstractReadOnlyList<E> implements PersistentList<E> {
    public static final PersistentArrayList<Object> EMPTY = new PersistentArrayList<>(new ArrayList<>(), ArrayList::new);
    private final @NonNull List<E> list;
    private final @NonNull Function<List<E>, List<E>> cloneFunction;

    public PersistentArrayList(@NonNull List<E> list, @NonNull Function<List<E>, List<E>> cloneFunction) {
        this.list = list;
        this.cloneFunction = cloneFunction;
    }

    public PersistentArrayList(@NonNull Iterable<E> list) {
        this.list = new ArrayList<>();
        for (E e : list) {
            this.list.add(e);
        }
        this.cloneFunction = ArrayList::new;
    }


    @SuppressWarnings("unchecked")
    public static <E> PersistentArrayList<E> of() {
        return (PersistentArrayList<E>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <E> PersistentArrayList<E> of(E... elements) {
        if (elements.length == 0) {
            return (PersistentArrayList<E>) EMPTY;
        } else {
            return new PersistentArrayList<E>(Arrays.asList(elements));
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> PersistentArrayList<E> copyOf(Iterable<? extends E> list) {
        if (list instanceof PersistentArrayList<?>) {
            return (PersistentArrayList<E>) list;
        }
        if (list instanceof ReadOnlyCollection<?>) {
            ReadOnlyCollection<E> c = (ReadOnlyCollection<E>) list;
            return c.isEmpty() ? (PersistentArrayList<E>) EMPTY : new PersistentArrayList<E>(c.asCollection());
        }
        if (list instanceof Collection<?>) {
            Collection<E> c = (Collection<E>) list;
            return c.isEmpty() ? (PersistentArrayList<E>) EMPTY : new PersistentArrayList<E>(c);
        }
        ArrayList<E> a = new ArrayList<>();
        list.forEach(a::add);
        return copyOf(a);
    }

    @Override
    public @NonNull PersistentArrayList<E> copyClear() {
        if (list.isEmpty()) {
            return this;
        }
        List<E> c = cloneFunction.apply(list);
        c.clear();
        return new PersistentArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull PersistentArrayList<E> copyAdd(@NonNull E element) {
        if (list.contains(element)) {
            return this;
        }
        List<E> c = cloneFunction.apply(list);
        c.add(element);
        return new PersistentArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull PersistentList<E> copyAdd(int index, @NonNull E element) {
        return null;
    }

    @Override
    public @NonNull PersistentArrayList<E> copyAddAll(@NonNull Iterable<? extends E> s) {
        List<E> c = cloneFunction.apply(list);
        boolean changed = false;
        for (E e : s) {
            changed |= c.add(e);
        }
        return changed ? new PersistentArrayList<>(c, cloneFunction) : this;
    }

    @Override
    public @NonNull PersistentList<E> copyAddAll(int index, @NonNull Iterable<? extends E> c) {
        return null;
    }

    @Override
    public @NonNull PersistentArrayList<E> copyRemove(@NonNull E element) {
        if (!list.contains(element)) {
            return this;
        }
        List<E> c = cloneFunction.apply(list);
        c.remove(element);
        return new PersistentArrayList<>(c, cloneFunction);

    }

    @Override
    public @NonNull PersistentList<E> copyRemoveAt(int index) {
        List<E> c = cloneFunction.apply(list);
        c.remove(index);
        return new PersistentArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull PersistentList<E> copyRemoveRange(int fromIndex, int toIndex) {
        List<E> c = cloneFunction.apply(list);
        c.subList(fromIndex, toIndex).clear();
        return new PersistentArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull PersistentArrayList<E> copyRemoveAll(@NonNull Iterable<? extends E> s) {
        List<E> c = cloneFunction.apply(list);
        boolean changed = false;
        for (E e : s) {
            changed |= c.remove(e);
        }
        return changed ? new PersistentArrayList<>(c, cloneFunction) : this;
    }

    @Override
    public @NonNull PersistentArrayList<E> copyRetainAll(@NonNull Collection<? extends E> s) {
        List<E> c = cloneFunction.apply(list);
        boolean changed = false;
        for (Iterator<E> iterator = c.iterator(); iterator.hasNext(); ) {
            E e = iterator.next();
            if (!s.contains(e)) {
                changed = true;
                iterator.remove();
            }
        }
        return changed ? new PersistentArrayList<>(c, cloneFunction) : this;
    }

    @Override
    public @NonNull PersistentList<E> copySet(int index, @NonNull E element) {
        List<E> c = cloneFunction.apply(list);
        c.set(index, element);
        return new PersistentArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull PersistentList<E> copySubList(int fromIndex, int toIndex) {
        List<E> c = cloneFunction.apply(list.subList(fromIndex, toIndex));
        return new PersistentArrayList<>(c, cloneFunction);
    }

    @Override
    public E get(int index) {
        return list.get(index);
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(list).iterator();
    }

    @Override
    public @NonNull ImmutableList<E> readOnlySubList(int fromIndex, int toIndex) {
        return new ImmutableArrayList<E>(list.subList(fromIndex, toIndex));
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
