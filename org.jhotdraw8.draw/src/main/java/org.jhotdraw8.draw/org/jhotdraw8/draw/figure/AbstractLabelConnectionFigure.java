/*
 * @(#)AbstractLabelConnectionFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.handle.BoundsInLocalOutlineHandle;
import org.jhotdraw8.draw.handle.Handle;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.handle.LabelConnectorHandle;
import org.jhotdraw8.draw.handle.MoveHandle;
import org.jhotdraw8.draw.key.CssPoint2DStyleableMapAccessor;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.NonNullEnumStyleableKey;
import org.jhotdraw8.draw.locator.BoundsLocator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.geom.Angles;
import org.jhotdraw8.geom.FXGeom;
import org.jhotdraw8.geom.FXPreciseRotate;
import org.jhotdraw8.geom.PointAndDerivative;
import org.jhotdraw8.icollection.ChampSet;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A label that can be attached to another figure by setting {@link #LABEL_CONNECTOR} and
 * {@link #LABEL_TARGET}.
 * <p>
 * When the label is attached it computes the {@link #LABELED_LOCATION} using the
 * {@link #LABEL_CONNECTOR} on the target figure. Then it computes the {@link #ORIGIN}
 * and rotation of the label using the properties {@link #LABEL_OFFSET}
 * and {@link #LABEL_AUTOROTATE}.
 * <pre>
 * LABELED_LOCATION:    x,y (has a derivative that can be rotated)
 *                       |
 *                       | + LABEL_OFFSET (perpendicular to
 *                       |                 LABELED_LOCATION)
 *                       ↓
 *                   +--------------------+
 * ORIGIN:           |  x,y               |
 * LABEL_AUTOROTATE: |   ↺                |
 *                   |                    |
 *                   |    layout bounds   |
 *                   +--------------------+
 *
 * </pre>
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractLabelConnectionFigure extends AbstractLabelFigure
        implements ConnectingFigure, TransformableFigure, LabelConnectionFigure {

    public static final CssSizeStyleableKey LABELED_LOCATION_X = new CssSizeStyleableKey("labeledLocationX", CssSize.ZERO);
    public static final CssSizeStyleableKey LABELED_LOCATION_Y = new CssSizeStyleableKey("labeledLocationY", CssSize.ZERO);
    public static final CssPoint2DStyleableMapAccessor LABELED_LOCATION = new CssPoint2DStyleableMapAccessor("labeledLocation", LABELED_LOCATION_X, LABELED_LOCATION_Y);

    /**
     * The perpendicular offset of the label.
     * <p>
     * The offset is perpendicular to the tangent line of the figure.
     */
    public static final CssSizeStyleableKey LABEL_OFFSET_Y = new CssSizeStyleableKey("labelOffsetY", CssSize.ZERO);
    /**
     * The tangential offset of the label.
     * <p>
     * The offset is on tangent line of the figure.
     */
    public static final CssSizeStyleableKey LABEL_OFFSET_X = new CssSizeStyleableKey("labelOffsetX", CssSize.ZERO);
    public static final CssPoint2DStyleableMapAccessor LABEL_OFFSET = new CssPoint2DStyleableMapAccessor("labelOffset", LABEL_OFFSET_X, LABEL_OFFSET_Y);
    /**
     * Whether the label should be rotated with the target.
     */
    public static final NonNullEnumStyleableKey<LabelAutorotate> LABEL_AUTOROTATE = new NonNullEnumStyleableKey<>("labelAutorotate", LabelAutorotate.class, LabelAutorotate.OFF);
    public static final CssSizeStyleableKey LABEL_TRANSLATE_Y = new CssSizeStyleableKey("labelTranslationY", CssSize.ZERO);
    public static final CssSizeStyleableKey LABEL_TRANSLATE_X = new CssSizeStyleableKey("labelTranslationX", CssSize.ZERO);
    /**
     * The position relative to the parent (respectively the offset).
     */
    public static final CssPoint2DStyleableMapAccessor LABEL_TRANSLATE = new CssPoint2DStyleableMapAccessor("labelTranslation", LABEL_TRANSLATE_X, LABEL_TRANSLATE_Y);
    private final ReadOnlyBooleanWrapper connected = new ReadOnlyBooleanWrapper();

    public AbstractLabelConnectionFigure() {
    }

    @Override
    protected <T> void onPropertyChanged(Key<T> key, @Nullable T oldValue, @Nullable T newValue, boolean wasAdded, boolean wasRemoved) {
        if (key == LABEL_TARGET) {
            if (getDrawing() != null) {
                if (oldValue != null) {
                    ((Figure) oldValue).getLayoutObservers().remove(this);
                }
                if (newValue != null) {
                    ((Figure) newValue).getLayoutObservers().add(this);
                }
            }
            updateConnectedProperty();
        } else if (key == LABEL_CONNECTOR) {
            updateConnectedProperty();
        }
        super.onPropertyChanged(key, oldValue, newValue, wasAdded, wasRemoved);
    }

    @Override
    public void doAddedToDrawing(final Drawing drawing) {
        final Figure labelTarget = get(LABEL_TARGET);
        if (labelTarget != null) {
            labelTarget.getLayoutObservers().add(this);
        }
    }

    @Override
    protected void doRemovedFromDrawing(final Drawing drawing) {
        final Figure labelTarget = get(LABEL_TARGET);
        if (labelTarget != null) {
            labelTarget.getLayoutObservers().remove(this);
        }
    }

    private void updateConnectedProperty() {
        connected.set(get(LABEL_CONNECTOR) != null
                && get(LABEL_TARGET) != null);
    }

    /**
     * This property is true when the figure is connected.
     *
     * @return the connected property
     */
    public ReadOnlyBooleanProperty connectedProperty() {
        return connected.getReadOnlyProperty();
    }

    @Override
    public void createHandles(HandleType handleType, List<Handle> list) {
        if (handleType == HandleType.MOVE) {
            list.add(new BoundsInLocalOutlineHandle(this));
            if (get(LABEL_CONNECTOR) == null) {
                list.add(new MoveHandle(this, BoundsLocator.NORTH_EAST));
                list.add(new MoveHandle(this, BoundsLocator.NORTH_WEST));
                list.add(new MoveHandle(this, BoundsLocator.SOUTH_EAST));
                list.add(new MoveHandle(this, BoundsLocator.SOUTH_WEST));
            }
        } else if (handleType == HandleType.RESIZE) {
            list.add(new BoundsInLocalOutlineHandle(this));
            list.add(new LabelConnectorHandle(this, ORIGIN, LABELED_LOCATION, LABEL_CONNECTOR, LABEL_TARGET));
        } else if (handleType == HandleType.POINT) {
            list.add(new BoundsInLocalOutlineHandle(this));
            list.add(new LabelConnectorHandle(this, ORIGIN, LABELED_LOCATION, LABEL_CONNECTOR, LABEL_TARGET));
        } else {
            super.createHandles(handleType, list);
        }
    }

    /**
     * Returns all figures which are connected by this figure - they provide to
     * the layout of this figure.
     *
     * @return a list of connected figures
     */
    @Override
    public ReadableSet<Figure> getLayoutSubjects() {
        final Figure labelTarget = get(LABEL_TARGET);
        return labelTarget == null ? ChampSet.of() : ChampSet.of(labelTarget);
    }

    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public boolean isGroupReshapeableWith(Set<Figure> others) {
        for (Figure f : getLayoutSubjects()) {
            if (others.contains(f)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isLayoutable() {
        return true;
    }

    @Override
    public void layout(final RenderContext ctx) {
        layoutOrigin(ctx);
        super.layout(ctx);
    }

    /**
     * If the label has a {@link #LABEL_TARGET} and a {@link #LABEL_CONNECTOR},
     * computes the {@link #LABELED_LOCATION}, {@link #ORIGIN} and
     * {@link #TRANSFORMS} of the label.
     * <p>
     * Else leaves the {@link #ORIGIN} and the {@link #TRANSFORMS} unchanged.
     * <p>
     * The following properties affect the result:
     * {@link #LABEL_OFFSET}, {@link #LABEL_AUTOROTATE}.
     *
     * @param ctx
     */
    protected void layoutOrigin(final RenderContext ctx) {
        final Figure labelTarget = get(LABEL_TARGET);
        final Connector labelConnector = get(LABEL_CONNECTOR);

        if (labelConnector == null || labelTarget == null) {
            return;
        }
        final UnitConverter units = ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY);

        final PointAndDerivative pointAndDerivative = labelConnector.getPointAndDerivativeInWorld(this, labelTarget);
        final Point2D labeledLoc = worldToParent(pointAndDerivative.getPoint(Point2D::new));
        final Point2D derivative = getWorldToParent().deltaTransform(pointAndDerivative.getDerivative(Point2D::new)).normalize();
        final Point2D perp = FXGeom.perp(derivative);

        final double labelOffsetX = getStyledNonNull(LABEL_OFFSET_X).getConvertedValue(units);
        final double labelOffsetY = getStyledNonNull(LABEL_OFFSET_Y).getConvertedValue(units);

        Point2D origin = labeledLoc
                .add(perp.multiply(-labelOffsetY))
                .add(derivative.multiply(labelOffsetX));

        Rotate rotate = null;
        final boolean layoutTransforms;
        switch (getStyledNonNull(LABEL_AUTOROTATE)) {
            case FULL: {// the label follows the rotation of its target figure in the full circle: 0..360°
                final double theta = (Math.toDegrees(Angles.atan2(derivative.getY(), derivative.getX())) + 360.0) % 360.0;
                rotate = new FXPreciseRotate(theta, origin.getX(), origin.getY());
                layoutTransforms = true;
            }
            break;
            case HALF: {// the label follows the rotation of its target figure in the half circle: -90..90°
                final double theta = (Math.toDegrees(Angles.atan2(derivative.getY(), derivative.getX())) + 360.0) % 360.0;
                final double halfTheta = theta <= 90.0 || theta > 270.0 ? theta : (theta + 180.0) % 360.0;
                rotate = new FXPreciseRotate(halfTheta, origin.getX(), origin.getY());
                layoutTransforms = true;
            }
            break;
            case OFF:
            default:
                layoutTransforms = false;
                break;
        }

        Point2D labelTranslation = getStyledNonNull(LABEL_TRANSLATE).getConvertedValue();
        origin = origin.add(labelTranslation);
        set(ORIGIN, new CssPoint2D(origin));
        set(LABELED_LOCATION, new CssPoint2D(labeledLoc));

        if (layoutTransforms) {
            final List<Transform> transforms = new ArrayList<>();
            if (!rotate.isIdentity()) {
                transforms.add(rotate);
            }
            setTransforms(transforms.toArray(new Transform[0]));
        }
    }

    @Override
    public void removeAllLayoutSubjects() {
        set(LABEL_TARGET, null);
    }

    @Override
    public void removeLayoutSubject(final Figure subject) {
        if (subject == get(LABEL_TARGET)) {
            set(LABEL_TARGET, null);
        }

    }

    @Override
    public void updateGroupNode(final RenderContext ctx, final Group node) {
        super.updateGroupNode(ctx, node);
        applyTransformableFigureProperties(ctx, node);
    }

    @Override
    public void reshapeInLocal(final CssSize x, final CssSize y, final CssSize width, final CssSize height) {
        if (get(LABEL_TARGET) == null) {
            super.reshapeInLocal(x, y, width, height);
            set(LABELED_LOCATION, getNonNull(ORIGIN));
            set(LABEL_TRANSLATE, new CssPoint2D(0, 0));
        } else {
            final CssRectangle2D bounds = getCssLayoutBounds();
            final CssPoint2D oldValue = getNonNull(LABEL_TRANSLATE);
            set(LABEL_TRANSLATE,
                    new CssPoint2D(x.subtract(bounds.getMinX()).add(oldValue.getX()),
                            y.subtract(bounds.getMinY()).add(oldValue.getY())));
        }
    }

    @Override
    public void translateInLocal(CssPoint2D delta) {
        if (get(LABEL_TARGET) == null) {
            super.translateInLocal(delta);
            set(LABELED_LOCATION, getNonNull(ORIGIN));
            set(LABEL_TRANSLATE, new CssPoint2D(0, 0));
        } else {
            CssPoint2D oldValue = getNonNull(LABEL_TRANSLATE);
            set(LABEL_TRANSLATE, oldValue.add(delta));
        }
    }

    public void setLabelConnection(final @Nullable Figure target, final @Nullable Connector connector) {
        set(LABEL_CONNECTOR, connector);
        set(LABEL_TARGET, target);
    }
}
