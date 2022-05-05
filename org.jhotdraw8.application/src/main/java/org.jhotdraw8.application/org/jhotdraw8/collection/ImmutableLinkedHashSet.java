/*
 * @(#)WrappedImmutableSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * An immutable set backed by a {@link LinkedHashSet}.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ImmutableLinkedHashSet<E> extends WrappedReadOnlySet<E> implements ImmutableSet<E> {
    private final static ImmutableSet<Object> EMPTY = new ImmutableLinkedHashSet<>(Collections.emptyList());

    @SuppressWarnings("unchecked")
    public static <E> ImmutableSet<E> emptySet() {
        return (ImmutableSet<E>) EMPTY;
    }

    public ImmutableLinkedHashSet(Collection<? extends E> backingSet) {
        super(new LinkedHashSet<>(backingSet));
    }


    @SuppressWarnings("unchecked")
    public static @NonNull <T> ImmutableSet<T> of() {
        return emptySet();
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static @NonNull <T> ImmutableSet<T> of(@NonNull T... items) {
        return new ImmutableLinkedHashSet<T>(Arrays.asList(items));
    }

    public static @NonNull <T> ImmutableSet<T> copyOf(@NonNull Collection<T> collection) {
        return new ImmutableLinkedHashSet<T>(collection);
    }

    public static @NonNull <T> ImmutableSet<T> copyOf(ReadOnlyCollection<T> collection) {
        return new ImmutableLinkedHashSet<T>(collection.asCollection());
    }


}
