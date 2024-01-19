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
 * QuadCurvesJmh.arcLengthIntegrated     avgt    4  191.519 ± 4.653  ns/op
 * QuadCurvesJmh.arcLengthClosedForm     avgt    4   31.658 ± 0.122  ns/op
 * QuadCurvesJmh.invArcLengthIntegrated  avgt    4  333.711 ± 7.070  ns/op
 * QuadCurvesJmh.invArcLengthClosedForm  avgt    4  132.272 ± 0.486  ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 4)
@Warmup(iterations = 4)
@Fork(value = 1, jvmArgsAppend = {"-Xmx15g",})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class QuadCurvesJmh {
    private final double[] curve = {190, 200, 330, 280, 240, 100};

    @Benchmark
    public double arcLength() {
        return QuadCurves.arcLengthIntegrated(curve, 0, 1, 0.125);
    }

    @Benchmark
    public double invArcLength() {
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
