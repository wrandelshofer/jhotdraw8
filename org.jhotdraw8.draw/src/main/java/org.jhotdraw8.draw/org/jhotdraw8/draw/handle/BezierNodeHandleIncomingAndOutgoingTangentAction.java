package org.jhotdraw8.draw.handle;

import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;

import static org.jhotdraw8.geom.shape.BezierNode.C1C2_MASK;

/**
 * This action sets the mask bits {@link BezierNode#C1_MASK},
 * {@link BezierNode#C2_MASK} to {@code true}, {@code true} in
 * the specified {@link BezierNode}.
 */
public class BezierNodeHandleIncomingAndOutgoingTangentAction extends AbstractBezierNodeHandleAction {
    public final static String ID = "handle.bezierNode.incomingAndOutgoingTangent";

    public BezierNodeHandleIncomingAndOutgoingTangentAction(@NonNull Figure owner, @NonNull MapAccessor<BezierPath> pathKey, @NonNull int nodeIndex, @NonNull DrawingView view) {
        super(ID, owner, pathKey, nodeIndex, view);

        BezierNode bnode = getBezierNode();
        if (bnode != null) {
            setSelected((bnode.getMask() & BezierNode.C1C2_MASK) == BezierNode.C1C2_MASK);
        }
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent event) {
        BezierNode bnode = getBezierNode();
        BezierPath path = owner.get(pathKey);
        if (bnode == null || path == null) return;
        BezierNode changedNode = bnode.withMaskBits(C1C2_MASK);
        path = path.set(nodeIndex, changedNode);
        view.getModel().set(owner, pathKey, path);
        view.recreateHandles();
    }
}
