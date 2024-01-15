package org.jhotdraw8.draw.handle;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.action.AbstractAction;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;

public abstract class AbstractBezierNodeHandleAction extends AbstractAction {
    protected final @NonNull Figure owner;
    protected final @NonNull MapAccessor<BezierPath> pathKey;
    protected final @NonNull int nodeIndex;

    protected final @NonNull DrawingView view;

    public AbstractBezierNodeHandleAction(final @NonNull String id, @NonNull Figure owner, @NonNull MapAccessor<BezierPath> pathKey, @NonNull int nodeIndex, @NonNull DrawingView view) {
        super(id);
        this.owner = owner;
        this.pathKey = pathKey;
        this.nodeIndex = nodeIndex;
        this.view = view;
        DrawLabels.getResources().configureAction(this, id);
    }

    protected @Nullable BezierNode getBezierNode() {
        BezierPath path = owner.get(pathKey);
        return path == null || path.size() <= nodeIndex ? null : path.get(nodeIndex);

    }
}
