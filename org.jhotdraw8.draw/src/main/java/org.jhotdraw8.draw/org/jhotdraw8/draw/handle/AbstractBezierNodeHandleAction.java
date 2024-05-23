package org.jhotdraw8.draw.handle;

import org.jhotdraw8.application.action.AbstractAction;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;
import org.jspecify.annotations.Nullable;

public abstract class AbstractBezierNodeHandleAction extends AbstractAction {
    protected final Figure owner;
    protected final MapAccessor<BezierPath> pathKey;
    protected final int nodeIndex;

    protected final DrawingView view;

    @SuppressWarnings("this-escape")
    public AbstractBezierNodeHandleAction(final String id, Figure owner, MapAccessor<BezierPath> pathKey, int nodeIndex, DrawingView view) {
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
