/*
 * @(#)IntersectionResultEx.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.immutable.ImmutableArrayList;
import org.jhotdraw8.collection.primitive.DoubleArrayList;

import java.util.Collections;
import java.util.List;

public class IntersectionResultEx extends ImmutableArrayList<IntersectionPointEx> {

    private final IntersectionStatus status;

    public IntersectionResultEx(@NonNull List<IntersectionPointEx> intersections) {
        this(intersections.isEmpty() ? IntersectionStatus.NO_INTERSECTION : IntersectionStatus.INTERSECTION, intersections);
    }

    public IntersectionResultEx(IntersectionStatus status) {
        this(status, Collections.emptyList());
    }

    public IntersectionResultEx(IntersectionStatus status, @NonNull List<IntersectionPointEx> intersections) {
        super(intersections);
        this.status = status;
    }


    public IntersectionStatus getStatus() {
        return status;
    }

    public DoubleArrayList getAllArgumentsB() {
        return stream()
                .mapToDouble(IntersectionPointEx::getArgumentB)
                .collect(DoubleArrayList::new, DoubleArrayList::add, DoubleArrayList::addAll);
    }

    public DoubleArrayList getAllArgumentsA() {
        return stream()
                .mapToDouble(IntersectionPointEx::getArgumentA)
                .collect(DoubleArrayList::new, DoubleArrayList::add, DoubleArrayList::addAll);
    }
}
