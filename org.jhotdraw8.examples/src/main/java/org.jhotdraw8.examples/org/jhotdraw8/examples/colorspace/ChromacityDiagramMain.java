package org.jhotdraw8.examples.colorspace;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChromacityDiagramMain extends Application {
    public ChromacityDiagramMain() {
    }


    @Override
    public void start(Stage stage) throws Exception {
        ChromacityDiagram chromacityDiagram = new ChromacityDiagram();
        Scene scene = new Scene(chromacityDiagram);
        stage.setScene(scene);
        stage.setTitle("ChromacityDiagram Demo");
        stage.show();
    }
}
