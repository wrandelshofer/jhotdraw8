/*
 * @(#)NodeFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.render;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.Region;
import javafx.scene.shape.Shape;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.geom.FXRectangles;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.Points;

import java.awt.*;

/**
 * Provides methods for finding JavaFX nodes within a radius around a point.
 */
public class NodeFinder {
    private static final double LINE45DEG = Math.sqrt(0.5);

    public NodeFinder() {
    }

    /**
     * Returns the distance to the node, if the node contains the specified point within a
     * tolerance.
     *
     * @param node          The node
     * @param pointInLocal  The point in local coordinates
     * @param radiusInLocal The maximal distance the point is allowed to be away
     *                      from the node, in local coordinates
     * @return a distance if the node contains the point, null otherwise
     */
    public @Nullable Double contains(@NonNull Node node, @NonNull Point2D pointInLocal, double radiusInLocal) {
        double toleranceInLocal = radiusInLocal / FXTransforms.deltaTransform(node.getLocalToSceneTransform(), LINE45DEG, LINE45DEG).magnitude();

        if (!node.isVisible()) {
            return null;
        }

        // If the node has a clip, we only proceed if the point is inside
        // the clip with tolerance.
        final Node nodeClip = node.getClip();
        if (nodeClip instanceof Shape) {
            final java.awt.Shape shape = FXShapes.awtShapeFromFX((Shape) nodeClip);
            if (!shape.intersects(pointInLocal.getX() - toleranceInLocal,
                    pointInLocal.getY() - toleranceInLocal, toleranceInLocal * 2, toleranceInLocal * 2)) {
                return null;
            }
        }


        switch (node) {
            case Shape shape -> {

                if (shape.contains(pointInLocal)) {
                    return 0.0;
                }

                double widthFactor = switch (shape.getStrokeType()) {
                    default -> 0.5;
                    case INSIDE -> 0;
                    case OUTSIDE -> 1;
                };
                if (FXRectangles.contains(shape.getBoundsInLocal(), pointInLocal, toleranceInLocal)) {
                    int cap = switch (shape.getStrokeLineCap()) {
                        case SQUARE -> BasicStroke.CAP_SQUARE;
                        case BUTT -> (toleranceInLocal > 0) ? BasicStroke.CAP_ROUND : BasicStroke.CAP_BUTT;
                        case ROUND -> BasicStroke.CAP_ROUND;
                    };
                    int join = switch (shape.getStrokeLineJoin()) {
                        case MITER -> BasicStroke.JOIN_MITER;
                        case BEVEL -> BasicStroke.JOIN_BEVEL;
                        case ROUND -> BasicStroke.JOIN_ROUND;
                    };
                    final java.awt.Shape awtShape = FXShapes.awtShapeFromFX(shape);
                    return new BasicStroke(2f * (float) (shape.getStrokeWidth() * widthFactor + toleranceInLocal),
                            cap, join, (float) shape.getStrokeMiterLimit()
                    ).createStrokedShape(awtShape)
                            .contains(new java.awt.geom.Point2D.Double(pointInLocal.getX(), pointInLocal.getY()))
                            ? Points.distanceFromShape(awtShape, pointInLocal.getX(), pointInLocal.getY()) : null;
                } else {
                    return null;
                }
            }
            case Group group -> {
                if (FXRectangles.contains(node.getBoundsInLocal(), pointInLocal, toleranceInLocal)) {
                    return childContains((Parent) node, pointInLocal, radiusInLocal);

                }
                return null;
            }
            case Region region1 -> {
                if (FXRectangles.contains(node.getBoundsInLocal(), pointInLocal, toleranceInLocal)) {
                    Region region = (Region) node;
                    final Background bg = region.getBackground();
                    final Border border = region.getBorder();
                    if ((bg == null || bg.isEmpty()) && (border == null || border.isEmpty())) {
                        return childContains((Parent) node, pointInLocal, radiusInLocal);
                    } else {
                        return 0.0;
                    }
                }
                return null;
            }
            default -> {
                return FXRectangles.contains(node.getBoundsInLocal(), pointInLocal, radiusInLocal) ? 0.0 : null;  // foolishly assumes that all other nodes are rectangular
            }
        }
    }

    private @Nullable Double childContains(final @NonNull Parent node, final @NonNull Point2D pointInLocal, final double tolerance) {
        double minDistance = Double.POSITIVE_INFINITY;
        for (Node child : node.getChildrenUnmodifiable()) {
            Double distance = contains(child, child.parentToLocal(pointInLocal), tolerance);
            if (distance != null) {
                minDistance = Math.min(minDistance, distance);
            }
        }
        return Double.isFinite(minDistance) ? minDistance : null;
    }

    public @Nullable Node findNodeRecursive(@NonNull Node n, double vx, double vy, double radius) {
        if (contains(n, new Point2D(vx, vy), radius) != null) {
            if (n instanceof Shape) {
                return n;
            } else if (n instanceof Group group) {
                Point2D pl = n.parentToLocal(vx, vy);
                ObservableList<Node> children = group.getChildren();
                // FIXME should take viewOrder into account
                for (int i = children.size() - 1; i >= 0; i--) {// front to back
                    Node child = children.get(i);
                    double radiusInChild = child.parentToLocal(radius, 0).magnitude();
                    Node found = findNodeRecursive(child, pl.getX(), pl.getY(), radiusInChild);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }

}
