/*
 * @(#)OffsetPathExampleMain.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.examples.mini;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Polyline;
import javafx.stage.Stage;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.Points;
import org.jhotdraw8.geom.contour.ContourBuilder;
import org.jhotdraw8.geom.contour.PlinePath;
import org.jhotdraw8.geom.contour.PlineVertex;

import java.util.List;

/**
 * OffsetPathSampleMain.
 *
 */
public class ContourPathExampleMain extends Application {
    private final Polyline polyline = new Polyline(
            110, 200,
            160, 180,
            210, 120,
            260, 180,
            310, 200);
    private final Path offsetPath1 = new Path();
    private final Path offsetPath2 = new Path();
    StackPane canvas = new StackPane();
    private final DoubleProperty offset = new SimpleDoubleProperty(0.0);
    private final BooleanProperty closed = new SimpleBooleanProperty();

    @Override
    public void start(Stage primaryStage) {
        BorderPane borderPane = new BorderPane();
        HBox hbox = new HBox();
        Slider slider = new Slider();
        slider.valueProperty().bindBidirectional(offset);
        slider.setMin(-100.0);
        slider.setMax(100.0);
        slider.setMajorTickUnit(10.0);
        slider.setSnapToTicks(true);
        CheckBox checkBox = new CheckBox("Closed");
        checkBox.selectedProperty().bindBidirectional(closed);
        checkBox.selectedProperty().addListener((v, o, n) -> updatePath());
        Button button = new Button("Create Test");
        button.setOnAction(this::createTest);
        hbox.getChildren().add(slider);
        hbox.getChildren().add(checkBox);
        hbox.getChildren().add(button);
        hbox.setSpacing(10.0);

        borderPane.setTop(hbox);
        canvas = new StackPane();
        borderPane.setCenter(canvas);
        polyline.setManaged(false);
        offsetPath1.setManaged(false);
        offsetPath2.setManaged(false);
        offsetPath1.setMouseTransparent(true);
        offsetPath2.setMouseTransparent(true);
        polyline.setMouseTransparent(true);
        offsetPath2.setStroke(Color.BLUE);
        offsetPath2.getStrokeDashArray().addAll(3.0, 3.0);
        polyline.setStroke(Color.LIGHTGRAY);
        canvas.getChildren().add(offsetPath1);
        canvas.getChildren().add(offsetPath2);
        canvas.getChildren().add(polyline);
        offset.addListener(this::onPropertyChanged);
        closed.addListener(this::onPropertyChanged);

        canvas.setOnMouseClicked(this::onMouseClicked);
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        Parent root = borderPane;
        Scene scene = new Scene(root, 400, 300);
        updatePath();
        primaryStage.setTitle("JHotDraw: Offset Path Sample");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createTest(ActionEvent actionEvent) {
        PlinePath pline = createPline(polyline);
        ContourBuilder papb = new ContourBuilder();
        List<PlinePath> offsetPlines = papb.parallelOffset(pline, offset.get());
        StringBuilder buf = new StringBuilder();
        buf.append("dynamicTest(\"1\", () -> doTest(\n");
        dumpPline(pline, buf);
        buf.append(",\n");
        buf.append(offset.get());
        buf.append(",\n");
        buf.append("Arrays.asList(");
        boolean first = true;
        for (PlinePath opl : offsetPlines) {
            if (first) {
                first = false;
            } else {
                buf.append(",\n");
            }
            dumpPline(opl, buf);
        }

        buf.append(")\n");
        buf.append(")),\n");
        System.out.println(buf);
    }

    private void dumpPline(PlinePath pline, StringBuilder buf) {
        buf.append("polylineOf(").append(pline.isClosed()).append(",new double[][]{");

        boolean first = true;
        for (PlineVertex v : pline) {
            if (first) {
                first = false;
            } else {
                buf.append(", ");
            }
            buf.append('{').append(v.getX()).append(", ").append(v.getY()).append(", ").append(v.bulge()).append('}');
        }
        buf.append("})");
    }

    private void onPropertyChanged(Observable observable) {
        updatePath();
    }

    private Integer activePolypoint = null;
    private final double grid = 10.0;

    private void onMouseDragged(MouseEvent mouseEvent) {
        if (activePolypoint != null) {
            polyline.getPoints().set(activePolypoint, Math.floor(mouseEvent.getX() / grid) * grid);
            polyline.getPoints().set(activePolypoint + 1, Math.floor(mouseEvent.getY() / grid) * grid);
            updatePath();
        }
    }

    private void onMousePressed(MouseEvent mouseEvent) {
        activePolypoint = findPolyPoint(mouseEvent.getX(), mouseEvent.getY());
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            if (activePolypoint == null) {
                polyline.getPoints().add(mouseEvent.getX());
                polyline.getPoints().add(mouseEvent.getY());
            } else {
                polyline.getPoints().remove(activePolypoint, activePolypoint + 2);
            }
            updatePath();
        }
    }

    private void updatePath() {

        doOffsetPath(polyline, offsetPath1, offset.get());
        doOffsetPath(polyline, offsetPath2, -offset.get());
        offsetPath1.setStrokeWidth(3.0);
        offsetPath2.setVisible(true);
    }


    private void doOffsetPath(Polyline polyline, Path path, double offset) {
        PlinePath pap = createPline(polyline);
        ContourBuilder papb = new ContourBuilder();
        List<PlinePath> offsetPlines = papb.parallelOffset(pap, offset);

        ObservableList<PathElement> elements = path.getElements();
        elements.clear();
        for (var offPap : offsetPlines) {
            elements.addAll(
                    FXShapes.awtPathIteratorToFXPathElements(offPap.getPathIterator(null)));
        }
    }

    private PlinePath createPline(Polyline polyline) {
        PlinePath pap = new PlinePath();
        ObservableList<Double> points = polyline.getPoints();
        for (int i = 0, n = points.size(); i < n; i += 2) {
            pap.addVertex(points.get(i), points.get(i + 1));
        }
        pap.isClosed(closed.get());
        return pap;
    }


    private Integer findPolyPoint(double x, double y) {
        ObservableList<Double> points = polyline.getPoints();
        Integer index = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int i = 0, n = points.size(); i < n; i += 2) {
            double px = points.get(i);
            double py = points.get(i + 1);
            double sq = Points.squaredDistance(x, y, px, py);
            if (sq < 25 && sq < bestDistance) {
                bestDistance = sq;
                index = i;
            }
        }
        return index;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
