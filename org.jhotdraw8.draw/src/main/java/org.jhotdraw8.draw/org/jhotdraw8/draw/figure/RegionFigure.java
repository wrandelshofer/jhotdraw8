/*
 * @(#)RegionFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Path;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.connector.PathConnector;
import org.jhotdraw8.draw.locator.BoundsLocator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jspecify.annotations.Nullable;

public class RegionFigure extends AbstractRegionFigure
        implements FillableFigure, StrokableFigure, CompositableFigure,
        StyleableFigure, TransformableFigure, HideableFigure,
        ConnectableFigure, LockableFigure, ResizableFigure {

    /**
     * The CSS type selector for this object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "Region";

    public RegionFigure() {
    }

    @Override
    public @Nullable Connector findConnector(Point2D pointInLocal, Figure connectingFigure, double tolerance) {
        return new PathConnector(new BoundsLocator(getLayoutBounds(), pointInLocal));
    }

    @Override
    public void updateNode(RenderContext ctx, Node node) {
        super.updateNode(ctx, node);
        applyHideableFigureProperties(ctx, node);
    }

    @Override
    protected void updatePathNode(RenderContext ctx, Path path) {
        super.updatePathNode(ctx, path);
        applyFillableFigureProperties(ctx, path);
        applyStrokableFigureProperties(ctx, path);
        applyCompositableFigureProperties(ctx, path);
        applyTransformableFigureProperties(ctx, path);
    }

    @Override
    public String getTypeSelector() {
        return TYPE_SELECTOR;
    }
}
