package org.jhotdraw8.draw.handle;

import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;

import static org.jhotdraw8.geom.shape.BezierNode.IN_OUT_MASK;
import static org.jhotdraw8.geom.shape.BezierNode.OUT_MASK;

/**
 * This action sets the mask bits {@link BezierNode#IN_MASK},
 * {@link BezierNode#OUT_MASK} to {@code false}, {@code true} in
 * the specified {@link BezierNode}.
 */
public class BezierNodeHandleOutgoingTangentAction extends AbstractBezierNodeHandleAction {
    public final static String ID = "handle.bezierNode.outgoingTangent";

    @SuppressWarnings("this-escape")
    public BezierNodeHandleOutgoingTangentAction(@NonNull Figure figure, @NonNull MapAccessor<BezierPath> pathKey, int nodeIndex, @NonNull DrawingView view) {
        super(ID, figure, pathKey, nodeIndex, view);

        BezierPath path = figure.get(pathKey);
        if (path != null && path.size() > nodeIndex) {
            BezierNode bnode = path.get(nodeIndex);
            setSelected((bnode.getMask() & BezierNode.IN_OUT_MASK) == BezierNode.OUT_MASK);
        }
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent event) {
        BezierPath path = owner.get(pathKey);
        if (path == null || path.size() <= nodeIndex) {
            return;
        }
        BezierNode bnode = path.get(nodeIndex);
        BezierNode changedNode = bnode.withMaskBitsClears(IN_OUT_MASK).withMaskBitsSet(OUT_MASK);
        path = path.set(nodeIndex, changedNode);
        view.getModel().set(owner, pathKey, path);
        view.recreateHandles();
    }
}
