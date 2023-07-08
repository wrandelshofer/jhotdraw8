/*
 * @(#)AABB.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

/**
 * Axis aligned bounding box (AABB).
 *
 * @param minX the minimal x value
 * @param minY the minimal y value
 * @param maxX the maximal x value
 * @param maxY the maximal y value
 */
public record AABB(double minX, double minY, double maxX, double maxY) {

    public double width() {
        return maxX - minX;
    }

    public double height() {
        return maxY - minY;
    }


}