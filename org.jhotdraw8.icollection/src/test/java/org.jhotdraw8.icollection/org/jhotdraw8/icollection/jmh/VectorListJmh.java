package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.VectorList;
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
/// # Mac Mini M4 Pro
///
/// Benchmark                         (size)  Mode  Cnt         Score   Error  Units
/// VectorListJmh.mAddAll                 10  avgt    2        19.055          ns/op
/// VectorListJmh.mAddAll               1000  avgt    2      1961.997          ns/op
/// VectorListJmh.mAddAll            1000000  avgt    2   6940017.149          ns/op
/// VectorListJmh.mAddAllArray            10  avgt    2        20.152          ns/op
/// VectorListJmh.mAddAllArray          1000  avgt    2      2027.454          ns/op
/// VectorListJmh.mAddAllArray       1000000  avgt    2   7118018.180          ns/op
/// VectorListJmh.mAddOneByOne            10  avgt    2       110.645          ns/op
/// VectorListJmh.mAddOneByOne          1000  avgt    2     15814.154          ns/op
/// VectorListJmh.mAddOneByOne       1000000  avgt    2  45808943.018          ns/op
///
/// VectorListJmh.mAddFirst               10  avgt    2        15.596          ns/op
/// VectorListJmh.mAddFirst             1000  avgt    2        16.859          ns/op
/// VectorListJmh.mAddFirst          1000000  avgt    2        54.892          ns/op
/// VectorListJmh.mAddLast                10  avgt    2         8.469          ns/op
/// VectorListJmh.mAddLast              1000  avgt    2        14.440          ns/op
/// VectorListJmh.mAddLast           1000000  avgt    2        37.381          ns/op
/// VectorListJmh.mContainsNotFound       10  avgt    2        14.659          ns/op
/// VectorListJmh.mContainsNotFound     1000  avgt    2      1244.560          ns/op
/// VectorListJmh.mContainsNotFound  1000000  avgt    2   2330092.960          ns/op
/// VectorListJmh.mGet                    10  avgt    2         2.078          ns/op
/// VectorListJmh.mGet                  1000  avgt    2         2.668          ns/op
/// VectorListJmh.mGet               1000000  avgt    2        25.608          ns/op
/// VectorListJmh.mHead                   10  avgt    2         1.294          ns/op
/// VectorListJmh.mHead                 1000  avgt    2         1.577          ns/op
/// VectorListJmh.mHead              1000000  avgt    2         2.292          ns/op
/// VectorListJmh.mIterate                10  avgt    2         5.297          ns/op
/// VectorListJmh.mIterate              1000  avgt    2      1758.457          ns/op
/// VectorListJmh.mIterate           1000000  avgt    2   3946697.972          ns/op
/// VectorListJmh.mListIterate            10  avgt    2        10.619          ns/op
/// VectorListJmh.mListIterate          1000  avgt    2      1433.092          ns/op
/// VectorListJmh.mListIterate       1000000  avgt    2   3066237.895          ns/op
/// VectorListJmh.mRemoveAtIndex          10  avgt    2        18.269          ns/op
/// VectorListJmh.mRemoveAtIndex        1000  avgt    2      1834.944          ns/op
/// VectorListJmh.mRemoveAtIndex     1000000  avgt    2   1623983.618          ns/op
/// VectorListJmh.mRemoveLast             10  avgt    2         3.626          ns/op
/// VectorListJmh.mRemoveLast           1000  avgt    2         4.312          ns/op
/// VectorListJmh.mRemoveLast        1000000  avgt    2         4.361          ns/op
/// VectorListJmh.mReversedIterate        10  avgt    2         6.615          ns/op
/// VectorListJmh.mReversedIterate      1000  avgt    2       754.507          ns/op
/// VectorListJmh.mReversedIterate   1000000  avgt    2   5513777.637          ns/op
/// VectorListJmh.mSet                    10  avgt    2         9.575          ns/op
/// VectorListJmh.mSet                  1000  avgt    2        19.629          ns/op
/// VectorListJmh.mSet               1000000  avgt    2        73.030          ns/op
/// VectorListJmh.mTail                   10  avgt    2        15.343          ns/op
/// VectorListJmh.mTail                 1000  avgt    2      2536.618          ns/op
/// VectorListJmh.mTail              1000000  avgt    2   3243621.286          ns/op
///
/// # JMH version: 1.36
/// # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
/// # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
///
/// Benchmark           (size)  Mode  Cnt          Score   Error  Units
/// mAddAll                 10  avgt              52.805          ns/op
/// mAddAll               1000  avgt            3875.245          ns/op
/// mAddAll            1000000  avgt        11166047.498          ns/op
/// mAddAllArray            10  avgt              36.627          ns/op
/// mAddAllArray          1000  avgt            3831.702          ns/op
/// mAddAllArray       1000000  avgt        18233901.719          ns/op
/// mAddFirst               10  avgt              43.874          ns/op
/// mAddFirst             1000  avgt              50.507          ns/op
/// mAddFirst          1000000  avgt             140.191          ns/op
/// mAddLast                10  avgt              22.597          ns/op
/// mAddLast              1000  avgt              34.994          ns/op
/// mAddLast           1000000  avgt             127.617          ns/op
/// mAddOneByOne            10  avgt             211.522          ns/op
/// mAddOneByOne          1000  avgt           43554.723          ns/op
/// mAddOneByOne       1000000  avgt       134181586.813          ns/op
/// mContainsNotFound       10  avgt              27.532          ns/op
/// mContainsNotFound     1000  avgt            2794.591          ns/op
/// mContainsNotFound  1000000  avgt         5675693.984          ns/op
/// mGet                    10  avgt               3.691          ns/op
/// mGet                  1000  avgt               7.463          ns/op
/// mGet               1000000  avgt              86.203          ns/op
/// mHead                   10  avgt               1.747          ns/op
/// mHead                 1000  avgt               2.236          ns/op
/// mHead              1000000  avgt               5.083          ns/op
/// mIterate                10  avgt              12.636          ns/op
/// mIterate              1000  avgt            1426.097          ns/op
/// mIterate           1000000  avgt        12576434.361          ns/op
/// mListIterate            10  avgt              23.463          ns/op
/// mListIterate          1000  avgt            2973.783          ns/op
/// mListIterate       1000000  avgt        23631687.953          ns/op
/// mRemoveAtIndex          10  avgt              40.534          ns/op
/// mRemoveAtIndex        1000  avgt            3046.037          ns/op
/// mRemoveAtIndex     1000000  avgt         3367569.843          ns/op
/// mRemoveLast             10  avgt              11.158          ns/op
/// mRemoveLast           1000  avgt              12.521          ns/op
/// mRemoveLast        1000000  avgt              10.877          ns/op
/// mReversedIterate        10  avgt              10.123          ns/op
/// mReversedIterate      1000  avgt            1841.430          ns/op
/// mReversedIterate   1000000  avgt        12294983.693          ns/op
/// mSet                    10  avgt              19.737          ns/op
/// mSet                  1000  avgt              40.689          ns/op
/// mSet               1000000  avgt             221.233          ns/op
/// mTail                   10  avgt               6.223          ns/op
/// mTail                 1000  avgt               6.307          ns/op
/// mTail              1000000  avgt               6.358          ns/op
///
/// Process finished with exit code 0
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class VectorListJmh {
    @Param({"10", "1000", "1000000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private VectorList<Key> listA;
    private VectorList<Key> listB;

    private int index;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        listA = VectorList.of();
        for (Key key : data.setA) {
            listA = listA.add(key);
        }
        listB = VectorList.copyOf(data.setB);
        index = Math.min(listA.size() - 1, BigInteger.valueOf(listA.size() / 2).nextProbablePrime().intValue());
    }


    @Benchmark
    public VectorList<Key> mAddAll() {
        return VectorList.copyOf(data.setA);
    }

    @Benchmark
    public VectorList<Key> mAddAllSameType() {
        return listA.addAll(listB);
    }

    @Benchmark
    public VectorList<Key> mAddAllArray() {
        return VectorList.<Key>of(data.setA.toArray(new Key[0]));
    }

    @Benchmark
    public VectorList<Key> mAddOneByOne() {
        VectorList<Key> l = VectorList.of();
        for (Key key : data.listA) {
            l = l.add(key);
        }
        return l;
    }

    /// This appears to be broken!
    //  @Benchmark
    public VectorList<Key> mRemoveOneByOne() {
        var l = listA;
        for (var e : data.listA) {
            l = l.remove(e);
        }
        if (!l.isEmpty()) throw new AssertionError("map: " + l);
        return l;
    }

    /// This appears to be broken!
    //  @Benchmark
    public VectorList<Key> mRemoveAll() {
        VectorList<Key> l = listA;
        return l.removeAll(data.listA);
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
    public VectorList<Key> mTail() {
        return listA.removeAt(0);
    }

    @Benchmark
    public VectorList<Key> mAddLast() {
        Key key = data.nextKeyInB();
        return (listA).add(key);
    }

    @Benchmark
    public VectorList<Key> mAddFirst() {
        Key key = data.nextKeyInB();
        return (listA).add(0, key);
    }


    @Benchmark
    public VectorList<Key> mRemoveLast() {
        return listA.removeAt(listA.size() - 1);
    }

    @Benchmark
    public VectorList<Key> mRemoveAtIndex() {
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
    public VectorList<Key> mSet() {
        int index = data.nextIndexInA();
        Key key = data.nextKeyInB();
        return listA.set(index, key);
    }


}
