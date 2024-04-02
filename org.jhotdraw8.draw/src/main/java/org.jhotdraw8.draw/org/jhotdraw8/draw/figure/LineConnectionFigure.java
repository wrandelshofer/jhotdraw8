/*
 * @(#)LineConnectionFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Line;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.handle.Handle;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.handle.LineConnectorHandle;
import org.jhotdraw8.draw.handle.LineOutlineHandle;
import org.jhotdraw8.draw.handle.MoveHandle;
import org.jhotdraw8.draw.handle.SelectionHandle;
import org.jhotdraw8.draw.locator.PointLocator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.FXShapes;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.List;

/**
 * A figure which draws a line connection between two figures.
 *
 * @author Werner Randelshofer
 */
public class LineConnectionFigure extends AbstractLineConnectionFigure
        implements StrokableFigure, HideableFigure, StyleableFigure, LockableFigure, CompositableFigure,
        PathIterableFigure {

    /**
     * The CSS type selector for this object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "LineConnection";

    public LineConnectionFigure() {
        this(0, 0, 1, 1);
    }

    public LineConnectionFigure(@NonNull Point2D start, @NonNull Point2D end) {
        this(start.getX(), start.getY(), end.getX(), end.getY());
    }

    public LineConnectionFigure(double startX, double startY, double endX, double endY) {
        super(startX, startY, endX, endY);
    }

    @Override
    public void createHandles(@NonNull HandleType handleType, @NonNull List<Handle> list) {
        if (handleType == HandleType.SELECT) {
            list.add(new LineOutlineHandle(this));
        } else if (handleType == HandleType.MOVE) {
            list.add(new LineOutlineHandle(this));
            if (get(START_CONNECTOR) == null) {
                list.add(new MoveHandle(this, new PointLocator(START)));
            } else {
                list.add(new SelectionHandle(this, new PointLocator(START)));
            }
            if (get(END_CONNECTOR) == null) {
                list.add(new MoveHandle(this, new PointLocator(END)));
            } else {
                list.add(new SelectionHandle(this, new PointLocator(END)));
            }
        } else if (handleType == HandleType.RESIZE) {
            list.add(new LineOutlineHandle(this));
            list.add(new LineConnectorHandle(this, START, START_CONNECTOR, START_TARGET));
            list.add(new LineConnectorHandle(this, END, END_CONNECTOR, END_TARGET));
        } else if (handleType == HandleType.POINT) {
            list.add(new LineOutlineHandle(this));
            list.add(new LineConnectorHandle(this, START, START_CONNECTOR, START_TARGET));
            list.add(new LineConnectorHandle(this, END, END_CONNECTOR, END_TARGET));
        } else if (handleType == HandleType.TRANSFORM) {
            list.add(new LineOutlineHandle(this));
        } else {
            super.createHandles(handleType, list);
        }
    }

    @Override
    public @NonNull Node createNode(@NonNull RenderContext drawingView) {
        Line n = new Line();
        n.setManaged(false);
        return n;
    }

    @Override
    public @NonNull String getTypeSelector() {
        return TYPE_SELECTOR;
    }

    @Override
    public void updateNode(@NonNull RenderContext ctx, @NonNull Node node) {

        Line lineNode = (Line) node;
        Point2D start = getNonNull(START).getConvertedValue();
        lineNode.setStartX(start.getX());
        lineNode.setStartY(start.getY());
        Point2D end = getNonNull(END).getConvertedValue();
        lineNode.setEndX(end.getX());
        lineNode.setEndY(end.getY());

        applyHideableFigureProperties(ctx, lineNode);
        applyStrokableFigureProperties(ctx, lineNode);
        applyCompositableFigureProperties(ctx, node);
        applyStyleableFigureProperties(ctx, node);
    }

    @Override
    public @NonNull PathIterator getPathIterator(@NonNull RenderContext ctx, @Nullable AffineTransform tx) {
        return FXShapes.fxShapeToAwtShape(new Line(
                getNonNull(START_X).getConvertedValue(),
                getNonNull(START_Y).getConvertedValue(),
                getNonNull(END_X).getConvertedValue(),
                getNonNull(END_Y).getConvertedValue())).getPathIterator(tx);
    }

    @Override
    public void layout(@NonNull RenderContext ctx) {
        Point2D start = getNonNull(START).getConvertedValue();
        Point2D end = getNonNull(END).getConvertedValue();
        Connector startConnector = get(START_CONNECTOR);
        Connector endConnector = get(END_CONNECTOR);
        Figure startTarget = get(START_TARGET);
        Figure endTarget = get(END_TARGET);
        if (startConnector != null && startTarget != null) {
            start = startConnector.getPointAndDerivativeInWorld(this, startTarget).getPoint(Point2D::new);
        }
        if (endConnector != null && endTarget != null) {
            end = endConnector.getPointAndDerivativeInWorld(this, endTarget).getPoint(Point2D::new);
        }

        if (startConnector != null && startTarget != null) {
            java.awt.geom.Point2D.Double chp = startConnector.chopStart(ctx, this, startTarget, start, end);
            final Point2D p = worldToParent(chp.getX(), chp.getY());
            set(START, new CssPoint2D(p));
        }
        if (endConnector != null && endTarget != null) {
            java.awt.geom.Point2D.Double chp = endConnector.chopStart(ctx, this, endTarget, end, start);
            final Point2D p = worldToParent(chp.getX(), chp.getY());
            set(END, new CssPoint2D(p));
        }

    }
}
