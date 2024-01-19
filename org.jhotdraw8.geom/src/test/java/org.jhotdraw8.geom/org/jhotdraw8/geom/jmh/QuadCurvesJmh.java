package org.jhotdraw8.geom.jmh;

import org.jhotdraw8.geom.QuadCurves;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * # JMH version: 1.37
 * # VM version: JDK 21.0.1, OpenJDK 64-Bit Server VM, 21.0.1+12-LTS
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 * <pre>
 * Benchmark                             Mode  Cnt    Score   Error  Units
 * QuadCurvesJmh.arcLengthClosedForm     avgt    2   31.815          ns/op
 * QuadCurvesJmh.arcLengthIntegrated     avgt    2  112.491          ns/op
 * QuadCurvesJmh.invArcLengthClosedForm  avgt    2  132.488          ns/op
 * QuadCurvesJmh.invArcLengthIntegrated  avgt    2  189.815          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@Fork(value = 1, jvmArgsAppend = {"-Xmx15g",})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class QuadCurvesJmh {
    private final double[] curve = {190, 200, 330, 280, 240, 100};

    @Benchmark
    public double arcLengthIntegrated() {
        return QuadCurves.arcLengthIntegrated(curve, 0, 1, 0.125);
    }

    @Benchmark
    public double invArcLengthIntegrated() {
        return QuadCurves.invArcLengthIntegrated(curve, 0, 70, 0.125);
    }

    @Benchmark
    public double arcLengthClosedForm() {
        return QuadCurves.arcLengthClosedForm(curve, 0, 1);
    }


    @Benchmark
    public double invArcLengthClosedForm() {
        return QuadCurves.invArcLengthClosedForm(curve, 0, 70, 0.125);
    }
}
