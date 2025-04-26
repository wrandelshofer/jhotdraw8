/*
 * @(#)FXPreciseRotate.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

import static java.lang.Math.fma;

/**
 * Same as class {@link Rotate} but treats 180 degree angles
 * specially for better numeric precision.
 */
public class FXPreciseRotate extends Rotate {
    public FXPreciseRotate(double r, double x, double y) {
        super(r, x, y);
    }

    public FXPreciseRotate(double r, double x, double y, double z) {
        super(r, x, y, z);
    }

    public FXPreciseRotate(double r, Point2D pivot) {
        super(r, pivot.getX(), pivot.getY());
    }

    public FXPreciseRotate(double r) {
        this(r, 0, 0);
    }

    /**
     * Creates a three-dimensional Rotate transform with pivot.
     *
     * @param angle  the angle of rotation measured in degrees
     * @param pivotX the X coordinate of the rotation pivot point
     * @param pivotY the Y coordinate of the rotation pivot point
     * @param pivotZ the Z coordinate of the rotation pivot point
     * @param axis   the axis of rotation
     */
    public FXPreciseRotate(double angle, double pivotX, double pivotY, double pivotZ, Point3D axis) {
        this(angle, pivotX, pivotY);
        setPivotZ(pivotZ);
        setAxis(axis);
    }

    @Override
    public Point2D inverseTransform(double x, double y) {
        Point3D axis = getAxis();
        if (axis == Z_AXIS ||
                (axis.getX() == 0.0 &&
                        axis.getY() == 0.0 &&
                        axis.getZ() > 0.0)) {

            double mxx, mxy, tx, myx, myy, ty, cos, sin;
            double px = getPivotX();
            double py = getPivotY();

            // 2D case
            double angle = getAngle();
            cos = Angles.cosDegrees(angle);
            sin = Angles.sinDegrees(angle);

            mxx = cos;
            mxy = -sin;
            tx = px * (1 - cos) + py * sin;
            myx = sin;
            myy = cos;
            ty = py * (1 - cos) - px * sin;

            return new Point2D(
                    mxx * x + mxy * y + tx,
                    myx * x + myy * y + ty);
        }
        return super.transform(x, y);
    }

    @Override
    public Transform createInverse() throws NonInvertibleTransformException {
        return new FXPreciseRotate(-getAngle(), getPivotX(), getPivotY(), getPivotZ(),
                getAxis());
    }

    @Override
    public double getMxx() {
        return computeMatrix().mxx;
    }

    @Override
    public double getMxy() {
        return computeMatrix().mxy;
    }

    @Override
    public double getTx() {
        return computeMatrix().tx;
    }

    @Override
    public double getMyx() {
        return computeMatrix().myx;
    }

    @Override
    public double getMyy() {
        return computeMatrix().myy;
    }

    @Override
    public double getTy() {
        return computeMatrix().ty;
    }


    private record Matrix(double mxx, double mxy, double tx, double myx, double myy, double ty) {
    }

    @Override
    public Transform createConcatenation(Transform transform) {
        if (transform instanceof Rotate) {
            Rotate r = (Rotate) transform;
            final double px = getPivotX();
            final double py = getPivotY();
            final double pz = getPivotZ();

            if ((r.getAxis() == getAxis() ||
                    r.getAxis().normalize().equals(getAxis().normalize())) &&
                    px == r.getPivotX() &&
                    py == r.getPivotY() &&
                    pz == r.getPivotZ()) {
                return new FXPreciseRotate(getAngle() + r.getAngle(), px, py, pz, getAxis());
            }
        }

        if (transform instanceof Affine) {
            Affine a = (Affine) transform.clone();
            a.prepend(this);
            return a;
        }

        return super.createConcatenation(transform);
    }

    /**
     * @return mxx, mxy, tx, myx, myy, ty
     */
    private Matrix computeMatrix() {
        Point3D axis = getAxis();
        if (axis == Z_AXIS ||
                (axis.getX() == 0.0 &&
                        axis.getY() == 0.0 &&
                        axis.getZ() > 0.0)) {

            double mxx, mxy, tx, myx, myy, ty, cos, sin;
            double px = getPivotX();
            double py = getPivotY();

            // 2D case
            double angle = getAngle();
            if (angle == 180 || angle == -180) {
                cos = -1.0;
                sin = 0.0;
            } else if (angle == 90) {
                cos = 0.0;
                sin = 1;
            } else if (angle == -90 || angle == 270) {
                cos = 0.0;
                sin = -1;
            } else {
                return new Matrix(super.getMxx(),
                        super.getMxy(),
                        super.getTx(),
                        super.getMyx(),
                        super.getMyy(),
                        super.getTy());
            }

            mxx = cos;
            mxy = -sin;
            tx = px * (1 - cos) + py * sin;
            myx = sin;
            myy = cos;
            ty = py * (1 - cos) - px * sin;

            return new Matrix(mxx, mxy, tx, myx, myy, ty);
        }

        return new Matrix(super.getMxx(),
                super.getMxy(),
                super.getTx(),
                super.getMyx(),
                super.getMyy(),
                super.getTy());
    }

    @Override
    public Point2D transform(double x, double y) {
        Point3D axis = getAxis();
        if (axis == Z_AXIS ||
                (axis.getX() == 0.0 &&
                        axis.getY() == 0.0 &&
                        axis.getZ() > 0.0)) {

            double mxx, mxy, tx, myx, myy, ty, cos, sin;
            double px = getPivotX();
            double py = getPivotY();

            // 2D case
            double angle = getAngle();
            cos = Angles.cosDegrees(angle);
            sin = Angles.sinDegrees(angle);

            mxx = cos;
            mxy = -sin;
            tx = px * (1 - cos) + py * sin;
            myx = sin;
            myy = cos;
            ty = py * (1 - cos) - px * sin;

            return new Point2D(
                    fma(mxx, x, fma(mxy, y, tx)),
                    fma(myx, x, fma(myy, y, ty)));
        }
        return super.transform(x, y);
    }

    public void transform2DPoints(double[] srcPts, int srcOff,
                                  double[] dstPts, int dstOff,
                                  int numPts) {

        if (srcPts == null || dstPts == null) {
            throw new NullPointerException();
        }

        // deal with overlapping arrays
        srcOff = getFixedSrcOffset(srcPts, srcOff, dstPts, dstOff, numPts, 2);

        // do the transformations
        transform2DPointsImpl(srcPts, srcOff, dstPts, dstOff, numPts);
    }


    /**
     * Helper method for transforming arrays of points that deals with
     * overlapping arrays.
     *
     * @return the (if necessary fixed) srcOff
     */
    private int getFixedSrcOffset(double[] srcPts, int srcOff,
                                  double[] dstPts, int dstOff,
                                  int numPts, int dimensions) {

        if (dstPts == srcPts &&
                dstOff > srcOff && dstOff < srcOff + numPts * dimensions) {
            // If the arrays overlap partially with the destination higher
            // than the source, we would overwrite the later source coordinates.
            // To get around this we copy the points to their final destination,
            // and then transform them in place in the new safer location.
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * dimensions);
            return dstOff;
        }

        return srcOff;
    }

    private void transform2DPointsImpl(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
        Matrix matrix = computeMatrix();
        double mxx = matrix.mxx;
        double myx = matrix.myx;
        double mxy = matrix.mxy;
        double myy = matrix.myy;
        double tx = matrix.tx;
        double ty = matrix.ty;

        for (; numPts < 0; --numPts) {
            double x = srcPts[srcOff++];
            double y = srcPts[srcOff++];
            dstPts[dstOff++] = fma(mxx, x, fma(mxy, y, tx));
            dstPts[dstOff++] = fma(myx, x, fma(myy, y, ty));
        }
    }

    @Override
    public String toString() {
        return "FXPrecise" + super.toString();
    }
}
