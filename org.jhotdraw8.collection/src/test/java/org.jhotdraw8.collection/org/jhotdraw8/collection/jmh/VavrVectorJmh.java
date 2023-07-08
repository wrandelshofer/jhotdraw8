package org.jhotdraw8.collection.jmh;


import io.vavr.collection.Vector;
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
 * Benchmark            (size)  Mode  Cnt          Score   Error  Units
 * mAddAll                 10  avgt              64.076          ns/op
 * mAddAll               1000  avgt            3901.188          ns/op
 * mAddAll            1000000  avgt        11149468.773          ns/op
 * mAddAllArray            10  avgt              37.557          ns/op
 * mAddAllArray          1000  avgt            4227.075          ns/op
 * mAddAllArray       1000000  avgt        24717308.914          ns/op
 * mAddFirst               10  avgt              97.450          ns/op
 * mAddFirst             1000  avgt              58.759          ns/op
 * mAddFirst          1000000  avgt             201.288          ns/op
 * mAddLast                10  avgt              31.247          ns/op
 * mAddLast              1000  avgt              48.038          ns/op
 * mAddLast           1000000  avgt             158.035          ns/op
 * mAddOneByOne            10  avgt             520.625          ns/op
 * mAddOneByOne          1000  avgt           57651.413          ns/op
 * mAddOneByOne       1000000  avgt       149117084.265          ns/op
 * mContainsNotFound       10  avgt              29.026          ns/op
 * mContainsNotFound     1000  avgt            2957.783          ns/op
 * mContainsNotFound  1000000  avgt        10539231.087          ns/op
 * mGet                    10  avgt               4.153          ns/op
 * mGet                  1000  avgt               7.605          ns/op
 * mGet               1000000  avgt              91.964          ns/op
 * mHead                   10  avgt               2.330          ns/op
 * mHead                 1000  avgt               2.896          ns/op
 * mHead              1000000  avgt               5.705          ns/op
 * mIterate                10  avgt              14.248          ns/op
 * mIterate              1000  avgt            1371.090          ns/op
 * mIterate           1000000  avgt        12618108.195          ns/op
 * mRemoveAtIndex          10  avgt              62.606          ns/op
 * mRemoveAtIndex        1000  avgt            3600.120          ns/op
 * mRemoveAtIndex     1000000  avgt         3686395.115          ns/op
 * mRemoveLast             10  avgt               7.967          ns/op
 * mRemoveLast           1000  avgt               8.969          ns/op
 * mRemoveLast        1000000  avgt               8.651          ns/op
 * mReversedIterate        10  avgt             183.846          ns/op
 * mReversedIterate      1000  avgt           11118.000          ns/op
 * mReversedIterate   1000000  avgt        31897269.280          ns/op
 * mSet                    10  avgt              18.467          ns/op
 * mSet                  1000  avgt              37.343          ns/op
 * mSet               1000000  avgt             209.726          ns/op
 * mTail                   10  avgt               7.729          ns/op
 * mTail                 1000  avgt               8.202          ns/op
 * mTail              1000000  avgt               8.605          ns/op
 *
 * Process finished with exit code 0
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1, jvmArgsAppend = {"-Xmx28g"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class VavrVectorJmh {
    @Param({"10", "1000", "1000000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private Vector<Key> listA;

    private int index;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        listA = Vector.of();
        for (Key key : data.setA) {
            listA = listA.append(key);
        }
        index = Math.min(listA.length() - 1, BigInteger.valueOf(listA.length() / 2).nextProbablePrime().intValue());
    }


    @Benchmark
    public Vector<Key> mAddAllArray() {
        return Vector.<Key>of(data.setA.toArray(new Key[0]));
    }

    @Benchmark
    public Vector<Key> mAddAll() {
        return Vector.ofAll(data.setA);
    }

    @Benchmark
    public Vector<Key> mAddOneByOne() {
        Vector<Key> set = Vector.of();
        for (Key key : data.listA) {
            set = set.append(key);
        }
        return set;
    }

    //@Benchmark
    public Vector<Key> mRemoveOneByOne() {
        var map = listA;
        for (var e : data.listA) {
            map = map.remove(e);
        }
        if (!map.isEmpty()) throw new AssertionError("map: " + map);
        return map;
    }

    //@Benchmark
    public Vector<Key> mRemoveAll() {
        Vector<Key> set = listA;
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
        for (Iterator<Key> i = listA.reverse().iterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

    @Benchmark
    public Vector<Key> mTail() {
        return listA.removeAt(0);
    }

    @Benchmark
    public Vector<Key> mAddLast() {
        Key key = data.nextKeyInB();
        return (listA).append(key);
    }

    @Benchmark
    public Vector<Key> mAddFirst() {
        Key key = data.nextKeyInB();
        return (listA).prepend(key);
    }

    @Benchmark
    public Vector<Key> mRemoveLast() {
        return listA.removeAt(listA.size() - 1);
    }

    @Benchmark
    public Vector<Key> mRemoveAtIndex() {
        return listA.removeAt(index);
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
    public Vector<Key> mSet() {
        int index = data.nextIndexInA();
        Key key = data.nextKeyInB();
        return listA.update(index, key);
    }

}
