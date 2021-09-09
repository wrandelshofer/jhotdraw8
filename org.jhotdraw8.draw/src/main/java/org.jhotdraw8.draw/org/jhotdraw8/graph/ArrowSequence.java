/*
 * @(#)ArrowPath.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.ReadOnlyCollection;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents an arrow sequence of a walk through a directed graph.
 * <p>
 * The same arrow may occur more than once in the sequence.
 *
 * @param <A> the arrow data type
 * @author Werner Randelshofer
 */
public class ArrowSequence<A> {

    private final @NonNull ImmutableList<A> arrows;

    public ArrowSequence(@NonNull ReadOnlyCollection<? extends A> elements) {
        this.arrows = ImmutableLists.ofCollection(elements);
    }

    public ArrowSequence(@NonNull Collection<? extends A> elements) {
        this.arrows = ImmutableLists.ofCollection(elements);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ArrowSequence<?> other = (ArrowSequence<?>) obj;
        return Objects.equals(this.arrows, other.arrows);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.arrows);
        return hash;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static @NonNull <AA> ArrowSequence<AA> of(AA... arrows) {
        return new ArrowSequence<>(ImmutableLists.of(arrows));
    }

    public @NonNull ImmutableList<A> getArrows() {
        return arrows;
    }

    public int size() {
        return arrows.size();
    }


    @Override
    public @NonNull String toString() {
        return "ArrowPath{" + arrows + '}';
    }


}
