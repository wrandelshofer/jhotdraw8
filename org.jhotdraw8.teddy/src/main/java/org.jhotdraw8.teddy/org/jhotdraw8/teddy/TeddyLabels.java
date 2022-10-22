/*
 * @(#)TeddyLabels.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.teddy;

import org.jhotdraw8.application.resources.Resources;

public class TeddyLabels {
    private static Resources labels;

    private TeddyLabels() {
    }

    public static Resources getResources() {
        if (labels == null) {
            labels = Resources.getResources("org.jhotdraw8.teddy", "org.jhotdraw8.teddy.Labels");
        }
        return labels;
    }
}
