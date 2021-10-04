package org.jhotdraw8.samples.mini;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.DoubleArrayList;
import org.jhotdraw8.geom.BezierCurves;
import org.jhotdraw8.geom.FXGeom;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.Geom;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;

import static java.lang.Math.round;

// https://math.stackexchange.com/questions/1954845/bezier-curvature-extrema
public class BezierArcLengthSampleMain extends Application {
    private class Handle {

        @NonNull
        Rectangle node = new Rectangle(5, 5);

        {
            node.setManaged(false);
            node.setOnMousePressed(this::onMousePressed);
            node.setOnMouseDragged(this::onMouseDragged);
        }

        public Handle(Color color) {
            node.setFill(color);
            node.setStroke(Color.BLACK);
        }

        private void onMousePressed(MouseEvent evt) {
            node.requestFocus();
        }

        public double getX() {
            return node.getX() + node.getWidth() * 0.5;
        }

        public double getY() {
            return node.getY() + node.getWidth() * 0.5;
        }

        public void setX(double x) {
            node.setX(x - node.getWidth() / 2);
        }

        public void setY(double y) {
            node.setY(y - node.getHeight() * 0.5);
        }

        public void setPosition(double x, double y) {
            node.setX(x - node.getWidth() * 0.5);
            node.setY(y - node.getHeight() * 0.5);
        }

        private void onMouseDragged(@NonNull MouseEvent evt) {
            setPosition(round(evt.getX() / 10) * 10, round(evt.getY() / 10) * 10);
            handlesChanged();
        }
    }

    private StackPane canvas;
    private Group pointsOfInterest;
    private Group timePoints;
    private Group distancePoints;
    private CubicCurve curve;
    private Handle start, end, c1, c2;
    private Text label;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        canvas = new StackPane();

        curve = new CubicCurve();
        curve.setFill(null);
        curve.setStroke(Color.BLACK);

        label = new Text();
        label.setX(10);
        label.setY(20);
        curve.setManaged(false);
        label.setManaged(false);
        canvas.getChildren().addAll(curve, label);

        pointsOfInterest = new Group();
        pointsOfInterest.setManaged(false);
        timePoints = new Group();
        timePoints.setManaged(false);
        distancePoints = new Group();
        distancePoints.setManaged(false);
        canvas.getChildren().addAll(timePoints, distancePoints, pointsOfInterest);

        start = new Handle(Color.RED);
        end = new Handle(Color.RED);
        c1 = new Handle(Color.RED);
        c2 = new Handle(Color.RED);
        start.setPosition(20, 40);
        c1.setPosition(40, 200);
        c2.setPosition(80, 210);
        end.setPosition(200, 60);
        canvas.getChildren().addAll(start.node, end.node, c1.node, c2.node);

        handlesChanged();

        Scene scene = new Scene(canvas, 300, 250);

        stage.setTitle("JHotDraw: Bezier Arc Length");
        stage.setScene(scene);
        stage.show();

    }

    public void handlesChanged() {
        curve.setStartX(start.getX());
        curve.setStartY(start.getY());
        curve.setEndX(end.getX());
        curve.setEndY(end.getY());
        curve.setControlX1(c1.getX());
        curve.setControlY1(c1.getY());
        curve.setControlX2(c2.getX());
        curve.setControlY2(c2.getY());

        updatePointsOfInterest();
        updateTimePoints();
        updateDistancePoints();
        updateText();

    }

    private void updateText() {
        double[] b = {curve.getStartX(),
                curve.getStartY(),
                curve.getControlX1(),
                curve.getControlY1(),
                curve.getControlX2(),
                curve.getControlY2(),
                curve.getEndX(),
                curve.getEndY()};

        BezierCurves.Characteristics characteristics = BezierCurves.characteristics(b);
        PathArcLengthParameterization param = new PathArcLengthParameterization(
                FXShapes.awtShapeFromFX(new CubicCurve(curve.getStartX(),
                        curve.getStartY(),
                        curve.getControlX1(),
                        curve.getControlY1(),
                        curve.getControlX2(),
                        curve.getControlY2(),
                        curve.getEndX(),
                        curve.getEndY())).getPathIterator(null, 1.0)
        );

        double gravesen = BezierCurves.arcLengthGravesen(b, 0.001);
        double romberg = BezierCurves.arcLengthRomberg(b, 0.001);
        double simpson = BezierCurves.arcLengthSimpson(b, 0.001);

        label.setText("length"
                + "\n ∑:" + (float) param.length
                + "\n gravesen:" + (float) gravesen
                + "\n romberg:" + (float) romberg
                + "\n simpson:" + (float) simpson
                + "\n " + characteristics
        );
    }

    private void updateDistancePoints() {
        PathArcLengthParameterization param = new PathArcLengthParameterization(
                FXShapes.awtShapeFromFX(new CubicCurve(curve.getStartX(),
                        curve.getStartY(),
                        curve.getControlX1(),
                        curve.getControlY1(),
                        curve.getControlX2(),
                        curve.getControlY2(),
                        curve.getEndX(),
                        curve.getEndY())).getPathIterator(null, 1.0)
        );
        distancePoints.getChildren().clear();

        for (PathArcLengthParameterization.Segment seg : param.segments) {
            Line e = new Line(seg.x0, seg.y0, seg.x1, seg.y1);
            e.setStroke(Color.LIGHTGRAY);
            distancePoints.getChildren().add(e);
        }


        for (int i = 1, n = 10; i < n; i++) {
            double s = i / (double) n;
            javafx.geometry.Point2D p = param.interpolate(s);
            Circle e = new Circle(p.getX(), p.getY(), 2);
            e.setFill(Color.BLUE);
            distancePoints.getChildren().add(e);
        }
    }

    private void updateTimePoints() {
        timePoints.getChildren().clear();
        for (int i = 1, n = 10; i < n; i++) {
            double t = i / (double) n;
            Point2D.Double p = BezierCurves.evalCubicCurve(curve.getStartX(),
                    curve.getStartY(),
                    curve.getControlX1(),
                    curve.getControlY1(),
                    curve.getControlX2(),
                    curve.getControlY2(),
                    curve.getEndX(),
                    curve.getEndY(), t);
            Point2D.Double tag = BezierCurves.evalCubicCurveTangent(curve.getStartX(),
                    curve.getStartY(),
                    curve.getControlX1(),
                    curve.getControlY1(),
                    curve.getControlX2(),
                    curve.getControlY2(),
                    curve.getEndX(),
                    curve.getEndY(), t);
            javafx.geometry.Point2D perp = new javafx.geometry.Point2D(tag.y, -tag.x);
            javafx.geometry.Point2D tg = perp.normalize();

            timePoints.getChildren().add(new Line(p.x - tg.getX(), p.y - tg.getY(),
                    p.x + tg.getX(), p.y + tg.getY()));

        }

    }

    /**
     * inflection points: black
     * cusp: white
     * max curvature: red
     */
    private void updatePointsOfInterest() {

        DoubleArrayList infl = BezierCurves.inflectionPoints(curve.getStartX(),
                curve.getStartY(),
                curve.getControlX1(),
                curve.getControlY1(),
                curve.getControlX2(),
                curve.getControlY2(),
                curve.getEndX(),
                curve.getEndY());
        pointsOfInterest.getChildren().clear();
        if (infl.size() == 2 && Geom.almostEqual(infl.get(0), infl.get(1), 0.09)) {
            double cusp = (infl.get(0) + infl.get(1)) / 2;
            addPoint(cusp, Color.WHITE, Color.BLACK);
        } else {
            addPoints(infl, Color.BLACK, null);
        }
        if (infl.isEmpty()) {
            Double p = BezierCurves.singularPoint(curve.getStartX(),
                    curve.getStartY(),
                    curve.getControlX1(),
                    curve.getControlY1(),
                    curve.getControlX2(),
                    curve.getControlY2(),
                    curve.getEndX(),
                    curve.getEndY());
            if (p != null) {
                addPoint(p, Color.GRAY, null);
            }
        }
    }

    private void addPoints(DoubleArrayList infl, Color fill, Color stroke) {
        for (double t : infl) {
            addPoint(t, fill, stroke);
        }
    }

    private void addPoint(double t, Color fill, Color stroke) {

        Point2D.Double p = BezierCurves.evalCubicCurve(curve.getStartX(),
                curve.getStartY(),
                curve.getControlX1(),
                curve.getControlY1(),
                curve.getControlX2(),
                curve.getControlY2(),
                curve.getEndX(),
                curve.getEndY(), t);
        Circle circle = new Circle(p.x, p.y, 4);
        circle.setFill(fill);
        circle.setStroke(stroke);
        pointsOfInterest.getChildren().add(circle);
    }

    /**
     * Provides an arc-length parameterization from a flattening path iterator.
     */
    private static class PathArcLengthParameterization {
        private static class Segment implements Comparable<Segment> {
            private final double x0, y0, x1, y1;
            private final double pos, length;

            public Segment(double pos) {
                this(pos, 0, 0, 0, 0);
            }

            public Segment(double pos, double x0, double y0, double x1, double y1) {
                this.x0 = x0;
                this.y0 = y0;
                this.x1 = x1;
                this.y1 = y1;
                this.pos = pos;
                this.length = Geom.distance(x0, y0, x1, y1);
            }

            /**
             * Absolute arc-length position.
             */
            public javafx.geometry.Point2D interpolate(double sabs) {
                //   return new javafx.geometry.Point2D(x0,y0);
                return FXGeom.lerp(x0, y0, x1, y1, (sabs - pos) / length);
            }

            @Override
            public int compareTo(Segment o) {
                return Double.compare(this.pos, o.pos);
            }
        }

        private final List<Segment> segments = new ArrayList<>();
        private final double length;

        public PathArcLengthParameterization(PathIterator it) {

            double[] coords = new double[6];
            double moveX = 0, moveY = 0;
            double prevX = 0, prevY = 0;
            DoubleSummaryStatistics sum = new DoubleSummaryStatistics();
            while (!it.isDone()) {
                switch (it.currentSegment(coords)) {
                case PathIterator.SEG_CLOSE:
                    addNonDegeneratedSegment(moveX, moveY, prevX, prevY, sum, segments);
                    break;
                case PathIterator.SEG_MOVETO:
                    moveX = prevX = coords[0];
                    moveY = prevY = coords[1];
                    break;
                case PathIterator.SEG_LINETO:
                    addNonDegeneratedSegment(prevX, prevY, coords[0], coords[1], sum, segments);
                    prevX = coords[0];
                    prevY = coords[1];
                    break;
                default:
                    throw new IllegalArgumentException("Path iterator is not flattened!");
                }
                it.next();
            }
            this.length = sum.getSum();
        }

        /**
         * Interpolates in {@code s in [0,1]
         */
        public javafx.geometry.Point2D interpolate(double s) {
            double pos = s * length;
            Segment key = new Segment(pos);
            int search = Collections.binarySearch(segments, key);
            int index = Math.min(search < 0 ? -2 - search : search, segments.size() - 1);
            if (index < 0) {
                return new javafx.geometry.Point2D(0, 0);// path is empty
            } else {
                Segment segment = segments.get(index);
                return segment.interpolate(pos);
            }
        }

        private void addNonDegeneratedSegment(double moveX, double moveY, double prevX, double prevY, DoubleSummaryStatistics sum, List<Segment> list) {
            Segment seg = new Segment(sum.getSum(), moveX, moveY, prevX, prevY);
            if (!Geom.almostZero(seg.length)) {
                list.add(seg);
                sum.accept(seg.length);
            }
        }

    }
}
