package org.jhotdraw8.draw.handle;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.action.AbstractAction;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.icollection.immutable.ImmutableList;

public abstract class AbstractBezierNodeHandleAction extends AbstractAction {
    protected final @NonNull Figure figure;
    protected final @NonNull MapAccessor<ImmutableList<BezierNode>> nodeListKey;
    protected final @NonNull int nodeIndex;

    protected final @NonNull DrawingView view;

    public AbstractBezierNodeHandleAction(final @NonNull String id, @NonNull Figure figure, @NonNull MapAccessor<ImmutableList<BezierNode>> nodeListKey, @NonNull int nodeIndex, @NonNull DrawingView view) {
        super(id);
        this.figure = figure;
        this.nodeListKey = nodeListKey;
        this.nodeIndex = nodeIndex;
        this.view = view;
        DrawLabels.getResources().configureAction(this, id);
    }
}
