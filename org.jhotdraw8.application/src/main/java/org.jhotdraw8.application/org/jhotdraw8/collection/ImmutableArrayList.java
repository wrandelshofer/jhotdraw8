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


    @SuppressWarnings("unchecked")
    public static <E> ImmutableArrayList<E> of() {
        return (ImmutableArrayList<E>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <E> ImmutableArrayList<E> of(E... elements) {
        if (elements.length == 0) {
            return (ImmutableArrayList<E>) EMPTY;
        } else {
            return new ImmutableArrayList<E>(Arrays.asList(elements));
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> ImmutableArrayList<E> copyOf(Iterable<? extends E> list) {
        if (list instanceof ImmutableArrayList<?>) {
            return (ImmutableArrayList<E>) list;
        }
        if (list instanceof ReadOnlyCollection<?>) {
            ReadOnlyCollection<E> c = (ReadOnlyCollection<E>) list;
            return c.isEmpty() ? (ImmutableArrayList<E>) EMPTY : new ImmutableArrayList<E>(c.asCollection());
        }
        if (list instanceof Collection<?>) {
            Collection<E> c = (Collection<E>) list;
            return c.isEmpty() ? (ImmutableArrayList<E>) EMPTY : new ImmutableArrayList<E>(c);
        }
        ArrayList<E> a = new ArrayList<>();
        list.forEach(a::add);
        return copyOf(a);
    }

    @Override
    public @NonNull ImmutableArrayList<E> copyClear() {
        if (list.isEmpty()) {
            return this;
        }
        List<E> c = cloneFunction.apply(list);
        c.clear();
        return new ImmutableArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull ImmutableArrayList<E> copyAdd(@NonNull E element) {
        List<E> c = cloneFunction.apply(list);
        c.add(element);
        return new ImmutableArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull ImmutableList<E> copyAdd(int index, @NonNull E element) {
        List<E> c = cloneFunction.apply(list);
        c.add(index, element);
        return new ImmutableArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull ImmutableArrayList<E> copyAddAll(@NonNull Iterable<? extends E> s) {
        List<E> c = cloneFunction.apply(list);
        boolean changed = false;
        for (E e : s) {
            changed |= c.add(e);
        }
        return changed ? new ImmutableArrayList<>(c, cloneFunction) : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ImmutableList<E> copyAddAll(int index, @NonNull Iterable<? extends E> it) {
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
    public @NonNull ImmutableArrayList<E> copyRemove(@NonNull E element) {
        if (!list.contains(element)) {
            return this;
        }
        List<E> c = cloneFunction.apply(list);
        c.remove(element);
        return new ImmutableArrayList<>(c, cloneFunction);

    }

    @Override
    public @NonNull ImmutableList<E> copyRemoveAt(int index) {
        List<E> c = cloneFunction.apply(list);
        c.remove(index);
        return new ImmutableArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull ImmutableList<E> copyRemoveRange(int fromIndex, int toIndex) {
        List<E> c = cloneFunction.apply(list);
        c.subList(fromIndex, toIndex).clear();
        return new ImmutableArrayList<>(c, cloneFunction);
    }

    @Override
    public @NonNull ImmutableArrayList<E> copyRemoveAll(@NonNull Iterable<? extends E> s) {
        List<E> c = cloneFunction.apply(list);
        boolean changed = false;
        for (E e : s) {
            changed |= c.remove(e);
        }
        return changed ? new ImmutableArrayList<>(c, cloneFunction) : this;
    }

    @Override
    public @NonNull ImmutableArrayList<E> copyRetainAll(@NonNull Collection<? extends E> s) {
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
    public @NonNull ImmutableList<E> copySet(int index, @NonNull E element) {
        List<E> c = cloneFunction.apply(list);
        c.set(index, element);
        return new ImmutableArrayList<>(c, cloneFunction);
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
