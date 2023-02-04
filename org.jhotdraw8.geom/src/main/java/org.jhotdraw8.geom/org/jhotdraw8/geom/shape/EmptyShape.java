/*
 * @(#)EmptyShape.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom.shape;

import org.jhotdraw8.geom.EmptyPathIterator;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * An immutable empty shape.
 */
public class EmptyShape implements Shape {
    public EmptyShape() {
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(0, 0, 0, 0);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(0, 0, 0, 0);
    }

    @Override
    public boolean contains(double x, double y) {
        return false;
    }

    @Override
    public boolean contains(Point2D p) {
        return false;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return false;
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return false;
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return false;
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return false;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new EmptyPathIterator();
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new EmptyPathIterator();
    }
}
