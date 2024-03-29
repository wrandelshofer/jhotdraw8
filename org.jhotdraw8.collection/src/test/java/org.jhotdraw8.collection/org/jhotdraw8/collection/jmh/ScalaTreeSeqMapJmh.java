package org.jhotdraw8.collection.jmh;

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
import scala.Tuple2;
import scala.collection.immutable.TreeSeqMap;
import scala.collection.mutable.Builder;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 *                    (size)  Mode  Cnt    _     Score   Error  Units
 * ContainsFound     1000000  avgt         _   348.505          ns/op
 * ContainsNotFound  1000000  avgt         _   264.846          ns/op
 * Head              1000000  avgt         _    53.705          ns/op
 * Iterate           1000000  avgt       33_279549.804          ns/op
 * Put               1000000  avgt         _  1074.934          ns/op
 * RemoveThenAdd     1000000  avgt         _  1509.428          ns/op
 * Tail              1000000  avgt         _   312.867          ns/op
 * CopyOf            1000000  avgt      846_489177.333          ns/op
 *
 * Benchmark                           (mask)   (size)  Mode  Cnt      _     Score   Error  Units
 * ScalaTreeSeqMapJmh.mRemoveOneByOne     -65       10  avgt    2      _   744.003          ns/op
 * ScalaTreeSeqMapJmh.mRemoveOneByOne     -65      100  avgt    2      _ 13533.827          ns/op
 * ScalaTreeSeqMapJmh.mRemoveOneByOne     -65     1000  avgt    2      _303397.737          ns/op
 * ScalaTreeSeqMapJmh.mRemoveOneByOne     -65    10000  avgt    2     5_825785.719          ns/op
 * ScalaTreeSeqMapJmh.mRemoveOneByOne     -65   100000  avgt    2   119_658560.411          ns/op
 * ScalaTreeSeqMapJmh.mRemoveOneByOne     -65  1000000  avgt    2  1971_439819.167          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 0)
@Warmup(iterations = 0)
@Fork(value = 0)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ScalaTreeSeqMapJmh {
    @Param({"10", "100", "1000", "10000", "100000", "1000000"})
    private int size;

    @Param({"-65"})
    private int mask;


    private BenchmarkData data;
    private TreeSeqMap<Key, Boolean> mapA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        Builder<Tuple2<Key, Boolean>, TreeSeqMap<Key, Boolean>> b = TreeSeqMap.newBuilder();
        for (Key key : data.setA) {
            b.addOne(new Tuple2<>(key, Boolean.TRUE));
        }
        mapA = b.result();
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (var i = mapA.keysIterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

    @SuppressWarnings("unchecked")
    @Benchmark
    public Object mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return mapA.$minus(key).$plus(new Tuple2<>(key, Boolean.TRUE));
    }

    @Benchmark
    public Object mPut() {
        Key key = data.nextKeyInA();
        return mapA.$plus(new Tuple2<>(key, Boolean.FALSE));
    }

    @Benchmark
    public boolean mContainsFound() {
        Key key = data.nextKeyInA();
        return mapA.contains(key);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return mapA.contains(key);
    }

    @Benchmark
    public Key mHead() {
        return mapA.head()._1;
    }

    @Benchmark
    public TreeSeqMap<Key, Boolean> mTail() {
        return mapA.tail();
    }

    @Benchmark
    public TreeSeqMap<Key, Boolean> mCopyOf() {
        Builder<Tuple2<Key, Boolean>, TreeSeqMap<Key, Boolean>> b = TreeSeqMap.newBuilder();
        for (Key key : data.setA) {
            b.addOne(new Tuple2<>(key, Boolean.TRUE));
        }
        return b.result();
    }

    @Benchmark
    public TreeSeqMap<Key, Boolean> mRemoveOneByOne() {
        var map = mapA;
        for (var e : data.listA) {
            map = map.removed(e);
        }
        if (!map.isEmpty()) throw new AssertionError("map: " + map);
        return map;
    }

}
