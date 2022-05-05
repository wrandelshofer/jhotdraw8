/*
 * @(#)PersistentSetWrapper.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class WrappedPersistentSet<E> extends AbstractReadOnlySet<E> implements PersistentSet<E> {
    private final @NonNull Set<E> set;
    private final @NonNull Function<Set<E>, Set<E>> cloneFunction;

    public WrappedPersistentSet(@NonNull Set<E> set, @NonNull Function<Set<E>, Set<E>> cloneFunction) {
        this.set = set;
        this.cloneFunction = cloneFunction;
    }

    @SuppressWarnings("unchecked")
    static <E> PersistentSet<E> of() {
        return new WrappedPersistentSet<>(new LinkedHashSet<>(), s -> (Set<E>) ((LinkedHashSet<E>) s).clone());
    }

    @SuppressWarnings("unchecked")
    static <E> PersistentSet<E> of(@NonNull E... keys) {
        return new WrappedPersistentSet<>(new LinkedHashSet<>(Arrays.asList(keys)), s -> (Set<E>) ((LinkedHashSet<E>) s).clone());
    }

    @SuppressWarnings("unchecked")
    static <E> PersistentSet<E> copyOf(@NonNull Iterable<? extends E> set) {
        if (set instanceof WrappedPersistentSet) {
            return (PersistentSet<E>) set;
        }
        LinkedHashSet<E> ss = new LinkedHashSet<>();
        for (E e : set) {
            ss.add(e);
        }

        return new WrappedPersistentSet<>(ss, s -> (Set<E>) ((LinkedHashSet<E>) s).clone());
    }

    @Override
    public @NonNull WrappedPersistentSet<E> copyClear() {
        if (set.isEmpty()) {
            return this;
        }
        Set<E> c = cloneFunction.apply(set);
        c.clear();
        return new WrappedPersistentSet<>(c, cloneFunction);
    }

    @Override
    public @NonNull WrappedPersistentSet<E> copyAdd(@NonNull E element) {
        if (set.contains(element)) {
            return this;
        }
        Set<E> c = cloneFunction.apply(set);
        c.add(element);
        return new WrappedPersistentSet<>(c, cloneFunction);
    }

    @Override
    public @NonNull WrappedPersistentSet<E> copyAddAll(@NonNull Iterable<? extends E> s) {
        Set<E> c = cloneFunction.apply(set);
        boolean changed = false;
        for (E e : s) {
            changed |= c.add(e);
        }
        return changed ? new WrappedPersistentSet<>(c, cloneFunction) : this;
    }

    @Override
    public @NonNull WrappedPersistentSet<E> copyRemove(@NonNull E element) {
        if (!set.contains(element)) {
            return this;
        }
        Set<E> c = cloneFunction.apply(set);
        c.remove(element);
        return new WrappedPersistentSet<>(c, cloneFunction);

    }

    @Override
    public @NonNull WrappedPersistentSet<E> copyRemoveAll(@NonNull Iterable<? extends E> s) {
        Set<E> c = cloneFunction.apply(set);
        boolean changed = false;
        for (E e : s) {
            changed |= c.remove(e);
        }
        return changed ? new WrappedPersistentSet<>(c, cloneFunction) : this;
    }

    @Override
    public @NonNull WrappedPersistentSet<E> copyRetainAll(@NonNull Collection<? extends E> s) {
        Set<E> c = cloneFunction.apply(set);
        boolean changed = false;
        for (Iterator<E> iterator = c.iterator(); iterator.hasNext(); ) {
            E e = iterator.next();
            if (!s.contains(e)) {
                changed = true;
                iterator.remove();
            }
        }
        return changed ? new WrappedPersistentSet<>(c, cloneFunction) : this;
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableSet(set).iterator();
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean contains(@Nullable Object e) {
        return set.contains(e);
    }


    @Override
    public int hashCode() {
        return set.hashCode();
    }
}
