/*
 * @(#)BezierArcLengthExampleMain.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.examples.mini;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.util.MathUtil;
import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.collection.primitive.DoubleArrayList;
import org.jhotdraw8.geom.*;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.ToDoubleFunction;

import static java.lang.Math.round;
import static org.jhotdraw8.geom.CubicCurves.getArcLengthIntegrand;

// https://math.stackexchange.com/questions/1954845/bezier-curvature-extrema
public class BezierArcLengthExampleMain extends Application {
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
            setPosition(MathUtil.clamp(round(evt.getX() / 10) * 10, 4, Integer.MAX_VALUE),
                    MathUtil.clamp(round(evt.getY() / 10) * 10, 4, Integer.MAX_VALUE));
            handlesChanged();
        }
    }

    private StackPane canvas;
    private Group pointsOfInterest;
    private Group timePoints;
    private Group distancePoints;
    private Group quadraticCurves;
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
        quadraticCurves = new Group();
        quadraticCurves.setManaged(false);
        canvas.getChildren().addAll(quadraticCurves, curve, label);

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
        distancePoints.getChildren().clear();
        addApproximatedDistancePointsGaussLegendre(distancePoints);
        addApproximatedDistancePoints(distancePoints);
        quadraticCurves.getChildren().clear();
        addApproximatedQuadraticCurves(quadraticCurves);
        updateDistancePoints(distancePoints);
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

        CubicCurveCharacteristics.Characteristics characteristics = CubicCurveCharacteristics.characteristics(b, 0);
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

        double romberg = arcLengthRomberg(b, 0, 0.001);
        double simpson = arcLengthSimpson(b, 0, 0.001);
        double gaussLegendre = arcLengthGaussLegendre(b, 0);

        label.setText("length"
                + "\n ∑:" + (float) param.length + " segs: " + param.segments.size()
                + "\n romberg:" + (float) romberg
                + "\n simpson:" + (float) simpson
                + "\n gauss:" + (float) gaussLegendre
                + "\n " + characteristics
        );
    }

    private void updateDistancePoints(Group distancePoints) {
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

    private void addApproximatedDistancePoints(Group distancePoints) {
        double[] b = FXCubicCurves.toArray(curve);

        double totalLength = CubicCurves.arcLength(b, 0, 1.0);


        for (int i = 1, n = 10; i < n; i++) {
            double s = i / (double) n;
            double t = CubicCurves.invArcLength(b, 0, s * totalLength);
            PointAndDerivative pat = CubicCurves.eval(b, 0, t);
            javafx.geometry.Point2D p = pat.getPoint(javafx.geometry.Point2D::new);
            Circle e = new Circle(p.getX(), p.getY(), 4);
            e.setFill(Color.RED);
            distancePoints.getChildren().add(e);
        }
    }

    private void addApproximatedQuadraticCurves(Group quadraticCurves) {
        double[] b = FXCubicCurves.toArray(curve);
        double[] q = new double[16 * 6];
        int n = QuadCurves.approximateCubicCurve(b, 0, q, 0, 0.5);


        for (int i = 0; i < n; i++) {
            int offset = i * 6;
            QuadCurve c = FXQuadCurves.ofArray(q, offset);
            c.setStroke(Color.YELLOW);
            c.setStrokeWidth(5.0);
            c.setFill(null);
            quadraticCurves.getChildren().add(c);
        }
    }

    private void addApproximatedDistancePointsGaussLegendre(Group distancePoints) {
        double[] b = {
                curve.getStartX(),
                curve.getStartY(),
                curve.getControlX1(),
                curve.getControlY1(),
                curve.getControlX2(),
                curve.getControlY2(),
                curve.getEndX(),
                curve.getEndY()
        };
        ToDoubleFunction<Double> f = getArcLengthIntegrand(b, 0);
        OrderedPair<ToDoubleFunction<Double>, Double> pair = Solvers.invPolynomialChebyshevApprox(20, Integrals::gaussLegendre7, f, 0, 1);
        //OrderedPair<ToDoubleFunction<Double>, Double> pair = IntegralAlgorithms.invSpeedPolynomialChebyshevApprox(20, (f1, t0, t1) -> IntegralAlgorithms.rombergQuadrature(f1, t0, t1,0.1), f, 0, 1);
        //OrderedPair<ToDoubleFunction<Double>, Double> pair = IntegralAlgorithms.invPolynomialApprox3( IntegralAlgorithms::gaussLegendre7, f, 0, 1);
        ToDoubleFunction<Double> sToT = pair.first();
        double totalLength = pair.second();


        for (int i = 1, n = 10; i < n; i++) {
            double s = i / (double) n;
            double t = sToT.applyAsDouble(s * totalLength);
            PointAndDerivative pat = CubicCurves.eval(b, 0, t);
            javafx.geometry.Point2D p = pat.getPoint(javafx.geometry.Point2D::new);
            Circle e = new Circle(p.getX(), p.getY(), 4);
            e.setFill(Color.ORANGE);
            distancePoints.getChildren().add(e);
        }
    }

    private void updateTimePoints() {
        timePoints.getChildren().clear();
        for (int i = 1, n = 10; i < n; i++) {
            double t = i / (double) n;
            PointAndDerivative pat = CubicCurves.eval(curve.getStartX(),
                    curve.getStartY(),
                    curve.getControlX1(),
                    curve.getControlY1(),
                    curve.getControlX2(),
                    curve.getControlY2(),
                    curve.getEndX(),
                    curve.getEndY(), t);
            javafx.geometry.Point2D perp = new javafx.geometry.Point2D(pat.dy(), -pat.dx());
            javafx.geometry.Point2D tg = perp.normalize();

            timePoints.getChildren().add(new Line(pat.x() - tg.getX(), pat.y() - tg.getY(),
                    pat.x() + tg.getX(), pat.y() + tg.getY()));

        }

    }

    /**
     * inflection points: black
     * cusp: white
     * max curvature: red
     */
    private void updatePointsOfInterest() {

        DoubleArrayList infl = CubicCurveCharacteristics.inflectionPoints(curve.getStartX(),
                curve.getStartY(),
                curve.getControlX1(),
                curve.getControlY1(),
                curve.getControlX2(),
                curve.getControlY2(),
                curve.getEndX(),
                curve.getEndY());
        pointsOfInterest.getChildren().clear();
        if (infl.size() == 2 && Points.almostEqual(infl.get(0), infl.get(1), 0.09)) {
            double cusp = (infl.get(0) + infl.get(1)) / 2;
            addPoint(cusp, Color.WHITE, Color.BLACK);
        } else {
            addPoints(infl, Color.BLACK, null);
        }
        //if (infl.isEmpty()) {
            Double p = CubicCurveCharacteristics.singularPoint(curve.getStartX(),
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
        //}
    }

    private void addPoints(DoubleArrayList infl, Color fill, Color stroke) {
        for (double t : infl) {
            addPoint(t, fill, stroke);
        }
    }

    private void addPoint(double t, Color fill, Color stroke) {

        PointAndDerivative pat = CubicCurves.eval(curve.getStartX(),
                curve.getStartY(),
                curve.getControlX1(),
                curve.getControlY1(),
                curve.getControlX2(),
                curve.getControlY2(),
                curve.getEndX(),
                curve.getEndY(), t);
        Circle circle = new Circle(pat.x(), pat.y(), 4);
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
                this.length = Points.distance(x0, y0, x1, y1);
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
            int index = Math.min(search < 0 ? ~search - 1 : search, segments.size() - 1);
            if (index < 0) {
                return new javafx.geometry.Point2D(0, 0);// path is empty
            } else {
                Segment segment = segments.get(index);
                return segment.interpolate(pos);
            }
        }

        private void addNonDegeneratedSegment(double moveX, double moveY, double prevX, double prevY, DoubleSummaryStatistics sum, List<Segment> list) {
            Segment seg = new Segment(sum.getSum(), moveX, moveY, prevX, prevY);
            if (!Points.almostZero(seg.length)) {
                list.add(seg);
                sum.accept(seg.length);
            }
        }
    }


    public static double arcLengthGaussLegendre(double[] b, int offset) {
        ToDoubleFunction<Double> f = getArcLengthIntegrand(b, offset);
        return Integrals.gaussLegendre7(f, 0, 1);
    }

    public static double arcLengthRomberg(double[] b, int offset, double eps) {
        ToDoubleFunction<Double> f = getArcLengthIntegrand(b, offset);
        return Integrals.rombergQuadrature(f, 0, 1, eps);
    }

    public static double arcLengthSimpson(double[] b, int offset, double eps) {
        ToDoubleFunction<Double> f = getArcLengthIntegrand(b, offset);
        return Integrals.simpson(f, 0, 1, eps);
    }
}
