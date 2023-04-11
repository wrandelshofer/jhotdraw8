package org.jhotdraw8.fxcontrols.spi;

import org.jhotdraw8.application.resources.Resources;

import java.util.ResourceBundle;

public class Labels {
    public static ResourceBundle getResourceBundle() {
        return Resources.getResources("org.jhotdraw8.fxcontrols",
                "org.jhotdraw8.fxcontrols.spi.labels").asResourceBundle();
    }
}
