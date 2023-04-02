/*
 * @(#)Drawing.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.paint.Color;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.primitive.IntArrayList;
import org.jhotdraw8.collection.reflect.TypeToken;
import org.jhotdraw8.css.manager.StylesheetsManager;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.NullableCssColorStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleNonNullListKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleNullableKey;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;
import org.jhotdraw8.graph.algo.TopologicalSortAlgo;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * A <em>drawing</em> is an image composed of graphical (figurative) elements.
 * <p>
 * <b>Styling.</b> A drawing can have a style sheet which affects the style of
 * the figures.
 * <p>
 * <b>Layers.</b> By convention the children of a {@code Drawing} must be
 * {@link Layer}s. To addChild figures to a drawing, first addChild a layer, and then addChild the figures to the layer.</p>
 *
 * @author Werner Randelshofer
 */
public interface Drawing extends Figure {

    /**
     * Specifies the home address of all relative URLs used in a drawing.
     * <p>
     * This property is not styleable.
     */
    @NonNull Key<URI> DOCUMENT_HOME = new SimpleNullableKey<>("documentHome", URI.class,
            Paths.get(System.getProperty("user.home")).toUri());

    /**
     * Holds a list of author stylesheets. If the value is null, then no
     * stylesheets are used.
     * <p>
     * Supports the following data types for list entries:
     * <ul>
     * <li>URI. The URI points to a CSS file. If the URI is relative, then it is
     * relative to {@code DOCUMENT_HOME}.</li>
     * <li>String. The String contains a CSS as a literal.</li>
     * </ul>
     * <p>
     * This property is not styleable.</p>
     */
    @NonNull NonNullKey<ImmutableList<URI>> AUTHOR_STYLESHEETS = new SimpleNonNullListKey<URI>("authorStylesheets",
            new TypeToken<ImmutableList<URI>>() {
            });
    /**
     * Holds a list of user agent stylesheets. If the value is null, then no
     * stylesheets are used.
     * <ul>
     * <li>URI. The URI points to a CSS file. If the URI is relative, then it is
     * relative to {@code DOCUMENT_HOME}.</li>
     * <li>String. The String contains a CSS as a literal.</li>
     * </ul>
     * <p>
     * This property is not styleable.</p>
     */
    @NonNull NonNullKey<ImmutableList<URI>> USER_AGENT_STYLESHEETS = new SimpleNonNullListKey<URI>("userAgentStylesheets", new TypeToken<ImmutableList<URI>>() {
    });
    /**
     * Holds a list of inline stylesheets. If the value is null, then no
     * stylesheets are used.
     * <p>
     * This property is not styleable.</p>
     */
    @NonNull NonNullKey<ImmutableList<String>> INLINE_STYLESHEETS = new SimpleNonNullListKey<String>("inlineStylesheets", new TypeToken<ImmutableList<String>>() {
    });


    /**
     * Defines the width of the canvas.
     */
    @NonNull CssSizeStyleableKey WIDTH = new CssSizeStyleableKey("width", CssSize.of(640.0));
    /**
     * Defines the height of the canvas.
     */
    @NonNull CssSizeStyleableKey HEIGHT = new CssSizeStyleableKey("height", CssSize.of(480.0));


    /**
     * Defines the canvas color.
     * <p>
     * A drawing typically renders a rectangle with the dimensions given by
     * {@code WIDTH} and {@code HEIGHT} and fills it with the {@code BACKGROUND}
     * paint.
     * </p>
     * <p>
     * This property is styleable with the key
     * {@code Figure.JHOTDRAW_CSS_PREFIX+"background"}.</p>
     */
    @NonNull NullableCssColorStyleableKey BACKGROUND = new NullableCssColorStyleableKey("background", new CssColor("white", Color.WHITE));

    /**
     * The CSS type selector for a label object is {@value #TYPE_SELECTOR}.
     */
    String TYPE_SELECTOR = "Drawing";

    @Override
    default @NonNull String getTypeSelector() {
        return TYPE_SELECTOR;
    }

    /**
     * Gets the style manager of the drawing.
     *
     * @return the style manager
     */
    @Nullable StylesheetsManager<Figure> getStyleManager();

    /**
     * Updates the stylesheets in the style manager.
     */
    void updateStyleManager();

    /**
     * Performs one layout pass over the entire drawing.
     * <p>
     * This method lays out figures that do not depend on the layout
     * of other figures first, and then lays out figures that depend
     * on them, until all figures are laid out once.
     * Circular dependencies are broken up deterministically.
     *
     * @param ctx the render context
     */
    default void layoutAll(@NonNull RenderContext ctx) {
        layoutAll(ctx, true);
    }

    /**
     * Performs one layout pass over the entire drawing.
     * <p>
     * This method lays out figures that do not depend on the layout
     * of other figures first, and then lays out figures that depend
     * on them, until all figures are laid out once.
     * Circular dependencies are broken up deterministically.
     *
     * @param ctx      the render context
     * @param parallel performs the layout in parallel or sequentially
     */
    default void layoutAll(@NonNull RenderContext ctx, boolean parallel) {
        // build a graph which includes all figures that must be laid out and all their observers
        // transitively
        // IdentityMap is slower for insertion than an equality-based map.
        SimpleMutableDirectedGraph<Figure, Figure> graphBuilder = new SimpleMutableDirectedGraph<>(1024, 1024, false);
        graphBuilder.setOrdered(false);
        for (Figure f : preorderIterable()) {
            graphBuilder.addVertex(f);
            for (Figure subj : f.getLayoutSubjects()) {
                graphBuilder.addVertex(subj);
                graphBuilder.addArrow(subj, f, null);
            }
            Figure parent = f.getParent();
            if (parent != null) {
                graphBuilder.addArrow(parent, f, null);
            }
        }

        OrderedPair<int[], IntArrayList> pair = new TopologicalSortAlgo().sortTopologicallyIntBatches(graphBuilder);
        int[] sorted = pair.first();
        if (pair.second().isEmpty()) {
            // graph has a loop => layout sequentially
            for (int i : sorted) {
                graphBuilder.getVertex(i).layout(ctx);
            }
        } else {
            // graph has no loop => layout each topologically independent batch in parallel.
            int start = 0;
            for (int end : pair.second()) {
                StreamSupport.intStream(Spliterators.spliterator(sorted, start, end, 0), parallel)
                        .forEach(i -> {
                            graphBuilder.getVertex(i).layout(ctx);
                        });
                start = end;
            }
        }
    }

    default void updateAllCss(@NonNull RenderContext ctx) {
        StylesheetsManager<Figure> styleManager = getStyleManager();
        if (styleManager != null && styleManager.hasStylesheets()) {
            // Performance: We copy preorderIterable into a list, so that it
            //              splits better for parallel execution.
            List<Figure> list = new ArrayList<>();
            preorderIterable().forEach(list::add);
            styleManager.applyStylesheetsTo(list);
            for (Figure f : preorderIterable()) {
                // XXX WR why do we updateCss again, after having done applyStylesheetsTo??
                //f.updateCss(ctx);
                f.invalidateTransforms();
            }
        }
    }

    /**
     * Returns all figures in topological order according to their layout dependencies.
     * Independent figures come first.
     *
     * @return figures in topological order according to layout dependencies
     */
    default @NonNull Iterable<Figure> layoutDependenciesIterable() {
        // build a graph which includes all figures that must be laid out and all their observers
        // transitively
        SimpleMutableDirectedGraph<Figure, Figure> graphBuilder = new SimpleMutableDirectedGraph<>(1024, 1024, true);
        for (Figure f : postorderIterable()) {
            graphBuilder.addVertex(f);
            for (Figure obs : f.getReadOnlyLayoutObservers()) {
                graphBuilder.addVertex(obs);
                graphBuilder.addArrow(f, obs, f);
            }
        }
        return new TopologicalSortAlgo().sortTopologically(graphBuilder);
    }
}
