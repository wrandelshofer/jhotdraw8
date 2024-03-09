/*
 * @(#)LineMain.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.examples.mini;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;
import org.jhotdraw8.geom.AwtShapes;
import org.jhotdraw8.geom.FXPathElementsBuilder;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.stream.Collectors;

public class LineMain extends Application {
    @Override
    public void start(Stage primaryStage) {
        StackPane pane = new StackPane();
        float v = 4f;
        for (int i = 0; i < 5; i++) {
            switch (i) {
            case 0: {
                Line line = new Line(100, 100, 200, 100);
                line.setStrokeWidth(v);
                line.setManaged(false);
                line.setStrokeLineCap(StrokeLineCap.ROUND);
                pane.getChildren().add(line);
                break;
            }
            case 4: {
                Polyline line = new Polyline(100, 100 + 20 * i, 200, 100 + 20 * i);
                line.setStrokeWidth(v);
                line.setManaged(false);
                line.setStrokeLineJoin(StrokeLineJoin.ROUND);
                line.setStrokeLineCap(StrokeLineCap.ROUND);
                pane.getChildren().add(line);
                break;
            }
            case 1: {
                Line line = new Line(100, 100 + 20 * i, 200, 100 + 20 * i);
                line.setStrokeWidth(v);
                line.setManaged(false);
                line.setStrokeLineCap(StrokeLineCap.SQUARE);
                pane.getChildren().add(line);
                break;
            }
            case 2: {
                Line line = new Line(100, 100 + 20 * i, 200, 100 + 20 * i);
                line.setStrokeWidth(v);
                line.setManaged(false);
                line.setStrokeLineCap(StrokeLineCap.BUTT);
                pane.getChildren().add(line);
                break;
            }
            case 3: {
                Line2D.Double line = new Line2D.Double(100, 100 + 20 * i, 200, 100 + 20 * i);
                BasicStroke stroke = new BasicStroke(v, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                FXPathElementsBuilder builder = new FXPathElementsBuilder();
                AwtShapes.buildFromPathIterator(builder, stroke.createStrokedShape(line).getPathIterator(null));
                Path path = new Path();
                path.getElements().setAll(builder.build());
                System.out.println(path.getElements().stream().map(Object::toString).collect(Collectors.joining("\n")));
                path.setFill(Color.BLACK);
                path.setStroke(null);
                path.setFillRule(FillRule.NON_ZERO);
                pane.getChildren().add(path);
                path.setManaged(false);

                break;
            }
            }
        }
        Parent root = pane;
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("JHotDraw: Line Sample");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
