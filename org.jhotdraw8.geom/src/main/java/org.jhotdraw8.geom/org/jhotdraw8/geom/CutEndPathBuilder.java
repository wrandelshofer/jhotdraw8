/*
 * @(#)CutEndPathBuilder.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.geom.intersect.*;

import java.awt.geom.PathIterator;

/**
 * CutEndPathBuilder.
 *
 * @author Werner Randelshofer
 */
public class CutEndPathBuilder<T> extends AbstractPathBuilder<T> {

    private final @NonNull PathBuilder<T> out;
    private final double radius;
    private double cx;
    private double cy;
    private final @NonNull PathIteratorPathBuilder path;

    public CutEndPathBuilder(@NonNull PathBuilder<T> out, double radius) {
        this.out = out;
        this.radius = radius;
        path = new PathIteratorPathBuilder();
    }

    @Override
    protected void doClosePath() {
        path.closePath();
    }

    @Override
    protected void doCurveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        path.curveTo(x1, y1, x2, y2, x3, y3);
    }

    @Override
    protected void doPathDone() {
        java.awt.geom.Point2D.Double currentPoint = getLastPoint();
        cx = currentPoint.getX();
        cy = currentPoint.getY();
        double[] seg = new double[6];
        double x = 0, y = 0;
        for (PathIterator i = path.build(); !i.isDone(); i.next()) {
            switch (i.currentSegment(seg)) {
            case PathIterator.SEG_CLOSE:
                out.closePath();
                break;
            case PathIterator.SEG_CUBICTO: {
                IntersectionResult isect = IntersectCircleCubicCurve.intersectCubicCurveCircle(x, y, seg[0], seg[1], seg[2], seg[3], seg[4], seg[5], cx, cy, radius);
                if (isect.getStatus() != IntersectionStatus.NO_INTERSECTION_INSIDE) {
                    if (isect.isEmpty()) {
                        out.curveTo(seg[0], seg[1], seg[2], seg[3], seg[4], seg[5]);
                    } else {
                        CubicCurves.splitCubicCurveTo(x, y, seg[0], seg[1], seg[2], seg[3], seg[4], seg[5], isect.getLast().getArgumentA(),
                                out::curveTo, null);
                    }
                }
                x = seg[4];
                y = seg[5];
                break;
            }
            case PathIterator.SEG_LINETO: {
                IntersectionResult isect = IntersectCircleLine.intersectLineCircle(x, y, seg[0], seg[1], cx, cy, radius);
                if (isect.getStatus() != IntersectionStatus.NO_INTERSECTION_INSIDE) {
                    if (isect.isEmpty()) {
                        out.lineTo(seg[0], seg[1]);
                    } else {
                        Lines.split(x, y, seg[0], seg[1], isect.getLast().getArgumentA(),
                                out::lineTo, null);
                    }
                }
                x = seg[0];
                y = seg[1];
                break;
            }
            case PathIterator.SEG_MOVETO: {
                out.moveTo(seg[0], seg[1]);
                x = seg[0];
                y = seg[1];
                break;
            }
            case PathIterator.SEG_QUADTO: {
                IntersectionResult isect = IntersectCircleQuadCurve.intersectQuadCurveCircle(x, y, seg[0], seg[1], seg[2], seg[3], cx, cy, radius);
                if (isect.getStatus() != IntersectionStatus.NO_INTERSECTION_INSIDE) {
                    if (isect.isEmpty()) {
                        out.quadTo(seg[0], seg[1], seg[2], seg[3]);
                    } else {
                        QuadCurves.split(x, y, seg[0], seg[1], seg[2], seg[3], isect.getLast().getArgumentA(),
                                out::quadTo, null);
                    }
                }
                x = seg[2];
                y = seg[3];
                break;
            }
            default:
                throw new IllegalArgumentException("illegal path command:" + i.currentSegment(seg));
            }
        }
        out.pathDone();
    }

    @Override
    protected void doLineTo(double x, double y) {
        path.lineTo(x, y);
    }

    @Override
    protected void doMoveTo(double x, double y) {
        path.moveTo(x, y);
    }

    @Override
    protected void doQuadTo(double x1, double y1, double x2, double y2) {
        path.quadTo(x1, y1, x2, y2);
    }

    @Override
    public @Nullable T build() {
        return out.build();
    }
}
