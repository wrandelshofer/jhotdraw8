/*
 * @(#)TransformableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.css.converter.Translate3DCssConverter;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.key.CssPoint2DStyleableKey;
import org.jhotdraw8.draw.key.DoubleStyleableKey;
import org.jhotdraw8.draw.key.Point3DStyleableMapAccessor;
import org.jhotdraw8.draw.key.Scale3DStyleableMapAccessor;
import org.jhotdraw8.draw.key.TransformListStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NonNullObjectKey;
import org.jhotdraw8.geom.FXPreciseRotate;
import org.jhotdraw8.geom.FXRectangles;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.SequencedSet;
import java.util.Set;

/**
 * A transformable figure supports the transformation of a figure.
 * <p>
 * The following transformations are supported:
 * <ul>
 * <li>Translation of the local bounds of the figure.</li>
 * <li>Rotation around the center of the untransformed local bounds
 * of the figure.</li>
 * <li>Scaling around the center of the untransformed local bounds
 * of the figure.</li>
 * <li>Arbitrary sequence of affine transformations of the
 * figure.</li>
 * </ul>
 * Note that transformation matrices computed from the Rotation and Scaling must
 * be recomputed every time when the local bounds of the figure
 * change.
 *
 * @author Werner Randelshofer
 */
public interface TransformableFigure extends TransformCachingFigure, Figure {
    boolean CACHE = true;
    /**
     * Defines the angle of rotation around the rotation pivot of the figure in degrees.
     * <p>
     * Default value: {@code 0}.
     */
    @NonNull
    DoubleStyleableKey ROTATE = new DoubleStyleableKey("rotate", 0.0);
    /**
     * Defines the pivot of the rotation.
     * <p>
     * Default value: {@code 0.5, 0.5}.
     */
    @NonNull
    CssPoint2DStyleableKey ROTATION_PIVOT = new CssPoint2DStyleableKey("rotation-pivot", new CssPoint2D(0.5, 0.5));
    /**
     * Defines the rotation axis used.
     * <p>
     * Default value: {@code Rotate.Z_AXIS}.
     */
    @NonNull
    NonNullObjectKey<Point3D> ROTATION_AXIS = new NonNullObjectKey<>("rotationAxis", Point3D.class, Rotate.Z_AXIS);
    /**
     * Defines the scale factor by which coordinates are scaled on the x axis
     * about the center of the figure. Default value: {@code 1}.
     */
    @NonNull
    DoubleStyleableKey SCALE_X = new DoubleStyleableKey("scaleX", 1.0);
    /**
     * Defines the scale factor by which coordinates are scaled on the y axis
     * about the center of the figure. Default value: {@code 1}.
     */
    @NonNull
    DoubleStyleableKey SCALE_Y = new DoubleStyleableKey("scaleY", 1.0);
    /**
     * Defines the scale factor by which coordinates are scaled on the z axis
     * about the center of the figure. Default value: {@code 1}.
     */
    @NonNull
    DoubleStyleableKey SCALE_Z = new DoubleStyleableKey("scaleZ", 1.0);
    /**
     * Defines the scale factor by which coordinates are scaled on the axes
     * about the center of the figure.
     */
    @NonNull
    Scale3DStyleableMapAccessor SCALE = new Scale3DStyleableMapAccessor("scale", SCALE_X, SCALE_Y, SCALE_Z);
    @NonNull
    TransformListStyleableKey TRANSFORMS = new TransformListStyleableKey("transform", VectorList.of());
    /**
     * Defines the translation on the x axis about the center of the figure.
     * Default value: {@code 0}.
     */
    @NonNull
    DoubleStyleableKey TRANSLATE_X = new DoubleStyleableKey("translateX", 0.0);
    /**
     * Defines the translation on the y axis about the center of the figure.
     * Default value: {@code 0}.
     */
    @NonNull
    DoubleStyleableKey TRANSLATE_Y = new DoubleStyleableKey("translateY", 0.0);
    /**
     * Defines the translation on the z axis about the center of the figure.
     * Default value: {@code 0}.
     */
    @NonNull
    DoubleStyleableKey TRANSLATE_Z = new DoubleStyleableKey("translateZ", 0.0);
    /**
     * Defines the translation on the axes about the center of the
     * figure.
     */
    @NonNull
    Point3DStyleableMapAccessor TRANSLATE = new Point3DStyleableMapAccessor("translate", TRANSLATE_X, TRANSLATE_Y, TRANSLATE_Z, new Translate3DCssConverter(false));

    static @NonNull Set<Key<?>> getDeclaredKeys() {
        SequencedSet<Key<?>> keys = new LinkedHashSet<>();
        Figure.getDeclaredKeys(TransformableFigure.class, keys);
        return keys;
    }

    /**
     * Updates a figure node with all transformation properties defined in this
     * interface.
     * <p>
     * Applies the following properties: {@code TRANSFORM}, translation
     * {@code TRANSLATE_X}, {@code TRANSLATE_Y}, {@code TRANSLATE_Z}, scale
     * {@code SCALE_X}, {@code SCALE_Y}, {@code SCALE_Z}, and rotation
     * {@code ROTATE}, {@code ROTATION_AXIS}.
     * <p>
     * This method is intended to be used by {@link #updateNode}.
     *
     * @param ctx  the render context
     * @param node a node which was created with method {@link #createNode}.
     */
    default void applyTransformableFigureProperties(@NonNull RenderContext ctx, @NonNull Node node) {
        Transform t = getLocalToParent();
        List<Transform> transforms = node.getTransforms();
        if (t.isIdentity()) {
            if (!transforms.isEmpty()) {
                transforms.clear();
            }
        } else if (transforms.size() == 1) {
            if (!Objects.equals(transforms.getFirst(), t)) {
                transforms.set(0, t);
            }
        } else {
            transforms.clear();
            transforms.add(t);
        }
    }

    default void clearTransforms() {
        remove(SCALE_X);
        remove(SCALE_Y);
        remove(ROTATE);
        remove(TRANSLATE_X);
        remove(TRANSLATE_Y);
        remove(TRANSFORMS);
    }

    default void flattenTransforms() {
        Transform p2l = getLocalToParent(false);
        remove(SCALE_X);
        remove(SCALE_Y);
        remove(ROTATE);
        remove(TRANSLATE_X);
        remove(TRANSLATE_Y);
        if (p2l.isIdentity()) {
            remove(TRANSFORMS);
        } else {
            set(TRANSFORMS, VectorList.of(p2l));
        }
    }


    default @NonNull Transform getInverseTransform() {
        ImmutableList<Transform> list = getStyledNonNull(TRANSFORMS);
        Transform t;
        if (list.isEmpty()) {
            t = FXTransforms.IDENTITY;
        } else {
            try {
                t = list.get(list.size() - 1).createInverse();
                for (int i = list.size() - 2; i >= 0; i--) {
                    t = FXTransforms.concat(t, list.get(i).createInverse());
                }
            } catch (NonInvertibleTransformException e) {
                throw new InternalError(e);
            }
        }
        return t;
    }

    @Override
    default @NonNull Transform getLocalToParent() {
        return getLocalToParent(true);
    }

    default @NonNull Transform getLocalToParent(boolean styled) {
        Transform l2p = CACHE && styled ? getCachedLocalToParent() : null;
        if (l2p == null) {
            final Bounds layoutBounds = getLayoutBounds();
            Point2D center = FXRectangles.center(layoutBounds);

            ImmutableList<Transform> transforms = styled ? getStyled(TRANSFORMS) : get(TRANSFORMS);
            double sx = styled ? getStyledNonNull(SCALE_X) : getNonNull(SCALE_X);
            double sy = styled ? getStyledNonNull(SCALE_Y) : getNonNull(SCALE_Y);
            double r = styled ? getStyledNonNull(ROTATE) : getNonNull(ROTATE);
            double tx = styled ? getStyledNonNull(TRANSLATE_X) : getNonNull(TRANSLATE_X);
            double ty = styled ? getStyledNonNull(TRANSLATE_Y) : getNonNull(TRANSLATE_Y);

            if (tx != 0.0 || ty != 0.0) {
                Translate tt = new Translate(tx, ty);
                l2p = FXTransforms.concat(null, tt);
            }
            if (r != 0) {
                CssPoint2D cssPivot = getStyledNonNull(ROTATION_PIVOT);
                Point2D pivot = CssPoint2D.getPointInBounds(cssPivot, layoutBounds);
                Rotate tr = new FXPreciseRotate(r, pivot.getX(), pivot.getY());
                l2p = FXTransforms.concat(l2p, tr);
            }
            if ((sx != 1.0 || sy != 1.0) && sx != 0.0 && sy != 0.0) {// check for 0.0 avoids creating a non-invertible transform
                Scale ts = new Scale(sx, sy, center.getX(), center.getY());
                l2p = FXTransforms.concat(l2p, ts);
            }
            if (transforms != null && !transforms.isEmpty()) {
                l2p = FXTransforms.concat(l2p, getTransform());
            }
            if (l2p == null) {
                l2p = FXTransforms.IDENTITY;
            }
            if (CACHE && styled) {
                setCachedLocalToParent(l2p);
            }
        }
        return l2p;
    }

    default @NonNull List<Transform> getLocalToParentAsList(boolean styled) {
        ArrayList<Transform> list = new ArrayList<>();

        Point2D center = getCenterInLocal();

        ImmutableList<Transform> t = styled ? getStyledNonNull(TRANSFORMS) : getNonNull(TRANSFORMS);
        double sx = styled ? getStyledNonNull(SCALE_X) : getNonNull(SCALE_X);
        double sy = styled ? getStyledNonNull(SCALE_Y) : getNonNull(SCALE_Y);
        double r = styled ? getStyledNonNull(ROTATE) : getNonNull(ROTATE);
        double tx = styled ? getStyledNonNull(TRANSLATE_X) : getNonNull(TRANSLATE_X);
        double ty = styled ? getStyledNonNull(TRANSLATE_Y) : getNonNull(TRANSLATE_Y);

        if (tx != 0.0 || ty != 0.0) {
            Translate tt = new Translate(tx, ty);
            list.add(tt);
        }
        if (r != 0) {
            Rotate tr = new FXPreciseRotate(r, center.getX(), center.getY());
            list.add(tr);
        }
        if ((sx != 1.0 || sy != 1.0) && sx != 0.0 && sy != 0.0) {// check for 0.0 avoids creating a non-invertible transform
            Scale ts = new Scale(sx, sy, center.getX(), center.getY());
            list.add(ts);
        }
        if (!t.isEmpty()) {
            list.addAll(t.asList());
        }
        return list;
    }


    @Override
    default @NonNull Transform getParentToLocal() {
        return getParentToLocal(true);
    }


    default @NonNull Transform getParentToLocal(boolean styled) {
        Transform p2l = CACHE ? getCachedParentToLocal() : null;
        if (p2l == null) {
            Point2D center = getCenterInLocal();

            ImmutableList<Transform> transforms = styled ? getStyledNonNull(TRANSFORMS) : getNonNull(TRANSFORMS);
            double sx = styled ? getStyledNonNull(SCALE_X) : getNonNull(SCALE_X);
            double sy = styled ? getStyledNonNull(SCALE_Y) : getNonNull(SCALE_Y);
            double r = styled ? getStyledNonNull(ROTATE) : getNonNull(ROTATE);
            double tx = styled ? getStyledNonNull(TRANSLATE_X) : getNonNull(TRANSLATE_X);
            double ty = styled ? getStyledNonNull(TRANSLATE_Y) : getNonNull(TRANSLATE_Y);

            if (!transforms.isEmpty()) {
                p2l = getInverseTransform();
            }
            if ((sx != 1.0 || sy != 1.0) && sx != 0.0 && sy != 0.0) {// check for 0.0 avoids creating a non-invertible transform
                Scale ts = new Scale(1.0 / sx, 1.0 / sy, center.getX(), center.getY());
                p2l = FXTransforms.concat(p2l, ts);
            }
            if (r != 0) {
                Rotate tr = new FXPreciseRotate(-r, center.getX(), center.getY());
                p2l = FXTransforms.concat(p2l, tr);
            }
            if (tx != 0.0 || ty != 0.0) {
                Translate tt = new Translate(-tx, -ty);
                p2l = FXTransforms.concat(p2l, tt);
            }
            if (p2l == null) {
                p2l = FXTransforms.IDENTITY;
            }
            if (CACHE) {
                setCachedParentToLocal(p2l);
            }
        }
        return p2l;
    }


    /**
     * Gets the {@link #TRANSFORMS} flattened into a single transform.
     *
     * @return the flattened transforms
     */
    default @NonNull Transform getTransform() {
        ImmutableList<Transform> list = getStyledNonNull(TRANSFORMS);
        Transform t;
        if (list.isEmpty()) {
            t = FXTransforms.IDENTITY;
        } else {
            t = list.get(0);
            for (int i = 1, n = list.size(); i < n; i++) {
                t = FXTransforms.concat(t, list.get(i));
            }
        }
        return t;
    }

    default boolean hasCenterTransforms() {
        double sx = getStyledNonNull(SCALE_X);
        double sy = getStyledNonNull(SCALE_Y);
        double r = getStyledNonNull(ROTATE);
        double tx = getStyledNonNull(TRANSLATE_X);
        double ty = getStyledNonNull(TRANSLATE_Y);
        return sx != 1 || sy != 1 || r != 0 || tx != 0 || ty != 0;
    }

    @Override
    default void reshapeInLocal(Transform transform) {
        if (hasCenterTransforms() && !(transform instanceof Translate)) {
            ImmutableList<Transform> ts = getNonNull(TRANSFORMS);
            if (ts.isEmpty()) {
                set(TRANSFORMS, VectorList.of(transform));
            } else {
                int last = ts.size() - 1;
                Transform concatenatedWithLast = FXTransforms.concat(ts.get(last), transform);
                if (concatenatedWithLast instanceof Affine) {
                    set(TRANSFORMS, ts.add(transform));
                } else {
                    set(TRANSFORMS, ts.set(last, concatenatedWithLast));
                }
            }
            return;
        }

        Bounds b = getLayoutBounds();
        b = transform.transform(b);
        reshapeInLocal(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
    }


    @Override
    default void reshapeInParent(@NonNull Transform transform) {
        if (transform instanceof Translate) {
            Point2D p = FXTransforms.deltaTransform(getInverseTransform(), transform.getTx(), transform.getTy());
            reshapeInLocal(new Translate(p.getX(), p.getY()));
        } else {
            // FIXME we do not want to svgStringReshapeToBuilder!
            Transform combined = FXTransforms.concat(transform, getTransform());
            set(TRANSFORMS, VectorList.of(combined));
        }
    }

    /**
     * Convenience method for setting a new value for the {@link #TRANSFORMS}
     * property.
     *
     * @param transforms new value
     */
    default void setTransforms(@NonNull Transform... transforms) {
        if (transforms.length == 1 && transforms[0].isIdentity()) {
            set(TRANSFORMS, VectorList.of());
        } else {
            set(TRANSFORMS, VectorList.of(transforms));
        }
    }

    @Override
    default void transformInLocal(@NonNull Transform t) {
        flattenTransforms();
        ImmutableList<Transform> transforms = getNonNull(TRANSFORMS);
        if (transforms.isEmpty()) {
            set(TRANSFORMS, VectorList.of(t));
        } else {
            set(TRANSFORMS, transforms.add(t));
        }
    }

    @Override
    default void transformInParent(@NonNull Transform t) {
        if (t.isIdentity()) {
            return;
        }
        if (t instanceof Translate tr) {
            flattenTransforms();
            ImmutableList<Transform> transforms = getNonNull(TRANSFORMS);
            if (transforms.isEmpty()) {
                translateInLocal(new CssPoint2D(tr.getTx(), tr.getTy()));
            } else {
                set(TRANSLATE_X, getNonNull(TRANSLATE_X) + tr.getTx());
                set(TRANSLATE_Y, getNonNull(TRANSLATE_Y) + tr.getTy());
            }
        } else {
            flattenTransforms();
            ImmutableList<Transform> transforms = getNonNull(TRANSFORMS);
            if (transforms.isEmpty()) {
                set(TRANSFORMS, VectorList.of(t));
            } else {
                set(TRANSFORMS, transforms.set(0, t));
            }
        }
    }


}
