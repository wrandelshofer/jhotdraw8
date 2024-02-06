/*
 * @(#)AbstractDrawing.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.collections.ObservableList;
import javafx.css.StyleOrigin;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.manager.SimpleStylesheetsManager;
import org.jhotdraw8.css.manager.StylesheetsManager;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.model.FigureSelectorModel;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DrawingFigure.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractDrawing extends AbstractCompositeFigure
        implements Drawing {

    /**
     * The style manager is created lazily.
     */
    private @Nullable StylesheetsManager<Figure> styleManager = null;

    public AbstractDrawing() {
    }

    public AbstractDrawing(double width, double height) {
        this(CssSize.of(width), CssSize.of(height));

    }

    public AbstractDrawing(CssSize width, CssSize height) {
        set(WIDTH, width);
        set(HEIGHT, height);
    }

    @Override
    public @NonNull Node createNode(@NonNull RenderContext drawingView) {
        Pane g = new Pane();
        g.setManaged(false);

        Group gg = new Group();
        gg.setManaged(false);
        g.getChildren().add(gg);

        return g;
    }

    protected @NonNull StylesheetsManager<Figure> createStyleManager() {
        return new SimpleStylesheetsManager<>(new FigureSelectorModel());
    }

    /**
     * The bounds of this drawing is determined by its {@code WIDTH} and its
     * {@code HEIGHT}.
     * <p>
     * The bounds of the child figures does not affect the bounds of the
     * drawing.
     *
     * @return bounding box (0, 0, WIDTH, HEIGHT).
     */
    @Override
    public @NonNull CssRectangle2D getCssLayoutBounds() {
        return new CssRectangle2D(CssSize.ZERO, CssSize.ZERO, getStyledNonNull(WIDTH), getStyledNonNull(HEIGHT));
    }

    @Override
    public @NonNull Bounds getLayoutBounds() {
        // Note: We must override getBoundsInLocal of AbstractCompositeFigure.
        return getCssLayoutBounds().getConvertedBoundsValue();
    }

    @Override
    public @Nullable StylesheetsManager<Figure> getStyleManager() {
        if (styleManager == null) {
            styleManager = createStyleManager();
            updateStyleManager();
        }
        return styleManager;
    }

    @Override
    public void updateStyleManager() {
        if (styleManager != null) {
            styleManager.setStylesheets(StyleOrigin.USER_AGENT, get(DOCUMENT_HOME), getList(USER_AGENT_STYLESHEETS));
            styleManager.setStylesheets(StyleOrigin.AUTHOR, get(DOCUMENT_HOME), getList(AUTHOR_STYLESHEETS));
            styleManager.setStylesheets(StyleOrigin.INLINE, getStringList(INLINE_STYLESHEETS));
        }
    }

    private List<URI> getList(Key<? extends ImmutableList<URI>> key) {
        ImmutableList<URI> list = get(key);
        return list == null ? Collections.emptyList() : list.asList();
    }

    private List<String> getStringList(Key<? extends ImmutableList<String>> key) {
        ImmutableList<String> list = get(key);
        return list == null ? Collections.emptyList() : list.asList();
    }

    @Override
    public void reshapeInLocal(@NonNull Transform transform) {
        Bounds b = getLayoutBounds();
        b = transform.transform(b);
        reshapeInLocal(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
    }

    @Override
    public void reshapeInLocal(@NonNull CssSize x, @NonNull CssSize y, @NonNull CssSize width, @NonNull CssSize height) {
        set(WIDTH, width.abs());
        set(HEIGHT, height.abs());
    }

    @Override
    public void stylesheetChanged(@NonNull RenderContext ctx) {
        if (styleManager != null) {
            styleManager.setStylesheets(StyleOrigin.USER_AGENT, get(DOCUMENT_HOME), getList(USER_AGENT_STYLESHEETS));
            styleManager.setStylesheets(StyleOrigin.AUTHOR, get(DOCUMENT_HOME), getList(AUTHOR_STYLESHEETS));
            styleManager.setStylesheets(StyleOrigin.INLINE, getStringList(INLINE_STYLESHEETS));
        }
        super.stylesheetChanged(ctx);
    }

    @Override
    public void updateNode(@NonNull RenderContext ctx, @NonNull Node n) {
        Pane g = (Pane) n;
        Bounds bounds = getLayoutBounds();
        g.setPrefWidth(bounds.getWidth());
        g.setPrefHeight(bounds.getHeight());
        g.resizeRelocate(
                bounds.getMinX(),
                bounds.getMinY(),
                bounds.getWidth(),
                bounds.getHeight());
        updateBackground(ctx, g);
        g.setClip(new Rectangle(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight()));

        Group gg = (Group) g.getChildren().getFirst();


        List<Node> nodes = new ArrayList<>(getChildren().size());
        for (Figure child : getChildren()) {
            nodes.add(ctx.getNode(child));
        }
        ObservableList<Node> group = gg.getChildren();
        if (!group.equals(nodes)) {
            group.setAll(nodes);
        }
    }

    public void updateBackground(RenderContext ctx, Pane g) {
        Paint paint = Paintable.getPaint(getStyled(BACKGROUND));
        if ((paint instanceof Color) && ((Color) paint).getOpacity() == 0) {
            paint = null;
        }
        g.setBackground(paint == null ? null : new Background(new BackgroundFill(
                paint, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    @Override
    public boolean isSuitableParent(@NonNull Figure newParent) {
        return true;
    }


}
