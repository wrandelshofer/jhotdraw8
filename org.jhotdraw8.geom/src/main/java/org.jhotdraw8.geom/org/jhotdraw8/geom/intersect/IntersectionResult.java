/*
 * @(#)IntersectionResult.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.immutable.ImmutableCollection;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.primitive.DoubleArrayList;

import java.util.Collection;

public class IntersectionResult {
    private final @NonNull IntersectionStatus status;
    private final @NonNull ImmutableList<IntersectionPoint> intersections;

    public IntersectionResult(@NonNull IntersectionStatus status, @NonNull Collection<? extends IntersectionPoint> copyItems) {
        this.intersections = VectorList.copyOf(copyItems);
        this.status = status;
    }

    public IntersectionResult(@NonNull IntersectionStatus status, @NonNull ImmutableCollection<? extends IntersectionPoint> copyItems) {
        this.intersections = VectorList.copyOf(copyItems);
        this.status = status;
    }

    public IntersectionResult(@NonNull Collection<? extends IntersectionPoint> copyItems) {
        this(copyItems.isEmpty() ? IntersectionStatus.NO_INTERSECTION : IntersectionStatus.INTERSECTION,
                copyItems);
    }

    public @NonNull IntersectionStatus getStatus() {
        return status;
    }

    public @NonNull DoubleArrayList getAllArgumentsA() {
        return intersections.stream()
                .mapToDouble(IntersectionPoint::getArgumentA)
                .collect(DoubleArrayList::new, DoubleArrayList::add, DoubleArrayList::addAll);
    }

    public @NonNull ImmutableList<IntersectionPoint> intersections() {
        return intersections;
    }
}
