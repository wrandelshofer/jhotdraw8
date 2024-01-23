/*
 * @(#)DrawLabels.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.text;


import java.util.ResourceBundle;

public class FXBaseLabels {
    private static ResourceBundle labels;

    private FXBaseLabels() {
    }

    public static ResourceBundle getResources() {
        if (labels == null) {
            labels = ResourceBundle.getBundle("org.jhotdraw8.fxbase.text.Labels");
        }
        return labels;
    }
}
