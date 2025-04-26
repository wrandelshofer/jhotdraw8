/*
 * @(#)FXTransforms.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jhotdraw8.icollection.readable.ReadableSequencedCollection;
import org.jspecify.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.SequencedCollection;

import static java.lang.Double.isNaN;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

/**
 * FXTransforms.
 */
public class FXTransforms {
    /**
     * 'Immutable' identity transform.
     * <p>
     * JavaFX Transforms are not actually immutable.
     * Do not change the value of this transform!
     */
    public static final Transform IDENTITY = new Translate();

    /**
     * Don't let anyone instantiate this class.
     */
    private FXTransforms() {
    }

    /**
     * Returns the concatenation of the provided transforms.
     */
    public static Transform concat(@Nullable Transform... transforms) {
        Transform a = null;
        for (Transform b : transforms) {
            a = (a == null || a.isIdentity()) ? b : (b == null || b.isIdentity() ? a : a.createConcatenation(b));
        }
        return a == null ? IDENTITY : a;
    }

    public static Transform createReshapeTransform(Bounds src, Bounds dest) {
        return createReshapeTransform(
                src.getMinX(), src.getMinY(), src.getWidth(), src.getHeight(),
                dest.getMinX(), dest.getMinY(), dest.getWidth(), dest.getHeight()
        );
    }

    public static Transform createReshapeTransform(Bounds src, double destX, double destY, double destW, double destH) {
        return createReshapeTransform(
                src.getMinX(), src.getMinY(), src.getWidth(), src.getHeight(),
                destX, destY, destW, destH
        );
    }

    public static Transform createReshapeTransform(Rectangle2D src, double destX, double destY, double destW, double destH) {
        return createReshapeTransform(
                src.getMinX(), src.getMinY(), src.getWidth(), src.getHeight(),
                destX, destY, destW, destH
        );
    }


    public static Transform createReshapeTransform(double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh) {
        double scaleX = dw / sw;
        double scaleY = dh / sh;

        Transform t = new Translate(dx - sx, dy - sy);
        if (!Double.isNaN(scaleX) && !Double.isNaN(scaleY)
                && !Double.isInfinite(scaleX) && !Double.isInfinite(scaleY)
                && (scaleX != 1d || scaleY != 1d)) {
            t = FXTransforms.concat(t, new Scale(scaleX, scaleY, sx, sy));
        }
        return t;
    }

    /**
     * Decomposes the given transformation matrix into rotation, followed by
     * scale and then translation. Returns the matrix if the decomposition
     * fails. Returns an empty list if the transform is the identity matrix.
     *
     * @param transform a transformation
     * @return decomposed transformation
     */
    public static List<Transform> decompose(Transform transform) {
        List<Transform> list = new ArrayList<>();
        if (transform.isIdentity()) {
        } else if (transform instanceof Translate) {
            list.add(transform);
        } else if (transform instanceof Scale) {
            list.add(transform);
        } else if (transform instanceof Rotate) {
            list.add(transform);
        } else {

            // xx the X coordinate scaling element of the 3x4 matrix
            // yx the Y coordinate shearing element of the 3x4 matrix
            // xy the X coordinate shearing element of the 3x4 matrix
            // yy the Y coordinate scaling element of the 3x4 matrix
            // tx the X coordinate translation element of the 3x4 matrix
            // ty the Y coordinate translation element of the 3x4 matrix
            //      [ xx xy tx ]    [ a b tx ]
            //      [ yx yy ty ] =  [ c d ty ]
            //      [  0  0  1 ]    [ 0 0  1 ]
            double a = transform.getMxx();
            double b = transform.getMxy();
            double c = transform.getMyx();
            double d = transform.getMyy();
            double tx = transform.getTx();
            double ty = transform.getTy();

            double sx = sqrt(a * a + c * c);
            double sy = sqrt(b * b + d * d);

            double rot1 = atan(c / d);
            double rot2 = atan(-b / a);

            if (isNaN(rot1) || isNaN(rot2) || abs(rot1 - rot2) > 1e-6) {
                list.add(transform);
                return list;
            }

            if (tx != 0.0 || ty != 0.0) {
                list.add(new Translate(tx, ty));
            }
            if (sx != 1.0 || sy != 1.0) {
                list.add(new Scale(tx, ty));
            }
            if (rot1 != 0.0 && rot2 != 0.0) {
                list.add(new FXPreciseRotate(Math.toDegrees(rot1)));
            }
        }

        return list;
    }

    public static Point2D deltaTransform(@Nullable Transform t, double x, double y) {
        if (t == null) {
            return new Point2D(x, y);
        } else {
            Point3D tp = t.deltaTransform(x, y, 0);
            return new Point2D(tp.getX(), tp.getY());
        }
    }

    public static Point2D deltaTransform(@Nullable Transform t, Point2D p) {
        if (t == null) {
            return p;
        } else {
            Point3D tp = t.deltaTransform(p.getX(), p.getY(), 0);
            return new Point2D(tp.getX(), tp.getY());
        }
    }

    public static Point2D inverseDeltaTransform(@Nullable Transform t, double x, double y) {
        if (t == null) {
            return new Point2D(x, y);
        } else {
            try {
                Point3D tp = t.inverseDeltaTransform(x, y, 0);
                return new Point2D(tp.getX(), tp.getY());
            } catch (NonInvertibleTransformException e) {
                return new Point2D(x, y);
            }
        }
    }

    public static Point2D inverseDeltaTransform(@Nullable Transform t, Point2D p) {
        if (t == null) {
            return p;
        } else {
            try {
                Point3D tp = t.inverseDeltaTransform(p.getX(), p.getY(), 0);
                return new Point2D(tp.getX(), tp.getY());
            } catch (NonInvertibleTransformException e) {
                return p;
            }
        }
    }

    public static @Nullable AffineTransform toAwt(@Nullable Transform t) {
        if (t == null) {
            return null;
        }
        return new AffineTransform(t.getMxx(), t.getMyx(), t.getMxy(), t.getMyy(), t.getTx(), t.getTy());
    }

    public static Bounds transform(@Nullable Transform tx, Bounds b) {
        return tx == null ? b : tx.transform(b);
    }

    public static Point2D transform(@Nullable Transform tx, Point2D b) {
        if (tx == null || tx.isIdentity()) {
            return b;
        }
        if (tx.isType2D()) {
            return tx.transform(b);
        }
        Point3D p = tx.transform(b.getX(), b.getY(), 0);
        return new Point2D(p.getX(), p.getY());
    }

    public static Point2D inverseTransform(@Nullable Transform tx, Point2D b) {
        try {
            if (tx == null || tx.isIdentity()) {
                return b;
            }
            if (tx.isType2D()) {
                return tx.inverseTransform(b);
            }
            Point3D p = null;
            p = tx.inverseTransform(b.getX(), b.getY(), 0);
            return new Point2D(p.getX(), p.getY());
        } catch (NonInvertibleTransformException e) {
            return b;
        }
    }

    public static Point2D transform(Iterable<Transform> txs, Point2D b) {
        for (Transform tx : txs) {
            b = transform(tx, b);
        }
        return b;
    }

    public static Point2D deltaTransform(Iterable<Transform> txs, Point2D b) {
        for (Transform tx : txs) {
            b = deltaTransform(tx, b);
        }
        return b;
    }

    public static Point2D inverseTransform(SequencedCollection<Transform> txs, Point2D b) {
        for (Transform tx : txs.reversed()) {
            b = inverseTransform(tx, b);
        }
        return b;
    }

    public static Point2D inverseTransform(ReadableSequencedCollection<Transform> txs, Point2D b) {
        for (Transform tx : txs.readOnlyReversed()) {
            b = inverseTransform(tx, b);
        }
        return b;
    }

    public static Point2D inverseDeltaTransform(ReadableSequencedCollection<Transform> txs, Point2D b) {
        for (Transform tx : txs.readOnlyReversed()) {
            b = inverseDeltaTransform(tx, b);
        }
        return b;
    }

    public static Point2D transform(@Nullable Transform tx, double x, double y) {
        if (tx == null || tx.isIdentity()) {
            return new Point2D(x, y);
        }
        if (tx.isType2D()) {
            return tx.transform(x, y);
        }
        Point3D p = tx.transform(x, y, 0);
        return new Point2D(p.getX(), p.getY());
    }

    /**
     * Rotates from tangent vector.
     * <p>
     * A tangent vector pointing to (1,0) results in an identity matrix.
     *
     * @param tangent a tangent vector
     * @param pivot   the pivot of the rotation
     * @return a rotation transform
     */
    public static Transform rotate(Point2D tangent, Point2D pivot) {
        double theta = Angles.atan2(tangent.getY(), tangent.getX());
        return rotateRadians(theta, pivot.getX(), pivot.getY());
    }

    /**
     * Creates a transform from an angle given in radians and the pivot point
     * of the rotation.
     *
     * @param theta  the angle of the rotation in radians
     * @param pivotX the X coordinate of the rotation pivot point
     * @param pivotY the Y coordinate of the rotation pivot point
     * @return a rotation matrix
     */
    private static Transform rotateRadians(double theta, double pivotX, double pivotY) {
        return new FXPreciseRotate(Math.toDegrees(theta), pivotX, pivotY);
    }

    /**
     * Rotates from tangent vector.
     * <p>
     * A tangent vector pointing to (1,0) results in an identity matrix.
     *
     * @param tangentX a tangent vector
     * @param tangentY a tangent vector
     * @param pivotX   the pivot of the rotation
     * @param pivotY   the pivot of the rotation
     * @return a rotation transform
     */
    public static Transform rotate(double tangentX, double tangentY, double pivotX, double pivotY) {
        double theta = Angles.atan2(tangentY, tangentX);
        return rotateRadians(theta, pivotX, pivotY);
    }

    /**
     * Creates a transformation matrix, which projects a point onto the given line.
     * The projection is orthogonal to the line.
     * The point will not be clipped off by the line.
     * <p>
     * Formula: b = project(a, p1,p2)
     * <pre>
     *  v = p2 - p1;
     *  b = vvT / vTv * (a - p1) + p1;
     *  b = [ vvT / vTv | vvT / vTv * p1 ] * a; // 2 by 3 matrix
     * </pre>
     *
     * @param x1 x-coordinate of p1 of the line
     * @param y1 y-coordinate of p1 of the line
     * @param x2 x-coordinate of p2 of the line
     * @param y2 y-coordinate of p2 of the line
     * @return the transformation matrix
     */
    public static Transform createProjectPointOnLineTransform(double x1, double y1, double x2, double y2) {
        double vx = x2 - x1;
        double vy = y2 - y1;
        double vxx = vx * vx;
        double vyy = vy * vy;
        double vTv = vxx + vyy;

        double xx = vxx / vTv;
        double xy = vx * vy / vTv;
        double yy = vyy / vTv;
        double yx = xy;
        double tx = xx * x1 + xy * y1;
        double ty = yx * x1 + yy * y1;

        return new Affine(xx, xy, tx, yx, yy, ty);
    }

    public static Point2D projectPointOnLine(double ax, double ay, double x1, double y1, double x2, double y2) {
        double vx = x2 - x1;
        double vy = y2 - y1;
        double vxx = vx * vx;
        double vyy = vy * vy;
        double vTv = vxx + vyy;

        double xx = vxx / vTv;
        double xy = vx * vy / vTv;
        double yy = vyy / vTv;
        double yx = xy;
        double tx = xx * x1 + xy * y1;
        double ty = yx * x1 + yy * y1;


        double bx = xx * ax + xy * ay + tx;
        double by = yx * ax + yy * ay + ty;
        return new Point2D(bx, by);
    }


    public static boolean isIdentityOrNull(@Nullable Transform t) {
        return t == null || t.isIdentity();
    }

    public static void transform2DPoints(Transform t,
                                         double[] srcPts, int srcOff,
                                         double[] dstPts, int dstOff,
                                         int numPts) {
        if (t.isType2D()) {
            t.transform2DPoints(srcPts, srcOff, dstPts, dstOff, numPts);
        } else {

            double[] points3d = new double[numPts * 3];
            for (int i = 0, i3 = 0; i < numPts; i++, i3 += 3) {
                points3d[i3] = srcPts[i * 2 + srcOff];
                points3d[i3 + 1] = srcPts[i * 2 + 1 + srcOff];
            }
            t.transform3DPoints(points3d, 0, points3d, 0, 4);
            for (int i = 0, i3 = 0; i < numPts; i++, i3 += 3) {
                dstPts[i * 2 + dstOff] = points3d[i3];
                dstPts[i * 2 + 1 + dstOff] = points3d[i3 + 1];
            }
        }

    }

    /**
     * Computes the bounding box in parent coordinates
     *
     * @param b a box in local coordinates
     * @return bounding box in parent coordinates
     */
    public static Bounds transformedBoundingBox(@Nullable Transform t, Bounds b) {
        if (t == null) {
            return b;
        }

        double[] points = new double[8];
        points[0] = b.getMinX();
        points[1] = b.getMinY();
        points[2] = b.getMaxX();
        points[3] = b.getMinY();
        points[4] = b.getMaxX();
        points[5] = b.getMaxY();
        points[6] = b.getMinX();
        points[7] = b.getMaxY();

        t.transform2DPoints(points, 0, points, 0, 4);

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < points.length; i += 2) {
            minX = min(minX, points[i]);
            maxX = max(maxX, points[i]);
            minY = min(minY, points[i + 1]);
            maxY = max(maxY, points[i + 1]);
        }
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

}
