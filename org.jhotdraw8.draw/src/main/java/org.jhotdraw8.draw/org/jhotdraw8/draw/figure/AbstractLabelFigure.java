/*
 * @(#)AbstractLabelFigure.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.css.CssColor;
import org.jhotdraw8.css.CssDimension2D;
import org.jhotdraw8.css.CssPoint2D;
import org.jhotdraw8.css.CssRectangle2D;
import org.jhotdraw8.css.CssSize;
import org.jhotdraw8.css.Paintable;
import org.jhotdraw8.css.UnitConverter;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.connector.RectangleConnector;
import org.jhotdraw8.draw.key.CssDimension2DStyleableKey;
import org.jhotdraw8.draw.key.CssPoint2DStyleableMapAccessor;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.DoubleStyleableKey;
import org.jhotdraw8.draw.key.EnumStyleableKey;
import org.jhotdraw8.draw.key.NullableFXPathElementsStyleableKey;
import org.jhotdraw8.draw.key.NullablePaintableStyleableKey;
import org.jhotdraw8.draw.locator.BoundsLocator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.FXPreciseRotate;
import org.jhotdraw8.geom.FXShapes;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A Label that can be placed anywhere on a drawing.
 * <p>
 * Label: The layout bounds of the label are controlled by
 * {@link FillableFigure}, {@link StrokableFigure}, {@link ShapeableFigure}.
 * Other figures can get its path with {@link PathIterableFigure}. The label
 * box is connectable by means of {@link ConnectableFigure}.
 * <pre>
 * +---------------+
 * | layout bounds |
 * +---------------+
 * </pre>
 * Layout bounds: The layout bounds consist of a content box with padding around it.
 * <pre>
 * +-------------------+
 * | padding           |
 * | +---------------+ |
 * | | content box   | |
 * | +---------------+ |
 * +-------------------+
 * </pre>
 * Content box: The content is controlled by {@link #ICON_POSITION},
 * {@link #ICON_SIZE}, {@link #ICON_TEXT_GAP}.
 * <pre>
 * +------+  +---------------+  +---------------+
 * | text |  | icon gap text |  | text gap icon |
 * +------+  +---------------+  +---------------+
 * </pre>
 * <p>
 * The placement of the label is controlled by {@link #ORIGIN},
 * {@link TextLayoutableFigure#TEXT_VPOS}, {@link #TEXT_HPOS}.
 * Note that the placement affects the content box.
 * <pre>
 * text-hpos: left;  ┆ center;            ┆ right;
 *                   ┆                    ┆
 * x                 ┆       x            ┆               x
 * +-------------+   ┆ +-------------+    ┆ +-------------+
 * | content box |   ┆ | content box |    ┆ | content box |
 * +-------------+   ┆ +-------------+    ┆ +-------------+
 * </pre>
 * <pre>
 * text-vpos: top; ┆ center;        ┆ baseline;     ┆ bottom;
 *                 ┆                ┆               ┆
 * y +---------+   ┆   +---------+  ┆   +---------+ ┆   +---------+
 *   | content |   ┆ y | content |  ┆ y_| content | ┆   | content |
 *   | box     |   ┆   | box     |  ┆   | box     | ┆   | box     |
 *   +---------+   ┆   +---------+  ┆   +---------+ ┆ y +---------+
 * </pre>
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractLabelFigure extends AbstractLeafFigure
        implements TextFillableFigure, FillableFigure, StrokableFigure,
        TextFontableFigure, TextLayoutableFigure, ConnectableFigure, PathIterableFigure, ShapeableFigure,
        PaddableFigure {

    public static final @NonNull CssSizeStyleableKey ORIGIN_X = new CssSizeStyleableKey("originX", CssSize.ZERO);
    public static final @NonNull CssSizeStyleableKey ORIGIN_Y = new CssSizeStyleableKey("originY", CssSize.ZERO);
    public static final @NonNull CssPoint2DStyleableMapAccessor ORIGIN = new CssPoint2DStyleableMapAccessor("origin", ORIGIN_X, ORIGIN_Y);
    public static final @NonNull NullableFXPathElementsStyleableKey ICON_SHAPE = new NullableFXPathElementsStyleableKey("iconShape", null);
    public static final @NonNull CssDimension2DStyleableKey ICON_SIZE = new CssDimension2DStyleableKey("iconSize", new CssDimension2D(16, 16));
    public static final @NonNull CssSizeStyleableKey ICON_TEXT_GAP = new CssSizeStyleableKey("iconTextGap", CssSize.from(4));
    public static final @NonNull EnumStyleableKey<IconPosition> ICON_POSITION =
            new EnumStyleableKey<>("iconPosition", IconPosition.class, IconPosition.LEFT);
    /**
     * Defines the paint used for filling the interior of the icon shape. Default
     * value: {@code Color.BLACK}.
     */
    public static final NullablePaintableStyleableKey ICON_FILL = new NullablePaintableStyleableKey("iconFill", new CssColor("canvastext", Color.BLACK));

    public static final @NonNull DoubleStyleableKey ICON_ROTATE = new DoubleStyleableKey("iconRotate", 0.0);

    public static final @NonNull CssSizeStyleableKey ICON_TRANSLATE_Y = new CssSizeStyleableKey("iconTranslateY", CssSize.ZERO);
    public static final @NonNull CssSizeStyleableKey ICON_TRANSLATE_X = new CssSizeStyleableKey("iconTranslateX", CssSize.ZERO);
    /**
     * The position relative to the parent (respectively the offset).
     */
    public static final @NonNull CssPoint2DStyleableMapAccessor ICON_TRANSLATE = new CssPoint2DStyleableMapAccessor("iconTranslate", ICON_TRANSLATE_X, ICON_TRANSLATE_Y);
    /**
     * The horizontal position of the text. Default value: {@link HPos#LEFT}.
     * FIXME Move this to {@link TextLayoutableFigure}.
     */
    public static final @NonNull EnumStyleableKey<HPos> TEXT_HPOS = new EnumStyleableKey<>("textHPos", HPos.class, HPos.LEFT);

    private @Nullable Bounds cachedLayoutBounds;

    public AbstractLabelFigure() {
        this(0, 0);
    }

    public AbstractLabelFigure(@NonNull Point2D position) {
        this(position.getX(), position.getY());
    }

    public AbstractLabelFigure(double x, double y) {
        // Performance: Only set properties if they differ from the default value.
        if (x != 0) {
            set(ORIGIN_X, CssSize.from(x));
        }
        if (y != 0) {
            set(ORIGIN_Y, CssSize.from(y));
        }
    }

    /**
     * Creates the node for this label. The node has the following structure:
     * <pre>
     * Group   holds all other elements of the label
     * . Path  the path draws the background and border of the label
     * . Text  draws the text of the label
     * . Group draws the icon of the label
     * </pre>
     *
     * @param ctx the render context
     * @return the node
     */
    @Override
    public @NonNull Node createNode(final @NonNull RenderContext ctx) {
        Group g = new Group();
        g.setManaged(false);
        g.setAutoSizeChildren(false);
        Path p = new Path();
        p.setManaged(false);
        Text text = new Text();
        text.setManaged(false);
        g.getProperties().put("pathNode", p);
        g.getProperties().put("textNode", text);
        Group ii = new Group();
        ii.setManaged(false);
        g.getProperties().put("iconNode", ii);
        return g;
    }

    @Override
    public @Nullable Connector findConnector(@NonNull Point2D p, @Nullable Figure prototype, double tolerance) {
        return new RectangleConnector(new BoundsLocator(getLayoutBounds(), p));
    }

    protected @Nullable Bounds getCachedLayoutBounds() {
        return cachedLayoutBounds;
    }

    protected void setCachedLayoutBounds(final @NonNull Bounds newValue) {
        if (!Objects.equals(cachedLayoutBounds, newValue)) {
            cachedLayoutBounds = newValue;
        }
    }

    @Override
    public @NonNull Bounds getLayoutBounds() {
        Bounds boundsInLocal = getCachedLayoutBounds();
        if (boundsInLocal == null) {
            Point2D origin = getNonNull(ORIGIN).getConvertedValue();
            return new BoundingBox(origin.getX(), origin.getY(), 0, 0);
        }
        return boundsInLocal;
    }

    @Override
    public @NonNull CssRectangle2D getCssLayoutBounds() {
        return new CssRectangle2D(getLayoutBounds());
    }

    /**
     * Returns true if this figure has an icon.
     * This method returns true if it has a non-null {@link #ICON_SHAPE}.
     * <p>
     * Subclasses can override this and
     *
     * @return
     */
    protected boolean hasIcon() {
        return getStyled(ICON_SHAPE) != null;
    }

    @Override
    public @NonNull PathIterator getPathIterator(@NonNull RenderContext ctx, @Nullable AffineTransform tx) {
        Text tn = new Text();
        tn.setX(getStyledNonNull(ORIGIN_X).getConvertedValue());
        tn.setY(getStyledNonNull(ORIGIN_Y).getConvertedValue());
        tn.setBoundsType(TextBoundsType.VISUAL);
        applyTextFontableFigureProperties(null, tn);
        applyTextLayoutableFigureProperties(null, tn);

        // We must set the font before we set the text, so that JavaFx does not need to retrieve
        // the system default font, which on Windows requires that the JavaFx Toolkit is launched.
        tn.setText(getText(null));

        return FXShapes.awtShapeFromFX(tn).getPathIterator(tx);
    }

    protected abstract @Nullable String getText(@NonNull RenderContext ctx);

    /**
     * Computes the layout bounds of this figure.
     *
     * @param ctx the render context
     */
    @Override
    public void layout(final @NonNull RenderContext ctx) {
        final Text textNode = new Text();
        updateTextNode(ctx, textNode);
        final Bounds textBounds = textNode.getLayoutBounds();
        final UnitConverter units = ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY);

        final Insets padding = getStyledNonNull(PADDING).getConvertedValue(units);
        final boolean hasIcon = hasIcon();
        final double width, height;
        if (hasIcon) {
            final CssDimension2D iconSize = getStyledNonNull(ICON_SIZE);
            final double iconWidth = iconSize.getWidth().getConvertedValue(units);
            final double gap = getStyledNonNull(ICON_TEXT_GAP).getConvertedValue(units);
            width = textBounds.getWidth() + iconWidth + gap;
            height = textBounds.getHeight();
        } else {
            width = textBounds.getWidth();
            height = textBounds.getHeight();
        }

        final double originX = getNonNull(ORIGIN_X).getConvertedValue(units);
        final double originY = getNonNull(ORIGIN_Y).getConvertedValue(units);
        final double x, y;
        switch (getStyledNonNull(TEXT_HPOS)) {
            default:
            case LEFT:
                x = originX;
                break;
            case CENTER:
                x = originX - width * 0.5;
                break;
            case RIGHT:
                x = originX - width;
                break;
        }
        switch (getStyledNonNull(TEXT_VPOS)) {
            case TOP:
                y = originY;
                break;
            case CENTER:
                y = originY - height * 0.5;
                break;
            default:
            case BASELINE:
                y = originY - textNode.getBaselineOffset();
                break;
            case BOTTOM:
                y = originY - height;
                break;
        }

        final Bounds b = new BoundingBox(
                x - padding.getLeft(),
                y - padding.getTop(),
                width + padding.getLeft() + padding.getRight(),
                height + padding.getTop() + padding.getBottom());
        setCachedLayoutBounds(b);
    }

    @Override
    public void reshapeInLocal(@NonNull CssSize x, @NonNull CssSize y, @NonNull CssSize width, @NonNull CssSize height) {
        CssRectangle2D layoutBounds = getCssLayoutBounds();
        CssSize dx = x.subtract(layoutBounds.getMinX());
        CssSize dy = y.subtract(layoutBounds.getMinY());
        CssPoint2D p = getNonNull(ORIGIN);
        set(ORIGIN_X, p.getX().add(dx));
        set(ORIGIN_Y, p.getY().add(dy));

    }

    @Override
    public void translateInLocal(@NonNull CssPoint2D delta) {
        set(ORIGIN, getNonNull(ORIGIN).add(delta));
    }

    /**
     * Updates the group node that holds all other nodes of the label.
     * <p>
     * This method is empty. Subclasses may apply properties to the group node.
     *
     * @param ctx  the render context
     * @param node the group node
     */
    protected void updateGroupNode(@NonNull RenderContext ctx, @NonNull Group node) {

    }

    /**
     * Updates the node of the label.
     *
     * @param ctx  the render context
     * @param node the node
     */
    @Override
    public void updateNode(final @NonNull RenderContext ctx, final @NonNull Node node) {
        Group g = (Group) node;
        Path p = (Path) g.getProperties().get("pathNode");
        Text t = (Text) g.getProperties().get("textNode");
        Group ii = (Group) g.getProperties().get("iconNode");

        updateGroupNode(ctx, g);
        updateTextNode(ctx, t);
        updatePathNode(ctx, p);
        updateIconNode(ctx, ii);

        // Note: we must not add individual elements to g.children because
        // its ObservableList fires too many events.
        ArrayList<Node> newChildren = new ArrayList<>(2);
        if (p.getStroke() != null || p.getFill() != null) {
            newChildren.add(p);
        }
        if (t.getStroke() != null || t.getFill() != null) {
            newChildren.add(t);
        }
        newChildren.add(ii);
        if (!newChildren.equals(g.getChildren())) {
            g.getChildren().setAll(newChildren);
        }
    }

    /**
     * Updates the icon node for rendering.
     *
     * @param ctx           the render context
     * @param iconGroupNode the group node that holds the icon image
     */
    protected void updateIconNode(final @NonNull RenderContext ctx, final @NonNull Group iconGroupNode) {
        updateIconNodeImage(ctx, iconGroupNode);
        updateIconNodeTransform(ctx, iconGroupNode);
    }

    /**
     * Updates the image of the icon node.
     *
     * @param ctx           the render context
     * @param iconGroupNode the group node that holds the icon image
     */
    protected void updateIconNodeImage(final @NonNull RenderContext ctx, final @NonNull Group iconGroupNode) {
        final ImmutableList<PathElement> elements = getStyled(ICON_SHAPE);
        iconGroupNode.setVisible(elements != null);
        if (elements == null) {
            return;
        }
        final Path path;
        if (!iconGroupNode.getChildren().isEmpty() && iconGroupNode.getChildren().get(0) instanceof Path) {
            path = (Path) iconGroupNode.getChildren().get(0);
        } else {
            path = new Path();
            iconGroupNode.getChildren().setAll(path);
        }
        path.setFill(Paintable.getPaint(getStyled(ICON_FILL)));
        path.setStroke(null);
        path.getElements().setAll(elements.asList());
    }

    /**
     * Updates the transforms (translate, rotate, ...) of the provided icon group node.
     * <p>
     * The icon is placed next to the text plus the icon gap.
     * The path for the icon is taken from {@link #ICON_SHAPE}.
     *
     * @param ctx           the render context
     * @param iconGroupNode the group node that holds the icon image
     */
    protected void updateIconNodeTransform(final @NonNull RenderContext ctx, final @NonNull Group iconGroupNode) {
        final UnitConverter units = ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY);
        final CssPoint2D iconTranslate = getStyledNonNull(ICON_TRANSLATE);
        final double tx = iconTranslate.getX().getConvertedValue(units);
        final double ty = iconTranslate.getY().getConvertedValue(units);

        final Insets padding = getStyledNonNull(PADDING).getConvertedValue(units);
        final Bounds b = getLayoutBounds();
        iconGroupNode.setTranslateY(b.getMinY() + padding.getTop() + ty);
        final double w = getStyledNonNull(ICON_SIZE).getWidth().getConvertedValue(units);
        final double h = getStyledNonNull(ICON_SIZE).getHeight().getConvertedValue(units);
        switch (getStyledNonNull(ICON_POSITION)) {
            default:
            case LEFT:
                iconGroupNode.setTranslateX(b.getMinX() + padding.getLeft() + tx);
                break;
            case RIGHT:
                iconGroupNode.setTranslateX(b.getMaxX() - padding.getRight() - w + tx);
                break;
        }

        final double iconRotate = getStyledNonNull(ICON_ROTATE);
        if (iconRotate != 0.0) {
            iconGroupNode.getTransforms().setAll(new FXPreciseRotate(iconRotate, w * 0.5, h * 0.5));
        } else {
            iconGroupNode.getTransforms().clear();
        }
    }

    /**
     * Updates the path that fills or strokes the visual bounds of the label.
     *
     * @param ctx  the render context
     * @param node the path node
     */
    protected void updatePathNode(final @NonNull RenderContext ctx, final @NonNull Path node) {
        applyFillableFigureProperties(ctx, node);
        applyStrokableFigureProperties(ctx, node);
        applyShapeableProperties(ctx, node, getVisualBounds());
    }

    /**
     * Updates the given text node with properties from this figure, so
     * that it can be rendered.
     * <p>
     * This method calls {@link #updateTextNodeFontAndText(RenderContext, Text)},
     * {@link #updateTextNodeLayout(RenderContext, Text)},
     * {@link #updateTextNodePaint(RenderContext, Text)}.
     * <p>
     * If {@link #hasIcon()} returns true, the text is placed next to the icon
     * plus the icon gap.
     *
     * @param ctx the render context
     * @param tn  the text node
     */
    protected void updateTextNode(final @NonNull RenderContext ctx, final @NonNull Text tn) {
        updateTextNodeFontAndText(ctx, tn);
        updateTextNodeLayout(ctx, tn);
        updateTextNodePaint(ctx, tn);
    }

    /**
     * Updates paint properties of the given text node with properties from this figure.
     *
     * @param ctx the render context
     * @param tn  the text node
     */
    protected void updateTextNodePaint(final @NonNull RenderContext ctx, final @NonNull Text tn) {
        applyTextFillableFigureProperties(ctx, tn);
    }

    /**
     * Updates properties that are relevant for the layout of the given text node
     * with properties from this figure.
     *
     * @param ctx the render context
     * @param tn  the text node
     */
    protected void updateTextNodeLayout(final @NonNull RenderContext ctx, final @NonNull Text tn) {
        // Place the text object inside the content box
        // ---------------------------------------------
        final UnitConverter units = ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY);
        final IconPosition iconPosition = getStyledNonNull(ICON_POSITION);
        final CssDimension2D iconSize = getStyledNonNull(ICON_SIZE);
        final CssSize gap = getStyledNonNull(ICON_TEXT_GAP);
        final Insets padding = getStyledNonNull(PADDING).getConvertedValue(units);
        final Bounds boundsInLocal = getLayoutBounds();
        double iconPaddingLeft = 0;
        if (hasIcon()) {
            if (iconPosition == IconPosition.LEFT) {
                iconPaddingLeft = gap.getConvertedValue(units) + iconSize.getWidth().getConvertedValue(units);
            }
        }

        // We use TOP here, because we have already computed the effect of
        // TEXT_VPOS in the layout method.
        // XXX However we must consider HPOS here, because it affects text with line breaks (?)
        tn.setTextOrigin(VPos.TOP);
        tn.setX(boundsInLocal.getMinX() + padding.getLeft() + iconPaddingLeft);
        tn.setY(boundsInLocal.getMinY() + padding.getTop());
    }

    /**
     * Updates the given text node with properties from this figure that affects
     * the layout of the text node. This includes the text, the font properties,
     * and the text alignment properties.
     *
     * @param ctx the render context
     * @param tn  the text node
     */
    protected void updateTextNodeFontAndText(final @NonNull RenderContext ctx, final @NonNull Text tn) {
        applyTextFontableFigureProperties(ctx, tn);
        applyTextLayoutableFigureProperties(ctx, tn);

        // We must set the font before we set the text, so that JavaFx does not
        // need to retrieve the system default font, which on Windows requires
        // that the JavaFx Toolkit is launched.
        final String text = getText(ctx);
        if (!Objects.equals(text, tn.getText())) {
            tn.setText(text);
        }
    }
}