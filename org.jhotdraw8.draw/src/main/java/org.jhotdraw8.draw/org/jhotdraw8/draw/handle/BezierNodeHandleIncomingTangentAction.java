package org.jhotdraw8.draw.handle;

import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;

import static org.jhotdraw8.geom.shape.BezierNode.IN_MASK;
import static org.jhotdraw8.geom.shape.BezierNode.IN_OUT_MASK;

/**
 * This action sets the mask bits {@link BezierNode#IN_MASK},
 * {@link BezierNode#OUT_MASK} to {@code true}, {@code false} in
 * the specified {@link BezierNode}.
 */
public class BezierNodeHandleIncomingTangentAction extends AbstractBezierNodeHandleAction {
    public final static String ID = "handle.bezierNode.incomingTangent";

    public BezierNodeHandleIncomingTangentAction(@NonNull Figure figure, @NonNull MapAccessor<BezierPath> nodeListKey, int nodeIndex, @NonNull DrawingView model) {
        super(ID, figure, nodeListKey, nodeIndex, model);

        BezierNode bnode = getBezierNode();
        if (bnode != null) {
            setSelected((bnode.getMask() & BezierNode.IN_OUT_MASK) == BezierNode.IN_MASK);
        }
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent event) {
        BezierPath path = owner.get(pathKey);
        BezierNode bnode = getBezierNode();
        if (path == null || bnode == null) return;
        BezierNode changedNode = bnode.withMaskBitsClears(IN_OUT_MASK).withMaskBitsSet(IN_MASK);
        path = path.set(nodeIndex, changedNode);
        view.getModel().set(owner, pathKey, path);
        view.recreateHandles();
    }
}
