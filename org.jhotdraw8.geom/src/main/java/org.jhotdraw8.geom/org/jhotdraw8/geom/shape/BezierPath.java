/*
 * @(#)BezierPath.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.shape;

import javafx.scene.shape.FillRule;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.geom.AwtShapes;
import org.jhotdraw8.geom.BoundingBoxBuilder;
import org.jhotdraw8.geom.CubicCurves;
import org.jhotdraw8.geom.PointAndDerivative;
import org.jhotdraw8.geom.QuadCurves;
import org.jhotdraw8.geom.ReversePathIterator;
import org.jhotdraw8.geom.intersect.IntersectPathIteratorPoint;
import org.jhotdraw8.geom.intersect.IntersectionPoint;
import org.jhotdraw8.geom.intersect.IntersectionResult;
import org.jhotdraw8.geom.intersect.IntersectionStatus;
import org.jhotdraw8.icollection.PrivateData;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A BezierPath is defined by its nodes. Each node has three control points:
 * C0, C1, C2. A mask defines which control points are in use. At a node, the path
 * passes through C0. C1 controls the curve going towards C0. C2 controls the
 * curve going away from C0.
 *
 * @author Werner Randelshofer
 */
public class BezierPath extends VectorList<BezierNode> implements Shape {
    /**
     * This field is used for memoizing PathMetrics that have been built fom this instance.
     */
    private transient @Nullable PathMetrics pathMetrics;
    /**
     * This field is used for memoizing Bounds that have been built fom this instance.
     */
    private transient Rectangle2D.@Nullable Double bounds;
    private final int windingRule;

    private BezierPath(int windingRule) {
        super();
        this.windingRule = windingRule;
    }

    public BezierPath(@NonNull PrivateData privateData, int windingRule) {
        super(privateData);
        this.windingRule = windingRule;
    }

    public BezierPath(@Nullable Iterable<BezierNode> nodes) {
        this(nodes, PathIterator.WIND_EVEN_ODD);
    }

    public BezierPath(@Nullable Iterable<BezierNode> nodes, FillRule windingRule) {
        this(nodes, windingRule == FillRule.EVEN_ODD ? PathIterator.WIND_EVEN_ODD : PathIterator.WIND_NON_ZERO);

    }

    public BezierPath(@Nullable Iterable<BezierNode> nodes, int windingRule) {
        super(nodes);
        this.windingRule = windingRule;
    }

    @Override
    public boolean contains(double x, double y) {
        return contains(x, y, 0);
    }

    public boolean contains(double x, double y, double tolerance) {
        final IntersectionResult isect = IntersectPathIteratorPoint.intersectPathIteratorPoint(getPathIterator(null), x, y, tolerance);
        IntersectionStatus status = isect.getStatus();
        return status == IntersectionStatus.NO_INTERSECTION_INSIDE || status == IntersectionStatus.INTERSECTION;
    }

    @Override
    public boolean contains(@NonNull Point2D p) {
        return contains(p.getX(), p.getY());
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean contains(@NonNull Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public Rectangle getBounds() {
        return getBounds2D().getBounds();
    }

    @Override
    public @NonNull Rectangle2D getBounds2D() {
        if (bounds == null) {
            bounds = AwtShapes.buildFromPathIterator(new BoundingBoxBuilder(), getPathIterator(null)).buildRectangle2D();
        }
        return new Rectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public @NonNull PathIterator getPathIterator(AffineTransform at) {
        return new BezierPathIterator(this, at);
    }

    @Override
    public @NonNull PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean intersects(@NonNull Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public boolean pathIntersects(double x, double y, double tolerance) {
        IntersectionResult isect = IntersectPathIteratorPoint.intersectPathIteratorPoint(getPathIterator(null), x, y, tolerance);
        return !isect.intersections().isEmpty();
    }

    public IntersectionResult pathIntersection(double x, double y, double tolerance) {
        IntersectionResult isect = IntersectPathIteratorPoint.intersectPathIteratorPoint(getPathIterator(null), x, y, tolerance);
        return isect;
    }

    public BezierPath split(double x, double y, double tolerance) {
        IntersectionResult isect = IntersectPathIteratorPoint.intersectPathIteratorPoint(getPathIterator(null), x, y, tolerance);
        ImmutableList<IntersectionPoint> intersections = isect.intersections();
        @SuppressWarnings("unchecked") VectorList<BezierNode>[] result = new VectorList[]{this};
        if (intersections.size() == 1) {
            int segment = (int) intersections.getFirst().getArgumentA();
            final BezierNode middle;
            Point2D.Double p = intersections.get(0);
            final int prevSegment = (segment - 1 + size()) % size();
            BezierNode prev = get(prevSegment);
            BezierNode next = get(segment);
            double t = intersections.getFirst().getArgumentA() - segment;
            boolean pc2 = prev.hasOut();
            boolean nc1 = next.hasIn();
            if (pc2) {
                if (nc1) {
                    // cubic curve
                    middle = new BezierNode(BezierNode.IN_OUT_MASK, true, true, p.getX(), p.getY(), p.getX(), p.getY(), p.getX(), p.getY());
                    result[0] = result[0].add(segment, middle);
                    CubicCurves.splitCubicCurveTo(prev.pointX(), prev.pointY(), prev.outX(), prev.outY(),
                            next.inX(), next.inY(), next.pointX(), next.pointY(), t,
                            (x1, y1, x2, y2, x3, y3) -> {
                                result[0] = result[0].set(prevSegment, prev.withOx(x1).withOy(y1));
                                result[0] = result[0].set(segment, result[0].get(segment).withIx(x2).withIy(y2));
                            },
                            (x1, y1, x2, y2, x3, y3) -> {
                                result[0] = result[0].set(segment, result[0].get(segment).withOx(x1).withOy(y1));
                                result[0] = result[0].set(segment + 1, next.withIx(x2).withIy(y2));
                            }
                    );
                } else {
                    // quadratic curve controlled by prev
                    middle = new BezierNode(BezierNode.OUT_MASK, true, true, p.getX(), p.getY(), p.getX(), p.getY(), p.getX(), p.getY());
                    prev.withCollinear(true);
                    result[0] = result[0].add(segment, middle);
                    QuadCurves.split(prev.pointX(), prev.pointY(),
                            next.inX(), next.inY(), next.pointX(), next.pointY(), t,
                            (x1, y1, x2, y2) -> {
                                result[0] = result[0].set(prevSegment, middle.withOx(x1).withOy(y1));
                                result[0] = result[0].set(segment, result[0].get(segment).withPx(x2).withPointY(y2));
                            },
                            (x1, y1, x2, y2) -> {
                                result[0] = result[0].set(segment, result[0].get(segment).withOx(x1).withOy(y1));
                            }
                    );
                }
            } else if (nc1) {
                // quadratic curve controlled by next
                middle = new BezierNode(BezierNode.IN_MASK, true, true, p.getX(), p.getY(), p.getX(), p.getY(), p.getX(), p.getY());
                result[0] = result[0].add(segment, middle);
                QuadCurves.split(prev.pointX(), prev.pointY(),
                        next.inX(), next.inY(), next.pointX(), next.pointY(), t,
                        (x1, y1, x2, y2) -> {
                            result[0] = result[0].set(segment, middle.withIx(x1).withIy(y1).withPx(x2).withPointY(y2));
                        },
                        (x1, y1, x2, y2) -> {
                            result[0] = result[0].set(segment + 1, next.withIx(x1).withIy(y1).withCollinear(true));
                        }
                );
            } else {
                // line
                middle = new BezierNode(BezierNode.POINT_MASK, true, true, p.getX(), p.getY(), p.getX(), p.getY(), p.getX(), p.getY());
                result[0] = result[0].add(segment, middle);
            }
        }
        return (BezierPath) result[0];
    }

    public BezierPath join(int segment, double tolerance) {
        @SuppressWarnings("unchecked") VectorList<BezierNode>[] result = new VectorList[]{this};

        final int prevSegment = (segment - 1 + size()) % size();
        final int nextSegment = (segment + 1) % size();
        BezierNode prev = get(prevSegment);
        BezierNode middle = get(segment);
        BezierNode next = get(nextSegment);
        boolean pc2 = prev.hasOut();
        boolean mc2 = middle.hasOut();
        boolean mc1 = middle.hasIn();
        boolean nc1 = next.hasIn();
        if (!pc2 && mc1 && nc1) {
            double[] p = QuadCurves.merge(
                    prev.pointX(), prev.pointY(), middle.inX(), middle.inY(), middle.pointX(), middle.pointY(),
                    next.inX(), next.inY(), next.pointX(), next.pointY(), tolerance);
            if (p != null) {
                result[0] = result[0].set(nextSegment, next.withIx(p[2]).withIy(p[3]));
            }
        } else if (pc2 && mc2 && !nc1) {
            double[] p = QuadCurves.merge(
                    prev.pointX(), prev.pointY(), prev.outX(), prev.outY(), middle.pointX(), middle.pointY(),
                    middle.outX(), middle.outY(), next.pointX(), next.pointY(), tolerance);
            if (p != null) {
                result[0] = result[0].set(prevSegment, prev.withOx(p[2]).withOy(p[3]));
            }
        } else if (pc2 && mc1 && mc2 && nc1) {
            double[] p = CubicCurves.merge(
                    prev.pointX(), prev.pointY(), prev.outX(), prev.outY(), middle.inX(), middle.inY(), middle.pointX(), middle.pointY(),
                    middle.outX(), middle.outY(), next.inX(), next.inY(), next.pointX(), next.pointY(), tolerance);
            if (p != null) {
                result[0] = result[0].set(prevSegment, prev.withOx(p[2]).withOy(p[3]));
                result[0] = result[0].set(nextSegment, next.withIx(p[4]).withIy(p[5]));
            }
        }
        result[0] = result[0].removeAt(segment);
        return (BezierPath) result[0];
    }

    /**
     * Gets the outgoing tangent point for the bezier node
     * at the specified index.
     *
     * @param index point of a bezier node
     * @return outgoing tangent point
     */
    public @Nullable Point2D getOutgoingTangentPoint(int index) {
        BezierNode node = get(index);
        if (node.hasOut()) {
            return new Point2D.Double(node.outX(), node.outY());
        }
        return null;
    }

    public BezierPath setWindingRule(int newValue) {
        return this.windingRule == newValue ? this : new BezierPath(this, newValue);
    }

    public BezierPath setWindingRule(FillRule newValue) {
        return setWindingRule(newValue == FillRule.EVEN_ODD ? PathIterator.WIND_EVEN_ODD : PathIterator.WIND_NON_ZERO);
    }

    /**
     * Reverses the direction of the path.
     */
    @Override
    public BezierPath reverse() {
        return AwtShapes.buildFromPathIterator(new BezierPathBuilder(), new ReversePathIterator(getPathIterator(null), windingRule)).build();
        /*
        int size = size();
        ArrayList<BezierNode> temp = new ArrayList<>(size);
        int lastMoveTo = -1;
        for (int i = 0; i < size; i++) {
            var n = get(i);
            BezierNode reversed = n;
            if (reversed.isMoveTo()) {
                lastMoveTo = i;
                if (i < size && !get(i + 1).isMoveTo()) {
                    // keep a move to, if it is followed by another move to
                    reversed = reversed.withClearMaskBits(BezierNode.MOVE_MASK);
                }
            } else if (reversed.isClosePath()) {
                temp.set(lastMoveTo, temp.get(lastMoveTo).withMaskBits(BezierNode.CLOSE_MASK));
                reversed = reversed.withClearMaskBits(BezierNode.CLOSE_MASK);
            }
            reversed = reversed.setX1(n.getX2()).setY1(n.getY2())
                    .setX2(n.getX1()).setY2(n.getY1());
            reversed = reversed.withClearMaskBits(BezierNode.C1C2_MASK);
            if (n.isC1()) {
                reversed = reversed.withMaskBits(BezierNode.C2_MASK);
            }
            if (n.isC2()) {
                reversed = reversed.withMaskBits(BezierNode.C1_MASK);
            }
            if (i == size - 1) {
                reversed = reversed.withMaskBits(BezierNode.MOVE_MASK);
            }
            temp.add(reversed);
        }
        return new BezierPath(temp, windingRule);

         */
    }

    private static final BezierPath EMPTY = new BezierPath(PathIterator.WIND_EVEN_ODD);


    public static @NonNull BezierPath of() {
        return EMPTY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull BezierPath empty() {
        return isEmpty() ? this : new BezierPath(windingRule);
    }

    @Override
    protected VectorList<BezierNode> newInstance(@NonNull PrivateData privateData) {
        return new BezierPath(privateData, windingRule);
    }

    @Override
    public @NonNull BezierPath add(@NonNull BezierNode element) {
        return (BezierPath) super.add(element);
    }

    @Override
    public @NonNull BezierPath add(int index, @NonNull BezierNode element) {
        return (BezierPath) super.add(index, element);
    }

    @Override
    public @NonNull BezierPath addAll(@NonNull Iterable<? extends BezierNode> c) {
        return (BezierPath) super.addAll(c);
    }

    @Override
    public @NonNull BezierPath addFirst(@Nullable BezierNode element) {
        return (BezierPath) super.addFirst(element);
    }

    @Override
    public @NonNull BezierPath addLast(@Nullable BezierNode element) {
        return (BezierPath) super.addLast(element);
    }

    @Override
    public @NonNull BezierPath addAll(int index, @NonNull Iterable<? extends BezierNode> c) {
        return (BezierPath) super.addAll(index, c);
    }


    @Override
    public @NonNull BezierPath remove(@NonNull BezierNode element) {
        return (BezierPath) super.remove(element);
    }

    @Override
    public @NonNull BezierPath removeAt(int index) {
        return (BezierPath) super.removeAt(index);
    }

    @Override
    public @NonNull BezierPath removeFirst() {
        return (BezierPath) super.removeFirst();
    }

    @Override
    public @NonNull BezierPath removeLast() {
        return (BezierPath) super.removeLast();
    }

    @Override
    public @NonNull BezierPath retainAll(@NonNull Iterable<?> c) {
        return (BezierPath) super.retainAll(c);
    }

    @Override
    public @NonNull BezierPath removeRange(int fromIndex, int toIndex) {
        return (BezierPath) super.removeRange(fromIndex, toIndex);
    }

    @Override
    public @NonNull BezierPath removeAll(@NonNull Iterable<?> c) {
        return (BezierPath) super.removeAll(c);
    }

    @Override
    public @NonNull BezierPath set(int index, @NonNull BezierNode element) {
        return (BezierPath) super.set(index, element);
    }

    @Override
    public BezierNode get(int index) {
        return super.get(index);
    }

    public @NonNull PathMetrics getPathMetrics() {
        if (pathMetrics == null) {
            pathMetrics = new SimplePathMetrics(this);
        }
        return pathMetrics;
    }

    @Override
    public @NonNull BezierPath readOnlySubList(int fromIndex, int toIndex) {
        return (BezierPath) super.readOnlySubList(fromIndex, toIndex);
    }

    @Override
    public int size() {
        return super.size();
    }

    /**
     * Evaluates the first point of the bezier path.
     *
     * @return the point and derivative of the first point in the path
     * @throws java.util.NoSuchElementException if the path is empty
     */
    public @NonNull PointAndDerivative evalFirst() {
        BezierNode first = getFirst();
        double y0 = first.pointY();
        double x0 = first.pointX();
        if (first.hasOut()) {
            return new PointAndDerivative(
                    x0, y0,
                    first.outX() - x0, first.outY() - y0);
        }
        if (size() < 2) return new PointAndDerivative(first.pointX(), y0, 1, 0);
        BezierNode second = get(1);
        if (second.hasIn()) {
            return new PointAndDerivative(x0, y0, second.inX() - x0, second.inY() - y0);
        }
        return new PointAndDerivative(x0, y0, second.pointX() - x0, second.pointY() - y0);
    }

    /**
     * Evaluates the reverse derivative of the last point.
     * <p>
     * The result is the same as reversing the path, and then
     * evaluating its first point.
     *
     * @return the reverse derivative
     * @throws java.util.NoSuchElementException if the path is empty
     */
    public @NonNull PointAndDerivative evalLastInReverse() {
        BezierNode last = getLast();
        BezierNode secondLast = size() > 1 ? get(size() - 2) : null;
        if (last.hasMask(BezierNode.CLOSE_MASK) && size() > 1) {
            for (int i = size() - 1; i >= 0; i--) {
                if (i == 0 || get(i).hasMask(BezierNode.MOVE_MASK)) {
                    secondLast = i == 0 ? last : get(i - 1);
                    last = get(i);
                    break;
                }
            }
        }

        double y0 = last.pointY();
        double x0 = last.pointX();
        if (last.hasIn()) {
            return new PointAndDerivative(x0, y0,
                    last.inX() - x0, last.inY() - y0);
        }
        if (secondLast == null) return new PointAndDerivative(x0, y0, -1, 0);
        if (secondLast.hasOut()) {
            return new PointAndDerivative(x0, y0,
                    secondLast.outX() - x0, secondLast.outY() - y0);
        }
        return new PointAndDerivative(x0, y0, secondLast.pointX() - x0, secondLast.pointY() - y0);
    }

    public int getWindingRule() {
        return windingRule;
    }
}
