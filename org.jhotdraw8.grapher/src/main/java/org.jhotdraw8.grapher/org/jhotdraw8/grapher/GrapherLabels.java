/*
 * @(#)GrapherLabels.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.grapher;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.resources.Resources;

public class GrapherLabels {
    private GrapherLabels() {

    }

    public static final @NonNull String RESOURCE_BUNDLE = "org.jhotdraw8.grapher.Labels";

    private static final @NonNull String MODULE_NAME = "org.jhotdraw8.grapher";

    public static @NonNull Resources getResources() {
        return Resources.getResources(MODULE_NAME, RESOURCE_BUNDLE);
    }

}
