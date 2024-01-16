/*
 * @(#)FXPathPointsBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import org.jhotdraw8.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a square at each move-to and at the end of the specified path.
 *
 * @author Werner Randelshofer
 */
public class FXPathPointsBuilder extends AbstractPathBuilder<List<PathElement>> {

    private final @NonNull List<PathElement> elements;
    private boolean needsSquareAtLastPoint;
    private double squareSize = 5;

    public FXPathPointsBuilder() {
        this(5, new ArrayList<>());
    }

    public FXPathPointsBuilder(@NonNull List<PathElement> elements) {
        this(5, elements);
    }

    public FXPathPointsBuilder(double squareSize, @NonNull List<PathElement> elements) {
        this.elements = elements;
        this.squareSize = squareSize;
    }

    private void addSquare(double x, double y) {
        double halfSize = squareSize * 0.5;
        elements.add(new MoveTo(x - halfSize, y - halfSize));
        elements.add(new LineTo(x + halfSize, y - halfSize));
        elements.add(new LineTo(x + halfSize, y + halfSize));
        elements.add(new LineTo(x - halfSize, y + halfSize));
        elements.add(new ClosePath());
    }

    @Override
    public @NonNull List<PathElement> build() {
        if (needsSquareAtLastPoint) {
            addSquare(getLastX(), getLastY());
            needsSquareAtLastPoint = false;
        }
        return elements;
    }

    @Override
    protected void doArcTo(double lastX, double lastY, double rx, double ry, double xAxisRotation, double x, double y, boolean largeArcFlag, boolean sweepFlag) {
        needsSquareAtLastPoint = true;
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        if (needsSquareAtLastPoint) {
            addSquare(lastX, lastY);
            needsSquareAtLastPoint = false;
        }
    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x, double y, double x0, double y0, double x1, double y1) {
        needsSquareAtLastPoint = true;
    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        needsSquareAtLastPoint = true;
    }

    @Override
    protected void doMoveTo(double x, double y) {
        if (needsSquareAtLastPoint) {
            addSquare(getLastX(), getLastY());
            needsSquareAtLastPoint = false;
        }
        addSquare(x, y);
    }

    @Override
    protected void doPathDone() {
// empty
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x, double y, double x0, double y0) {
        needsSquareAtLastPoint = true;
    }

    public List<PathElement> getElements() {
        if (needsSquareAtLastPoint) {
            addSquare(getLastX(), getLastY());
            needsSquareAtLastPoint = false;
        }
        return elements;
    }

}
