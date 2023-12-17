/*
 * @(#)CssExample.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.examples.mini;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.SimpleDrawingEditor;
import org.jhotdraw8.draw.SimpleDrawingView;
import org.jhotdraw8.draw.connector.RectangleConnector;
import org.jhotdraw8.draw.constrain.GridConstrainer;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.LineConnectionFigure;
import org.jhotdraw8.draw.figure.LineFigure;
import org.jhotdraw8.draw.figure.RectangleFigure;
import org.jhotdraw8.draw.figure.SimpleDrawing;
import org.jhotdraw8.draw.figure.StyleableFigure;
import org.jhotdraw8.draw.figure.TextFigure;
import org.jhotdraw8.draw.render.SimpleRenderContext;
import org.jhotdraw8.draw.tool.SelectionTool;
import org.jhotdraw8.draw.tool.Tool;
import org.jhotdraw8.icollection.VectorList;

import java.net.URI;
import java.util.ArrayList;

/**
 * CssExample..
 *
 * @author Werner Randelshofer
 */
public class CssExample extends Application {

    @Override
    public void start(@NonNull Stage primaryStage) throws Exception {
        Drawing drawing = new SimpleDrawing();

        RectangleFigure vertex1 = new RectangleFigure(10, 10, 30, 20);
        RectangleFigure vertex2 = new RectangleFigure(50, 40, 30, 20);
        TextFigure vertex3 = new TextFigure(120, 50, "Lorem Ipsum");
        RectangleFigure vertex4 = new RectangleFigure(90, 100, 30, 20);

        LineConnectionFigure edge12 = new LineConnectionFigure();
        LineConnectionFigure edge23 = new LineConnectionFigure();
        LineConnectionFigure edge3Null = new LineConnectionFigure();
        LineConnectionFigure edgeNullNull = new LineConnectionFigure();

        edge12.setStartConnection(vertex1, new RectangleConnector());
        edge12.setEndConnection(vertex2, new RectangleConnector());

        edge23.setStartConnection(vertex2, new RectangleConnector());
        edge23.setEndConnection(vertex3, new RectangleConnector());
        edge3Null.setStartConnection(vertex3, new RectangleConnector());
        edge3Null.set(LineConnectionFigure.END, new CssPoint2D(145, 15));
        edgeNullNull.set(LineConnectionFigure.START, new CssPoint2D(65, 90));
        edgeNullNull.set(LineConnectionFigure.END, new CssPoint2D(145, 95));

        LineFigure line1 = new LineFigure();
        line1.set(LineFigure.START, new CssPoint2D(50, 150));
        line1.set(LineFigure.END, new CssPoint2D(100, 150));

        drawing.addChild(vertex1);
        drawing.addChild(vertex2);
        drawing.addChild(vertex3);
        drawing.addChild(vertex4);

        drawing.addChild(edge12);
        drawing.addChild(edge23);
        drawing.addChild(edge3Null);
        drawing.addChild(edgeNullNull);
        drawing.addChild(line1);

        vertex1.set(StyleableFigure.ID, "vertex1");
        vertex2.set(StyleableFigure.ID, "vertex2");
        vertex3.set(StyleableFigure.ID, "vertex3");
        vertex4.set(StyleableFigure.ID, "vertex4");

        ArrayList<URI> stylesheets = new ArrayList<>();
        stylesheets.add(CssExample.class.getResource("CssExample.css").toURI());
        drawing.set(Drawing.USER_AGENT_STYLESHEETS, VectorList.copyOf(stylesheets));

        SimpleRenderContext ctx = new SimpleRenderContext();
        drawing.updateAllCss(ctx);
        drawing.layoutAll(ctx);

        DrawingView drawingView = new SimpleDrawingView();

        drawingView.setDrawing(drawing);
        drawingView.setConstrainer(new GridConstrainer(10, 10));
        //drawingView.setHandleType(HandleType.RESHAPE);

        DrawingEditor drawingEditor = new SimpleDrawingEditor();
        drawingEditor.drawingViewsProperty().add(drawingView);

        Tool tool = new SelectionTool();
        drawingEditor.setActiveTool(tool);

        ScrollPane root = new ScrollPane();
        root.setContent(drawingView.getNode());
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
