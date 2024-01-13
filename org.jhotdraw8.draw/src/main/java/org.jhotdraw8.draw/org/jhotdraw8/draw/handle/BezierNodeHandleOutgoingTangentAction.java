package org.jhotdraw8.draw.handle;

import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;

import static org.jhotdraw8.geom.shape.BezierNode.C1C2_MASK;
import static org.jhotdraw8.geom.shape.BezierNode.C2_MASK;

/**
 * This action sets the mask bits {@link BezierNode#C1_MASK},
 * {@link BezierNode#C2_MASK} to {@code false}, {@code true} in
 * the specified {@link BezierNode}.
 */
public class BezierNodeHandleOutgoingTangentAction extends AbstractBezierNodeHandleAction {
    public final static String ID = "handle.bezierNode.outgoingTangent";

    public BezierNodeHandleOutgoingTangentAction(@NonNull Figure figure, @NonNull MapAccessor<BezierPath> bezierPathKey, @NonNull int nodeIndex, @NonNull DrawingView view) {
        super(ID, figure, bezierPathKey, nodeIndex, view);

        BezierPath path = figure.get(bezierPathKey);
        if (path != null && path.size() > nodeIndex) {
            BezierNode bnode = path.get(nodeIndex);
            setSelected((bnode.getMask() & BezierNode.C1C2_MASK) == BezierNode.C2_MASK);
        }
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent event) {
        BezierPath path = figure.get(pathKey);
        if (path == null || path.size() <= nodeIndex) return;
        BezierNode bnode = path.get(nodeIndex);
        BezierNode changedNode = bnode.withClearMaskBits(C1C2_MASK).withMaskBits(C2_MASK);
        path = path.set(nodeIndex, changedNode);
        view.getModel().set(figure, pathKey, path);
        view.recreateHandles();
    }
}
