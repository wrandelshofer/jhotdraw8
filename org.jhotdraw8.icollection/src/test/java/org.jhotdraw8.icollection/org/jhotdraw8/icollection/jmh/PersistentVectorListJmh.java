package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.PersistentVectorList;
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

/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro, 4.40 GHz
///
/// Benchmark                                  (size)  Mode  Cnt        Score   Error  Units
/// PersistentVectorListJmh.mAddAll                10  avgt    2       22.105          ns/op
/// PersistentVectorListJmh.mAddAll              1000  avgt    2     2259.933          ns/op
/// PersistentVectorListJmh.mAddAll            100000  avgt    2   219268.753          ns/op
/// PersistentVectorListJmh.mAddAllArray           10  avgt    2       13.647          ns/op
/// PersistentVectorListJmh.mAddAllArray         1000  avgt    2     1148.524          ns/op
/// PersistentVectorListJmh.mAddAllArray       100000  avgt    2   113523.461          ns/op
/// PersistentVectorListJmh.mAddAllSameType        10  avgt    2       18.547          ns/op
/// PersistentVectorListJmh.mAddAllSameType      1000  avgt    2      507.318          ns/op
/// PersistentVectorListJmh.mAddAllSameType    100000  avgt    2     4122.169          ns/op
/// PersistentVectorListJmh.mAddFirst              10  avgt    2        8.680          ns/op
/// PersistentVectorListJmh.mAddFirst            1000  avgt    2        7.761          ns/op
/// PersistentVectorListJmh.mAddFirst          100000  avgt    2       14.562          ns/op
/// PersistentVectorListJmh.mAddLast               10  avgt    2        6.011          ns/op
/// PersistentVectorListJmh.mAddLast             1000  avgt    2        8.352          ns/op
/// PersistentVectorListJmh.mAddLast           100000  avgt    2       12.545          ns/op
/// PersistentVectorListJmh.mAddOneByOne           10  avgt    2       60.972          ns/op
/// PersistentVectorListJmh.mAddOneByOne         1000  avgt    2     7901.860          ns/op
/// PersistentVectorListJmh.mAddOneByOne       100000  avgt    2  1052549.564          ns/op
/// PersistentVectorListJmh.mContainsNotFound      10  avgt    2       16.284          ns/op
/// PersistentVectorListJmh.mContainsNotFound    1000  avgt    2     1199.331          ns/op
/// PersistentVectorListJmh.mContainsNotFound  100000  avgt    2   128610.229          ns/op
/// PersistentVectorListJmh.mCopyOf                10  avgt    2       44.327          ns/op
/// PersistentVectorListJmh.mCopyOf              1000  avgt    2     4220.194          ns/op
/// PersistentVectorListJmh.mCopyOf            100000  avgt    2   876591.051          ns/op
/// PersistentVectorListJmh.mGet                   10  avgt    2        1.870          ns/op
/// PersistentVectorListJmh.mGet                 1000  avgt    2        2.180          ns/op
/// PersistentVectorListJmh.mGet               100000  avgt    2        5.860          ns/op
/// PersistentVectorListJmh.mHead                  10  avgt    2        0.781          ns/op
/// PersistentVectorListJmh.mHead                1000  avgt    2        0.888          ns/op
/// PersistentVectorListJmh.mHead              100000  avgt    2        0.866          ns/op
/// PersistentVectorListJmh.mIterate               10  avgt    2       16.426          ns/op
/// PersistentVectorListJmh.mIterate             1000  avgt    2      926.852          ns/op
/// PersistentVectorListJmh.mIterate           100000  avgt    2   134645.518          ns/op
/// PersistentVectorListJmh.mListIterate           10  avgt    2        3.408          ns/op
/// PersistentVectorListJmh.mListIterate         1000  avgt    2     1169.167          ns/op
/// PersistentVectorListJmh.mListIterate       100000  avgt    2   194232.813          ns/op
/// PersistentVectorListJmh.mRemoveAll             10  avgt    2       44.431          ns/op
/// PersistentVectorListJmh.mRemoveAll           1000  avgt    2     3523.836          ns/op
/// PersistentVectorListJmh.mRemoveAll         100000  avgt    2   544446.586          ns/op
/// PersistentVectorListJmh.mRemoveAtIndex         10  avgt    2        7.957          ns/op
/// PersistentVectorListJmh.mRemoveAtIndex       1000  avgt    2      321.195          ns/op
/// PersistentVectorListJmh.mRemoveAtIndex     100000  avgt    2    23788.564          ns/op
/// PersistentVectorListJmh.mRemoveLast            10  avgt    2        4.778          ns/op
/// PersistentVectorListJmh.mRemoveLast          1000  avgt    2        5.793          ns/op
/// PersistentVectorListJmh.mRemoveLast        100000  avgt    2       10.727          ns/op
/// PersistentVectorListJmh.mRemoveOneByOne        10  avgt    2      224.356          ns/op
/// PersistentVectorListJmh.mRemoveOneByOne      1000  avgt    2   807260.577          ns/op
/// PersistentVectorListJmh.mReversedIterate       10  avgt    2        3.324          ns/op
/// PersistentVectorListJmh.mReversedIterate     1000  avgt    2      750.826          ns/op
/// PersistentVectorListJmh.mReversedIterate   100000  avgt    2   174570.704          ns/op
/// PersistentVectorListJmh.mSet                   10  avgt    2        7.090          ns/op
/// PersistentVectorListJmh.mSet                 1000  avgt    2       21.033          ns/op
/// PersistentVectorListJmh.mSet               100000  avgt    2       42.444          ns/op
/// PersistentVectorListJmh.mRemoveFirst           10  avgt    2        6.202          ns/op
/// PersistentVectorListJmh.mRemoveFirst         1000  avgt    2        6.910          ns/op
/// PersistentVectorListJmh.mRemoveFirst       100000  avgt    2        9.112          ns/op
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class PersistentVectorListJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private PersistentVectorList<Key> listA;
    private PersistentVectorList<Key> listB;

    private int index;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        listA = PersistentVectorList.of();
        for (Key key : data.setA) {
            listA = listA.adding(key);
        }
        listB = PersistentVectorList.copyOf(data.setB);
        index = Math.min(listA.size() - 1, BigInteger.valueOf(listA.size() / 2).nextProbablePrime().intValue());
    }


    @Benchmark
    public PersistentVectorList<Key> mCopyOf() {
        return PersistentVectorList.copyOf(data.setA);
    }

    @Benchmark
    public PersistentVectorList<Key> mAddAll() {
        return listA.addingAll(data.listB);
    }

    @Benchmark
    public PersistentVectorList<Key> mAddAllSameType() {
        return listA.addingAll(listB);
    }

    @Benchmark
    public PersistentVectorList<Key> mOfArray() {
        return PersistentVectorList.<Key>of(data.arrayA);
    }

    @Benchmark
    public PersistentVectorList<Key> mAddOneByOne() {
        PersistentVectorList<Key> l = PersistentVectorList.of();
        for (Key key : data.listA) {
            l = l.adding(key);
        }
        return l;
    }


    /// This appears to be broken!
    @Benchmark
    public PersistentVectorList<Key> mRemoveOneByOne() {
        var l = listA;
        for (var e : data.listA) {
            l = l.removing(e);
        }
        if (!l.isEmpty()) throw new AssertionError("map: " + l);
        return l;
    }


    @Benchmark
    public PersistentVectorList<Key> mRemoveAll() {
        PersistentVectorList<Key> l = listA;
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
    public PersistentVectorList<Key> mRemoveFirst() {
        return listA.removingFirst();
    }

    @Benchmark
    public PersistentVectorList<Key> mAddLast() {
        Key key = data.nextKeyInB();
        return (listA).adding(key);
    }

    @Benchmark
    public PersistentVectorList<Key> mAddFirst() {
        Key key = data.nextKeyInB();
        return (listA).addingFirst(key);
    }


    @Benchmark
    public PersistentVectorList<Key> mRemoveLast() {
        return listA.removingAt(listA.size() - 1);
    }

    @Benchmark
    public PersistentVectorList<Key> mRemoveAtIndex() {
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
    public PersistentVectorList<Key> mSet() {
        int offset = data.nextIndexInA();
        Key key = data.nextKeyInB();
        return listA.settingAt(offset, key);
    }
}
