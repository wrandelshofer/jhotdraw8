/*
 * @(#)WrappedImmutableSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.ArrayList;
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
    public static <E> ImmutableSet<E> of() {
        return (ImmutableSet<E>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <E> @NonNull ImmutableLinkedHashSet<E> of(E... elements) {
        return new ImmutableLinkedHashSet<E>(Arrays.asList(elements));
    }

    @SuppressWarnings("unchecked")
    public static <E> ImmutableLinkedHashSet<E> copyOf(Iterable<? extends E> list) {
        if (list instanceof ImmutableLinkedHashSet) {
            return (ImmutableLinkedHashSet<E>) list;
        }
        if (list instanceof Collection) {
            return new ImmutableLinkedHashSet<>((Collection<E>) list);
        }
        ArrayList<E> a = new ArrayList<>();
        list.forEach(a::add);
        return new ImmutableLinkedHashSet<>(a);
    }

    public ImmutableLinkedHashSet(Collection<? extends E> set) {
        super(new LinkedHashSet<>(set));
    }
}
