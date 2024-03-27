/*
 * @(#)IntersectionPointEx.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import java.awt.geom.Point2D;
import java.io.Serial;

public class IntersectionPointEx extends IntersectionPoint {
    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * @see #getArgumentB()
     */
    private final double argumentB;


    /**
     * @see #getDerivativeA()
     */
    private final Point2D.Double derivativeA;

    /**
     * @see #getDerivativeB()
     */
    private final Point2D.Double derivativeB;

    /**
     * @see #getSegmentB()
     */
    private final int segmentB;

    public IntersectionPointEx(Point2D.Double point, double argumentA, Point2D.Double derivativeA, double argumentB, Point2D.Double derivativeB) {
        this(point, argumentA, derivativeA, 0, argumentB, derivativeB, 0);
    }

    public IntersectionPointEx(double px, double py,
                               double argumentA, double derivativeAX, double derivativeAY,
                               double argumentB, double derivativeBX, double derivativeBY) {
        this(px, py, argumentA, derivativeAX, derivativeAY, 0, argumentB, derivativeBX, derivativeBY, 0);
    }

    public IntersectionPointEx(Point2D.Double point,
                               double argumentA, Point2D.Double derivativeA, int segmentA,
                               double argumentB, Point2D.Double derivativeB, int segmentB) {
        super(point.getX(), point.getY(), argumentA, segmentA);
        this.derivativeA = derivativeA;
        this.argumentB = argumentB;
        this.derivativeB = derivativeB;
        this.segmentB = segmentB;
    }

    public IntersectionPointEx(double px, double py, double argumentA, double tx1, double ty1, int segmentA, double t2, double tx2, double ty2, int segmentB) {
        super(px, py, argumentA, segmentA);
        this.derivativeA = new Point2D.Double(tx1, ty1);
        this.argumentB = t2;
        this.derivativeB = new Point2D.Double(tx2, ty2);
        this.segmentB = segmentB;
    }


    /**
     * If parametric function 'b' is a segment of a segmented function,
     * then this field is used to indicate to which segment the parametric
     * function belongs.
     * <p>
     * The index of the segment.
     */
    public int getSegmentB() {
        return segmentB;
    }


    /**
     * The derivative vector at the intersection of the parametric function 'a'.
     * This vector is not normalized.
     */
    public Point2D.Double getDerivativeA() {
        return derivativeA;
    }

    /**
     * The value of the argument of the parametric function 'b' at the intersection.
     */
    public double getArgumentB() {
        return argumentB;
    }

    /**
     * The derivative vector at the intersection of the second parametric function.
     * This vector is not normalized.
     */
    public Point2D.Double getDerivativeB() {
        return derivativeB;
    }

    public IntersectionPointEx withSegmentA(int segmentIndex) {
        return new IntersectionPointEx(this, this.argumentA, this.derivativeA, segmentIndex, this.argumentB, this.derivativeB, this.segmentB);
    }

    public IntersectionPointEx withSegmentB(int segmentIndex) {
        return new IntersectionPointEx(this, this.argumentA, this.derivativeA, this.segmentA, this.argumentB, this.derivativeB, segmentIndex);
    }

    @Override
    public String toString() {
        return "IntersectionPoint{" +
                "t1=" + argumentA +
                ", t2=" + argumentB +
                ", point=" + getX() + ", " + getY() +
                ", tangent1=" + derivativeA +
                ", tangent2=" + derivativeB +
                ", segment1=" + segmentA +
                ", segment2=" + segmentB +
                '}';
    }

}
