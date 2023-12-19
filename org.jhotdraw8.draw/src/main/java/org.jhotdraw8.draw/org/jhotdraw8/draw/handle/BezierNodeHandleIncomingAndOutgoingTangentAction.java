package org.jhotdraw8.draw.handle;

import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierNodePath;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import static org.jhotdraw8.geom.shape.BezierNode.C1C2_MASK;

/**
 * This action sets the mask bits {@link BezierNode#C1_MASK},
 * {@link BezierNode#C2_MASK} to {@code true}, {@code true} in
 * the specified {@link BezierNode}.
 */
public class BezierNodeHandleIncomingAndOutgoingTangentAction extends AbstractBezierNodeHandleAction {
    public final static String ID = "handle.bezierNode.incomingAndOutgoingTangent";

    public BezierNodeHandleIncomingAndOutgoingTangentAction(@NonNull Figure figure, @NonNull MapAccessor<ImmutableList<BezierNode>> nodeListKey, @NonNull int nodeIndex, @NonNull DrawingView view) {
        super(ID, figure, nodeListKey, nodeIndex, view);

        BezierNodePath path = new BezierNodePath(figure.get(nodeListKey));
        BezierNode bnode = path.getNodes().get(nodeIndex);
        setSelected((bnode.getMask() & BezierNode.C1C2_MASK) == BezierNode.C1C2_MASK);


    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent event) {
        BezierNodePath path = new BezierNodePath(figure.get(nodeListKey));
        BezierNode bnode = path.getNodes().get(nodeIndex);
        BezierNode changedNode = bnode.setMaskBits(C1C2_MASK);
        path.getNodes().set(nodeIndex, changedNode);
        view.getModel().set(figure, nodeListKey, VectorList.copyOf(path.getNodes()));
        view.recreateHandles();
    }
}
