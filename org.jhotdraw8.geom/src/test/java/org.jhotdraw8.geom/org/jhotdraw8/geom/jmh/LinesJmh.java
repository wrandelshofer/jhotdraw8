package org.jhotdraw8.geom.jmh;

import org.jhotdraw8.geom.Lines;
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
 * Benchmark              Mode  Cnt  Score   Error  Units
 * LinesJmh.arcLength     avgt    4  1.554 ± 0.030  ns/op
 * LinesJmh.invArcLength  avgt    4  2.447 ± 0.016  ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 4)
@Warmup(iterations = 4)
@Fork(value = 1, jvmArgsAppend = {"-Xmx15g",})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class LinesJmh {
    private final double[] curve = {190, 200, 430, 280};

    @Benchmark
    public double arcLength() {
        return Lines.arcLength(curve, 0);

    }

    @Benchmark
    public double invArcLength() {
        return Lines.invArcLength(curve, 0, 70);
    }
}
