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

/**
 * <pre>
 * # JMH version: 1.37
 * # VM version: JDK 21, OpenJDK 64-Bit Server VM, 21+35
 * # Apple M2 Max
 *                    (mask)  (size)  Mode  Cnt         Score   Error  Units
 * mContainsFound        -65  100000  avgt    2        38.214          ns/op
 * mContainsNotFound     -65  100000  avgt    2        39.175          ns/op
 * mCopyOf               -65  100000  avgt    2   9738079.390          ns/op
 * mCopyOnyByOne         -65  100000  avgt    2  15165845.241          ns/op
 * mHead                 -65  100000  avgt    2        29.194          ns/op
 * mIterate              -65  100000  avgt    2    933066.347          ns/op
 * mPut                  -65  100000  avgt    2       106.105          ns/op
 * mRemoveThenAdd        -65  100000  avgt    2       216.574          ns/op
 * mTail                 -65  100000  avgt    2        58.428          ns/op
 * </pre>
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 *                    (size)  Mode  Cnt     _     Score   Error  Units
 * ContainsFound     1000000  avgt          _   184.674          ns/op
 * ContainsNotFound  1000000  avgt          _   208.197          ns/op
 * CopyOf            1000000  avgt       399_299237.577          ns/op
 * Head              1000000  avgt          _    44.703          ns/op
 * Iterate           1000000  avgt        46_259569.668          ns/op
 * Put               1000000  avgt          _   353.429          ns/op
 * RemoveThenAdd     1000000  avgt          _   571.652          ns/op
 * Tail              1000000  avgt          _   131.255          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@Fork(value = 1, jvmArgsAppend = {"-Xmx28g"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class KotlinxPersistentHashMapJmh {
    @Param({"100000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private PersistentMap<Key, Boolean> mapA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        mapA = ExtensionsKt.persistentHashMapOf();
        for (Key key : data.setA) {
            mapA = mapA.put(key, Boolean.TRUE);
        }
    }


    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Key k : mapA.keySet()) {
            sum += k.value;
        }
        return sum;
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return mapA.remove(key).put(key, Boolean.TRUE);
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mPut() {
        Key key = data.nextKeyInA();
        return mapA.put(key, Boolean.FALSE);
    }

    @Benchmark
    public boolean mContainsFound() {
        Key key = data.nextKeyInA();
        return mapA.containsKey(key);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return mapA.containsKey(key);
    }

    @Benchmark
    public Key mHead() {
        return mapA.keySet().iterator().next();
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mTail() {
        return mapA.remove(mapA.keySet().iterator().next());
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mCopyOnyByOne() {
        PersistentMap<Key, Boolean> map = ExtensionsKt.persistentHashMapOf();
        for (Key key : data.setA) {
            map = map.put(key, Boolean.TRUE);
        }
        return map;
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mCopyOf() {
        return ExtensionsKt.toPersistentHashMap(data.mapA);
    }
}
