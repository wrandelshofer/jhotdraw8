/*
 * @(#)IntersectionResult.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.immutable.ImmutableArrayList;
import org.jhotdraw8.collection.immutable.ImmutableCollection;
import org.jhotdraw8.collection.primitive.DoubleArrayList;

import java.util.Collection;

public class IntersectionResult extends ImmutableArrayList<IntersectionPoint> {
    private final IntersectionStatus status;

    public IntersectionResult(@NonNull IntersectionStatus status, @NonNull Collection<? extends IntersectionPoint> copyItems) {
        super(copyItems);
        this.status = status;
    }

    public IntersectionResult(@NonNull IntersectionStatus status, @NonNull ImmutableCollection<? extends IntersectionPoint> copyItems) {
        super(copyItems);
        this.status = status;
    }

    public IntersectionResult(@NonNull Collection<? extends IntersectionPoint> copyItems) {
        this(copyItems.isEmpty() ? IntersectionStatus.NO_INTERSECTION : IntersectionStatus.INTERSECTION,
                copyItems);
    }

    public IntersectionStatus getStatus() {
        return status;
    }

    public DoubleArrayList getAllArgumentsA() {
        return stream()
                .mapToDouble(IntersectionPoint::getArgumentA)
                .collect(DoubleArrayList::new, DoubleArrayList::add, DoubleArrayList::addAll);
    }


}
