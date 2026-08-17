package org.jhotdraw8.icollection.jmh;

import kotlinx.collections.immutable.ExtensionsKt;
import kotlinx.collections.immutable.PersistentSet;
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

/// <pre>
/// # JMH version: 1.37
/// /// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// /// # Mac Mini M4 Pro, 4.40 GHz
/// /// org.jetbrains.kotlin:kotlinx-collections-immutable-jvm:0.5.1
///
/// KotlinxPersistentOrderedSetJmh.mAddOneByOne            10  avgt    2        260.016          ns/op
/// KotlinxPersistentOrderedSetJmh.mAddOneByOne          1000  avgt    2      57478.315          ns/op
/// KotlinxPersistentOrderedSetJmh.mAddOneByOne       1000000  avgt    2  294205511.069          ns/op
/// KotlinxPersistentOrderedSetJmh.mContainsFound          10  avgt    2          2.183          ns/op
/// KotlinxPersistentOrderedSetJmh.mContainsFound        1000  avgt    2          7.025          ns/op
/// KotlinxPersistentOrderedSetJmh.mContainsFound     1000000  avgt    2         64.619          ns/op
/// KotlinxPersistentOrderedSetJmh.mContainsNotFound       10  avgt    2          2.139          ns/op
/// KotlinxPersistentOrderedSetJmh.mContainsNotFound     1000  avgt    2          6.934          ns/op
/// KotlinxPersistentOrderedSetJmh.mContainsNotFound  1000000  avgt    2         71.897          ns/op
/// KotlinxPersistentOrderedSetJmh.mHead                   10  avgt    2          1.631          ns/op
/// KotlinxPersistentOrderedSetJmh.mHead                 1000  avgt    2          4.458          ns/op
/// KotlinxPersistentOrderedSetJmh.mHead              1000000  avgt    2          8.080          ns/op
/// KotlinxPersistentOrderedSetJmh.mIterate                10  avgt    2         42.249          ns/op
/// KotlinxPersistentOrderedSetJmh.mIterate              1000  avgt    2      31220.172          ns/op
/// KotlinxPersistentOrderedSetJmh.mIterate           1000000  avgt    2  199119751.894          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemoveThenAdd          10  avgt    2         61.013          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemoveThenAdd        1000  avgt    2        239.771          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemoveThenAdd     1000000  avgt    2       1215.506          ns/op
/// KotlinxPersistentOrderedSetJmh.mTail                   10  avgt    2         17.053          ns/op
/// KotlinxPersistentOrderedSetJmh.mTail                 1000  avgt    2         74.904          ns/op
/// KotlinxPersistentOrderedSetJmh.mTail              1000000  avgt    2        195.761          ns/op
/// </pre>
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class KotlinxPersistentOrderedSetJmh {
    @Param({"10", "1000", "1000000"})
    private int size;

    private final int mask = ~64;

    private BenchmarkData data;
    private PersistentSet<Key> setA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = ExtensionsKt.toPersistentSet(data.setA);
    }

    @Benchmark
    public PersistentSet<Integer> mAddOneByOne() {
        PersistentSet<Integer> set = ExtensionsKt.persistentSetOf();
        for (int i = 0; i < size; i++) {
            set = set.add(i);
        }
        return set;
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
    public PersistentSet<Key> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return setA.remove(key).add(key);
    }

    @Benchmark
    public Key mHead() {
        return setA.iterator().next();
    }

    @Benchmark
    public PersistentSet<Key> mTail() {
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
}
