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
import org.jhotdraw8.css.converter.DoubleCssConverter;
import org.jhotdraw8.css.value.CssPoint2D;
import org.jhotdraw8.draw.css.converter.Translate3DCssConverter;
import org.jhotdraw8.draw.key.CssPoint2DStyleableKey;
import org.jhotdraw8.draw.key.DoubleStyleableKey;
import org.jhotdraw8.draw.key.NonNullObjectStyleableKey;
import org.jhotdraw8.draw.key.Point3DStyleableMapAccessor;
import org.jhotdraw8.draw.key.Scale3DStyleableMapAccessor;
import org.jhotdraw8.draw.key.TransformListStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.SimpleNonNullKey;
import org.jhotdraw8.geom.FXPreciseRotate;
import org.jhotdraw8.geom.FXRectangles;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.NonNull;

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
 */
public interface TransformableFigure extends TransformCachingFigure, Figure {
    boolean CACHE = true;
    /**
     * Defines the angle of rotation around the rotation pivot of the figure in degrees.
     * <p>
     * Default value: {@code 0}.
     */
    NonNullObjectStyleableKey<Double> ROTATE = new NonNullObjectStyleableKey<>("rotate", Double.class, new DoubleCssConverter(false), 0.0,
            VectorList.of("0", "45", "90", "135", "180", "225", "270", "315"));
    /**
     * Defines the pivot of the rotation.
     * <p>
     * Default value: {@code 0.5, 0.5}.
     */
    CssPoint2DStyleableKey ROTATION_PIVOT = new CssPoint2DStyleableKey("rotation-pivot", new CssPoint2D(0.5, 0.5));
    /**
     * Defines the rotation axis used.
     * <p>
     * Default value: {@code Rotate.Z_AXIS}.
     */
    SimpleNonNullKey<Point3D> ROTATION_AXIS = new SimpleNonNullKey<>("rotationAxis", Point3D.class, Rotate.Z_AXIS);
    /**
     * Defines the scale factor by which coordinates are scaled on the x axis
     * about the center of the figure. Default value: {@code 1}.
     */
    DoubleStyleableKey SCALE_X = new DoubleStyleableKey("scaleX", 1.0);
    /**
     * Defines the scale factor by which coordinates are scaled on the y axis
     * about the center of the figure. Default value: {@code 1}.
     */
    DoubleStyleableKey SCALE_Y = new DoubleStyleableKey("scaleY", 1.0);
    /**
     * Defines the scale factor by which coordinates are scaled on the z axis
     * about the center of the figure. Default value: {@code 1}.
     */
    DoubleStyleableKey SCALE_Z = new DoubleStyleableKey("scaleZ", 1.0);
    /**
     * Defines the scale factor by which coordinates are scaled on the axes
     * about the center of the figure.
     */
    Scale3DStyleableMapAccessor SCALE = new Scale3DStyleableMapAccessor("scale", SCALE_X, SCALE_Y, SCALE_Z);
    TransformListStyleableKey TRANSFORMS = new TransformListStyleableKey("transform", VectorList.of());
    /**
     * Defines the translation on the x axis about the center of the figure.
     * Default value: {@code 0}.
     */
    DoubleStyleableKey TRANSLATE_X = new DoubleStyleableKey("translateX", 0.0);
    /**
     * Defines the translation on the y axis about the center of the figure.
     * Default value: {@code 0}.
     */
    DoubleStyleableKey TRANSLATE_Y = new DoubleStyleableKey("translateY", 0.0);
    /**
     * Defines the translation on the z axis about the center of the figure.
     * Default value: {@code 0}.
     */
    DoubleStyleableKey TRANSLATE_Z = new DoubleStyleableKey("translateZ", 0.0);
    /**
     * Defines the translation on the axes about the center of the
     * figure.
     */
    Point3DStyleableMapAccessor TRANSLATE = new Point3DStyleableMapAccessor("translate", TRANSLATE_X, TRANSLATE_Y, TRANSLATE_Z, new Translate3DCssConverter(false));

    static Set<Key<?>> getDeclaredKeys() {
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
    default void applyTransformableFigureProperties(RenderContext ctx, Node node) {
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


    default Transform getInverseTransform(boolean styled) {
        PersistentList<Transform> list = styled ? getStyledNonNull(TRANSFORMS) : getNonNull(TRANSFORMS);
        Transform t = null;
        try {
            for (Transform tx : list.readOnlyReversed()) {
                t = FXTransforms.concat(t, tx.createInverse());
            }
        } catch (NonInvertibleTransformException e) {
            t = null;
        }
        return t == null ? FXTransforms.IDENTITY : t;
    }

    @Override
    default Transform getLocalToParent() {
        return getLocalToParent(true);
    }

    default Transform getLocalToParent(boolean styled) {
        Transform l2p = CACHE && styled ? getCachedLocalToParent() : null;
        if (l2p == null) {
            l2p = computeLocalToParent(styled);
            if (CACHE && styled) {
                setCachedLocalToParent(l2p);
            }
        }
        return l2p;
    }

    private @NonNull Transform computeLocalToParent(boolean styled) {
        Transform l2p = null;
        final Bounds layoutBounds = getLayoutBounds();
        Point2D center = FXRectangles.center(layoutBounds);

        double sx = styled ? getStyledNonNull(SCALE_X) : getNonNull(SCALE_X);
        double sy = styled ? getStyledNonNull(SCALE_Y) : getNonNull(SCALE_Y);
        double r = styled ? getStyledNonNull(ROTATE) : getNonNull(ROTATE);
        double tx = styled ? getStyledNonNull(TRANSLATE_X) : getNonNull(TRANSLATE_X);
        double ty = styled ? getStyledNonNull(TRANSLATE_Y) : getNonNull(TRANSLATE_Y);

        Transform transform = getTransform(styled);
        if (tx != 0.0 || ty != 0.0) {
            Translate tt = new Translate(tx, ty);
            l2p = FXTransforms.concat(null, tt);
        }
        if (r != 0) {
            CssPoint2D cssPivot = getStyledNonNull(ROTATION_PIVOT);
            Point2D pivot = CssPoint2D.getPointInBounds(cssPivot, layoutBounds);
            Point2D transformedPivot = transform.transform(pivot);
            Rotate tr = new FXPreciseRotate(r, transformedPivot.getX(), transformedPivot.getY());
            l2p = FXTransforms.concat(l2p, tr);
        }
        if ((sx != 1.0 || sy != 1.0) && sx != 0.0 && sy != 0.0) {// check for 0.0 avoids creating a non-invertible transform
            Scale ts = new Scale(sx, sy, center.getX(), center.getY());
            l2p = FXTransforms.concat(l2p, ts);
        }
        if (!transform.isIdentity()) {
            l2p = FXTransforms.concat(l2p, transform);
        }
        if (l2p == null) {
            l2p = FXTransforms.IDENTITY;
        }
        return l2p;
    }


    @Override
    default Transform getParentToLocal() {
        return getParentToLocal(true);
    }


    default Transform getParentToLocal(boolean styled) {
        Transform p2l = CACHE && styled ? getCachedParentToLocal() : null;
        if (p2l == null) {
            try {
                p2l = getLocalToParent(styled).createInverse();
            } catch (NonInvertibleTransformException e) {
                p2l = null;
            }
            if (CACHE && styled) {
                setCachedParentToLocal(p2l);
            }
        }
        return p2l;
    }

    default Transform computeParentToLocal(boolean styled) {
        Transform p2l = null;
        final Bounds layoutBounds = getLayoutBounds();
        Point2D center = FXRectangles.center(layoutBounds);

        double sx = styled ? getStyledNonNull(SCALE_X) : getNonNull(SCALE_X);
        double sy = styled ? getStyledNonNull(SCALE_Y) : getNonNull(SCALE_Y);
        double r = styled ? getStyledNonNull(ROTATE) : getNonNull(ROTATE);
        double tx = styled ? getStyledNonNull(TRANSLATE_X) : getNonNull(TRANSLATE_X);
        double ty = styled ? getStyledNonNull(TRANSLATE_Y) : getNonNull(TRANSLATE_Y);

        Transform transform = getTransform(styled);
        if (tx != 0.0 || ty != 0.0) {
            Translate tt = new Translate(-tx, -ty);
            p2l = tt;
        }
        if (r != 0) {
            CssPoint2D cssPivot = getStyledNonNull(ROTATION_PIVOT);
            Point2D pivot = CssPoint2D.getPointInBounds(cssPivot, layoutBounds);
            Point2D transformedPivot = transform.transform(pivot);
            Rotate tr = new FXPreciseRotate(-r, transformedPivot.getX(), transformedPivot.getY());
            p2l = FXTransforms.concat(tr, p2l);
        }
        if ((sx != 1.0 || sy != 1.0) && sx != 0.0 && sy != 0.0) {// check for 0.0 avoids creating a non-invertible transform
            Scale ts = new Scale(1 / sx, 1 / sy, center.getX(), center.getY());
            p2l = FXTransforms.concat(ts, p2l);
        }
        if (!transform.isIdentity()) {
            try {
                p2l = FXTransforms.concat(transform.createInverse(), p2l);
            } catch (NonInvertibleTransformException e) {
                // bail
            }
        }
        if (p2l == null) {
            p2l = FXTransforms.IDENTITY;
        }
        return p2l;
    }


    /**
     * Gets the {@link #TRANSFORMS} flattened into a single transform.
     *
     * @return the flattened transforms
     */
    default Transform getTransform(boolean styled) {
        PersistentList<Transform> list = styled ? getStyledNonNull(TRANSFORMS) : getNonNull(TRANSFORMS);
        Transform t = null;
        for (Transform tx : list) {
            t = FXTransforms.concat(t, tx);
        }
        return t == null ? FXTransforms.IDENTITY : t;
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
            PersistentList<Transform> ts = getNonNull(TRANSFORMS);
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
    default void reshapeInParent(Transform transform) {
        if (transform instanceof Translate) {
            Point2D p = FXTransforms.deltaTransform(getInverseTransform(true), transform.getTx(), transform.getTy());
            reshapeInLocal(new Translate(p.getX(), p.getY()));
        } else {
            // FIXME we do not want to reshape!
            Transform combined = FXTransforms.concat(transform, getTransform(true));
            set(TRANSFORMS, VectorList.of(combined));
        }
    }

    /**
     * Convenience method for setting a new value for the {@link #TRANSFORMS}
     * property.
     *
     * @param transforms new value
     */
    default void setTransforms(Transform... transforms) {
        if (transforms.length == 1 && transforms[0].isIdentity()) {
            set(TRANSFORMS, VectorList.of());
        } else {
            set(TRANSFORMS, VectorList.of(transforms));
        }
    }

    @Override
    default void transformInLocal(Transform t) {
        flattenTransforms();
        PersistentList<Transform> transforms = getNonNull(TRANSFORMS);
        if (transforms.isEmpty()) {
            set(TRANSFORMS, VectorList.of(t));
        } else {
            set(TRANSFORMS, transforms.add(t));
        }
    }

    @Override
    default void transformInParent(Transform t) {
        if (t.isIdentity()) {
            return;
        }
        if (t instanceof Translate tr) {
            flattenTransforms();
            PersistentList<Transform> transforms = getNonNull(TRANSFORMS);
            if (transforms.isEmpty()) {
                translateInLocal(new CssPoint2D(tr.getTx(), tr.getTy()));
            } else {
                set(TRANSLATE_X, getNonNull(TRANSLATE_X) + tr.getTx());
                set(TRANSLATE_Y, getNonNull(TRANSLATE_Y) + tr.getTy());
            }
        } else {
            flattenTransforms();
            PersistentList<Transform> transforms = getNonNull(TRANSFORMS);
            if (transforms.isEmpty()) {
                set(TRANSFORMS, VectorList.of(t));
            } else {
                set(TRANSFORMS, transforms.set(0, t));
            }
        }
    }


}
