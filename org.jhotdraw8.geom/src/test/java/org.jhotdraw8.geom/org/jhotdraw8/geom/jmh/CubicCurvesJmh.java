package org.jhotdraw8.geom.jmh;

import org.jhotdraw8.geom.CubicCurves;
import org.jhotdraw8.geom.Lines;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.PathIterator;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.37
 * # VM version: JDK 21.0.1, OpenJDK 64-Bit Server VM, 21.0.1+12-LTS
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 * Benchmark                                                (loop)  Mode  Cnt    Score   Error  Units
 * CubicCurvesJmh.arcLengthIntegrated                            0  avgt    2   89.458          ns/op
 * CubicCurvesJmh.arcLengthIntegrated                            1  avgt    2   97.762          ns/op
 * CubicCurvesJmh.invArcLengthIntegrated                         0  avgt    2  175.696          ns/op
 * CubicCurvesJmh.invArcLengthIntegrated                         1  avgt    2  282.595          ns/op
 * CubicCurvesJmh.invArcLengthIntegratedWithKnownArcLength       0  avgt    2   80.758          ns/op
 * CubicCurvesJmh.invArcLengthIntegratedWithKnownArcLength       1  avgt    2  196.637          ns/op
 * </pre>
 * <pre>
 * # JMH version: 1.37
 * # VM version: JDK 21, OpenJDK 64-Bit Server VM, 21+35
 * # Apple M2 Max @ 3.70GHz
 * Benchmark                                                (loop)  Mode  Cnt    Score   Error  Units
 * CubicCurvesJmh.arcLengthIntegrated                            0  avgt    2   33.879          ns/op
 * CubicCurvesJmh.arcLengthIntegrated                            1  avgt    2   34.120          ns/op
 * CubicCurvesJmh.invArcLengthIntegrated                         0  avgt    2  104.898          ns/op
 * CubicCurvesJmh.invArcLengthIntegrated                         1  avgt    2  189.029          ns/op
 * CubicCurvesJmh.invArcLengthIntegratedWithKnownArcLength       0  avgt    2   39.451          ns/op
 * CubicCurvesJmh.invArcLengthIntegratedWithKnownArcLength       1  avgt    2  117.550          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@Fork(value = 1, jvmArgsAppend = {"-Xmx15g",})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class CubicCurvesJmh {
    @Param({"0", "1"})
    private int loop;

    // Curve with a loop
    private final double[] curveWithLoop = {190, 200, 310, 100, 250, 280, 240, 100};

    // Curve without a loop
    private final double[] curveWithoutLoop = {190, 200, 270, 280, 330, 280, 240, 100};
    private double[] curve;
    private double arcLength;

    @Setup
    public void setup() {
        if (loop == 0) curve = curveWithoutLoop;
        else curve = curveWithLoop;
        arcLength = CubicCurves.arcLength(curve, 0, 1, 0.125);
    }

    @Benchmark
    public double arcLengthIntegrated() {
        return CubicCurves.arcLength(curve, 0, 1, 0.125);
    }

    @Benchmark
    public double invArcLengthIntegrated() {
        return CubicCurves.invArcLength(curve, 0, 70, 0.125);
    }


    @Benchmark
    public double invArcLengthIntegratedWithKnownArcLength() {
        return CubicCurves.invArcLength(curve, 0, 70, arcLength, 0.125);
    }

    /**
     * Computes the arc length s from time 0 to time t using a flattening path iterator.
     *
     * @param p       points of the curve
     * @param offset  index of the first point in array {@code p}
     * @param t       the time
     * @param epsilon the error tolerance
     * @return the arc length
     */
    public static double arcLengthIterator(double[] p, int offset, double t, double epsilon) {
        PathIterator it = new CubicCurve2D.Double(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]).getPathIterator(null, epsilon);
        double arcLength = 0;
        double lastX = 0, lastY = 0;
        while (!it.isDone()) {
            if (it.currentSegment(p) == PathIterator.SEG_LINETO) {
                arcLength += Lines.arcLength(lastX, lastY, p[0], p[1]);
            }
            lastX = p[0];
            lastY = p[1];
            it.next();
        }
        return arcLength;
    }
}
