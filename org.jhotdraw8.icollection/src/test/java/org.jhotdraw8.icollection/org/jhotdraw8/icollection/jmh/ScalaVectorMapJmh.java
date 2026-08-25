package org.jhotdraw8.icollection.jmh;

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
import scala.collection.immutable.Map;
import scala.collection.immutable.VectorMap;
import scala.collection.mutable.Builder;

import java.util.concurrent.TimeUnit;

/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro
/// # org.scala-lang:scala-library:3.9.0-RC4
///
/// Benchmark                            (mask)   (size)  Mode  Cnt          Score   Error  Units
/// ScalaVectorMapJmh.mContainsFound        -65       10  avgt    2          2.722          ns/op
/// ScalaVectorMapJmh.mContainsFound        -65     1000  avgt    2          6.559          ns/op
/// ScalaVectorMapJmh.mContainsFound        -65  1000000  avgt    2         91.466          ns/op
/// ScalaVectorMapJmh.mContainsNotFound     -65       10  avgt    2          2.740          ns/op
/// ScalaVectorMapJmh.mContainsNotFound     -65     1000  avgt    2          6.589          ns/op
/// ScalaVectorMapJmh.mContainsNotFound     -65  1000000  avgt    2         83.953          ns/op
/// ScalaVectorMapJmh.mCopyOf               -65       10  avgt    2        326.806          ns/op
/// ScalaVectorMapJmh.mCopyOf               -65     1000  avgt    2      39331.491          ns/op
/// ScalaVectorMapJmh.mCopyOf               -65  1000000  avgt    2  234333686.535          ns/op
/// ScalaVectorMapJmh.mHead                 -65       10  avgt    2          2.667          ns/op
/// ScalaVectorMapJmh.mHead                 -65     1000  avgt    2          5.815          ns/op
/// ScalaVectorMapJmh.mHead                 -65  1000000  avgt    2          8.372          ns/op
/// ScalaVectorMapJmh.mIterate              -65       10  avgt    2         33.585          ns/op
/// ScalaVectorMapJmh.mIterate              -65     1000  avgt    2       9395.086          ns/op
/// ScalaVectorMapJmh.mIterate              -65  1000000  avgt    2  106866265.734          ns/op
/// ScalaVectorMapJmh.mPut                  -65       10  avgt    2         15.636          ns/op
/// ScalaVectorMapJmh.mPut                  -65     1000  avgt    2         36.752          ns/op
/// ScalaVectorMapJmh.mPut                  -65  1000000  avgt    2        369.541          ns/op
/// ScalaVectorMapJmh.mRemoveOneByOne       -65       10  avgt    2        343.803          ns/op
/// ScalaVectorMapJmh.mRemoveOneByOne       -65     1000  avgt    2     116820.951          ns/op
/// ScalaVectorMapJmh.mRemoveOneByOne       -65  1000000  avgt    2  951841187.500          ns/op
/// ScalaVectorMapJmh.mRemoveThenAdd        -65       10  avgt    2         61.402          ns/op
/// ScalaVectorMapJmh.mRemoveThenAdd        -65     1000  avgt    2        165.058          ns/op
/// ScalaVectorMapJmh.mRemoveThenAdd        -65  1000000  avgt    2        894.186          ns/op
/// ScalaVectorMapJmh.mTail                 -65       10  avgt    2         20.827          ns/op
/// ScalaVectorMapJmh.mTail                 -65     1000  avgt    2         57.453          ns/op
/// ScalaVectorMapJmh.mTail                 -65  1000000  avgt    2        115.194          ns/op
///
/// ```
@SuppressWarnings("unchecked")
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ScalaVectorMapJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private VectorMap<Key, Boolean> mapA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        Builder<Tuple2<Key, Boolean>, VectorMap<Key, Boolean>> b = VectorMap.newBuilder();
        for (Key key : data.setA) {
            b.addOne(new Tuple2<>(key, Boolean.TRUE));
        }
        mapA = b.result();
    }

    @Benchmark
    public Map<Key, Boolean> mAdd() {
        Map<Key, Boolean> b = (Map<Key, Boolean>) (Map<?, ?>) VectorMap.newBuilder().result();
        for (Key key : data.setA) {
            b = b.$plus(new Tuple2<>(key, Boolean.TRUE));
        }
        return b;
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
    public VectorMap<Key, Boolean> mCopyOf() {
        Builder<Tuple2<Key, Boolean>, VectorMap<Key, Boolean>> b = VectorMap.newBuilder();
        for (Key key : data.setA) {
            b.addOne(new Tuple2<>(key, Boolean.TRUE));
        }
        return b.result();
    }

    @Benchmark
    public Key mHead() {
        return mapA.head()._1;
    }

    @Benchmark
    public int mIterateEntries() {
        int sum = 0;
        for (var i = mapA.iterator(); i.hasNext(); ) {
            sum += i.next()._1.value;
        }
        return sum;
    }

    @Benchmark
    public int mIterateKeys() {
        int sum = 0;
        for (var i = mapA.keysIterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

    @Benchmark
    public Object mPut() {
        Key key = data.nextKeyInA();
        return mapA.$plus(new Tuple2<>(key, Boolean.FALSE));
    }

    @Benchmark
    public VectorMap<Key, Boolean> mRemoveOneByOne() {
        var map = mapA;
        for (var e : data.listA) {
            map = map.removed(e);
        }
        if (!map.isEmpty()) throw new AssertionError("map: " + map);
        return map;
    }

    @Benchmark
    public Object mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return mapA.$minus(key).$plus(new Tuple2<>(key, Boolean.TRUE));
    }

    @Benchmark
    public VectorMap<Key, Boolean> mTail() {
        return mapA.tail();
    }


}
