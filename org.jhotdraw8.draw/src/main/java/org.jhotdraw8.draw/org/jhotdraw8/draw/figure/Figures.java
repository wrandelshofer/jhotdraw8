/*
 * @(#)Figures.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import org.jhotdraw8.collection.iterator.Iterators;
import org.jhotdraw8.geom.BoundsCalculator;

import static java.lang.Double.max;
import static java.lang.Math.min;

/**
 * Figures.
 *
 */
public class Figures {

    private Figures() {

    }

    public static Bounds getBoundsInWorld(Iterable<? extends Figure> figures) {
        return Iterators.toList(figures).stream().parallel().map(Figure::getLayoutBoundsInWorld)
                .filter(b -> Double.isFinite(b.getMaxX()) && Double.isFinite(b.getMaxY()))
                .collect(BoundsCalculator::new, BoundsCalculator::accept,
                        BoundsCalculator::combine).getBounds();
    }

    public static Bounds getCenterBounds(Iterable<? extends Figure> figures) {
        double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE,
                maxx = Double.MIN_VALUE, maxy = Double.MIN_VALUE;
        for (Figure f : figures) {
            Bounds b = f.localToWorld(f.getLayoutBounds());
            double cx = b.getMinX() + b.getWidth() * 0.5;
            double cy = b.getMinY() + b.getHeight() * 0.5;
            minx = min(minx, cx);
            maxx = max(maxx, cx);
            miny = min(miny, cy);
            maxy = max(maxy, cy);
        }
        return new BoundingBox(minx, miny, maxx - minx, maxy - miny);
    }
}
