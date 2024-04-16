/*
 * @(#)AbstractDrawingModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.model;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxbase.tree.AbstractTreeModel;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AbstractDrawingModel.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractDrawingModel extends AbstractTreeModel<Figure> implements DrawingModel {

    private final @NonNull CopyOnWriteArrayList<Listener<DrawingModelEvent>> drawingModelListeners = new CopyOnWriteArrayList<>();

    public AbstractDrawingModel() {
    }

    @Override
    public final @NonNull CopyOnWriteArrayList<Listener<DrawingModelEvent>> getDrawingModelListeners() {
        return drawingModelListeners;
    }
}
