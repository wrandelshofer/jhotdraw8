/*
 * @(#)Drawing.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.paint.Color;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.collection.primitive.IntArrayList;
import org.jhotdraw8.css.manager.StylesheetsManager;
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.NullableCssColorStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.fxcollection.typesafekey.NonNullListKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleNullableKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;
import org.jhotdraw8.graph.algo.TopologicalSortAlgo;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/// A _drawing_ is an image composed of graphical (figurative) elements.
///
/// **Styling.** A drawing can have a style sheet which affects the style of
/// the figures.
///
/// **Layers.** By convention the children of a `Drawing` must be
/// [Layer]s. To addChild figures to a drawing, first addChild a layer, and then addChild the figures to the layer.
public interface Drawing extends Figure {

    /// Specifies the home address of all relative URLs used in a drawing.
    ///
    /// This property is not styleable.
    Key<URI> DOCUMENT_HOME = new SimpleNullableKey<>("documentHome", URI.class,
            Paths.get(System.getProperty("user.home")).toUri());

    /// Holds a list of author stylesheets. If the value is null, then no
    /// stylesheets are used.
    ///
    /// Supports the following data types for list entries:
    ///
    ///   - URI. The URI points to a CSS file. If the URI is relative, then it is
    ///     relative to `DOCUMENT_HOME`.
    ///   - String. The String contains a CSS as a literal.
    ///
    ///
    /// This property is not styleable.
    NonNullKey<PersistentList<URI>> AUTHOR_STYLESHEETS = new NonNullListKey<>("authorStylesheets",
            new SimpleParameterizedType(PersistentList.class, URI.class));
    /// Holds a list of user agent stylesheets. If the value is null, then no
    /// stylesheets are used.
    ///
    ///   - URI. The URI points to a CSS file. If the URI is relative, then it is
    ///     relative to `DOCUMENT_HOME`.
    ///   - String. The String contains a CSS as a literal.
    ///
    ///
    /// This property is not styleable.
    NonNullKey<PersistentList<URI>> USER_AGENT_STYLESHEETS = new NonNullListKey<>("userAgentStylesheets", new SimpleParameterizedType(PersistentList.class, URI.class));
    /// Holds a list of inline stylesheets. If the value is null, then no
    /// stylesheets are used.
    ///
    /// This property is not styleable.
    NonNullKey<PersistentList<String>> INLINE_STYLESHEETS = new NonNullListKey<>("inlineStylesheets", new SimpleParameterizedType(PersistentList.class, String.class));


    /// Defines the width of the canvas.
    CssSizeStyleableKey WIDTH = new CssSizeStyleableKey("width", CssSize.of(640.0));
    /// Defines the height of the canvas.
    CssSizeStyleableKey HEIGHT = new CssSizeStyleableKey("height", CssSize.of(480.0));


    /// Defines the canvas color.
    ///
    /// A drawing typically renders a rectangle with the dimensions given by
    /// `WIDTH` and `HEIGHT` and fills it with the `BACKGROUND`
    /// paint.
    ///
    ///
    /// This property is styleable with the key
    /// `Figure.JHOTDRAW_CSS_PREFIX+"background"`.
    NullableCssColorStyleableKey BACKGROUND = new NullableCssColorStyleableKey("background", new CssColor("white", Color.WHITE));

    /// The CSS type selector for a label object is {@value #TYPE_SELECTOR}.
    String TYPE_SELECTOR = "Drawing";

    @Override
    default String getTypeSelector() {
        return TYPE_SELECTOR;
    }

    /// Gets the style manager of the drawing.
    ///
    /// @return the style manager
    @Nullable
    StylesheetsManager<Figure> getStyleManager();

    /// Updates the stylesheets in the style manager.
    void updateStyleManager();

    /// Performs one layout pass over the entire drawing.
    ///
    /// This method lays out figures that do not depend on the layout
    /// of other figures first, and then lays out figures that depend
    /// on them, until all figures are laid out once.
    /// Circular dependencies are broken up deterministically.
    ///
    /// @param ctx the render context
    default void layoutAll(RenderContext ctx) {
        layoutAll(ctx, true);
    }

    /// Performs one layout pass over the entire drawing.
    ///
    /// This method lays out figures that do not depend on the layout
    /// of other figures first, and then lays out figures that depend
    /// on them, until all figures are laid out once.
    /// Circular dependencies are broken up deterministically.
    ///
    /// @param ctx      the render context
    /// @param parallel performs the layout in parallel or sequentially
    default void layoutAll(RenderContext ctx, boolean parallel) {
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

        SimpleOrderedPair<int[], IntArrayList> pair = new TopologicalSortAlgo().sortTopologicallyIntBatches(graphBuilder);
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
                        .forEach(i -> graphBuilder.getVertex(i).layout(ctx));
                start = end;
            }
        }
    }

    default void updateAllCss(RenderContext ctx) {
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

    /// Returns all figures in topological order according to their layout dependencies.
    /// Independent figures come first.
    ///
    /// @return figures in topological order according to layout dependencies
    default Iterable<Figure> layoutDependenciesIterable() {
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
