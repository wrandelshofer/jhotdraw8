/*
 * @(#)PageFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.draw.css.value.CssDimension2D;
import org.jhotdraw8.draw.css.value.CssInsets;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.key.CssInsetsStyleableMapAccessor;
import org.jhotdraw8.draw.key.CssPoint2DStyleableMapAccessor;
import org.jhotdraw8.draw.key.CssRectangle2DStyleableMapAccessor;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.DoubleStyleableKey;
import org.jhotdraw8.draw.key.PaperSizeStyleableMapAccessor;
import org.jhotdraw8.draw.key.Point2DStyleableMapAccessor;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.draw.render.RenderingIntent;
import org.jhotdraw8.geom.FXRectangles;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.icollection.SimpleImmutableList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.max;

/**
 * Defines a page layout for printing.
 *
 * @author Werner Randelshofer
 */
public class PageFigure extends AbstractCompositeFigure
        implements Page, Grouping, TransformableFigure, ResizableFigure, HideableFigure, LockableFigure, StyleableFigure,
        FillableFigure, StrokableFigure {

    public static final @NonNull CssSizeStyleableKey HEIGHT = RectangleFigure.HEIGHT;
    /**
     * The computed number of pages along the x-axis.
     */
    public static final @NonNull DoubleStyleableKey NUM_PAGES_X = new DoubleStyleableKey("num-pages-x", 1.0);
    /** The computed number of pages along the y-axis. */
    public static final @NonNull DoubleStyleableKey NUM_PAGES_Y = new DoubleStyleableKey("num-pages-y", 1.0);
    public static final @NonNull Point2DStyleableMapAccessor NUM_PAGES_X_Y = new Point2DStyleableMapAccessor("num-pages", NUM_PAGES_X, NUM_PAGES_Y);
    public static final @NonNull CssSizeStyleableKey PAGE_INSETS_BOTTOM = new CssSizeStyleableKey("page-insets-bottom", CssSize.ZERO);
    public static final @NonNull CssSizeStyleableKey PAGE_INSETS_LEFT = new CssSizeStyleableKey("page-insets-left", CssSize.ZERO);
    public static final @NonNull CssSizeStyleableKey PAGE_INSETS_RIGHT = new CssSizeStyleableKey("page-insets-right", CssSize.ZERO);
    public static final @NonNull CssSizeStyleableKey PAGE_INSETS_TOP = new CssSizeStyleableKey("page-insets-top", CssSize.ZERO);
    public static final @NonNull CssInsetsStyleableMapAccessor PAGE_INSETS = new CssInsetsStyleableMapAccessor("page-insets", PAGE_INSETS_TOP, PAGE_INSETS_RIGHT, PAGE_INSETS_BOTTOM, PAGE_INSETS_LEFT);
    public static final @NonNull CssSizeStyleableKey PAGE_OVERLAP_X = new CssSizeStyleableKey("page-overlap-x", CssSize.ZERO);
    public static final @NonNull CssSizeStyleableKey PAGE_OVERLAP_Y = new CssSizeStyleableKey("page-overlap-y", CssSize.ZERO);
    public static final @NonNull CssPoint2DStyleableMapAccessor PAGE_OVERLAP = new CssPoint2DStyleableMapAccessor("page-overlap", PAGE_OVERLAP_X, PAGE_OVERLAP_Y);
    public static final @NonNull CssSizeStyleableKey PAPER_HEIGHT = new CssSizeStyleableKey("paper-size-height", CssSize.of(297.0, "mm"));
    public static final @NonNull CssSizeStyleableKey PAPER_WIDTH = new CssSizeStyleableKey("paper-size-width", CssSize.of(210.0, "mm"));
    public static final @NonNull PaperSizeStyleableMapAccessor PAPER_SIZE = new PaperSizeStyleableMapAccessor("paper-size", PAPER_WIDTH, PAPER_HEIGHT);
    /**
     * The CSS type selector for this object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "Page";
    public static final CssSizeStyleableKey WIDTH = RectangleFigure.WIDTH;
    public static final CssSizeStyleableKey X = RectangleFigure.X;
    public static final CssSizeStyleableKey Y = RectangleFigure.Y;
    public static final CssRectangle2DStyleableMapAccessor BOUNDS = RectangleFigure.BOUNDS;
    private static final Object CONTENT_BOUNDS_PROPERTY = new Object();
    private static final Object PAGE_INSETS_PROPERTY = new Object();
    private static final Object PAGE_BOUNDS_PROPERTY = new Object();
    private static final Object CURRENT_PAGE_PROPERTY = new Object();

    public PageFigure() {
    }

    private void addBounds(final @NonNull List<PathElement> pbList, @NonNull Bounds b) {
        double x = b.getMinX();
        double y = b.getMinY();
        double w = b.getWidth();
        double h = b.getHeight();
        pbList.add(new MoveTo(x, y));
        pbList.add(new LineTo(x + w, y));
        pbList.add(new LineTo(x + w, y + h));
        pbList.add(new LineTo(x, y + h));
        pbList.add(new ClosePath());
    }

    private double computeContentAreaFactor() {
        if (true) return 1;
        String units = getNonNull(WIDTH).getUnits();
        DefaultUnitConverter uc = DefaultUnitConverter.getInstance();

        double contentWidth = getNonNull(WIDTH).getConvertedValue(uc, units);
        double contentHeight = getNonNull(HEIGHT).getConvertedValue(uc, units);
        Insets insets = getStyledNonNull(PAGE_INSETS).getConvertedValue(uc, units);
        CssPoint2D overlap = getStyledNonNull(PAGE_OVERLAP);
        double overX = overlap.getX().getConvertedValue(uc, units);
        double overY = overlap.getY().getConvertedValue(uc, units);
        int numPagesX = Math.max(1, getStyledNonNull(NUM_PAGES_X).intValue());
        int numPagesY = Math.max(1, getStyledNonNull(NUM_PAGES_Y).intValue());
        double innerPageW = (getStyledNonNull(PAPER_WIDTH).getConvertedValue(uc, units) - insets.getLeft() - insets.getRight());
        double innerPageH = (getStyledNonNull(PAPER_HEIGHT).getConvertedValue(uc, units) - insets.getTop() - insets.getBottom());
        double totalInnerPageWidth = innerPageW * numPagesX - overX * max(0, numPagesX - 1);
        double totalInnerPageHeight = innerPageH * numPagesY - overY * max(0, numPagesY - 1);
        double contentRatio = contentWidth / contentHeight;
        double innerPageRatio = totalInnerPageWidth / totalInnerPageHeight;
        double contentAreaFactor;
        if (contentRatio > innerPageRatio) {
            contentAreaFactor = (contentWidth) / totalInnerPageWidth;
        } else {
            contentAreaFactor = (contentHeight) / totalInnerPageHeight;
        }
        return contentAreaFactor;
    }

    @Override
    public @NonNull Node createNode(@NonNull RenderContext ctx) {
        javafx.scene.Group n = new javafx.scene.Group();
        n.setManaged(false);
        n.setAutoSizeChildren(false);

        Rectangle contentBoundsNode = new Rectangle();
        contentBoundsNode.setFill(null);
        contentBoundsNode.setStroke(Color.LIGHTGRAY);
        contentBoundsNode.setStrokeType(StrokeType.INSIDE);

        Path pageBoundsNode = new Path();

        Path insetsBoundsNode = new Path();
        insetsBoundsNode.setFill(null);
        insetsBoundsNode.setStroke(Color.LIGHTGRAY);
        insetsBoundsNode.setStrokeType(StrokeType.CENTERED);
        insetsBoundsNode.getStrokeDashArray().setAll(5.0);

        javafx.scene.Group currentPageNode = new javafx.scene.Group();

        n.getChildren().addAll(pageBoundsNode, insetsBoundsNode, contentBoundsNode, currentPageNode);
        n.getProperties().put(PAGE_BOUNDS_PROPERTY, pageBoundsNode);
        n.getProperties().put(PAGE_INSETS_PROPERTY, insetsBoundsNode);
        n.getProperties().put(CONTENT_BOUNDS_PROPERTY, contentBoundsNode);
        n.getProperties().put(CURRENT_PAGE_PROPERTY, currentPageNode);
        return n;
    }

    @Override
    public @NonNull Node createPageNode(int internalPageNumber) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body ofCollection generated methods, choose Tools | Templates.
    }

    @Override
    public @NonNull Bounds getLayoutBounds() {
        return getCssLayoutBounds().getConvertedBoundsValue();
    }

    @Override
    public @NonNull CssRectangle2D getCssLayoutBounds() {
        return new CssRectangle2D(getNonNull(X),
                getNonNull(Y),
                getNonNull(WIDTH),
                getNonNull(HEIGHT));
    }

    @Override
    public int getNumberOfSubPages() {
        int numPagesX = Math.max(1, getStyledNonNull(NUM_PAGES_X).intValue());
        int numPagesY = Math.max(1, getStyledNonNull(NUM_PAGES_Y).intValue());
        return numPagesX * numPagesY;
    }

    @Override
    public @NonNull Bounds getPageBounds(int internalPageNumber) {
        double contentAreaFactor = computeContentAreaFactor();
        Insets insets = getStyledNonNull(PAGE_INSETS).getConvertedValue();
        CssPoint2D overlap = getStyledNonNull(PAGE_OVERLAP);
        double overX = overlap.getX().getConvertedValue();
        double overY = overlap.getY().getConvertedValue();
        int numPagesX = Math.max(1, getStyledNonNull(NUM_PAGES_X).intValue());

        double pageX = getNonNull(X).getConvertedValue() - insets.getLeft() * contentAreaFactor;
        double pageY = getNonNull(Y).getConvertedValue() - insets.getTop() * contentAreaFactor;
        double pageW = getStyledNonNull(PAPER_WIDTH).getConvertedValue() * contentAreaFactor;
        double pageH = getStyledNonNull(PAPER_HEIGHT).getConvertedValue() * contentAreaFactor;
        double pageOverX = (overX + insets.getLeft() + insets.getRight()) * contentAreaFactor;
        double pageOverY = (overY + insets.getTop() + insets.getBottom()) * contentAreaFactor;
        int px = internalPageNumber % numPagesX;
        int py = internalPageNumber / numPagesX;
        double x = pageX + (pageW - pageOverX) * px;
        double y = pageY + (pageH - pageOverY) * py;
        return new BoundingBox(x, y, pageW, pageH);
    }

    private @NonNull Bounds getContentBounds(int internalPageNumber) {
        double contentAreaFactor = computeContentAreaFactor();
        Insets insets = getStyledNonNull(PAGE_INSETS).getConvertedValue();
        CssPoint2D overlap = getStyledNonNull(PAGE_OVERLAP);
        double overX = overlap.getX().getConvertedValue();
        double overY = overlap.getY().getConvertedValue();
        int numPagesX = Math.max(1, getStyledNonNull(NUM_PAGES_X).intValue());

        double pageX = getNonNull(X).getConvertedValue();
        double pageY = getNonNull(Y).getConvertedValue();
        double pageW = getNonNull(PAPER_WIDTH).getConvertedValue() * contentAreaFactor;
        double pageH = getNonNull(PAPER_HEIGHT).getConvertedValue() * contentAreaFactor;
        double marginH = insets.getLeft() + insets.getRight();
        double marginV = insets.getTop() + insets.getBottom();
        double pageOverX = (overX + marginH) * contentAreaFactor;
        double pageOverY = (overY + marginV) * contentAreaFactor;
        int px = internalPageNumber % numPagesX;
        int py = internalPageNumber / numPagesX;
        double x = pageX + (pageW - pageOverX) * px;
        double y = pageY + (pageH - pageOverY) * py;
        return new BoundingBox(x, y, pageW - marginH * contentAreaFactor, pageH - marginV * contentAreaFactor);
    }

    @Override
    public @NonNull Shape getPageClip(int internalPageNumber) {
        double contentAreaFactor = computeContentAreaFactor();
        Insets insets = getStyledNonNull(PAGE_INSETS).getConvertedValue();
        CssPoint2D overlap = getStyledNonNull(PAGE_OVERLAP);
        double ox = overlap.getX().getConvertedValue();
        double oy = overlap.getY().getConvertedValue();
        int numPagesX = Math.max(1, getStyledNonNull(NUM_PAGES_X).intValue());

        double pageX = getNonNull(X).getConvertedValue() - insets.getLeft() * contentAreaFactor;
        double pageY = getNonNull(Y).getConvertedValue() - insets.getTop() * contentAreaFactor;
        double pageWidth = getNonNull(PAPER_WIDTH).getConvertedValue() * contentAreaFactor;
        double pageHeight = getNonNull(PAPER_HEIGHT).getConvertedValue() * contentAreaFactor;
        double pageOverlapX = (ox + insets.getLeft() + insets.getRight()) * contentAreaFactor;
        double pageOverlapY = (oy + insets.getTop() + insets.getBottom()) * contentAreaFactor;
        int px = internalPageNumber % numPagesX;
        int py = internalPageNumber / numPagesX;
        double x = pageX + (pageWidth - pageOverlapX) * px;
        double y = pageY + (pageHeight - pageOverlapY) * py;


        Bounds b = FXRectangles.intersection(getLayoutBounds(),
                new BoundingBox(x + insets.getLeft() * contentAreaFactor, y + insets.getTop() * contentAreaFactor,
                        pageWidth - (insets.getLeft() + insets.getRight()) * contentAreaFactor,
                        pageHeight - (insets.getTop() + insets.getBottom()) * contentAreaFactor));

        return new Rectangle(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
    }

    @Override
    public CssDimension2D getPaperSize() {
        return getStyled(PAPER_SIZE);
    }

    @Override
    public @NonNull Transform getPageTransform(int internalPageNumber) {
        int numPagesX = Math.max(1, getStyledNonNull(NUM_PAGES_X).intValue());
        int numPagesY = Math.max(1, getStyledNonNull(NUM_PAGES_Y).intValue());

        internalPageNumber = Math.max(0, Math.min(internalPageNumber, numPagesX * numPagesY));

        int px = internalPageNumber % numPagesX;
        int py = internalPageNumber / numPagesX;
        Insets insets = getStyledNonNull(PAGE_INSETS).getConvertedValue();
        CssPoint2D overlap = getStyledNonNull(PAGE_OVERLAP);
        double overlapX = overlap.getX().getConvertedValue();
        double overlapY = overlap.getY().getConvertedValue();
        double contentAreaFactor = computeContentAreaFactor();
        double pageX = getNonNull(X).getConvertedValue() - insets.getLeft() * contentAreaFactor;
        double pageY = getNonNull(Y).getConvertedValue() - insets.getTop() * contentAreaFactor;
        double pageWidth = getNonNull(PAPER_WIDTH).getConvertedValue() * contentAreaFactor;
        double pageHeight = getNonNull(PAPER_HEIGHT).getConvertedValue() * contentAreaFactor;
        double pageOverlapX = (overlapX + insets.getLeft() + insets.getRight()) * contentAreaFactor;
        double pageOverlapY = (overlapY + insets.getTop() + insets.getBottom()) * contentAreaFactor;
        double x = pageX + (pageWidth - pageOverlapX) * px;
        double y = pageY + (pageHeight - pageOverlapY) * py;

        return FXTransforms.concat(new Translate(x, y), new Scale(contentAreaFactor, contentAreaFactor));
    }

    private @NonNull Translate getPageTranslate(int internalPageNumber) {
        int numPagesX = Math.max(1, getStyledNonNull(NUM_PAGES_X).intValue());
        int numPagesY = Math.max(1, getStyledNonNull(NUM_PAGES_Y).intValue());

        internalPageNumber = Math.max(0, Math.min(internalPageNumber, numPagesX * numPagesY));

        int px = internalPageNumber % numPagesX;
        int py = internalPageNumber / numPagesX;
        Insets insets = getStyledNonNull(PAGE_INSETS).getConvertedValue();
        CssPoint2D overlap = getStyledNonNull(PAGE_OVERLAP);
        double overlapX = overlap.getX().getConvertedValue();
        double overlapY = overlap.getY().getConvertedValue();
        double contentAreaFactor = computeContentAreaFactor();
        double pageX = getNonNull(X).getConvertedValue() - insets.getLeft() * contentAreaFactor;
        double pageY = getNonNull(Y).getConvertedValue() - insets.getTop() * contentAreaFactor;
        double pageWidth = getNonNull(PAPER_WIDTH).getConvertedValue() * contentAreaFactor;
        double pageHeight = getNonNull(PAPER_HEIGHT).getConvertedValue() * contentAreaFactor;
        double pageOverlapX = (overlapX + insets.getLeft() + insets.getRight()) * contentAreaFactor;
        double pageOverlapY = (overlapY + insets.getTop() + insets.getBottom()) * contentAreaFactor;
        double x = (pageWidth - pageOverlapX) * px;
        double y = (pageHeight - pageOverlapY) * py;

        return new Translate(x, y);
    }

    @Override
    public @NonNull String getTypeSelector() {
        return TYPE_SELECTOR;
    }

    @Override
    public boolean isLayoutable() {
        return true;
    }

    @Override
    public boolean isSuitableChild(@NonNull Figure newChild) {
        return true;
    }

    @Override
    public void layout(@NonNull RenderContext ctx) {
        final CssSize width = get(WIDTH);
        final CssSize height = get(HEIGHT);
        final CssDimension2D paperSize = getPaperSize();
        final CssInsets pageInsets = getStyled(PAGE_INSETS);
        final CssPoint2D pageOverlap = get(PAGE_OVERLAP);
        CssDimension2D innerPageSize = paperSize.subtract(
                new CssDimension2D(pageInsets.getLeft().add(pageInsets.getRight()),
                        pageInsets.getTop().add(pageInsets.getBottom())));
        final int numPagesX = 1 + Math.max(0, (int) Math.ceil(
                width.subtract(innerPageSize.getWidth()).getConvertedValue()
                        / (innerPageSize.getWidth().subtract(pageOverlap.getX())).getConvertedValue()));
        final int numPagesY = 1 + Math.max(0, (int) Math.ceil(
                height.subtract(innerPageSize.getHeight()).getConvertedValue()
                        / (innerPageSize.getHeight().subtract(pageOverlap.getY())).getConvertedValue()));
        set(NUM_PAGES_X, (double) numPagesX);
        set(NUM_PAGES_Y, (double) numPagesY);

        int currentPage = 0;
        final Transform pageTransform = getPageTransform(currentPage);

        ImmutableList<Transform> transforms = SimpleImmutableList.of();
        if (!pageTransform.isIdentity()) {
            transforms = transforms.add(pageTransform);
        }

        for (Figure child : getChildren()) {
            child.set(TRANSFORMS, transforms);
        }
    }

    @Override
    public void reshapeInLocal(@NonNull CssSize x, @NonNull CssSize y, @NonNull CssSize width, @NonNull CssSize height) {
        set(X, width.getValue() < 0 ? x.add(width) : x);
        set(Y, height.getValue() < 0 ? y.add(height) : y);
        set(WIDTH, width.abs());
        set(HEIGHT, height.abs());
    }

    @Override
    public void reshapeInLocal(@NonNull Transform transform) {
        Bounds newBounds = transform.transform(getLayoutBounds());
        set(X, CssSize.of(newBounds.getMinX()));
        set(Y, CssSize.of(newBounds.getMinY()));
        set(WIDTH, CssSize.of(newBounds.getWidth()));
        set(HEIGHT, CssSize.of(newBounds.getHeight()));
    }

    @Override
    public void updateNode(@NonNull RenderContext ctx, @NonNull Node node) {
        javafx.scene.Group groupNode = (javafx.scene.Group) node;
        // We can't use #applyTransformableFigureProperties(node) because
        // this will rotate around an unpredictable center!
        node.getTransforms().setAll(getLocalToParent(true));

        Rectangle contentBoundsNode = (Rectangle) groupNode.getProperties().get(CONTENT_BOUNDS_PROPERTY);
        Path pageBoundsNode = (Path) groupNode.getProperties().get(PAGE_BOUNDS_PROPERTY);
        Path pageInsetsNode = (Path) groupNode.getProperties().get(PAGE_INSETS_PROPERTY);
        javafx.scene.Group currentPageNode = (javafx.scene.Group) groupNode.getProperties().get(CURRENT_PAGE_PROPERTY);

        applyFillableFigureProperties(ctx, pageBoundsNode);
        applyStrokableFigureProperties(ctx, pageBoundsNode);

        if (ctx.get(RenderContext.RENDERING_INTENT) == RenderingIntent.EDITOR) {
            applyHideableFigureProperties(ctx, node);
            contentBoundsNode.setVisible(true);
            pageBoundsNode.setVisible(true);
        } else if (ctx.get(RenderContext.RENDER_PAGE) == this) {
            applyHideableFigureProperties(ctx, node);
            contentBoundsNode.setVisible(false);
            pageBoundsNode.setVisible(false);
            pageInsetsNode.setVisible(false);
        } else {
            node.setVisible(false);
        }

        double contentWidth = getNonNull(WIDTH).getConvertedValue();
        double contentHeight = getNonNull(HEIGHT).getConvertedValue();
        contentBoundsNode.setX(getNonNull(X).getConvertedValue());
        contentBoundsNode.setY(getNonNull(Y).getConvertedValue());
        contentBoundsNode.setWidth(contentWidth);
        contentBoundsNode.setHeight(contentHeight);

        int numPagesX = Math.max(1, getStyledNonNull(NUM_PAGES_X).intValue());
        int numPagesY = Math.max(1, getStyledNonNull(NUM_PAGES_Y).intValue());
        final int n = numPagesX * numPagesY;
        final List<PathElement> pbList = new ArrayList<>(n * 4);
        final List<PathElement> pmList = new ArrayList<>(n * 4);
        for (int i = 0; i < n; i++) {
            addBounds(pbList, getPageBounds(i));
            addBounds(pmList, getContentBounds(i));
        }
        pageBoundsNode.getElements().setAll(pbList);
        pageInsetsNode.getElements().setAll(pmList);

        Integer currentPage = ctx.get(RenderContext.RENDER_PAGE_INTERNAL_NUMBER);
        currentPageNode.getTransforms().setAll(getPageTranslate(currentPage == null ? 0 : currentPage));

        List<Node> currentPageChildren = new ArrayList<>(getChildren().size() + 2);
        for (Figure child : getChildren()) {
            currentPageChildren.add(ctx.getNode(child));
        }
        ObservableList<Node> group = currentPageNode.getChildren();
        if (!group.equals(currentPageChildren)) {
            group.setAll(currentPageChildren);
        }
    }
}
