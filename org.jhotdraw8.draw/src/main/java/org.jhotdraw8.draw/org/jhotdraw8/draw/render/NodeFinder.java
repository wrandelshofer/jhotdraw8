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
import org.jhotdraw8.geom.FXGeom;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.Geom;

import java.awt.*;

/**
 * Provides methods for finding JavaFX nodes within a radius around a point.
 */
public class NodeFinder {
    private static final double LINE45DEG = Math.sqrt(0.5);

    public NodeFinder() {
    }

    /**
     * Returns true if the node contains the specified point within a
     * tolerance.
     *
     * @param node          The node
     * @param pointInLocal  The point in local coordinates
     * @param radiusInLocal The maximal distance the point is allowed to be away
     *                      from the node, in local coordinates
     * @return a distance if the node contains the point, null otherwise
     */
    public Double contains(@NonNull Node node, @NonNull Point2D pointInLocal, double radiusInLocal) {
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


        if (node instanceof Shape) {
            Shape shape = (Shape) node;

            if (shape.contains(pointInLocal)) {
                return 0.0;
            }

            double widthFactor;
            switch (shape.getStrokeType()) {
            case CENTERED:
            default:
                widthFactor = 0.5;
                break;
            case INSIDE:
                widthFactor = 0;
                break;
            case OUTSIDE:
                widthFactor = 1;
                break;
            }
            if (FXGeom.contains(shape.getBoundsInLocal(), pointInLocal, toleranceInLocal)) {
                int cap;
                switch (shape.getStrokeLineCap()) {
                case SQUARE:
                    cap = BasicStroke.CAP_SQUARE;
                    break;
                case BUTT:
                    cap = (toleranceInLocal > 0) ? BasicStroke.CAP_ROUND : BasicStroke.CAP_BUTT;
                    break;
                case ROUND:
                    cap = BasicStroke.CAP_ROUND;
                    break;
                default:
                    throw new IllegalArgumentException();
                }
                int join;
                switch (shape.getStrokeLineJoin()) {
                case MITER:
                    join = BasicStroke.JOIN_MITER;
                    break;
                case BEVEL:
                    join = BasicStroke.JOIN_BEVEL;
                    break;
                case ROUND:
                    join = BasicStroke.JOIN_ROUND;
                    break;
                default:
                    throw new IllegalArgumentException();
                }
                final java.awt.Shape awtShape = FXShapes.awtShapeFromFX(shape);
                return new BasicStroke(2f * (float) (shape.getStrokeWidth() * widthFactor + toleranceInLocal),
                        cap, join, (float) shape.getStrokeMiterLimit()
                ).createStrokedShape(awtShape)
                        .contains(new java.awt.geom.Point2D.Double(pointInLocal.getX(), pointInLocal.getY()))
                        ? Geom.distanceFromShape(awtShape, pointInLocal.getX(), pointInLocal.getY()) : null;
            } else {
                return null;
            }
        } else if (node instanceof Group) {
            if (FXGeom.contains(node.getBoundsInLocal(), pointInLocal, toleranceInLocal)) {
                return childContains((Parent) node, pointInLocal, radiusInLocal);

            }
            return null;
        } else if (node instanceof Region) {
            if (FXGeom.contains(node.getBoundsInLocal(), pointInLocal, toleranceInLocal)) {
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
        } else { // foolishly assumes that all other nodes are rectangular
            return FXGeom.contains(node.getBoundsInLocal(), pointInLocal, radiusInLocal) ? 0.0 : null;
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
            } else if (n instanceof Group) {
                Point2D pl = n.parentToLocal(vx, vy);
                Group group = (Group) n;
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
