/*
 * @(#)Labels.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxcontrols.spi;

import org.jhotdraw8.application.resources.Resources;

import java.util.ResourceBundle;

public class Labels {
    /**
     * Dont' let anyone instantiate this class.
     */
    private Labels() {
    }

    public static ResourceBundle getResourceBundle() {
        return Resources.getResources("org.jhotdraw8.fxcontrols",
                "org.jhotdraw8.fxcontrols.spi.labels").asResourceBundle();
    }
}
