package org.jhotdraw8.draw.handle;

import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;

import static org.jhotdraw8.geom.shape.BezierNode.IN_OUT_MASK;

/**
 * This action sets the mask bits {@link BezierNode#IN_MASK},
 * {@link BezierNode#OUT_MASK} to {@code true}, {@code true} in
 * the specified {@link BezierNode}.
 */
public class BezierNodeHandleIncomingAndOutgoingTangentAction extends AbstractBezierNodeHandleAction {
    public final static String ID = "handle.bezierNode.incomingAndOutgoingTangent";

    public BezierNodeHandleIncomingAndOutgoingTangentAction(@NonNull Figure owner, @NonNull MapAccessor<BezierPath> pathKey, int nodeIndex, @NonNull DrawingView view) {
        super(ID, owner, pathKey, nodeIndex, view);

        BezierNode bnode = getBezierNode();
        if (bnode != null) {
            setSelected((bnode.getMask() & BezierNode.IN_OUT_MASK) == BezierNode.IN_OUT_MASK);
        }
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent event) {
        BezierNode bnode = getBezierNode();
        BezierPath path = owner.get(pathKey);
        if (bnode == null || path == null) return;
        BezierNode changedNode = bnode.withMaskBitsSet(IN_OUT_MASK);
        path = path.set(nodeIndex, changedNode);
        view.getModel().set(owner, pathKey, path);
        view.recreateHandles();
    }
}
