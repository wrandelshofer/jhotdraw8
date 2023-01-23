/*
 * @(#)GrapherMain.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.grapher;

import javafx.application.Application;
import javafx.stage.Stage;
import org.jhotdraw8.theme.ThemeManager;
import org.jhotdraw8.theme.atlantafx.NordDarkTheme;
import org.jhotdraw8.theme.atlantafx.NordLightTheme;
import org.jhotdraw8.theme.atlantafx.PrimerDarkTheme;
import org.jhotdraw8.theme.atlantafx.PrimerLightTheme;

/**
 * GrapherMain.
 *
 * @author Werner Randelshofer
 */
public class GrapherMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        new GrapherApplication().start(primaryStage);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        final ThemeManager mgr = ThemeManager.getInstance();
        mgr.getThemes().addAll(new PrimerDarkTheme(), new PrimerLightTheme(), new NordLightTheme(), new NordDarkTheme());
        mgr.setTheme(mgr.getThemes().get(mgr.getThemes().size() - 1));
        GrapherApplication.main(args);
    }

}
