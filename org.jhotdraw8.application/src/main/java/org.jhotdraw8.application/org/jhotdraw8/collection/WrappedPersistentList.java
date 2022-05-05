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

public class WrappedPersistentList<E> extends AbstractReadOnlyList<E> implements PersistentList<E> {
    public static final WrappedPersistentList<Object> EMPTY = new WrappedPersistentList<>(new ArrayList<>(), s -> (List<Object>) ((ArrayList<Object>) s).clone());
    private final @NonNull List<E> list;
    private final @NonNull Function<List<E>, List<E>> cloneFunction;

    public WrappedPersistentList(@NonNull List<E> list, @NonNull Function<List<E>, List<E>> cloneFunction) {
        this.list = list;
        this.cloneFunction = cloneFunction;
    }

    @SuppressWarnings("unchecked")
    public static <E> PersistentList<E> emptyList() {
        return (PersistentList<E>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <E> PersistentList<E> of(@NonNull E... keys) {
        return new WrappedPersistentList<>(new ArrayList<>(Arrays.asList(keys)), s -> (List<E>) ((ArrayList<E>) s).clone());
    }

    @SuppressWarnings("unchecked")
    public static <E> PersistentList<E> copyOf(@NonNull Iterable<? extends E> list) {
        if (list instanceof WrappedPersistentList) {
            return (PersistentList<E>) list;
        }
        ArrayList<E> ss = new ArrayList<>();
        for (E e : list) {
            ss.add(e);
        }

        return new WrappedPersistentList<>(ss, s -> (List<E>) ((ArrayList<E>) s).clone());
    }

    @Override
    public @NonNull WrappedPersistentList<E> copyClear() {
        if (list.isEmpty()) {
            return this;
        }
        List<E> c = cloneFunction.apply(list);
        c.clear();
        return new WrappedPersistentList<>(c, cloneFunction);
    }

    @Override
    public @NonNull WrappedPersistentList<E> copyAdd(@NonNull E element) {
        if (list.contains(element)) {
            return this;
        }
        List<E> c = cloneFunction.apply(list);
        c.add(element);
        return new WrappedPersistentList<>(c, cloneFunction);
    }

    @Override
    public @NonNull PersistentList<E> copyAdd(int index, @NonNull E element) {
        return null;
    }

    @Override
    public @NonNull WrappedPersistentList<E> copyAddAll(@NonNull Iterable<? extends E> s) {
        List<E> c = cloneFunction.apply(list);
        boolean changed = false;
        for (E e : s) {
            changed |= c.add(e);
        }
        return changed ? new WrappedPersistentList<>(c, cloneFunction) : this;
    }

    @Override
    public @NonNull PersistentList<E> copyAddAll(int index, @NonNull Iterable<? extends E> c) {
        return null;
    }

    @Override
    public @NonNull WrappedPersistentList<E> copyRemove(@NonNull E element) {
        if (!list.contains(element)) {
            return this;
        }
        List<E> c = cloneFunction.apply(list);
        c.remove(element);
        return new WrappedPersistentList<>(c, cloneFunction);

    }

    @Override
    public @NonNull PersistentList<E> copyRemoveAt(int index) {
        List<E> c = cloneFunction.apply(list);
        c.remove(index);
        return new WrappedPersistentList<>(c, cloneFunction);
    }

    @Override
    public @NonNull PersistentList<E> copyRemoveRange(int fromIndex, int toIndex) {
        List<E> c = cloneFunction.apply(list);
        c.subList(fromIndex, toIndex).clear();
        return new WrappedPersistentList<>(c, cloneFunction);
    }

    @Override
    public @NonNull WrappedPersistentList<E> copyRemoveAll(@NonNull Iterable<? extends E> s) {
        List<E> c = cloneFunction.apply(list);
        boolean changed = false;
        for (E e : s) {
            changed |= c.remove(e);
        }
        return changed ? new WrappedPersistentList<>(c, cloneFunction) : this;
    }

    @Override
    public @NonNull WrappedPersistentList<E> copyRetainAll(@NonNull Collection<? extends E> s) {
        List<E> c = cloneFunction.apply(list);
        boolean changed = false;
        for (Iterator<E> iterator = c.iterator(); iterator.hasNext(); ) {
            E e = iterator.next();
            if (!s.contains(e)) {
                changed = true;
                iterator.remove();
            }
        }
        return changed ? new WrappedPersistentList<>(c, cloneFunction) : this;
    }

    @Override
    public @NonNull PersistentList<E> copySet(int index, @NonNull E element) {
        List<E> c = cloneFunction.apply(list);
        c.set(index, element);
        return new WrappedPersistentList<>(c, cloneFunction);
    }

    @Override
    public @NonNull PersistentList<E> copySubList(int fromIndex, int toIndex) {
        List<E> c = cloneFunction.apply(list.subList(fromIndex, toIndex));
        return new WrappedPersistentList<>(c, cloneFunction);
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
    public @NonNull ReadOnlyList<E> readOnlySubList(int fromIndex, int toIndex) {
        return new WrappedReadOnlyList<>(list.subList(fromIndex, toIndex));
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
