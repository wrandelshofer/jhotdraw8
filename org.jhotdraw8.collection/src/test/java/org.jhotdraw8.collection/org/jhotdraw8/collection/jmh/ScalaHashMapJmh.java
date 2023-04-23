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
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;
import scala.Tuple2;
import scala.collection.immutable.HashMap;
import scala.collection.mutable.ReusableBuilder;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                           (size)  Mode  Cnt     _     Score   Error  Units
 * ScalaHashMapJmh.mContainsFound          10  avgt    2     _     6.293          ns/op
 * ScalaHashMapJmh.mContainsFound         100  avgt    2     _    11.190          ns/op
 * ScalaHashMapJmh.mContainsFound        1000  avgt    2     _    19.242          ns/op
 * ScalaHashMapJmh.mContainsFound       10000  avgt    2     _    37.319          ns/op
 * ScalaHashMapJmh.mContainsFound      100000  avgt    2     _    61.474          ns/op
 * ScalaHashMapJmh.mContainsFound     1000000  avgt    2     _   236.853          ns/op
 * ScalaHashMapJmh.mContainsNotFound       10  avgt    2     _     6.142          ns/op
 * ScalaHashMapJmh.mContainsNotFound      100  avgt    2     _    11.060          ns/op
 * ScalaHashMapJmh.mContainsNotFound     1000  avgt    2     _    18.948          ns/op
 * ScalaHashMapJmh.mContainsNotFound    10000  avgt    2     _    36.833          ns/op
 * ScalaHashMapJmh.mContainsNotFound   100000  avgt    2     _    62.170          ns/op
 * ScalaHashMapJmh.mContainsNotFound  1000000  avgt    2     _   236.771          ns/op
 * ScalaHashMapJmh.mCopyOf                 10  avgt    2     _   427.623          ns/op
 * ScalaHashMapJmh.mCopyOf                100  avgt    2     _  6041.627          ns/op
 * ScalaHashMapJmh.mCopyOf               1000  avgt    2     _116621.624          ns/op
 * ScalaHashMapJmh.mCopyOf              10000  avgt    2    1_214286.106          ns/op
 * ScalaHashMapJmh.mCopyOf             100000  avgt    2   19_659581.694          ns/op
 * ScalaHashMapJmh.mCopyOf            1000000  avgt    2  378_301523.574          ns/op
 * ScalaHashMapJmh.mHead                   10  avgt    2     _     1.703          ns/op
 * ScalaHashMapJmh.mHead                  100  avgt    2     _     8.889          ns/op
 * ScalaHashMapJmh.mHead                 1000  avgt    2     _     9.973          ns/op
 * ScalaHashMapJmh.mHead                10000  avgt    2     _    17.188          ns/op
 * ScalaHashMapJmh.mHead               100000  avgt    2     _    17.328          ns/op
 * ScalaHashMapJmh.mHead              1000000  avgt    2     _    23.052          ns/op
 * ScalaHashMapJmh.mIterate                10  avgt    2     _     9.738          ns/op
 * ScalaHashMapJmh.mIterate               100  avgt    2     _   333.533          ns/op
 * ScalaHashMapJmh.mIterate              1000  avgt    2     _  3001.226          ns/op
 * ScalaHashMapJmh.mIterate             10000  avgt    2     _ 41247.895          ns/op
 * ScalaHashMapJmh.mIterate            100000  avgt    2    1_035473.760          ns/op
 * ScalaHashMapJmh.mIterate           1000000  avgt    2   41_417461.544          ns/op
 * ScalaHashMapJmh.mPut                    10  avgt    2     _    19.341          ns/op
 * ScalaHashMapJmh.mPut                   100  avgt    2     _    38.989          ns/op
 * ScalaHashMapJmh.mPut                  1000  avgt    2     _    60.961          ns/op
 * ScalaHashMapJmh.mPut                 10000  avgt    2     _    92.897          ns/op
 * ScalaHashMapJmh.mPut                100000  avgt    2     _   159.113          ns/op
 * ScalaHashMapJmh.mPut               1000000  avgt    2     _   391.962          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd          10  avgt    2     _    87.681          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd         100  avgt    2     _   128.919          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd        1000  avgt    2     _   265.190          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd       10000  avgt    2     _   269.952          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd      100000  avgt    2     _   350.621          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd     1000000  avgt    2     _   670.761          ns/op
 * ScalaHashMapJmh.mTail                   10  avgt    2     _    36.535          ns/op
 * ScalaHashMapJmh.mTail                  100  avgt    2     _    44.574          ns/op
 * ScalaHashMapJmh.mTail                 1000  avgt    2     _    72.841          ns/op
 * ScalaHashMapJmh.mTail                10000  avgt    2     _    92.309          ns/op
 * ScalaHashMapJmh.mTail               100000  avgt    2     _    90.661          ns/op
 * ScalaHashMapJmh.mTail              1000000  avgt    2     _   113.395          ns/op
 *
 * Process finished with exit code 0
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 0)
@Warmup(iterations = 0)
@Fork(value = 0)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ScalaHashMapJmh {
    @Param({"10", "100", "1000", "10000", "100000", "1000000"})
    private int size;

    private final int mask = ~64;

    private BenchmarkData data;
    private HashMap<Key, Boolean> mapA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        ReusableBuilder<Tuple2<Key, Boolean>, HashMap<Key, Boolean>> b = HashMap.newBuilder();
        for (Key key : data.setA) {
            b.addOne(new Tuple2<>(key, Boolean.TRUE));
        }
        mapA = b.result();

        //estimateMemoryUsage(mapA, mapA.head());
    }

    private void estimateMemoryUsage(HashMap<Key, Boolean> mapA, Tuple2<Key, Boolean> head) {
        VM.current();
        long instanceSize = ClassLayout.parseInstance(mapA).instanceSize();
        long elementSize = ClassLayout.parseInstance(head).instanceSize();
        long dataSize = elementSize * mapA.size();
        long dataStructureSize = instanceSize - dataSize;
        System.out.println("\ninstance size           : " + instanceSize);
        System.out.println("element size            : " + elementSize);
        System.out.println("data size               : " + dataSize);
        System.out.println("data structure size     : " + dataStructureSize + " " + (100 * dataStructureSize / instanceSize) + "%");
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
    public HashMap<Key, Boolean> mTail() {
        return mapA.tail();
    }

    @Benchmark
    public HashMap<Key, Boolean> mCopyOf() {
        ReusableBuilder<Tuple2<Key, Boolean>, HashMap<Key, Boolean>> b = HashMap.newBuilder();
        for (Key key : data.setA) {
            b.addOne(new Tuple2<>(key, Boolean.TRUE));
        }
        return b.result();
    }
}
