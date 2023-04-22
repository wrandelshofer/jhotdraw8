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
import scala.collection.immutable.VectorMap;
import scala.collection.mutable.Builder;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * implementation 'org.scala-lang:scala-library:2.13.11-M1'
 *
 * Benchmark                            (mask)   (size)  Mode  Cnt          Score   Error  Units
 * ScalaVectorMapJmh.mContainsFound        -65  1000000  avgt             251.689          ns/op
 * ScalaVectorMapJmh.mContainsNotFound     -65  1000000  avgt             269.800          ns/op
 * ScalaVectorMapJmh.mCopyOf               -65  1000000  avgt       470588123.727          ns/op
 * ScalaVectorMapJmh.mHead                 -65  1000000  avgt              35.886          ns/op
 * ScalaVectorMapJmh.mIterate              -65  1000000  avgt       332692170.290          ns/op
 * ScalaVectorMapJmh.mPut                  -65  1000000  avgt             532.145          ns/op
 * ScalaVectorMapJmh.mRemoveThenAdd        -65  1000000  avgt            1269.680          ns/op
 * ScalaVectorMapJmh.mTail                 -65  1000000  avgt             232.868          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 2)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ScalaVectorMapJmh {
    @Param({"1000000"})
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
        public int mIterate() {
            int sum = 0;
            for (var i = mapA.keysIterator(); i.hasNext(); ) {
                sum += i.next().value;
            }
            return sum;
        }

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
        public VectorMap<Key, Boolean> mTail() {
            return mapA.tail();
        }

    @Benchmark
    public VectorMap<Key, Boolean> mCopyOf() {
        Builder<Tuple2<Key, Boolean>, VectorMap<Key, Boolean>> b = VectorMap.newBuilder();
        for (Key key : data.setA) {
            b.addOne(new Tuple2<>(key, Boolean.TRUE));
        }
        return b.result();
    }

}
