/*
 * @(#)AbstractCreationTool.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.tool;

import org.jspecify.annotations.Nullable;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.Layer;

import java.util.function.Supplier;

/**
 * AbstractCreationTool.
 *
 * @param <F> the type of the figures that can be created by this tool
 * @author Werner Randelshofer
 */
public abstract class AbstractCreationTool<F extends Figure> extends AbstractTool {
    protected Supplier<Layer> layerFactory;
    protected Supplier<? extends F> figureFactory;
    /**
     * The created figure.
     */
    protected @Nullable F createdFigure;

    public AbstractCreationTool(String name, Resources rsrc, Supplier<? extends F> figureFactory, Supplier<Layer> layerFactory) {
        super(name, rsrc);
        this.figureFactory = figureFactory;
        this.layerFactory = layerFactory;
    }


    public void setFigureFactory(Supplier<F> factory) {
        this.figureFactory = factory;
    }

    public void setLayerFactory(Supplier<Layer> factory) {
        this.layerFactory = factory;
    }

    protected F createFigure() {
        return figureFactory.get();
    }


    /**
     * Finds a layer for the specified figure. Creates a new layer if no
     * suitable layer can be found.
     *
     * @param dv        the drawing view
     * @param newFigure the figure
     * @return a suitable parent for the figure
     */
    protected @Nullable Figure getOrCreateParent(DrawingView dv, Figure newFigure) {
        Drawing drawing = dv.getDrawing();
        if (drawing == null) {
            return null;
        }

        // try to use the active layer
        Figure activeParent = dv.getActiveParent();
        if (activeParent != null && activeParent.isEditable() && activeParent.isAllowsChildren()
                && activeParent.isSuitableChild(newFigure)
                && newFigure.isSuitableParent(activeParent)) {
            return activeParent;
        }
        // search for a suitable parent front to back
        Figure layer = null;
        var layers = drawing.getChildren();
        for (int i = layers.size() - 1; i >= 0; i--) {
            Figure candidate = layers.get(i);
            if (candidate.isEditable() && candidate.isAllowsChildren()
                    && newFigure.isSuitableParent(candidate)
                    && candidate.isSuitableChild(newFigure)) {
                layer = candidate;
                break;
            }
        }
        // create a new layer if necessary
        if (layer == null) {
            layer = layerFactory.get();
            dv.getModel().addChildTo(layer, drawing);
            if (layer.getParent() != drawing) {
                // the drawing does not accept the layer!
                return drawing;
            }
        }
        return layer;
    }
}