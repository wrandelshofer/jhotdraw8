package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A shape that can have multiple style-dimensions like a multiple-master font.
 */
public class MultipleMasterShape extends AbstractShape {
    private @NonNull PathData defaultShape;
    private List<double[]> deltas = new ArrayList<>();
    private List<Double> weights = new ArrayList<>();

    /**
     * Creates a new instance from the given shapes.
     * <p>
     * All shapes must have an identical command sequence!
     *
     * @param shapes
     */
    public MultipleMasterShape(PathIterator... shapes) {
        this(Arrays.asList(shapes));
    }

    public MultipleMasterShape(List<PathIterator> shapes) {
        if (shapes.size() < 1) throw new IllegalArgumentException("at least one shape must be given");
        for (var s : shapes) {
            PathData data = AwtShapes.buildFromPathIterator(new PathDataBuilder(), s).build();
            if (defaultShape == null) {
                defaultShape = data;
            } else {
                double[] defaultCoords = defaultShape.coords();
                double[] deltaCoords = data.coords().clone();
                for (int i = 0; i < defaultCoords.length; i++) {
                    deltaCoords[i] = deltaCoords[i] - defaultCoords[i];
                }
                deltas.add(deltaCoords);
                weights.add(0.0);
            }
        }

    }

    public int getNumberOfDimensions() {
        return weights.size();
    }

    /**
     * Set a weight in the range 0 to 1 for a specific style dimension.
     * <p>
     * You can set a weight outside the range to under- or over-compensate a specific style dimension.
     *
     * @param dimension
     * @param value
     */
    public void setWeight(int dimension, double value) {
        weights.set(dimension, value);
    }

    public double getWeight(int dimension) {
        return weights.get(dimension);
    }

    public PathIterator getPathIterator(final @Nullable AffineTransform tx) {
        final AffineTransform tt = tx == null ? AffineTransform.getTranslateInstance(0, 0) : tx;

        return new PathIterator() {
            int current = 0;
            final double[] mixedCoords;
            final int windingRule = defaultShape.windingRule();
            final byte[] commands = defaultShape.commands();
            final int[] offsets = defaultShape.offsets();

            {
                mixedCoords = defaultShape.coords().clone();
                for (int i = 0, n = weights.size(); i < n; i++) {
                    double w = weights.get(i);
                    if (w != 0) {
                        double[] deltas = MultipleMasterShape.this.deltas.get(i);
                        for (int j = 0, m = deltas.length; j < m; j++) {
                            mixedCoords[j] += w * deltas[j];
                        }
                    }
                }
            }

            @Override
            public int getWindingRule() {
                return windingRule;
            }

            @Override
            public boolean isDone() {
                return current >= commands.length;
            }

            @Override
            public void next() {
                if (!isDone()) current++;
            }

            @Override
            public int currentSegment(float[] coords) {
                final int offset = offsets[current];
                switch (commands[current]) {
                    case SEG_MOVETO, SEG_LINETO -> {
                        tt.transform(mixedCoords, offset, coords, 0, 1);
                    }
                    case SEG_QUADTO -> {
                        tt.transform(mixedCoords, offset, coords, 0, 2);
                    }
                    case SEG_CUBICTO -> {
                        tt.transform(mixedCoords, offset, coords, 0, 3);
                    }
                    default -> {//SEG CLOSE

                    }
                }
                return commands[current];
            }

            @Override
            public int currentSegment(double[] coords) {
                final int offset = offsets[current];
                switch (commands[current]) {
                    case SEG_MOVETO, SEG_LINETO -> {
                        tt.transform(mixedCoords, offset, coords, 0, 1);
                    }
                    case SEG_QUADTO -> {
                        tt.transform(mixedCoords, offset, coords, 0, 2);
                    }
                    case SEG_CUBICTO -> {
                        tt.transform(mixedCoords, offset, coords, 0, 3);
                    }
                    default -> {//SEG CLOSE

                    }
                }
                return commands[current];
            }
        };
    }
}
