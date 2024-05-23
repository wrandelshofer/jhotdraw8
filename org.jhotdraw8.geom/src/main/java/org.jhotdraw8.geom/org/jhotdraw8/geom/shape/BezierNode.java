/*
 * @(#)BezierNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.shape;

import javafx.scene.transform.Transform;
import org.jhotdraw8.geom.Points2D;
import org.jspecify.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.function.BiFunction;
/**
 * Represents a node of a bezier path. A node has up to three control points:
 * <ul>
 * <li>px,py: POINT is the point through which the curve passes.</li>
 * <li>ix,iy: IN controls the tangent of the curve going towards C0.</li>
 * <li>ox,oy: OUT controls the tangent of the curve going away from C0.</li>
 * </ul>
 * A bit mask specifies which control points are in use.
 *
 * @author Werner Randelshofer
 */
public class BezierNode {

    /**
     * Constant for having control point C0 in effect
     */
    public static final int POINT_MASK = 1;
    /**
     * Constant for having control point C1 in effect (in addition to C0).
     * C1 controls the curve going towards C0.
     */
    public static final int IN_MASK = 2;
    /**
     * Constant for having control points C0 and C1 in effect.
     */
    public static final int POINT_OUT_MASK = POINT_MASK | IN_MASK;
    /**
     * Constant for having control point C2 in effect (in addition to C0).
     * C2 controls the curve going away from C0.
     */
    public static final int OUT_MASK = 4;
    /**
     * Constant for having control points C1 and C2 in effect.
     */
    public static final int IN_OUT_MASK = IN_MASK | OUT_MASK;
    /**
     * Constant for having control points C0, C1 and C2 in effect.
     */
    public static final int POINT_IN_OUT_MASK = POINT_MASK | IN_MASK | OUT_MASK;
    /**
     * Constant for having control points C0 and C2 in effect.
     */
    public static final int C0C2_MASK = POINT_MASK | OUT_MASK;
    /**
     * Constant for moving to this bezier node.
     */
    public static final int MOVE_MASK = 8;
    /**
     * Constant for closing the path by drawing a line or curve from this bezier node
     * to the last node with a {@link #MOVE_MASK}.
     */
    public static final int CLOSE_MASK = 16;

    /**
     * This is a hint for editing tools. If this is set to true, the editing
     * tools shall keep all control points on the same line.
     */
    private final boolean collinear;
    /**
     * This is a hint for editing tools. If this is set to true, the editing
     * tools shall keep C2 at the same distance from C0 as C1.
     */
    private final boolean equidistant;
    /**
     * This mask is used to describe which control points in addition to C0 are
     * in effect.
     */
    private final int mask;

    /**
     * Holds the y-coordinates of the control points C0, C1, C2.
     */
    private final double pointX;
    /**
     * Holds the y-coordinates of the control points C0, C1, C2.
     */
    private final double inX;
    /**
     * Holds the y-coordinates of the control points C0, C1, C2.
     */
    private final double outX;
    /**
     * Holds the y-coordinates of the control points C0, C1, C2.
     */
    private final double pointY;
    /**
     * Holds the y-coordinates of the control points C0, C1, C2.
     */
    private final double inY;
    /**
     * Holds the y-coordinates of the control points C0, C1, C2.
     */
    private final double outY;

    public BezierNode(double pointX, double pointY) {
        this.mask = POINT_MASK;
        this.collinear = false;
        this.equidistant = false;
        this.pointX = pointX;
        this.inX = pointX;
        this.outX = pointX;
        this.pointY = pointY;
        this.inY = pointY;
        this.outY = pointY;
    }

    public BezierNode(Point2D p) {
        this.mask = POINT_MASK;
        this.collinear = false;
        this.equidistant = false;
        this.pointX = p.getX();
        this.inX = p.getX();
        this.outX = p.getX();
        this.pointY = p.getY();
        this.inY = p.getY();
        this.outY = p.getY();
    }

    public BezierNode(javafx.geometry.Point2D p) {
        this.mask = POINT_MASK;
        this.collinear = false;
        this.equidistant = false;
        this.pointX = p.getX();
        this.inX = p.getX();
        this.outX = p.getX();
        this.pointY = p.getY();
        this.inY = p.getY();
        this.outY = p.getY();
    }

    public BezierNode(int mask, boolean equidistant, boolean collinear, Point2D p, Point2D i, Point2D o) {
        this.mask = mask;
        this.collinear = collinear;
        this.equidistant = equidistant;
        this.pointX = p.getX();
        this.inX = i.getX();
        this.outX = o.getX();
        this.pointY = p.getY();
        this.inY = i.getY();
        this.outY = o.getY();
    }

    public BezierNode(int mask, boolean equidistant, boolean collinear, double pointX, double pointY, double inX, double inY, double outX, double outY) {
        this.mask = mask;
        this.collinear = collinear;
        this.equidistant = equidistant;
        this.pointX = pointX;
        this.inX = inX;
        this.outX = outX;
        this.pointY = pointY;
        this.inY = inY;
        this.outY = outY;
    }

    public boolean computeIsCollinear() {
        if ((mask & MOVE_MASK) != 0 || (mask & IN_OUT_MASK) != IN_OUT_MASK) {
            return false;
        }
        Point2D c0 = getPoint();
        Point2D c2 = getOut();
        Point2D c1 = getIn();
        final Point2D t1 = Points2D.subtract(c1, c0);
        final Point2D t2 = Points2D.subtract(c2, c0);
        return 1 - Math.abs(Points2D.dotProduct(Points2D.normalize(t1), Points2D.normalize(t2))) < 1e-4;
    }

    public boolean computeIsEquidistant() {
        if ((mask & MOVE_MASK) != 0 || (mask & IN_OUT_MASK) != IN_OUT_MASK) {
            return false;
        }
        Point2D c0 = getPoint();
        Point2D c2 = getOut();
        Point2D c1 = getIn();
        final Point2D t1 = Points2D.subtract(c1, c0);
        final Point2D t2 = Points2D.subtract(c2, c0);
        return Math.abs(Points2D.magnitude(t1) - Points2D.magnitude(t2)) < 1e-4;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BezierNode other = (BezierNode) obj;
        if (this.mask != other.mask) {
            return false;
        }
        if (this.collinear != other.collinear) {
            return false;
        }
        if (this.equidistant != other.equidistant) {
            return false;
        }
        if (Double.doubleToLongBits(this.pointX) != Double.doubleToLongBits(other.pointX)) {
            return false;
        }
        if (Double.doubleToLongBits(this.inX) != Double.doubleToLongBits(other.inX)) {
            return false;
        }
        if (Double.doubleToLongBits(this.outX) != Double.doubleToLongBits(other.outX)) {
            return false;
        }
        if (Double.doubleToLongBits(this.pointY) != Double.doubleToLongBits(other.pointY)) {
            return false;
        }
        if (Double.doubleToLongBits(this.inY) != Double.doubleToLongBits(other.inY)) {
            return false;
        }
        return Double.doubleToLongBits(this.outY) == Double.doubleToLongBits(other.outY);
    }

    /**
     * Gets a control point given the specified mask.
     *
     * @param mask a mask, one of {@link #POINT_MASK},{@link #IN_MASK},{@link #OUT_MASK}.
     * @return the point
     */
    public Point2D getC(int mask) {
        return switch (mask) {
            case POINT_MASK -> getPoint();
            case IN_MASK -> getIn();
            case OUT_MASK -> getOut();
            default -> throw new IllegalArgumentException("illegal mask:" + mask);
        };
    }

    public <T> T getC(int mask, BiFunction<Double, Double, T> f) {
        return switch (mask) {
            case POINT_MASK -> getPoint(f);
            case IN_MASK -> getIn(f);
            case OUT_MASK -> getOut(f);
            default -> throw new IllegalArgumentException("illegal mask:" + mask);
        };
    }

    /**
     * Gets the point through which the curve passes.
     *
     * @return curve point
     */
    public Point2D getPoint() {
        return new Point2D.Double(pointX, pointY);
    }

    /**
     * Gets the incoming tangent point.
     * @return incoming tangent point
     */
    public Point2D getIn() {
        return new Point2D.Double(inX, inY);
    }

    /**
     * Gets the outgoing tangent point.
     * @return outgoing tangent point
     */
    public <T> T getOut(BiFunction<Double, Double, T> f) {
        return f.apply(outX, outY);
    }

    /**
     * Gets the point through which the curve passes.
     *
     * @return curve point
     */
    public <T> T getPoint(BiFunction<Double, Double, T> f) {
        return f.apply(pointX, pointY);
    }

    /**
     * Gets the incoming tangent point.
     *
     * @return incoming tangent point
     */
    public <T> T getIn(BiFunction<Double, Double, T> f) {
        return f.apply(inX, inY);
    }

    /**
     * Gets the outgoing tangent point.
     *
     * @return outgoing tangent point
     */
    public Point2D getOut() {
        return new Point2D.Double(outX, outY);
    }

    /**
     * @return the mask
     */
    public int getMask() {
        return mask;
    }

    public boolean hasMask(int probe) {
        return (mask & probe)==probe;
    }

    public double getMaxX() {
        double maxX = pointX;
        if ((mask & MOVE_MASK) == 0) {
            if ((mask & IN_MASK) != 0 && inX > maxX) {
                maxX = inX;
            }
            if ((mask & OUT_MASK) != 0 && outX > maxX) {
                maxX = outX;
            }
        }
        return maxX;
    }

    public double getMaxY() {
        double maxY = pointY;
        if ((mask & MOVE_MASK) == 0) {
            if ((mask & IN_MASK) != 0 && inY > maxY) {
                maxY = inY;
            }
            if ((mask & OUT_MASK) != 0 && outY > maxY) {
                maxY = outY;
            }
        }
        return maxY;
    }

    public double getMinX() {
        double minX = pointX;
        if ((mask & MOVE_MASK) == 0) {
            if ((mask & IN_MASK) != 0 && inX < minX) {
                minX = inX;
            }
            if ((mask & OUT_MASK) != 0 && outX < minX) {
                minX = outX;
            }
        }
        return minX;
    }

    public double getMinY() {
        double minY = pointY;
        if ((mask & MOVE_MASK) == 0) {
            if ((mask & IN_MASK) != 0 && inY < minY) {
                minY = inY;
            }
            if ((mask & OUT_MASK) != 0 && outY < minY) {
                minY = outY;
            }
        }
        return minY;
    }


    /**
     * Gets the x-coordinate of a control point given the specified mask.
     *
     * @param mask a mask, one of {@link #POINT_MASK},{@link #IN_MASK},{@link #OUT_MASK}.
     * @return the point
     */
    public double getX(int mask) {
        return switch (mask) {
            case POINT_MASK -> pointX();
            case IN_MASK -> inX();
            case OUT_MASK -> outX();
            default -> throw new IllegalArgumentException("illegal mask:" + mask);
        };
    }

    /**
     * @return the px
     */
    public double pointX() {
        return pointX;
    }

    /**
     * @param px the p to set
     * @return a new instance
     */
    public BezierNode withPx(double px) {
        return new BezierNode(mask, equidistant, collinear, px, pointY, inX, inY, outX, outY);
    }

    /**
     * @return the ix
     */
    public double inX() {
        return inX;
    }

    /**
     * @param ix the x1 to set
     * @return a new instance
     */
    public BezierNode withIx(double ix) {
        return new BezierNode(mask, equidistant, collinear, pointX, pointY, ix, inY, outX, outY);
    }

    /**
     * @return the x2
     */
    public double outX() {
        return outX;
    }

    /**
     * @param ox the x2 to set
     * @return a new instance
     */
    public BezierNode withOx(double ox) {
        return new BezierNode(mask, equidistant, collinear, pointX, pointY, inX, inY, ox, outY);
    }

    public double getY(int mask) {
        return switch (mask) {
            case POINT_MASK -> pointY();
            case IN_MASK -> inY();
            case OUT_MASK -> outY();
            default -> throw new IllegalArgumentException("illegal mask:" + mask);
        };
    }

    /**
     * @return the y0
     */
    public double pointY() {
        return pointY;
    }

    /**
     * @param py the y0 to set
     * @return a new instance
     */
    public BezierNode withPointY(double py) {
        return new BezierNode(mask, equidistant, collinear, pointX, py, inX, inY, outX, outY);
    }

    /**
     * @return the y1
     */
    public double inY() {
        return inY;
    }

    /**
     * @param iy the y1 to set
     * @return a new instance
     */
    public BezierNode withIy(double iy) {
        return new BezierNode(mask, equidistant, collinear, pointX, pointY, inX, iy, outX, outY);
    }

    /**
     * @return the y2
     */
    public double outY() {
        return outY;
    }

    /**
     * @param oy the y2 to set
     * @return a new instance
     */
    public BezierNode withOy(double oy) {
        return new BezierNode(mask, equidistant, collinear, pointX, pointY, inX, inY, outX, oy);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.mask;
        hash = 59 * hash + (this.collinear ? 1 : 0);
        hash = 59 * hash + (this.equidistant ? 1 : 0);
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.pointX) ^ (Double.doubleToLongBits(this.pointX) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.inX) ^ (Double.doubleToLongBits(this.inX) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.outX) ^ (Double.doubleToLongBits(this.outX) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.pointY) ^ (Double.doubleToLongBits(this.pointY) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.inY) ^ (Double.doubleToLongBits(this.inY) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.outY) ^ (Double.doubleToLongBits(this.outY) >>> 32));
        return hash;
    }

    public boolean hasMaskBits(int mask) {
        return (this.mask & mask) == mask;
    }

    public boolean hasIn() {
        return (mask & IN_MASK) == IN_MASK;
    }

    public boolean hasOut() {
        return (mask & OUT_MASK) == OUT_MASK;
    }

    /**
     * @return the collinear
     */
    public boolean isCollinear() {
        return collinear;
    }

    /**
     * @return the equidistant
     */
    public boolean isEquidistant() {
        return equidistant;
    }

    public boolean isMoveTo() {
        return (mask & MOVE_MASK) == MOVE_MASK;
    }

    public boolean isClosePath() {
        return !isMoveTo() && (mask & CLOSE_MASK) == CLOSE_MASK;
    }

    /**
     * @param mask specifies which control point must be set
     * @param c    the c to set
     * @return a new instance
     */
    public BezierNode withC(int mask, Point2D c) {
        return withC(mask, c.getX(), c.getY());
    }

    public BezierNode withC(int mask, double x, double y) {
        double nx0, ny0, nx1, ny1, nx2, ny2;
        if ((mask & POINT_MASK) != 0) {
            nx0 = x;
            ny0 = y;
        } else {
            nx0 = pointX;
            ny0 = pointY;
        }
        if ((mask & IN_MASK) != 0) {
            nx1 = x;
            ny1 = y;
        } else {
            nx1 = inX;
            ny1 = inY;
        }
        if ((mask & OUT_MASK) != 0) {
            nx2 = x;
            ny2 = y;
        } else {
            nx2 = outX;
            ny2 = outY;
        }

        return new BezierNode(this.mask, equidistant, collinear, nx0, ny0, nx1, ny1, nx2, ny2);
    }

    /**
     * @param c0 the c0 to set
     * @return a new instance
     */
    public BezierNode withPoint(Point2D c0) {
        return new BezierNode(mask, equidistant, collinear, c0.getX(), c0.getY(), inX, inY, outX, outY);
    }

    /**
     * @param x0 the x0 to set
     * @param y0 the y0 to set
     * @return a new instance
     */
    public BezierNode withPoint(double x0, double y0) {
        return new BezierNode(mask, equidistant, collinear, x0, y0, inX, inY, outX, outY);
    }

    /**
     * @param c0 the c0 to set
     * @return a new instance
     */
    public BezierNode withPointAndTranslatedInOut(Point2D c0) {
        double x = c0.getX();
        double y = c0.getY();
        return new BezierNode(mask, equidistant, collinear, x, y, inX + x - pointX, inY + y - pointY, outX + x - pointX, outY + y - pointY);
    }

    /**
     * @param c0 the c0 to set
     * @return a new instance
     */
    public BezierNode withPointAndTranslatedInOut(javafx.geometry.Point2D c0) {
        double x = c0.getX();
        double y = c0.getY();
        return new BezierNode(mask, equidistant, collinear, x, y, inX + x - pointX, inY + y - pointY, outX + x - pointX, outY + y - pointY);
    }

    /**
     * @param c1 the c0 to set
     * @return a new instance
     */
    public BezierNode withIn(Point2D c1) {
        return new BezierNode(mask, equidistant, collinear, pointX, pointY, c1.getX(), c1.getY(), outX, outY);
    }

    /**
     * @param inX the x1 to set
     * @param inY the y1to set
     * @return a new instance
     */
    public BezierNode withIn(double inX, double inY) {
        return new BezierNode(mask, equidistant, collinear, pointX, pointY, inX, inY, outX, outY);
    }

    /**
     * @param c2 the c0 to set
     * @return a new instance
     */
    public BezierNode withOut(Point2D c2) {
        return new BezierNode(mask, equidistant, collinear, pointX, pointY, inX, inY, c2.getX(), c2.getY());
    }

    /**
     * @param outX the x2 to set
     * @param outY the y2 to set
     * @return a new instance
     */
    public BezierNode withOut(double outX, double outY) {
        return new BezierNode(mask, equidistant, collinear, pointX, pointY, inX, inY, outX, outY);
    }

    /**
     * @param collinear the collinear to set
     * @return a new instance
     */
    public BezierNode withCollinear(boolean collinear) {
        return new BezierNode(mask, equidistant, collinear, pointX, pointY, inX, inY, outX, outY);
    }

    /**
     * @param equidistant the equidistant to set
     * @return a new instance
     */
    public BezierNode withEquidistant(boolean equidistant) {
        return new BezierNode(mask, equidistant, collinear, pointX, pointY, inX, inY, outX, outY);
    }

    /**
     * @param mask the mask to set
     * @return a new instance
     */
    public BezierNode withMask(int mask) {
        return mask == this.mask ? this : new BezierNode(mask, equidistant, collinear, pointX, pointY, inX, inY, outX, outY);
    }

    /**
     * Sets all the bits in the specified mask.
     *
     * @param mask the mask to set
     * @return a new instance
     */
    public BezierNode withMaskBitsSet(int mask) {
        int newMask = this.mask | mask;
        return newMask == this.mask ? this : new BezierNode(newMask, equidistant, collinear, pointX, pointY, inX, inY, outX, outY);
    }

    /**
     * Clears all the bits in the specified mask.
     *
     * @param mask the mask to set
     * @return a new instance
     */
    public BezierNode withMaskBitsClears(int mask) {
        int newMask = this.mask & ~mask;
        return newMask == this.mask ? this : new BezierNode(newMask, equidistant, collinear, pointX, pointY, inX, inY, outX, outY);
    }

    @Override
    public String toString() {
        return "BezierNode{"
                + (collinear ? "isCollinear " : "")
                + (equidistant ? "isEquidistant " : "")
                + "mask=" + mask + ", " + pointX + "," + pointY + " " + inX + "," + inY + " " + outX + "," + outY + '}';
    }

    public BezierNode transform(AffineTransform transform) {
        Point2D p0 = transform.transform(new Point2D.Double(pointX, pointY), null);
        Point2D p1 = transform.transform(new Point2D.Double(inX, inY), null);
        Point2D p2 = transform.transform(new Point2D.Double(outX, outY), null);
        return new BezierNode(mask, equidistant, collinear, p0.getX(), p0.getY(), p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public BezierNode transform(Transform transform) {
        var p0 = transform.transform(pointX, pointY);
        var p1 = transform.transform(inX, inY);
        var p2 = transform.transform(outX, outY);
        return new BezierNode(mask, equidistant, collinear, p0.getX(), p0.getY(), p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

}
