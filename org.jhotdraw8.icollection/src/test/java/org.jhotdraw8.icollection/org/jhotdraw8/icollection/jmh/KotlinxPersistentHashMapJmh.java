package org.jhotdraw8.icollection.jmh;

import kotlinx.collections.immutable.ExtensionsKt;
import kotlinx.collections.immutable.PersistentMap;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
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
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class KotlinxPersistentHashMapJmh {
    @Param({"1000000"})
    private int size;

    private final int mask = ~64;

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
    public PersistentMap<Key, Boolean> mCopyOf() {
        PersistentMap<Key, Boolean> map = ExtensionsKt.persistentHashMapOf();
        for (Key key : data.setA) {
            map = map.put(key, Boolean.TRUE);
        }
        return map;
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mCopyOfX() {
        return ExtensionsKt.toPersistentHashMap(data.mapA);
    }
}