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
 * Benchmark                            (mask)   (size)  Mode  Cnt     _     Score   Error  Units
 * ScalaVectorMapJmh.mContainsFound        -65       10  avgt    2     _     6.529          ns/op
 * ScalaVectorMapJmh.mContainsFound        -65      100  avgt    2     _    11.084          ns/op
 * ScalaVectorMapJmh.mContainsFound        -65     1000  avgt    2     _    20.634          ns/op
 * ScalaVectorMapJmh.mContainsFound        -65    10000  avgt    2     _    36.600          ns/op
 * ScalaVectorMapJmh.mContainsFound        -65   100000  avgt    2     _   100.555          ns/op
 * ScalaVectorMapJmh.mContainsFound        -65  1000000  avgt    2     _   269.156          ns/op
 * ScalaVectorMapJmh.mContainsNotFound     -65       10  avgt    2     _     7.007          ns/op
 * ScalaVectorMapJmh.mContainsNotFound     -65      100  avgt    2     _    11.134          ns/op
 * ScalaVectorMapJmh.mContainsNotFound     -65     1000  avgt    2     _    20.664          ns/op
 * ScalaVectorMapJmh.mContainsNotFound     -65    10000  avgt    2     _    35.706          ns/op
 * ScalaVectorMapJmh.mContainsNotFound     -65   100000  avgt    2     _   104.273          ns/op
 * ScalaVectorMapJmh.mContainsNotFound     -65  1000000  avgt    2     _   254.167          ns/op
 * ScalaVectorMapJmh.mCopyOf               -65       10  avgt    2     _   847.698          ns/op
 * ScalaVectorMapJmh.mCopyOf               -65      100  avgt    2     _  8552.204          ns/op
 * ScalaVectorMapJmh.mCopyOf               -65     1000  avgt    2     _145905.646          ns/op
 * ScalaVectorMapJmh.mCopyOf               -65    10000  avgt    2    1_495972.812          ns/op
 * ScalaVectorMapJmh.mCopyOf               -65   100000  avgt    2   25_365742.926          ns/op
 * ScalaVectorMapJmh.mCopyOf               -65  1000000  avgt    2  469_077151.250          ns/op
 * ScalaVectorMapJmh.mHead                 -65       10  avgt    2     _     7.234          ns/op
 * ScalaVectorMapJmh.mHead                 -65      100  avgt    2     _    21.065          ns/op
 * ScalaVectorMapJmh.mHead                 -65     1000  avgt    2     _    25.789          ns/op
 * ScalaVectorMapJmh.mHead                 -65    10000  avgt    2     _    25.612          ns/op
 * ScalaVectorMapJmh.mHead                 -65   100000  avgt    2     _    26.300          ns/op
 * ScalaVectorMapJmh.mHead                 -65  1000000  avgt    2     _    35.969          ns/op
 * ScalaVectorMapJmh.mIterate              -65       10  avgt    2     _    90.502          ns/op
 * ScalaVectorMapJmh.mIterate              -65      100  avgt    2     _  1609.942          ns/op
 * ScalaVectorMapJmh.mIterate              -65     1000  avgt    2     _ 24484.242          ns/op
 * ScalaVectorMapJmh.mIterate              -65    10000  avgt    2     _487208.667          ns/op
 * ScalaVectorMapJmh.mIterate              -65   100000  avgt    2    9_449481.765          ns/op
 * ScalaVectorMapJmh.mIterate              -65  1000000  avgt    2  330_425467.570          ns/op
 * ScalaVectorMapJmh.mPut                  -65       10  avgt    2     _    32.974          ns/op
 * ScalaVectorMapJmh.mPut                  -65      100  avgt    2     _    60.249          ns/op
 * ScalaVectorMapJmh.mPut                  -65     1000  avgt    2     _    92.300          ns/op
 * ScalaVectorMapJmh.mPut                  -65    10000  avgt    2     _   133.474          ns/op
 * ScalaVectorMapJmh.mPut                  -65   100000  avgt    2     _   230.001          ns/op
 * ScalaVectorMapJmh.mPut                  -65  1000000  avgt    2     _   501.866          ns/op
 * ScalaVectorMapJmh.mRemoveThenAdd        -65       10  avgt    2     _   153.322          ns/op
 * ScalaVectorMapJmh.mRemoveThenAdd        -65      100  avgt    2     _   228.369          ns/op
 * ScalaVectorMapJmh.mRemoveThenAdd        -65     1000  avgt    2     _   392.195          ns/op
 * ScalaVectorMapJmh.mRemoveThenAdd        -65    10000  avgt    2     _   454.382          ns/op
 * ScalaVectorMapJmh.mRemoveThenAdd        -65   100000  avgt    2     _   676.448          ns/op
 * ScalaVectorMapJmh.mRemoveThenAdd        -65  1000000  avgt    2     _  1229.155          ns/op
 * ScalaVectorMapJmh.mTail                 -65       10  avgt    2     _    58.791          ns/op
 * ScalaVectorMapJmh.mTail                 -65      100  avgt    2     _   101.469          ns/op
 * ScalaVectorMapJmh.mTail                 -65     1000  avgt    2     _   133.833          ns/op
 * ScalaVectorMapJmh.mTail                 -65    10000  avgt    2     _   117.610          ns/op
 * ScalaVectorMapJmh.mTail                 -65   100000  avgt    2     _   155.144          ns/op
 * ScalaVectorMapJmh.mTail                 -65  1000000  avgt    2     _   220.500          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ScalaVectorMapJmh {
    @Param({"10", "100", "1000", "10000", "100000", "1000000"})
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
