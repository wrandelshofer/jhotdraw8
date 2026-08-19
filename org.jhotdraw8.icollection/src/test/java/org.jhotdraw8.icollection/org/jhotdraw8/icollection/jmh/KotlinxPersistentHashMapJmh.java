package org.jhotdraw8.icollection.jmh;

import kotlinx.collections.immutable.ExtensionsKt;
import kotlinx.collections.immutable.PersistentMap;
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
/// # VM version: JDK 21, OpenJDK 64-Bit Server VM, 21+35
/// # Apple M2 Max
///                    (mask)  (size)  Mode  Cnt         Score   Error  Units
/// mContainsFound        -65  100000  avgt    2        38.214          ns/op
/// mContainsNotFound     -65  100000  avgt    2        39.175          ns/op
/// mCopyOf               -65  100000  avgt    2   9738079.390          ns/op
/// mCopyOnyByOne         -65  100000  avgt    2  15165845.241          ns/op
/// mHead                 -65  100000  avgt    2        29.194          ns/op
/// mIterate              -65  100000  avgt    2    933066.347          ns/op
/// mPut                  -65  100000  avgt    2       106.105          ns/op
/// mRemoveThenAdd        -65  100000  avgt    2       216.574          ns/op
/// mTail                 -65  100000  avgt    2        58.428          ns/op
/// </pre>
/// <pre>
/// # JMH version: 1.28
/// # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
/// # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
///
///                    (size)  Mode  Cnt     _     Score   Error  Units
/// ContainsFound     1000000  avgt          _   184.674          ns/op
/// ContainsNotFound  1000000  avgt          _   208.197          ns/op
/// CopyOf            1000000  avgt       399_299237.577          ns/op
/// Head              1000000  avgt          _    44.703          ns/op
/// Iterate           1000000  avgt        46_259569.668          ns/op
/// Put               1000000  avgt          _   353.429          ns/op
/// RemoveThenAdd     1000000  avgt          _   571.652          ns/op
/// Tail              1000000  avgt          _   131.255          ns/op
/// </pre>
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class KotlinxPersistentHashMapJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private PersistentMap<Key, Boolean> setA;
    private PersistentMap<Key, Boolean> setAA;
    private PersistentMap<Key, Boolean> setC;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = ExtensionsKt.persistentHashMapOf();
        setAA = ExtensionsKt.persistentHashMapOf();
        setC = ExtensionsKt.persistentHashMapOf();
        for (Key key : data.setA) {
            setA = setA.put(key, Boolean.TRUE);
        }
        for (Key key : data.listA) {
            setAA = setAA.put(key, Boolean.TRUE);
        }
        for (Key key : data.setC) {
            setC = setC.put(key, Boolean.FALSE);
        }
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mPut() {
        PersistentMap<Key, Boolean> set = ExtensionsKt.persistentHashMapOf();
        for (Key key : data.listA) {
            set = set.putting(key, true);
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mPutAll() {
        PersistentMap<Key, Boolean> set = setA;
        PersistentMap<Key, Boolean> updated = set.puttingAll(data.mapC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mPutAllSameType() {
        PersistentMap<Key, Boolean> set = setA;
        PersistentMap<Key, Boolean> updated = set.puttingAll(setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mPutContained() {
        PersistentMap<Key, Boolean> set = setA;
        for (Key key : data.listA) {
            set = set.putting(key, true);
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public int mContainsKey() {
        int count = 0;
        for (Key k : data.listC) {
            if (setA.containsKey(k)) count++;
        }
        assert count == data.listC.size() / 2;
        return count;
    }

    @Benchmark
    public boolean mContainsAll() {
        return setA.keySet().containsAll(data.setA);
    }

    @Benchmark
    public boolean mContainsAllSameType() {
        return setA.keySet().containsAll(setAA.keySet());
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mCopyOf() {
        PersistentMap.Builder<Key, Boolean> builder = ExtensionsKt.<Key, Boolean>persistentHashMapOf().builder();
        builder.putAll(data.mapA);
        PersistentMap<Key, Boolean> set = builder.build();
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public Key mGetFirst() {
        return setA.keySet().iterator().next();
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Key k : setA.keySet()) {
            sum += k.value;
        }
        return sum;
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mRemove() {
        PersistentMap<Key, Boolean> set = setA;
        for (Key key : data.listA) {
            set = set.removing(key);
        }
        assert set.isEmpty();
        return set;
    }

    /*

    @Benchmark
    public PersistentMap<Key,Boolean> mRemoveAll() {
        PersistentMap<Key,Boolean> set = setA;
        PersistentMap<Key,Boolean> updated = set.removingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentMap<Key,Boolean> mRemoveAllSameType() {
        PersistentMap<Key,Boolean> set = setA;
        PersistentMap<Key,Boolean> updated = set.removingAll(setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }*/

    @Benchmark
    public PersistentMap<Key, Boolean> mRemoveFirst() {
        PersistentMap<Key, Boolean> set = setA;
        while (!set.isEmpty()) {
            set = set.removing(set.keySet().iterator().next());
        }
        return set;
    }
/*
    @Benchmark
    public PersistentMap<Key,Boolean> mRetainAll() {
        PersistentMap<Key,Boolean> set = setA;
        PersistentMap<Key,Boolean> updated = set.retainingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentMap<Key,Boolean> mRetainAllSameType() {
        PersistentMap<Key,Boolean> set = setA;
        PersistentMap<Key,Boolean> updated = set.retainingAll(setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }*/
}
