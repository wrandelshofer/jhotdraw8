package org.jhotdraw8.collection.jmh;


import org.jhotdraw8.collection.ChampSet2;
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

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                       (mask)    (size)  Mode  Cnt           Score   Error  Units
 * ChampSet2Jmh.mAddAll               -65        10  avgt              274.325          ns/op
 * ChampSet2Jmh.mAddAll               -65      1000  avgt            90300.208          ns/op
 * ChampSet2Jmh.mAddAll               -65    100000  avgt         13388451.599          ns/op
 * ChampSet2Jmh.mAddAll               -65  10000000  avgt       2365112655.200          ns/op
 * ChampSet2Jmh.mAddOneByOne          -65        10  avgt              288.286          ns/op
 * ChampSet2Jmh.mAddOneByOne          -65      1000  avgt            95478.077          ns/op
 * ChampSet2Jmh.mAddOneByOne          -65    100000  avgt         26756657.203          ns/op
 * ChampSet2Jmh.mAddOneByOne          -65  10000000  avgt       7163838121.500          ns/op
 * Benchmark                       (mask)    (size)  Mode  Cnt           Score   Error  Units
 * ChampSetJmh2.mAddAll               -65        10  avgt              299.029          ns/op
 * ChampSetJmh2.mAddAll               -65      1000  avgt           101661.056          ns/op
 * ChampSetJmh2.mAddAll               -65    100000  avgt         28510783.276          ns/op
 * ChampSetJmh2.mAddAll               -65  10000000  avgt       7867896525.500          ns/op
 * ChampSetJmh2.mAddOneByOne          -65        10  avgt              386.462          ns/op
 * ChampSetJmh2.mAddOneByOne          -65      1000  avgt            95329.003          ns/op
 * ChampSetJmh2.mAddOneByOne          -65    100000  avgt         26094557.031          ns/op
 * ChampSetJmh2.mAddOneByOne          -65  10000000  avgt       8582859814.000          ns/op
 * ChampSetJmh2.mContainsFound        -65        10  avgt                5.341          ns/op
 * ChampSetJmh2.mContainsFound        -65      1000  avgt               20.362          ns/op
 * ChampSetJmh2.mContainsFound        -65    100000  avgt              101.872          ns/op
 * ChampSetJmh2.mContainsFound        -65  10000000  avgt              408.755          ns/op
 * ChampSetJmh2.mContainsNotFound     -65        10  avgt                6.483          ns/op
 * ChampSetJmh2.mContainsNotFound     -65      1000  avgt               21.479          ns/op
 * ChampSetJmh2.mContainsNotFound     -65    100000  avgt              108.947          ns/op
 * ChampSetJmh2.mContainsNotFound     -65  10000000  avgt              408.480          ns/op
 * ChampSetJmh2.mHead                 -65        10  avgt               20.987          ns/op
 * ChampSetJmh2.mHead                 -65      1000  avgt               38.869          ns/op
 * ChampSetJmh2.mHead                 -65    100000  avgt               56.003          ns/op
 * ChampSetJmh2.mHead                 -65  10000000  avgt               69.059          ns/op
 * ChampSetJmh2.mIterate              -65        10  avgt               82.528          ns/op
 * ChampSetJmh2.mIterate              -65      1000  avgt            15732.755          ns/op
 * ChampSetJmh2.mIterate              -65    100000  avgt          3162080.575          ns/op
 * ChampSetJmh2.mIterate              -65  10000000  avgt        741951114.286          ns/op
 * ChampSetJmh2.mRemoveAll            -65        10  avgt              349.331          ns/op
 * ChampSetJmh2.mRemoveAll            -65      1000  avgt            96891.189          ns/op
 * ChampSetJmh2.mRemoveAll            -65    100000  avgt         27901980.554          ns/op
 * ChampSetJmh2.mRemoveAll            -65  10000000  avgt       7407686125.000          ns/op
 * ChampSetJmh2.mRemoveOneByOne       -65        10  avgt              348.263          ns/op
 * ChampSetJmh2.mRemoveOneByOne       -65      1000  avgt           100670.795          ns/op
 * ChampSetJmh2.mRemoveOneByOne       -65    100000  avgt         27430586.644          ns/op
 * ChampSetJmh2.mRemoveOneByOne       -65  10000000  avgt       7717383428.500          ns/op
 * ChampSetJmh2.mRemoveThenAdd        -65        10  avgt               75.306          ns/op
 * ChampSetJmh2.mRemoveThenAdd        -65      1000  avgt              190.574          ns/op
 * ChampSetJmh2.mRemoveThenAdd        -65    100000  avgt              371.420          ns/op
 * ChampSetJmh2.mRemoveThenAdd        -65  10000000  avgt              892.006          ns/op
 * ChampSetJmh2.mTail                 -65        10  avgt               47.406          ns/op
 * ChampSetJmh2.mTail                 -65      1000  avgt               94.677          ns/op
 * ChampSetJmh2.mTail                 -65    100000  avgt              146.932          ns/op
 * ChampSetJmh2.mTail                 -65  10000000  avgt              185.135          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1, jvmArgsAppend = {"-Xmx28g"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ChampSet2Jmh {
    @Param({"10", "1000", "100000", "10000000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private ChampSet2<Key> setA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = ChampSet2.copyOf(data.setA);
    }

    @Benchmark
    public ChampSet2<Key> mAddAll() {
        return ChampSet2.copyOf(data.listA);
    }

    @Benchmark
    public ChampSet2<Key> mAddOneByOne() {
        ChampSet2<Key> set = ChampSet2.of();
        for (Key key : data.listA) {
            set = set.add(key);
        }
        return set;
    }
/*
    @Benchmark
    public ChampSet2<Key> mRemoveOneByOne() {
        ChampSet2<Key> set = setA;
        for (Key key : data.listA) {
            set = set.remove(key);
        }
        return set;
    }

    @Benchmark
    public ChampSet2<Key> mRemoveAll() {
        ChampSet2<Key> set = setA;
        return set.removeAll(data.listA);
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Key k : setA) {
            sum += k.value;
        }
        return sum;
    }

    @Benchmark
    public ChampSet2<Key> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return setA.remove(key).add(key);
    }

    @Benchmark
    public Key mHead() {
        return setA.iterator().next();
    }

    @Benchmark
    public ChampSet2<Key> mTail() {
        return setA.remove(setA.iterator().next());
    }

    @Benchmark
    public boolean mContainsFound() {
        Key key = data.nextKeyInA();
        return setA.contains(key);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return setA.contains(key);
    }

 */
}
