/*
 * @(#)LineConnectorHandle.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.geometry.Point2D;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Transform;
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.css.value.CssPoint2D;
import org.jhotdraw8.css.value.Paintable;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.figure.ConnectingFigure;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.geom.FXTransforms;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATE;
import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATION_AXIS;

/**
 * Handle for the start or end point of a connection figure.
 * <p>
 * Pressing the alt or the control key while dragging the handle prevents
 * connecting the point.
 *
 */
public class LineConnectorHandle extends AbstractConnectorHandle {
    public static final @Nullable BorderStrokeStyle INSIDE_STROKE = new BorderStrokeStyle(StrokeType.INSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 1.0, 0, null);

    private Background REGION_BACKGROUND_CONNECTED = new Background(new BackgroundFill(Color.BLUE, null, null));
    private final Background REGION_BACKGROUND_DISCONNECTED = new Background(new BackgroundFill(Color.WHITE, null, null));

    private static final Function<Color, Border> REGION_BORDER = color -> new Border(
            new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, null, new BorderWidths(2)),
            new BorderStroke(color, BorderStrokeStyle.SOLID, null, null)
    );
    private static final Circle REGION_SHAPE = new Circle(4);

    private final Region targetNode;

    public LineConnectorHandle(ConnectingFigure figure,
                               NonNullMapAccessor<CssPoint2D> pointKey,
                               MapAccessor<Connector> connectorKey, MapAccessor<Figure> targetKey) {
        super(figure, pointKey,
                connectorKey, targetKey);
        targetNode = new Region();
        targetNode.setShape(REGION_SHAPE);
        targetNode.setMouseTransparent(true);
        targetNode.setManaged(false);
        targetNode.setScaleShape(true);
        targetNode.setCenterShape(true);
        targetNode.resize(10, 10);
    }


    @Override
    public Region getNode(DrawingView view) {
        double size = view.getEditor().getHandleSize();
        if (targetNode.getWidth() != size) {
            targetNode.resize(size, size);
        }
        CssColor color = view.getEditor().getHandleColor();
        Color color1 = (Color) Paintable.getPaint(color);
        targetNode.setBorder(REGION_BORDER.apply(color.getColor()));
        REGION_BACKGROUND_CONNECTED = new Background(new BackgroundFill(color1, null, null));
        return targetNode;
    }


    @Override
    public void updateNode(DrawingView view) {
        Figure f = getOwner();
        Transform t = FXTransforms.concat(view.getWorldToView(), f.getLocalToWorld());
        Point2D p = f.getNonNull(pointKey).getConvertedValue();
        pickLocation = p = FXTransforms.transform(t, p);
        Connector connector = f.get(connectorKey);
        Figure target = f.get(targetKey);
        boolean isConnected = connector != null && target != null;
        targetNode.setBackground(isConnected ? REGION_BACKGROUND_CONNECTED : REGION_BACKGROUND_DISCONNECTED);
        double size = targetNode.getWidth();
        targetNode.relocate(p.getX() - size * 0.5, p.getY() - size * 0.5);
        // rotates the node:
        targetNode.setRotate(f.getStyledNonNull(ROTATE));
        targetNode.setRotationAxis(f.getStyledNonNull(ROTATION_AXIS));

        if (connector != null && target != null) {
            connectorLocation = view.worldToView(connector.getPointAndDerivativeInWorld(owner, target).getPoint(Point2D::new));
            targetNode.relocate(connectorLocation.getX() - size * 0.5, connectorLocation.getY() - size * 0.5);
        } else {
            connectorLocation = null;
        }
    }

}
