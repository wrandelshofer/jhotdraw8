/*
 * @(#)PrintRenderContext.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.render;

import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.SimpleNullableKey;

/**
 * PrintRenderContext.
 *
 */
public interface PrintRenderContext extends RenderContext {

    // ---
    // keys
    // ---
    /**
     * The figure which defines the layout of the current print page. This is
     * typically a {@link org.jhotdraw8.draw.figure.Page}.
     */
    Key<Figure> PAGE_FIGURE = new SimpleNullableKey<>("pageFigure", Figure.class);
    /**
     * Defines the current internal page number of a page figure. A page figure
     * may define the layout for multiple pages - for example for continuous
     * form paper.
     */
    Key<Integer> INTERNAL_PAGE_NUMBER = new SimpleNullableKey<>("internalPageNumber", Integer.class);
    /**
     * Defines the current page number of the print job.
     * <p>
     * This number starts with 1 instead of with 0.
     */
    Key<Integer> PAGE_NUMBER = new SimpleNullableKey<>("pageNumber", Integer.class);
    /**
     * Defines the total number of a pages in the print job.
     */
    Key<Integer> NUMBER_OF_PAGES = new SimpleNullableKey<>("numberOfPages", Integer.class);

}
