package org.jhotdraw8.geom;

import org.jhotdraw8.geom.intersect.IntersectPathIteratorPoint;
import org.jhotdraw8.geom.intersect.IntersectionResult;
import org.jhotdraw8.geom.intersect.IntersectionStatus;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

public abstract class AbstractShape implements Shape {
    @Override
    public Rectangle getBounds() {
        return getBounds2D().getBounds();
    }

    @Override
    public Rectangle2D getBounds2D() {
        double minx = POSITIVE_INFINITY, miny = POSITIVE_INFINITY, maxx = NEGATIVE_INFINITY, maxy = NEGATIVE_INFINITY;
        double[] coords = new double[6];
        PathIterator it = getPathIterator(null);
        while (!it.isDone()) {
            switch (it.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> {
                    minx = Math.min(minx, coords[0]);
                    miny = Math.min(miny, coords[1]);
                    maxx = Math.max(maxx, coords[0]);
                    maxy = Math.max(maxy, coords[1]);
                }
                case PathIterator.SEG_QUADTO -> {
                    minx = Math.min(minx, coords[0]);
                    miny = Math.min(miny, coords[1]);
                    maxx = Math.max(maxx, coords[0]);
                    maxy = Math.max(maxy, coords[1]);
                    minx = Math.min(minx, coords[2]);
                    miny = Math.min(miny, coords[3]);
                    maxx = Math.max(maxx, coords[2]);
                    maxy = Math.max(maxy, coords[3]);
                }
                case PathIterator.SEG_CUBICTO -> {
                    minx = Math.min(minx, coords[0]);
                    miny = Math.min(miny, coords[1]);
                    maxx = Math.max(maxx, coords[0]);
                    maxy = Math.max(maxy, coords[1]);
                    minx = Math.min(minx, coords[2]);
                    miny = Math.min(miny, coords[3]);
                    maxx = Math.max(maxx, coords[2]);
                    maxy = Math.max(maxy, coords[3]);
                    minx = Math.min(minx, coords[4]);
                    miny = Math.min(miny, coords[5]);
                    maxx = Math.max(maxx, coords[4]);
                    maxy = Math.max(maxy, coords[5]);

                }
            }
            it.next();
        }
        return new Rectangle2D.Double(minx, miny, maxx - minx, maxy - miny);
    }

    @Override
    public boolean contains(double x, double y) {
        IntersectionResult result = IntersectPathIteratorPoint.intersectPathIteratorPoint(getPathIterator(null), x, y, 0);
        return result.getStatus() == IntersectionStatus.NO_INTERSECTION_INSIDE || result.getStatus() == IntersectionStatus.INTERSECTION;
    }


    @Override
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        // FIXME implement me
        return true;
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
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
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
    }
}
