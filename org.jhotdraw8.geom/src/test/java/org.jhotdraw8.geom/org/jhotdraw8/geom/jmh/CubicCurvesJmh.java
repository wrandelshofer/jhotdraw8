package org.jhotdraw8.geom.jmh;

import org.jhotdraw8.annotation.NonNull;
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
 * # JMH version: 1.37
 * # VM version: JDK 21.0.1, OpenJDK 64-Bit Server VM, 21.0.1+12-LTS
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 * <pre>
 * Benchmark                                   (loop)  Mode  Cnt     Score    Error  Units
 * CubicCurvesJmh.arcLengthIntegrated               0  avgt    2   118.195           ns/op
 * CubicCurvesJmh.arcLengthIntegrated               1  avgt    2   126.333           ns/op
 * CubicCurvesJmh.arcLengthIntegratedFloat          0  avgt    2   104.553           ns/op
 * CubicCurvesJmh.arcLengthIntegratedFloat          1  avgt    2   102.218           ns/op
 * CubicCurvesJmh.invArcLengthIntegrated            0  avgt    2   212.101           ns/op
 * CubicCurvesJmh.invArcLengthIntegrated            1  avgt    2   325.903           ns/op
 * CubicCurvesJmh.invArcLengthIntegratedFloat       0  avgt    2   177.288           ns/op
 * CubicCurvesJmh.invArcLengthIntegratedFloat       1  avgt    2   270.904           ns/op
 * CubicCurvesJmh.arcLengthApproximated             0  avgt    4   539.221 ± 31.352  ns/op
 * CubicCurvesJmh.arcLengthApproximated             1  avgt    4   755.975 ±  1.517  ns/op
 * CubicCurvesJmh.arcLengthIterator                 0  avgt    4  1061.041 ± 17.832  ns/op
 * CubicCurvesJmh.arcLengthIterator                 1  avgt    4  1263.672 ± 11.160  ns/op
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

    @Setup
    public void setup() {
        if (loop == 0) curve = curveWithoutLoop;
        else curve = curveWithLoop;
    }
    /*
    public static void main(String... args){
     double[]curve={190,200 ,270,280,330,280,240,1000};
        double[]q=new double[8*6];
        int n = QuadCurves.approximateCubicCurve(curve, 0, q, 0, 0.125);
System.out.println(n);
    }*/

    @Benchmark
    public double arcLengthIntegrated() {
        return CubicCurves.arcLengthIntegrated(curve, 0, 1, 0.125);
    }

    @Benchmark
    public float arcLengthIntegratedFloat() {
        return CubicCurves.arcLengthIntegratedFloat(curve, 0, 1, 0.125);
    }

    /*
    @Benchmark
    public double arcLengthApproximated() {
        double[] q = new double[8 * 6];
        int n = QuadCurves.approximateCubicCurve(curve, 0, q, 0, 0.125);
        double arcLength = 0;
        for (int i = 0; i < n; i++) {
            arcLength += QuadCurves.arcLengthClosedForm(q, i * 6, 1);
        }
        return arcLength;
    }

    @Benchmark
    public double arcLengthIterator() {
        return arcLengthIterator(curve, 0, 1, 0.125);
    }*/


    @Benchmark
    public double invArcLengthIntegrated() {
        return CubicCurves.invArcLength(curve, 0, 70, 0.125);
    }

    @Benchmark
    public float invArcLengthIntegratedFloat() {
        return CubicCurves.invArcLengthFloat(curve, 0, 70, 0.125f);
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
    public static double arcLengthIterator(double @NonNull [] p, int offset, double t, double epsilon) {
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
