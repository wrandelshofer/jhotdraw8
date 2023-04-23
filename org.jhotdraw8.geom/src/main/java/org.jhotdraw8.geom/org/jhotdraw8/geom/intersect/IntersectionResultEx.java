/*
 * @(#)IntersectionResultEx.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.primitive.DoubleArrayList;

import java.util.Collections;
import java.util.List;

public class IntersectionResultEx {
    private final @NonNull ImmutableList<IntersectionPointEx> intersections;
    private final @NonNull IntersectionStatus status;

    public IntersectionResultEx(@NonNull List<IntersectionPointEx> intersections) {
        this(intersections.isEmpty() ? IntersectionStatus.NO_INTERSECTION : IntersectionStatus.INTERSECTION, intersections);
    }

    public IntersectionResultEx(IntersectionStatus status) {
        this(status, Collections.emptyList());
    }

    public IntersectionResultEx(@NonNull IntersectionStatus status, @NonNull List<IntersectionPointEx> intersections) {
        this.intersections = VectorList.copyOf(intersections);
        this.status = status;
    }


    public @NonNull IntersectionStatus getStatus() {
        return status;
    }

    public DoubleArrayList getAllArgumentsB() {
        return intersections.stream()
                .mapToDouble(IntersectionPointEx::getArgumentB)
                .collect(DoubleArrayList::new, DoubleArrayList::add, DoubleArrayList::addAll);
    }

    public DoubleArrayList getAllArgumentsA() {
        return intersections.stream()
                .mapToDouble(IntersectionPointEx::getArgumentA)
                .collect(DoubleArrayList::new, DoubleArrayList::add, DoubleArrayList::addAll);
    }

    public ImmutableList<IntersectionPointEx> intersections() {
        return intersections;
    }
}
