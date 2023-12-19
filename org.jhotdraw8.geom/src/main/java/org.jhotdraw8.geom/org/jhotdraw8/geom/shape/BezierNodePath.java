/*
 * @(#)BezierNodePath.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.shape;

import javafx.scene.shape.FillRule;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.geom.CubicCurves;
import org.jhotdraw8.geom.QuadCurves;
import org.jhotdraw8.geom.intersect.IntersectPathIteratorPoint;
import org.jhotdraw8.geom.intersect.IntersectionPoint;
import org.jhotdraw8.geom.intersect.IntersectionResult;
import org.jhotdraw8.geom.intersect.IntersectionStatus;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A BezierNodePath is defined by its nodes. Each node has three control points:
 * C0, C1, C2. A mask defines which control points are in use. At a node, the path
 * passes through C0. C1 controls the curve going towards C0. C2 controls the
 * curve going away from C0.
 *
 * @author Werner Randelshofer
 */
public class BezierNodePath implements Shape {

    private List<BezierNode> nodes;
    private int windingRule;

    public BezierNodePath() {
        this(new ArrayList<>(), PathIterator.WIND_EVEN_ODD);
    }

    public BezierNodePath(@NonNull Iterable<BezierNode> nodes) {
        this(nodes, PathIterator.WIND_EVEN_ODD);
    }

    public BezierNodePath(@NonNull Iterable<BezierNode> nodes, FillRule windingRule) {
        this(nodes, windingRule == FillRule.EVEN_ODD ? PathIterator.WIND_EVEN_ODD : PathIterator.WIND_NON_ZERO);

    }

    public BezierNodePath(@NonNull Iterable<BezierNode> nodes, int windingRule) {
        this.nodes = new ArrayList<>();
        for (BezierNode n : nodes) {
            this.nodes.add(n);
        }
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
        double x1 = Double.POSITIVE_INFINITY, y1 = Double.POSITIVE_INFINITY,
                x2 = Double.NEGATIVE_INFINITY, y2 = Double.NEGATIVE_INFINITY;
        for (BezierNode n : nodes) {
            double y = n.getY0();
            double x = n.getX0();
            if (x < x1) {
                x1 = x;
            }
            if (y < y1) {
                y1 = y;
            }
            if (x > x2) {
                x2 = x;
            }
            if (y > y2) {
                y2 = y;
            }
            if (n.isC1()) {
                y = n.getY1();
                x = n.getX1();
                if (x < x1) {
                    x1 = x;
                }
                if (y < y1) {
                    y1 = y;
                }
                if (x > x2) {
                    x2 = x;
                }
                if (y > y2) {
                    y2 = y;
                }
            }
            if (n.isC2()) {
                y = n.getY2();
                x = n.getX2();
                if (x < x1) {
                    x1 = x;
                }
                if (y < y1) {
                    y1 = y;
                }
                if (x > x2) {
                    x2 = x;
                }
                if (y > y2) {
                    y2 = y;
                }
            }
        }
        return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }

    public List<BezierNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<BezierNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public @NonNull PathIterator getPathIterator(AffineTransform at) {
        return new BezierNodePathIterator(nodes, windingRule, at);
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

    public boolean split(double x, double y, double tolerance) {
        IntersectionResult isect = IntersectPathIteratorPoint.intersectPathIteratorPoint(getPathIterator(null), x, y, tolerance);
        ImmutableList<IntersectionPoint> intersections = isect.intersections();
        if (intersections.size() == 1) {
            int segment = (int) intersections.getFirst().getArgumentA();
            final BezierNode middle;
            Point2D.Double p = intersections.get(0);
            final int prevSegment = (segment - 1 + nodes.size()) % nodes.size();
            BezierNode prev = nodes.get(prevSegment);
            BezierNode next = nodes.get(segment);
            double t = intersections.getFirst().getArgumentA() - segment;
            boolean pc2 = prev.isC2();
            boolean nc1 = next.isC1();
            if (pc2) {
                if (nc1) {
                    // cubic curve
                    middle = new BezierNode(BezierNode.C1C2_MASK, true, true, p.getX(), p.getY(), p.getX(), p.getY(), p.getX(), p.getY());
                    nodes.add(segment, middle);
                    CubicCurves.splitCubicCurveTo(prev.getX0(), prev.getY0(), prev.getX2(), prev.getY2(),
                            next.getX1(), next.getY1(), next.getX0(), next.getY0(), t,
                            (x1, y1, x2, y2, x3, y3) -> {
                                nodes.set(prevSegment, prev.setX2(x1).setY2(y1));
                                nodes.set(segment, nodes.get(segment).setX1(x2).setY1(y2));
                            },
                            (x1, y1, x2, y2, x3, y3) -> {
                                nodes.set(segment, nodes.get(segment).setX2(x1).setY2(y1));
                                nodes.set(segment + 1, next.setX1(x2).setY1(y2));
                            }
                    );
                } else {
                    // quadratic curve controlled by prev
                    middle = new BezierNode(BezierNode.C2_MASK, true, true, p.getX(), p.getY(), p.getX(), p.getY(), p.getX(), p.getY());
                    prev.setCollinear(true);
                    nodes.add(segment, middle);
                    QuadCurves.split(prev.getX0(), prev.getY0(),
                            next.getX1(), next.getY1(), next.getX0(), next.getY0(), t,
                            (x1, y1, x2, y2) -> {
                                nodes.set(prevSegment, middle.setX2(x1).setY2(y1));
                                nodes.set(segment, nodes.get(segment).setX0(x2).setY0(y2));
                            },
                            (x1, y1, x2, y2) -> {
                                nodes.set(segment, nodes.get(segment).setX2(x1).setY2(y1));
                            }
                    );
                }
            } else if (nc1) {
                // quadratic curve controlled by next
                middle = new BezierNode(BezierNode.C1_MASK, true, true, p.getX(), p.getY(), p.getX(), p.getY(), p.getX(), p.getY());
                nodes.add(segment, middle);
                QuadCurves.split(prev.getX0(), prev.getY0(),
                        next.getX1(), next.getY1(), next.getX0(), next.getY0(), t,
                        (x1, y1, x2, y2) -> {
                            nodes.set(segment, middle.setX1(x1).setY1(y1).setX0(x2).setY0(y2));
                        },
                        (x1, y1, x2, y2) -> {
                            nodes.set(segment + 1, next.setX1(x1).setY1(y1).setCollinear(true));
                        }
                );
            } else {
                // line
                middle = new BezierNode(BezierNode.C0_MASK, true, true, p.getX(), p.getY(), p.getX(), p.getY(), p.getX(), p.getY());
                nodes.add(segment, middle);
            }

            return true;
        }
        return false;
    }

    public void join(int segment, double tolerance) {
        final int prevSegment = (segment - 1 + nodes.size()) % nodes.size();
        final int nextSegment = (segment + 1) % nodes.size();
        BezierNode prev = nodes.get(prevSegment);
        BezierNode middle = nodes.get(segment);
        BezierNode next = nodes.get(nextSegment);
        boolean pc2 = prev.isC2();
        boolean mc2 = middle.isC2();
        boolean mc1 = middle.isC1();
        boolean nc1 = next.isC1();
        if (!pc2 && mc1 && nc1) {
            double[] p = QuadCurves.merge(
                    prev.getX0(), prev.getY0(), middle.getX1(), middle.getY1(), middle.getX0(), middle.getY0(),
                    next.getX1(), next.getY1(), next.getX0(), next.getY0(), tolerance);
            if (p != null) {
                nodes.set(nextSegment, next.setX1(p[2]).setY1(p[3]));
            }
        } else if (pc2 && mc2 && !nc1) {
            double[] p = QuadCurves.merge(
                    prev.getX0(), prev.getY0(), prev.getX2(), prev.getY2(), middle.getX0(), middle.getY0(),
                    middle.getX2(), middle.getY2(), next.getX0(), next.getY0(), tolerance);
            if (p != null) {
                nodes.set(prevSegment, prev.setX2(p[2]).setY2(p[3]));
            }
        } else if (pc2 && mc1 && mc2 && nc1) {
            double[] p = CubicCurves.merge(
                    prev.getX0(), prev.getY0(), prev.getX2(), prev.getY2(), middle.getX1(), middle.getY1(), middle.getX0(), middle.getY0(),
                    middle.getX2(), middle.getY2(), next.getX1(), next.getY1(), next.getX0(), next.getY0(), tolerance);
            if (p != null) {
                nodes.set(prevSegment, prev.setX2(p[2]).setY2(p[3]));
                nodes.set(nextSegment, next.setX1(p[4]).setY1(p[5]));
            }
        }
        nodes.remove(segment);
    }

    /**
     * Gets the outgoing tangent point for the bezier node
     * at the specified index.
     *
     * @param index point of a bezier node
     * @return outgoing tangent point
     */
    public @Nullable Point2D getOutgoingTangentPoint(int index) {
        BezierNode node = nodes.get(index);
        if (node.isC2()) {
            return new Point2D.Double(node.getX2(), node.getY2());
        }
        return null;
    }

    public void setWindingRule(int newValue) {
        this.windingRule = newValue;
    }

    /**
     * Reverses the direction of the path.
     */
    public void reverse() {
        int size = nodes.size();
        ArrayList<BezierNode> temp = new ArrayList<>(size);
        int lastMoveTo = -1;
        for (int i = 0; i < size; i++) {
            var n = nodes.get(i);
            BezierNode reversed = n;
            if (reversed.isMoveTo()) {
                lastMoveTo = i;
                if (i < size && !nodes.get(i + 1).isMoveTo()) {
                    // keep a move to, if it is followed by another move to
                    reversed = reversed.clearMaskBits(BezierNode.MOVE_MASK);
                }
            } else if (reversed.isClosePath()) {
                temp.set(lastMoveTo, temp.get(lastMoveTo).setMaskBits(BezierNode.CLOSE_MASK));
                reversed = reversed.clearMaskBits(BezierNode.CLOSE_MASK);
            }
            reversed = reversed.setX1(n.getX2()).setY1(n.getY2())
                    .setX2(n.getX1()).setY2(n.getY1());
            reversed = reversed.clearMaskBits(BezierNode.C1C2_MASK);
            if (n.isC1()) {
                reversed = reversed.setMaskBits(BezierNode.C2_MASK);
            }
            if (n.isC2()) {
                reversed = reversed.setMaskBits(BezierNode.C1_MASK);
            }
            if (i == size - 1) {
                reversed = reversed.setMaskBits(BezierNode.MOVE_MASK);
            }
            temp.add(reversed);
        }
        nodes.clear();
        nodes.addAll(temp.reversed());
    }
}
