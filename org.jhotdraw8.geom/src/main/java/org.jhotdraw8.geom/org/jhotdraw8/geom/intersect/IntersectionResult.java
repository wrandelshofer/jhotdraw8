/*
 * @(#)IntersectionResult.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.collection.primitive.DoubleArrayList;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentCollection;
import org.jhotdraw8.icollection.persistent.PersistentList;

import java.util.Collection;

public class IntersectionResult {
    private final IntersectionStatus status;
    private final PersistentList<IntersectionPoint> intersections;

    public IntersectionResult(IntersectionStatus status, Collection<? extends IntersectionPoint> copyItems) {
        this.intersections = VectorList.copyOf(copyItems);
        this.status = status;
    }

    public IntersectionResult(IntersectionStatus status, PersistentCollection<? extends IntersectionPoint> copyItems) {
        this.intersections = VectorList.copyOf(copyItems);
        this.status = status;
    }

    public IntersectionResult(Collection<? extends IntersectionPoint> copyItems) {
        this(copyItems.isEmpty() ? IntersectionStatus.NO_INTERSECTION : IntersectionStatus.INTERSECTION,
                copyItems);
    }

    public IntersectionStatus getStatus() {
        return status;
    }

    public DoubleArrayList getAllArgumentsA() {
        return intersections.stream()
                .mapToDouble(IntersectionPoint::argumentA)
                .collect(DoubleArrayList::new, DoubleArrayList::add, DoubleArrayList::addAll);
    }

    public PersistentList<IntersectionPoint> intersections() {
        return intersections;
    }
}
