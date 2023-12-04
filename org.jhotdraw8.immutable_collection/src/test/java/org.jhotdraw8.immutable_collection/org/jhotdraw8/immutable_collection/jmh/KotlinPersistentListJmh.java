package org.jhotdraw8.immutable_collection.jmh;

import kotlinx.collections.immutable.ExtensionsKt;
import kotlinx.collections.immutable.PersistentList;
import org.openjdk.jmh.annotations.*;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                                   (size)  Mode  Cnt         Score   Error  Units
 * mAddAll                 10  avgt             97.160          ns/op
 * mAddAll               1000  avgt          10188.886          ns/op
 * mAddAll            1000000  avgt       20908916.386          ns/op
 * mAddFirst               10  avgt             23.522          ns/op
 * mAddFirst             1000  avgt            861.836          ns/op
 * mAddFirst          1000000  avgt         879627.244          ns/op
 * mAddLast                10  avgt             15.656          ns/op
 * mAddLast              1000  avgt             17.050          ns/op
 * mAddLast           1000000  avgt            121.539          ns/op
 * mAddOneByOne            10  avgt            134.930          ns/op
 * mAddOneByOne          1000  avgt          17646.919          ns/op
 * mAddOneByOne       1000000  avgt       54663359.929          ns/op
 * mContainsNotFound       10  avgt              9.016          ns/op
 * mContainsNotFound     1000  avgt           1932.576          ns/op
 * mContainsNotFound  1000000  avgt        9304874.263          ns/op
 * mGet                    10  avgt              4.035          ns/op
 * mGet                  1000  avgt              9.014          ns/op
 * mGet               1000000  avgt             86.176          ns/op
 * mHead                   10  avgt              1.416          ns/op
 * mHead                 1000  avgt              3.071          ns/op
 * mHead              1000000  avgt              4.623          ns/op
 * mIterate                10  avgt              8.479          ns/op
 * mIterate              1000  avgt           2889.195          ns/op
 * mIterate           1000000  avgt       16389335.173          ns/op
 * mRemoveAtIndex          10  avgt             21.289          ns/op
 * mRemoveAtIndex        1000  avgt            414.379          ns/op
 * mRemoveAtIndex     1000000  avgt         383755.161          ns/op
 * mRemoveLast             10  avgt             11.260          ns/op
 * mRemoveLast           1000  avgt             14.001          ns/op
 * mRemoveLast        1000000  avgt             14.021          ns/op
 * mReversedIterate        10  avgt              9.135          ns/op
 * mReversedIterate      1000  avgt           3529.019          ns/op
 * mReversedIterate   1000000  avgt       21765132.591          ns/op
 * mSet                    10  avgt             14.596          ns/op
 * mSet                  1000  avgt             30.664          ns/op
 * mSet               1000000  avgt            199.503          ns/op
 * mTail                   10  avgt             20.860          ns/op
 * mTail                 1000  avgt            746.861          ns/op
 * mTail              1000000  avgt         760448.066          ns/op
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1, jvmArgsAppend = {"-Xmx28g"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class KotlinPersistentListJmh {
    @Param({"10", "1000", "1000000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private PersistentList<Key> listA;

    private int index;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        listA = ExtensionsKt.persistentListOf();
        for (Key key : data.setA) {
            listA = listA.add(key);
        }
        index = Math.min(listA.size() - 1, BigInteger.valueOf(listA.size() / 2).nextProbablePrime().intValue());
    }


    @Benchmark
    public PersistentList<Key> mAddAll() {
        return ExtensionsKt.<Key>persistentListOf().addAll(data.setA);
    }

    @Benchmark
    public PersistentList<Key> mAddOneByOne() {
        PersistentList<Key> set = ExtensionsKt.<Key>persistentListOf();
        for (Key key : data.listA) {
            set = set.add(key);
        }
        return set;
    }

    //@Benchmark
    public PersistentList<Key> mRemoveOneByOne() {
        var map = listA;
        for (var e : data.listA) {
            map = map.remove(e);
        }
        if (!map.isEmpty()) throw new AssertionError("map: " + map);
        return map;
    }

    //@Benchmark
    public PersistentList<Key> mRemoveAll() {
        PersistentList<Key> set = listA;
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
    public PersistentList<Key> mTail() {
        return listA.removeAt(0);
    }

    @Benchmark
    public PersistentList<Key> mAddLast() {
        Key key = data.nextKeyInB();
        return (listA).add(key);
    }

    @Benchmark
    public PersistentList<Key> mAddFirst() {
        Key key = data.nextKeyInB();
        return (listA).add(0, key);
    }

    @Benchmark
    public PersistentList<Key> mRemoveLast() {
        return listA.removeAt(listA.size() - 1);
    }

    @Benchmark
    public PersistentList<Key> mRemoveAtIndex() {
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
    public PersistentList<Key> mSet() {
        int index = data.nextIndexInA();
        Key key = data.nextKeyInB();
        return listA.set(index, key);
    }


}
