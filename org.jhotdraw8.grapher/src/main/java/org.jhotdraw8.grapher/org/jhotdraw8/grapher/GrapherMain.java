/*
 * @(#)GrapherMain.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.grapher;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * GrapherMain.
 *
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
        GrapherApplication.main(args);
    }

}
