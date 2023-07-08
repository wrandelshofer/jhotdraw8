package org.jhotdraw8.collection.jmh;

import org.jhotdraw8.collection.MutableVectorList;
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

import java.math.BigInteger;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                                (size)  Mode  Cnt          Score   Error  Units
 * mAddAll                 10  avgt              77.242          ns/op
 * mAddAll               1000  avgt            3709.905          ns/op
 * mAddAll            1000000  avgt        11080872.333          ns/op
 * mAddFirst               10  avgt              38.269          ns/op
 * mAddFirst             1000  avgt              53.137          ns/op
 * mAddFirst          1000000  avgt             146.635          ns/op
 * mAddLast                10  avgt              19.775          ns/op
 * mAddLast              1000  avgt              31.118          ns/op
 * mAddLast           1000000  avgt             119.156          ns/op
 * mAddOneByOne            10  avgt             182.656          ns/op
 * mAddOneByOne          1000  avgt           40228.752          ns/op
 * mAddOneByOne       1000000  avgt       135684419.365          ns/op
 * mContainsNotFound       10  avgt              11.607          ns/op
 * mContainsNotFound     1000  avgt            1253.487          ns/op
 * mContainsNotFound  1000000  avgt         8327974.089          ns/op
 * mGet                    10  avgt               4.193          ns/op
 * mGet                  1000  avgt               7.513          ns/op
 * mGet               1000000  avgt              88.663          ns/op
 * mHead                   10  avgt               2.158          ns/op
 * mHead                 1000  avgt               2.660          ns/op
 * mHead              1000000  avgt               5.910          ns/op
 * mIterate                10  avgt              12.823          ns/op
 * mIterate              1000  avgt            2088.217          ns/op
 * mIterate           1000000  avgt        22462897.942          ns/op ?
 * mRemoveAtIndex          10  avgt              44.574          ns/op
 * mRemoveAtIndex        1000  avgt            2872.760          ns/op
 * mRemoveAtIndex     1000000  avgt         3297735.069          ns/op
 * mRemoveLast             10  avgt              15.569          ns/op
 * mRemoveLast           1000  avgt              17.339          ns/op
 * mRemoveLast        1000000  avgt              19.416          ns/op
 * mReversedIterate        10  avgt              10.292          ns/op
 * mReversedIterate      1000  avgt            1915.076          ns/op
 * mReversedIterate   1000000  avgt        22342912.355          ns/op ?
 * mSet                    10  avgt              24.423          ns/op
 * mSet                  1000  avgt              41.456          ns/op
 * mSet               1000000  avgt             237.644          ns/op
 * mTail                   10  avgt              16.312          ns/op
 * mTail                 1000  avgt              16.619          ns/op
 * mTail              1000000  avgt              20.296          ns/op
 *
 * Process finished with exit code 0
 *
 * Process finished with exit code 0
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1, jvmArgsAppend = {"-Xmx28g"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class MutableVectorListJmh {
    @Param({"10", "1000", "1000000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private MutableVectorList<Key> listA;

    private int index;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        listA = new MutableVectorList();
        for (Key key : data.setA) {
            listA.add(key);
        }
        index = Math.min(listA.size() - 1, BigInteger.valueOf(listA.size() / 2).nextProbablePrime().intValue());
    }


    @Benchmark
    public MutableVectorList<Key> mAddAll() {
        return new MutableVectorList<>(data.setA);
    }

    @Benchmark
    public MutableVectorList<Key> mAddOneByOne() {
        MutableVectorList<Key> set = new MutableVectorList<>();
        for (Key key : data.listA) {
            set.add(key);
        }
        return set;
    }

    //@Benchmark
    public MutableVectorList<Key> mRemoveOneByOne() {
        var map = listA.clone();
        for (var e : data.listA) {
            map.remove(e);
        }
        if (!map.isEmpty()) throw new AssertionError("map: " + map);
        return map;
    }

    //@Benchmark
    public boolean mRemoveAll() {
        MutableVectorList<Key> set = listA.clone();
        return set.removeAll(data.listA);
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Iterator<Key> i = listA.iterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

    @Benchmark
    public int mReversedIterate() {
        int sum = 0;
        for (int i = listA.size() - 1; i >= 0; i--) {
            sum += listA.get(i).value;
        }
        return sum;
    }

    @Benchmark
    public Key mTail() {
        return listA.clone().remove(0);
    }

    @Benchmark
    public boolean mAddLast() {
        Key key = data.nextKeyInB();
        return listA.clone().add(key);
    }

    @Benchmark
    public int mAddFirst() {
        Key key = data.nextKeyInB();
        MutableVectorList<Key> clone = listA.clone();
        clone.addFirst(key);
        return clone.size();
    }


    @Benchmark
    public Key mRemoveLast() {
        MutableVectorList<Key> clone = listA.clone();
        return clone.remove(clone.size() - 1);
    }

    @Benchmark
    public Key mRemoveAtIndex() {
        MutableVectorList<Key> clone = listA.clone();
        return clone.remove(index);
    }

    @Benchmark
    public Key mGet() {
        int index = data.nextIndexInA();
        return listA.get(index);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return listA.contains(key);
    }

    @Benchmark
    public Key mHead() {
        return listA.get(0);
    }

    @Benchmark
    public Key mSet() {
        int index = data.nextIndexInA();
        Key key = data.nextKeyInB();
        return listA.set(index, key);
    }


}
