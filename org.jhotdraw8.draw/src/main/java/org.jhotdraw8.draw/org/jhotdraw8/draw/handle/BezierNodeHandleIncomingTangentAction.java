package org.jhotdraw8.draw.handle;

import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;

import static org.jhotdraw8.geom.shape.BezierNode.C1C2_MASK;
import static org.jhotdraw8.geom.shape.BezierNode.C1_MASK;

/**
 * This action sets the mask bits {@link BezierNode#C1_MASK},
 * {@link BezierNode#C2_MASK} to {@code true}, {@code false} in
 * the specified {@link BezierNode}.
 */
public class BezierNodeHandleIncomingTangentAction extends AbstractBezierNodeHandleAction {
    public final static String ID = "handle.bezierNode.incomingTangent";

    public BezierNodeHandleIncomingTangentAction(@NonNull Figure figure, @NonNull MapAccessor<BezierPath> nodeListKey, @NonNull int nodeIndex, @NonNull DrawingView model) {
        super(ID, figure, nodeListKey, nodeIndex, model);

        BezierNode bnode = getBezierNode();
        if (bnode != null) {
            setSelected((bnode.getMask() & BezierNode.C1C2_MASK) == BezierNode.C1_MASK);
        }
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent event) {
        BezierPath path = owner.get(pathKey);
        BezierNode bnode = getBezierNode();
        if (path == null || bnode == null) return;
        BezierNode changedNode = bnode.withClearMaskBits(C1C2_MASK).withMaskBits(C1_MASK);
        path = path.set(nodeIndex, changedNode);
        view.getModel().set(owner, pathKey, path);
        view.recreateHandles();
    }
}
