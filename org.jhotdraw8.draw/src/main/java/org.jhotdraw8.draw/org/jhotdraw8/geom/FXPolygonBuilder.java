package org.jhotdraw8.geom;

import javafx.scene.shape.Polygon;
import org.jhotdraw8.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a list of {@link Double}s for a {@link Polygon}.
 */
public class FXPolygonBuilder extends AbstractPathBuilder<List<Double>> {
    private final List<Double> poly = new ArrayList<>();

    @Override
    public @NonNull List<Double> build() {
        return poly;
    }

    @Override
    protected void doClosePath() {
    }

    @Override
    protected void doPathDone() {

    }

    @Override
    protected void doCurveTo(double x1, double y1, double x2, double y2, double x, double y) {
        poly.add(x);
        poly.add(y);
    }

    @Override
    protected void doLineTo(double x, double y) {
        poly.add(x);
        poly.add(y);
    }

    @Override
    protected void doMoveTo(double x, double y) {
        poly.add(x);
        poly.add(y);
    }

    @Override
    protected void doQuadTo(double x1, double y1, double x, double y) {
        poly.add(x);
        poly.add(y);
    }

}
