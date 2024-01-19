package org.jhotdraw8.geom.jmh;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.geom.CubicCurves;
import org.jhotdraw8.geom.Lines;
import org.jhotdraw8.geom.QuadCurves;
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
 * Benchmark                              (loop)  Mode  Cnt     Score     Error  Units
 * CubicCurvesJmh.arcLengthApproximated        0  avgt    4   567.448 ±  17.763  ns/op
 * CubicCurvesJmh.arcLengthApproximated        1  avgt    4   800.193 ±  40.960  ns/op
 * CubicCurvesJmh.arcLengthIntegrated          0  avgt    4   399.558 ±   5.771  ns/op
 * CubicCurvesJmh.arcLengthIntegrated          1  avgt    4   390.106 ±  60.572  ns/op
 * CubicCurvesJmh.arcLengthIterator            0  avgt    4  1090.686 ±  23.838  ns/op
 * CubicCurvesJmh.arcLengthIterator            1  avgt    4  1355.627 ± 579.652  ns/op
 * CubicCurvesJmh.invArcLengthIntegrated       0  avgt    4   654.982 ± 131.034  ns/op
 * CubicCurvesJmh.invArcLengthIntegrated       1  avgt    4   892.800 ±  37.217  ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 4)
@Warmup(iterations = 4)
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
    }


    @Benchmark
    public double invArcLengthIntegrated() {
        return CubicCurves.invArcLength(curve, 0, 70, 0.125);
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
