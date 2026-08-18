package org.jhotdraw8.icollection.jmh;

import kotlinx.collections.immutable.ExtensionsKt;
import kotlinx.collections.immutable.PersistentList;
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
import java.util.concurrent.TimeUnit;

/// <pre>
/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro, 4.40 GHz
/// org.jetbrains.kotlin:kotlinx-collections-immutable-jvm:0.5.1
///
/// Benchmark                                  (size)  Mode  Cnt           Score   Error  Units
/// KotlinPersistentListJmh.mAddAll                10  avgt               13.111          ns/op
/// KotlinPersistentListJmh.mAddAll              1000  avgt              611.895          ns/op
/// KotlinPersistentListJmh.mAddAll            100000  avgt            64386.547          ns/op
/// KotlinPersistentListJmh.mAddAllSameType        10  avgt               20.086          ns/op
/// KotlinPersistentListJmh.mAddAllSameType      1000  avgt             5597.727          ns/op
/// KotlinPersistentListJmh.mAddAllSameType    100000  avgt           424630.770          ns/op
/// KotlinPersistentListJmh.mAddFirst              10  avgt                8.043          ns/op
/// KotlinPersistentListJmh.mAddFirst            1000  avgt              300.565          ns/op
/// KotlinPersistentListJmh.mAddFirst          100000  avgt            28858.991          ns/op
/// KotlinPersistentListJmh.mAddLast               10  avgt                5.998          ns/op
/// KotlinPersistentListJmh.mAddLast             1000  avgt                7.628          ns/op
/// KotlinPersistentListJmh.mAddLast           100000  avgt               22.403          ns/op
/// KotlinPersistentListJmh.mAddOneByOne           10  avgt               64.078          ns/op
/// KotlinPersistentListJmh.mAddOneByOne         1000  avgt             8650.419          ns/op
/// KotlinPersistentListJmh.mAddOneByOne       100000  avgt           838244.928          ns/op
/// KotlinPersistentListJmh.mContainsNotFound      10  avgt                3.656          ns/op
/// KotlinPersistentListJmh.mContainsNotFound    1000  avgt             1044.319          ns/op
/// KotlinPersistentListJmh.mContainsNotFound  100000  avgt           110674.669          ns/op
/// KotlinPersistentListJmh.mCopyOf                10  avgt               43.164          ns/op
/// KotlinPersistentListJmh.mCopyOf              1000  avgt             3852.093          ns/op
/// KotlinPersistentListJmh.mCopyOf            100000  avgt           835146.953          ns/op
/// KotlinPersistentListJmh.mGet                   10  avgt                1.665          ns/op
/// KotlinPersistentListJmh.mGet                 1000  avgt                2.945          ns/op
/// KotlinPersistentListJmh.mGet               100000  avgt                7.164          ns/op
/// KotlinPersistentListJmh.mGetFirst              10  avgt                0.804          ns/op
/// KotlinPersistentListJmh.mGetFirst            1000  avgt                1.340          ns/op
/// KotlinPersistentListJmh.mGetFirst          100000  avgt                2.030          ns/op
/// KotlinPersistentListJmh.mIndexOfLast           10  avgt    2           4.914          ns/op
/// KotlinPersistentListJmh.mIndexOfLast         1000  avgt    2        2056.409          ns/op
/// KotlinPersistentListJmh.mIndexOfLast       100000  avgt    2      214645.041          ns/op
/// KotlinPersistentListJmh.mIterate               10  avgt                3.510          ns/op
/// KotlinPersistentListJmh.mIterate             1000  avgt             1755.806          ns/op
/// KotlinPersistentListJmh.mIterate           100000  avgt           213130.443          ns/op
/// KotlinPersistentListJmh.mListIterate           10  avgt                3.462          ns/op
/// KotlinPersistentListJmh.mListIterate         1000  avgt             2101.354          ns/op
/// KotlinPersistentListJmh.mListIterate       100000  avgt           213100.092          ns/op
/// KotlinPersistentListJmh.mOfArray               10  avgt               11.084          ns/op
/// KotlinPersistentListJmh.mOfArray             1000  avgt              619.701          ns/op
/// KotlinPersistentListJmh.mOfArray           100000  avgt            60960.484          ns/op
/// KotlinPersistentListJmh.mRemoveAll             10  avgt               33.012          ns/op
/// KotlinPersistentListJmh.mRemoveAll           1000  avgt             3149.606          ns/op
/// KotlinPersistentListJmh.mRemoveAll         100000  avgt           651873.012          ns/op
/// KotlinPersistentListJmh.mRemoveAtIndex         10  avgt                7.772          ns/op
/// KotlinPersistentListJmh.mRemoveAtIndex       1000  avgt              158.889          ns/op
/// KotlinPersistentListJmh.mRemoveAtIndex     100000  avgt            14191.018          ns/op
/// KotlinPersistentListJmh.mRemoveFirst           10  avgt                7.754          ns/op
/// KotlinPersistentListJmh.mRemoveFirst         1000  avgt              286.080          ns/op
/// KotlinPersistentListJmh.mRemoveFirst       100000  avgt            27482.872          ns/op
/// KotlinPersistentListJmh.mRemoveLast            10  avgt                6.228          ns/op
/// KotlinPersistentListJmh.mRemoveLast          1000  avgt                7.521          ns/op
/// KotlinPersistentListJmh.mRemoveLast        100000  avgt                7.406          ns/op
/// KotlinPersistentListJmh.mRemoveAtOneByOne         10  avgt    2          78.067          ns/op
/// KotlinPersistentListJmh.mRemoveAtOneByOne       1000  avgt    2       95167.765          ns/op
/// KotlinPersistentListJmh.mRemoveAtOneByOne     100000  avgt    2   735195900.286          ns/op
/// KotlinPersistentListJmh.mRemoveFirstOneByOne      10  avgt    2          66.441          ns/op
/// KotlinPersistentListJmh.mRemoveFirstOneByOne    1000  avgt    2      151218.929          ns/op
/// KotlinPersistentListJmh.mRemoveFirstOneByOne  100000  avgt    2  1401137705.750          ns/op
/// KotlinPersistentListJmh.mRemoveLastOneByOne       10  avgt    2          52.109          ns/op
/// KotlinPersistentListJmh.mRemoveLastOneByOne     1000  avgt    2        8216.852          ns/op
/// KotlinPersistentListJmh.mRemoveLastOneByOne   100000  avgt    2      772901.746          ns/op
/// KotlinPersistentListJmh.mRemoveOneByOne           10  avgt    2          89.926          ns/op
/// KotlinPersistentListJmh.mRemoveOneByOne         1000  avgt    2      602170.981          ns/op
/// KotlinPersistentListJmh.mRemoveOneByOne       100000  avgt    2  5937762614.750          ns/op
/// KotlinPersistentListJmh.mReversedIterate       10  avgt                3.358          ns/op
/// KotlinPersistentListJmh.mReversedIterate     1000  avgt             1272.305          ns/op
/// KotlinPersistentListJmh.mReversedIterate   100000  avgt           237995.515          ns/op
/// KotlinPersistentListJmh.mSet                   10  avgt                6.151          ns/op
/// KotlinPersistentListJmh.mSet                 1000  avgt               16.005          ns/op
/// KotlinPersistentListJmh.mSet               100000  avgt               39.772          ns/op
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class KotlinPersistentListJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private PersistentList<Key> listA;

    private int index;
    private PersistentList<Key> listB;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);

        PersistentList.Builder<Key> builder = ExtensionsKt.<Key>persistentListOf().builder();
        builder.addAll(data.setA);
        listA = builder.build();
        builder.addAll(data.setB);
        listB = builder.build();
        index = Math.min(listA.size() - 1, BigInteger.valueOf(listA.size() / 2).nextProbablePrime().intValue());
    }

    /*
        @Benchmark
        public PersistentList<Key> mCopyOf() {
            PersistentList.Builder<Key> builder = ExtensionsKt.<Key>persistentListOf().builder();
            builder.addAll(data.setA);
            return builder.build();
        }

        @Benchmark
        public PersistentList<Key> mAddAll() {
            return listA.addingAll(data.listB);
        }

        @Benchmark
        public PersistentList<Key> mAddAllSameType() {
            return listA.addingAll(listB);
        }

        @Benchmark
        public PersistentList<Key> mOfArray() {
            return ExtensionsKt.persistentListOf(data.arrayA);
        }

        @Benchmark
        public PersistentList<Key> mAddOneByOne() {
            PersistentList<Key> l = ExtensionsKt.persistentListOf();
            for (Key key : data.listA) {
                l = l.adding(key);
            }
            return l;
        }


    @Benchmark
    public PersistentList<Key> mRemoveFirstOneByOne() {
        var l = listA;
        for (var e : data.listA) {
            l = l.removing(e);
        }
        if (!l.isEmpty()) throw new AssertionError("map: " + l);
        return l;
    }

    @Benchmark
    public PersistentList<Key> mRemoveAll() {
        PersistentList<Key> l = listA;
        return l.removingAll(data.setA);
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
    public int mListIterate() {
        int sum = 0;
        for (Iterator<Key> i = listA.listIterator(); i.hasNext(); ) {
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
    public PersistentList<Key> mRemoveFirst() {
        return listA.removingAt(0);
    }

    @Benchmark
    public PersistentList<Key> mAddLast() {
        Key key = data.nextKeyInB();
        return (listA).adding(key);
    }

    @Benchmark
    public PersistentList<Key> mAddFirst() {
        Key key = data.nextKeyInB();
        return (listA).addingAt(0, key);
    }


    @Benchmark
    public PersistentList<Key> mRemoveLast() {
        return listA.removingAt(listA.size() - 1);
    }

    @Benchmark
    public PersistentList<Key> mRemoveAtIndex() {
        return listA.removingAt(index);
    }

    @Benchmark
    public Key mGet() {
        int offset = data.nextIndexInA();
        return listA.get(offset);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return listA.contains(key);
    }

    @Benchmark
    public Key mGetFirst() {
        return listA.get(0);
    }

    @Benchmark
    public PersistentList<Key> mSet() {
        int offset = data.nextIndexInA();
        Key key = data.nextKeyInB();
        return listA.replacingAt(offset, key);
    }
    @Benchmark
    public int mIndexOfLast() {
        return listA.indexOf(listA.getLast());
    }
*/
    @Benchmark
    public PersistentList<Key> mRemoveFirstOneByOne() {
        var l = listA;
        while (!l.isEmpty()) {
            l = l.removeAt(0);
        }
        return l;
    }

    @Benchmark
    public PersistentList<Key> mRemoveLastOneByOne() {
        var l = listA;
        while (!l.isEmpty()) {
            l = l.removeAt(l.size() - 1);
        }
        return l;
    }

    @Benchmark
    public PersistentList<Key> mRemoveOneByOne() {
        var l = listA;
        for (var e : data.listA) {
            l = l.removing(e);
        }
        if (!l.isEmpty()) throw new AssertionError("map: " + l);
        return l;
    }

    @Benchmark
    public PersistentList<Key> mRemoveAtOneByOne() {
        var l = listA;
        for (var e : data.listA) {
            l = l.removingAt(l.size() / 2);
        }
        if (!l.isEmpty()) throw new AssertionError("map: " + l);
        return l;
    }
}
