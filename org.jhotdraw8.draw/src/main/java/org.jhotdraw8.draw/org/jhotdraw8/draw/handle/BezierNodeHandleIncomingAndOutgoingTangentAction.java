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

    public BezierNodeHandleIncomingAndOutgoingTangentAction(@NonNull Figure figure, @NonNull MapAccessor<BezierPath> pathKey, @NonNull int nodeIndex, @NonNull DrawingView view) {
        super(ID, figure, pathKey, nodeIndex, view);

        BezierPath path = figure.get(pathKey);
        if (path != null && path.size() > nodeIndex) {
            BezierNode bnode = path.get(nodeIndex);
            setSelected((bnode.getMask() & BezierNode.C1C2_MASK) == BezierNode.C1C2_MASK);
        }
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent event) {
        BezierPath path = figure.get(pathKey);
        if (path == null) return;
        BezierNode bnode = path.get(nodeIndex);
        BezierNode changedNode = bnode.withMaskBits(C1C2_MASK);
        path = path.set(nodeIndex, changedNode);
        view.getModel().set(figure, pathKey, path);
        view.recreateHandles();
    }
}
