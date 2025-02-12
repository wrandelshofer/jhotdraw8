/*
 * @(#)IntersectionResultEx.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.collection.primitive.DoubleArrayList;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;

import java.util.Collections;
import java.util.List;

public class IntersectionResultEx {
    private final PersistentList<IntersectionPointEx> intersections;
    private final IntersectionStatus status;

    public IntersectionResultEx(List<IntersectionPointEx> intersections) {
        this(intersections.isEmpty() ? IntersectionStatus.NO_INTERSECTION : IntersectionStatus.INTERSECTION, intersections);
    }

    public IntersectionResultEx(IntersectionStatus status) {
        this(status, Collections.emptyList());
    }

    public IntersectionResultEx(IntersectionStatus status, List<IntersectionPointEx> intersections) {
        this.intersections = VectorList.copyOf(intersections);
        this.status = status;
    }


    public IntersectionStatus getStatus() {
        return status;
    }

    public DoubleArrayList getAllArgumentsB() {
        return intersections.stream()
                .mapToDouble(IntersectionPointEx::getArgumentB)
                .collect(DoubleArrayList::new, DoubleArrayList::add, DoubleArrayList::addAll);
    }

    public DoubleArrayList getAllArgumentsA() {
        return intersections.stream()
                .mapToDouble(IntersectionPointEx::argumentA)
                .collect(DoubleArrayList::new, DoubleArrayList::add, DoubleArrayList::addAll);
    }

    public PersistentList<IntersectionPointEx> intersections() {
        return intersections;
    }
}
