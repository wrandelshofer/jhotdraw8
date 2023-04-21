/*
 * @(#)TeddyMain.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.teddy;

import javafx.application.Application;
import javafx.stage.Stage;

public class TeddyMain extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {
        new TeddyApplication().start(primaryStage);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        TeddyApplication.main(args);
    }

}
