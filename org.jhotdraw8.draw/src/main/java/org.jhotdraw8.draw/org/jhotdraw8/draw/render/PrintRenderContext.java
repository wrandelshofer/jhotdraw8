/*
 * @(#)PrintRenderContext.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.render;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NullableObjectKey;

/**
 * PrintRenderContext.
 *
 * @author Werner Randelshofer
 */
public interface PrintRenderContext extends RenderContext {

    // ---
    // keys
    // ---
    /**
     * The figure which defines the layout of the current print page. This is
     * typically a {@link org.jhotdraw8.draw.figure.Page}.
     */
    @NonNull
    Key<Figure> PAGE_FIGURE = new NullableObjectKey<>("pageFigure", Figure.class);
    /**
     * Defines the current internal page number of a page figure. A page figure
     * may define the layout for multiple pages - for example for continuous
     * form paper.
     */
    @NonNull
    Key<Integer> INTERNAL_PAGE_NUMBER = new NullableObjectKey<>("internalPageNumber", Integer.class);
    /**
     * Defines the current page number of the print job.
     * <p>
     * This number starts with 1 instead of with 0.
     */
    @NonNull
    Key<Integer> PAGE_NUMBER = new NullableObjectKey<>("pageNumber", Integer.class);
    /**
     * Defines the total number of a pages in the print job.
     */
    @NonNull
    Key<Integer> NUMBER_OF_PAGES = new NullableObjectKey<>("numberOfPages", Integer.class);

}
