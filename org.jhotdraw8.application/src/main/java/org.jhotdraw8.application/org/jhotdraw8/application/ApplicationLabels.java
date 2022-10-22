/*
 * @(#)ApplicationLabels.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application;

import org.jhotdraw8.util.Resources;

/**
 * ApplicationLabels.
 *
 * @author Werner Randelshofer
  */
public class ApplicationLabels {

    private static Resources labels;
    private static Resources guilabels;

    /**
     * Prevent instanced creation.
     */
    private ApplicationLabels() {
    }

    public static Resources getResources() {
        if (labels == null) {
            labels = Resources.getResources("org.jhotdraw8.application", "org.jhotdraw8.application.Labels");
        }
        return labels;
    }

    public static void setResources(Resources labels) {
        ApplicationLabels.labels = labels;
    }

    public static void setGuiResources(Resources labels) {
        ApplicationLabels.guilabels = labels;
    }

    public static Resources getGuiResources() {
        if (guilabels == null) {
            guilabels = Resources.getResources("org.jhotdraw8.application", "org.jhotdraw8.gui.Labels");
        }
        return guilabels;
    }


}
