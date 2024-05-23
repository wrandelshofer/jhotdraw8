/*
 * @(#)FXPathElementsBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.QuadCurveTo;

import java.util.ArrayList;
import java.util.List;

/**
 * FXPathElementsBuilder.
 *
 * @author Werner Randelshofer
 */
public class FXPathElementsBuilder extends AbstractPathBuilder<List<PathElement>> {
    public static final ClosePath CLOSE_PATH = new ClosePath();
    private final List<PathElement> elements;

    public FXPathElementsBuilder() {
        this(new ArrayList<>());
    }

    public FXPathElementsBuilder(List<PathElement> elements) {
        this.elements = elements;
    }

    @Override
    public List<PathElement> build() {
        return elements;
    }

    @Override
    protected void doArcTo(double lastX, double lastY, double rx, double ry, double xAxisRotation, double x, double y, boolean largeArcFlag, boolean sweepFlag) {
        elements.add(new ArcTo(rx, ry, xAxisRotation, x, y, largeArcFlag, sweepFlag));
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        elements.add(CLOSE_PATH);
    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x, double y, double x0, double y0, double x1, double y1) {
        elements.add(new CubicCurveTo(x, y, x0, y0, x1, y1));
    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        elements.add(new LineTo(x, y));
    }

    @Override
    protected void doMoveTo(double x, double y) {
        elements.add(new MoveTo(x, y));
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x, double y, double x0, double y0) {
        elements.add(new QuadCurveTo(x, y, x0, y0));
    }


}
