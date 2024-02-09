package org.jhotdraw8.icollection.jmh;


import kotlinx.collections.immutable.ExtensionsKt;
import kotlinx.collections.immutable.PersistentSet;
import org.jhotdraw8.icollection.ChampSet;
import org.jhotdraw8.icollection.MutableChampSet;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.37
 * # VM version: JDK 21.0.1, OpenJDK 64-Bit Server VM, 21.0.1+12-LTS
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 * Benchmark                (size)  Mode  Cnt           Score   Error  Units
 * mAddImmutableOneByOne  16777216  avgt       9474719088.500          ns/op
 * mAddMutableOneByOne    16777216  avgt       7219866077.000          ns/op
 * mKotlinXAddOneByOne    16777216  avgt       8008086475.000          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1, jvmArgsAppend = {"-Xmx31g",})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class MutableChampSetJmh {
    @Param({"16777216"})
    int size;


    @Benchmark
    public ChampSet<Integer> mAddMutableOneByOne() {
        Random rng = new Random(7);
        MutableChampSet<Integer> set = new MutableChampSet<>();
        for (int i = 0; i < size; i++) {
            set.add(rng.nextInt());
        }
        return set.toImmutable();
    }

    @Benchmark
    public ChampSet<Integer> mAddImmutableOneByOne() {
        Random rng = new Random(7);
        ChampSet<Integer> set = ChampSet.of();
        for (int i = 0; i < size; i++) {
            set = set.add(rng.nextInt());
        }
        return set;
    }

    @Benchmark
    public PersistentSet<Integer> mKotlinXAddOneByOne() {
        Random rng = new Random(7);
        PersistentSet<Integer> set = ExtensionsKt.persistentHashSetOf();
        for (int i = 0; i < size; i++) {
            set = set.add(rng.nextInt());
        }
        return set;
    }
}
