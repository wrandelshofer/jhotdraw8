/*
 * @(#)BezierPathIterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.shape;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.NoSuchElementException;

/**
 * BezierPathIterator.
 *
 * @author Werner Randelshofer
 */
public class BezierPathIterator implements PathIterator {
    enum State {
        PRODUCE_SEGMENT,
        CLOSE_PATH,
        PRODUCE_CLOSE,
        FINAL_SEGMENT, DONE
    }

    private @NonNull State state;
    private int index;
    private int lastMoveTo;
    private final @NonNull BezierPath path;
    private final @NonNull AffineTransform transform;

    private final double @NonNull [] segCoords = new double[6];
    private int segType;

    public BezierPathIterator(@NonNull BezierPath path, @Nullable AffineTransform transform) {
        this.path = path;
        this.transform = transform == null ? AffineTransform.getTranslateInstance(0, 0) : transform;
        if (path.isEmpty()) {
            this.state = State.DONE;
        } else {
            this.segType = SEG_MOVETO;
            BezierNode current = path.get(0);
            this.segCoords[0] = current.getX0();
            this.segCoords[1] = current.getY0();
            if (path.size() > 1) {
                this.state = State.PRODUCE_SEGMENT;
                index++;
            } else {
                this.state = State.FINAL_SEGMENT;
            }
        }
    }

    /**
     * Return the winding rule for determining the interior of the path.
     *
     * @see PathIterator#WIND_EVEN_ODD
     * @see PathIterator#WIND_NON_ZERO
     */
    @Override
    public int getWindingRule() {
        return path.getWindingRule();
    }

    /**
     * Tests if there are more points to read.
     *
     * @return true if there are more points to read
     */
    @Override
    public boolean isDone() {
        return state == State.DONE;
    }

    @Override
    public void next() {
        if (isDone()) return;
        switch (state) {
            case FINAL_SEGMENT -> {
                state = State.DONE;
            }
            case PRODUCE_SEGMENT -> {
                BezierNode prev = path.get(index - 1);
                BezierNode current = path.get(index);
                if (current.hasMask(BezierNode.MOVE_MASK)) {
                    segCoords[0] = current.getX0();
                    segCoords[1] = current.getY0();
                    segType = SEG_MOVETO;
                    lastMoveTo = index;
                } else {
                    if (prev.isC2() && current.isC1()) {
                        segCoords[0] = prev.getX2();
                        segCoords[1] = prev.getY2();
                        segCoords[2] = current.getX1();
                        segCoords[3] = current.getY1();
                        segCoords[4] = current.getX0();
                        segCoords[5] = current.getY0();
                        segType = SEG_CUBICTO;
                    } else if (prev.isC2()) {
                        segCoords[0] = prev.getX2();
                        segCoords[1] = prev.getY2();
                        segCoords[2] = current.getX0();
                        segCoords[3] = current.getY0();
                        segType = SEG_QUADTO;
                    } else if (current.isC1()) {
                        segCoords[0] = current.getX1();
                        segCoords[1] = current.getY1();
                        segCoords[2] = current.getX0();
                        segCoords[3] = current.getY0();
                        segType = SEG_QUADTO;
                    } else {
                        segCoords[0] = current.getX0();
                        segCoords[1] = current.getY0();
                        segType = SEG_LINETO;
                    }
                }
                if (current.hasMask(BezierNode.CLOSE_MASK)) {
                    state = State.CLOSE_PATH;
                } else if (index < path.size() - 1) {
                    index++;
                } else {
                    state = State.FINAL_SEGMENT;
                }
            }
            case CLOSE_PATH -> {
                BezierNode current = path.get(index);
                BezierNode lastMove = path.get(lastMoveTo);
                if (current.isC2() && lastMove.isC1()) {
                    segCoords[0] = current.getX2();
                    segCoords[1] = current.getY2();
                    segCoords[2] = lastMove.getX1();
                    segCoords[3] = lastMove.getY1();
                    segCoords[4] = lastMove.getX0();
                    segCoords[5] = lastMove.getY0();
                    segType = SEG_CUBICTO;
                    state = State.PRODUCE_CLOSE;
                } else if (current.isC2()) {
                    segCoords[0] = current.getX2();
                    segCoords[1] = current.getY2();
                    segCoords[2] = lastMove.getX0();
                    segCoords[3] = lastMove.getY0();
                    segType = SEG_QUADTO;
                    state = State.PRODUCE_CLOSE;
                } else if (lastMove.isC1()) {
                    segCoords[0] = lastMove.getX1();
                    segCoords[1] = lastMove.getY1();
                    segCoords[2] = lastMove.getX0();
                    segCoords[3] = lastMove.getY0();
                    segType = SEG_QUADTO;
                    state = State.PRODUCE_CLOSE;
                } else {
                    if (index < path.size() - 1) {
                        segType = SEG_CLOSE;
                        state = State.PRODUCE_SEGMENT;
                        index++;
                    } else {
                        segType = SEG_CLOSE;
                        state = State.FINAL_SEGMENT;
                    }
                }
            }

            case PRODUCE_CLOSE -> {
                segType = SEG_CLOSE;
                if (index < path.size() - 1) {
                    segType = SEG_CLOSE;
                    state = State.PRODUCE_SEGMENT;
                    index++;
                } else {
                    state = State.FINAL_SEGMENT;
                }
            }

            default -> {
                throw new NoSuchElementException();
            }

        }
    }


    @Override
    public int currentSegment(double[] coords) {
        switch (segType) {
            case SEG_MOVETO, SEG_LINETO -> transform.transform(segCoords, 0, coords, 0, 1);
            case SEG_QUADTO -> transform.transform(segCoords, 0, coords, 0, 2);
            case SEG_CUBICTO -> transform.transform(segCoords, 0, coords, 0, 3);
            case SEG_CLOSE -> {
            }
            default -> throw new NoSuchElementException();
        }
        return segType;
    }

    @Override
    public int currentSegment(float[] coords) {
        switch (segType) {
            case SEG_MOVETO, SEG_LINETO -> transform.transform(segCoords, 0, coords, 0, 1);
            case SEG_QUADTO -> transform.transform(segCoords, 0, coords, 0, 2);
            case SEG_CUBICTO -> transform.transform(segCoords, 0, coords, 0, 3);
            case SEG_CLOSE -> {
            }
            default -> throw new NoSuchElementException();
        }
        return segType;
    }

}
