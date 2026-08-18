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
/// # JMH version: 1.28
/// # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
/// # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
///
/// Benchmark           (size)  Mode  Cnt    _     Score         Error  Units
/// mContainsFound     1000000  avgt    4    _   165.449 ±      13.209  ns/op
/// mContainsNotFound  1000000  avgt    4    _   169.791 ±       2.502  ns/op
/// mHead              1000000  avgt    4    _   104.946 ±       3.025  ns/op
/// mIterate           1000000  avgt    4  71_505927.591 ± 1063359.317  ns/op
/// mRemoveThenAdd     1000000  avgt    4    _   458.736 ±       6.936  ns/op
/// mTail              1000000  avgt    4    _   197.068 ±       3.920  ns/op
/// </pre>
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class KotlinxPersistentHashSetJmh {
    @Param({"10", "1000", "1000000"})
    private int size;

    private final int mask = ~64;

    private BenchmarkData data;
    private PersistentSet<Key> setA;

    private PersistentSet<Key> setAA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = ExtensionsKt.toPersistentHashSet(data.setA);
        setAA = ExtensionsKt.toPersistentHashSet(data.listA);
    }

    @Benchmark
    public PersistentSet<Key> mCopyOf() {
        PersistentSet<Key> set = ExtensionsKt.toPersistentHashSet(data.listA);
        assert set.size() == data.listA.size();
        return set;
    }


    @Benchmark
    public PersistentSet<Key> mAdd() {
        PersistentSet<Key> set = ExtensionsKt.persistentSetOf();
        for (Key key : data.listA) {
            set = set.adding(key);
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public PersistentSet<Key> mRemove() {
        PersistentSet<Key> set = setA;
        for (Key key : data.listA) {
            set = set.removing(key);
        }
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public PersistentSet<Key> mRemoveAll() {
        PersistentSet<Key> set = setA;
        PersistentSet<Key> updated = set.removingAll(data.setA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public PersistentSet<Key> mRemoveAllSameType() {
        PersistentSet<Key> set = setA;
        PersistentSet<Key> updated = set.removingAll(setAA);
        assert updated.isEmpty();
        return updated;
    }


    @Benchmark
    public PersistentSet<Key> mRetainAllAllRetained() {
        PersistentSet<Key> set = setA;
        PersistentSet<Key> updated = set.retainingAll(data.setA);
        assert updated == setA;
        return updated;
    }

    @Benchmark
    public PersistentSet<Key> mRetainAllNoneRetained() {
        PersistentSet<Key> set = setA;
        PersistentSet<Key> updated = set.retainingAll(data.setB);
        assert updated.isEmpty();
        return updated;
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
    public Key mGetFirst() {
        return setA.iterator().next();
    }

    @Benchmark
    public PersistentSet<Key> mRemoveFirst() {
        return setA.removing(setA.iterator().next());
    }

    @Benchmark
    public boolean mContainsFound() {
        boolean found = true;
        for (Key k : data.listA) {
            found = setA.contains(k) & found;//must be long-circuit and operator
        }
        return found;
    }

    @Benchmark
    public boolean mContainsNotFound() {
        boolean found = true;
        for (Key k : data.listB) {
            found = setA.contains(k) & found;//must be long-circuit and operator
        }
        return found;
    }
}
