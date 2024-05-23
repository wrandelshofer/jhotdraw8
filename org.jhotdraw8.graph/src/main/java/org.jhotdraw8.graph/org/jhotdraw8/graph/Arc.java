/*
 * @(#)Arc.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Data record for an arrow with associated data in a directed graph.
 * <p>
 * "Arc" is used as a synonym for arrow in some definitions for directed
 * graphs. In this design, "Arc" explicitly means a data object that contains
 * the start and end vertices of and arrow and an associated data object.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 */
public class Arc<V, A> {
    private final V start;
    private final V end;
    private final @Nullable A data;

    public Arc(V start, V end, @Nullable A data) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        this.start = start;
        this.end = end;
        this.data = data;
    }

    public @Nullable A getArrow() {
        return data;
    }

    public V getEnd() {
        return end;
    }

    public V getStart() {
        return start;
    }

    @Override
    public String toString() {
        return "Arc{" + start +
                "->" + end +
                ", " + data +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Arc<?, ?> arc)) {
            return false;
        }
        return Objects.equals(start, arc.start) &&
                end.equals(arc.end) &&
                Objects.equals(data, arc.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, data);
    }
}
