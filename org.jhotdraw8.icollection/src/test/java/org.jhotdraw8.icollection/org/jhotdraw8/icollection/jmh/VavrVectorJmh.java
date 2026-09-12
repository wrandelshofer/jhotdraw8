package org.jhotdraw8.icollection.jmh;

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

/// ```
/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro, 4.40 GHz
///
/// Benchmark                        (size)  Mode  Cnt            Score   Error  Units
/// VavrVectorJmh.mAdd                   10  avgt               147.260          ns/op
/// VavrVectorJmh.mAdd                 1000  avgt             23659.417          ns/op
/// VavrVectorJmh.mAdd               100000  avgt           4194729.665          ns/op
/// VavrVectorJmh.mAddAll                10  avgt                22.433          ns/op
/// VavrVectorJmh.mAddAll              1000  avgt              1996.897          ns/op
/// VavrVectorJmh.mAddAll            100000  avgt            617845.709          ns/op
/// VavrVectorJmh.mAddAllSameType        10  avgt                20.775          ns/op
/// VavrVectorJmh.mAddAllSameType      1000  avgt              3102.321          ns/op
/// VavrVectorJmh.mAddAllSameType    100000  avgt            314964.253          ns/op
/// VavrVectorJmh.mAddAt                 10  avgt               605.977          ns/op
/// VavrVectorJmh.mAddAt               1000  avgt            957602.637          ns/op
/// VavrVectorJmh.mAddAt             100000  avgt       13034088000.000          ns/op
/// VavrVectorJmh.mAddFirst              10  avgt               201.979          ns/op
/// VavrVectorJmh.mAddFirst            1000  avgt             20392.101          ns/op
/// VavrVectorJmh.mAddFirst          100000  avgt           2798310.741          ns/op
/// VavrVectorJmh.mAddLast               10  avgt               149.128          ns/op
/// VavrVectorJmh.mAddLast             1000  avgt             23866.846          ns/op
/// VavrVectorJmh.mAddLast           100000  avgt           4316758.308          ns/op
/// VavrVectorJmh.mContainsNotFound      10  avgt                12.942          ns/op
/// VavrVectorJmh.mContainsNotFound    1000  avgt              1249.637          ns/op
/// VavrVectorJmh.mContainsNotFound  100000  avgt            139885.574          ns/op
/// VavrVectorJmh.mCopyOf                10  avgt                20.029          ns/op
/// VavrVectorJmh.mCopyOf              1000  avgt              2054.083          ns/op
/// VavrVectorJmh.mCopyOf            100000  avgt            657834.852          ns/op
/// VavrVectorJmh.mGet                   10  avgt                 2.121          ns/op
/// VavrVectorJmh.mGet                 1000  avgt                 2.659          ns/op
/// VavrVectorJmh.mGet               100000  avgt                 8.032          ns/op
/// VavrVectorJmh.mGetFirst              10  avgt                 1.329          ns/op
/// VavrVectorJmh.mGetFirst            1000  avgt                 1.689          ns/op
/// VavrVectorJmh.mGetFirst          100000  avgt                 2.574          ns/op
/// VavrVectorJmh.mIndexOfLast           10  avgt                25.349          ns/op
/// VavrVectorJmh.mIndexOfLast         1000  avgt              3048.140          ns/op
/// VavrVectorJmh.mIndexOfLast       100000  avgt            427207.584          ns/op
/// VavrVectorJmh.mIterate               10  avgt                 5.359          ns/op
/// VavrVectorJmh.mIterate             1000  avgt              1457.782          ns/op
/// VavrVectorJmh.mIterate           100000  avgt            197108.013          ns/op
/// VavrVectorJmh.mListIterate           10  avgt                 5.365          ns/op
/// VavrVectorJmh.mListIterate         1000  avgt              1463.508          ns/op
/// VavrVectorJmh.mListIterate       100000  avgt            193696.139          ns/op
/// VavrVectorJmh.mOfArray               10  avgt                 2.676          ns/op
/// VavrVectorJmh.mOfArray             1000  avgt               168.888          ns/op
/// VavrVectorJmh.mOfArray           100000  avgt             18177.694          ns/op
/// VavrVectorJmh.mRemove                10  avgt               165.158          ns/op
/// VavrVectorJmh.mRemove              1000  avgt           1121755.364          ns/op
/// VavrVectorJmh.mRemove            100000  avgt       14914560833.000          ns/op
/// VavrVectorJmh.mRemoveAll             10  avgt               196.635          ns/op
/// VavrVectorJmh.mRemoveAll           1000  avgt             41006.259          ns/op
/// VavrVectorJmh.mRemoveAll         100000  avgt          16295205.756          ns/op
/// VavrVectorJmh.mRemoveAt              10  avgt               202.594          ns/op
/// VavrVectorJmh.mRemoveAt            1000  avgt            660155.622          ns/op
/// VavrVectorJmh.mRemoveAt          100000  avgt        9573558625.000          ns/op
/// VavrVectorJmh.mRemoveFirst           10  avgt                24.297          ns/op
/// VavrVectorJmh.mRemoveFirst         1000  avgt              4445.531          ns/op
/// VavrVectorJmh.mRemoveFirst       100000  avgt            410016.443          ns/op
/// VavrVectorJmh.mRemoveLast            10  avgt                24.580          ns/op
/// VavrVectorJmh.mRemoveLast          1000  avgt              5549.121          ns/op
/// VavrVectorJmh.mRemoveLast        100000  avgt            366018.305          ns/op
/// VavrVectorJmh.mReversedIterate       10  avgt                 5.588          ns/op
/// VavrVectorJmh.mReversedIterate     1000  avgt               795.155          ns/op
/// VavrVectorJmh.mReversedIterate   100000  avgt            229833.227          ns/op
/// VavrVectorJmh.mSet                   10  avgt                 9.374          ns/op
/// VavrVectorJmh.mSet                 1000  avgt                20.512          ns/op
/// VavrVectorJmh.mSet               100000  avgt                41.219          ns/op
///
/// Process finished with exit code 0
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1, jvmArgsAppend = {"-Xmx28g"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class VavrVectorJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private Vector<Key> listA;
    private Vector<Key> listB;

    private int index;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        listA = Vector.of();
        for (Key key : data.setA) {
            listA = listA.append(key);
        }
        listB = Vector.of();
        for (Key key : data.setB) {
            listB = listB.append(key);
        }
        index = Math.min(listA.length() - 1, BigInteger.valueOf(listA.length() / 2).nextProbablePrime().intValue());
    }


    @Benchmark
    public Vector<Key> mCopyOf() {
        return Vector.<Key>of(data.setA.toArray(new Key[0]));
    }

    @Benchmark
    public Vector<Key> mAddAll() {
        return Vector.<Key>of(data.setA.toArray(new Key[0]));
    }

    @Benchmark
    public Vector<Key> mAddAllSameType() {
        return listA.appendAll(listB);
    }

    @Benchmark
    public Vector<Key> mOfArray() {
        return Vector.<Key>of(data.arrayA);
    }

    @Benchmark
    public Vector<Key> mAdd() {
        Vector<Key> l = Vector.of();
        for (Key key : data.listA) {
            l = l.append(key);
        }
        return l;
    }

    @Benchmark
    public Vector<Key> mAddLast() {
        Vector<Key> l = Vector.of();
        for (Key key : data.listA) {
            l = l.append(key);
        }
        return l;
    }

    @Benchmark
    public Vector<Key> mAddFirst() {
        Vector<Key> l = Vector.of();
        for (Key key : data.listA) {
            l = l.prepend(key);
        }
        return l;
    }

    @Benchmark
    public Vector<Key> mAddAt() {
        Vector<Key> l = Vector.of();
        for (Key key : data.listA) {
            l = l.insert(l.size() / 2, key);
        }
        return l;
    }

    @Benchmark
    public Vector<Key> mRemove() {
        var l = listA;
        for (var e : data.listA) {
            l = l.remove(e);
        }
        if (!l.isEmpty()) throw new AssertionError("map: " + l);
        return l;
    }

    @Benchmark
    public Vector<Key> mRemoveAt() {
        var l = listA;
        for (var e : data.listA) {
            l = l.removeAt(l.size() / 2);
        }
        if (!l.isEmpty()) throw new AssertionError("map: " + l);
        return l;
    }

    @Benchmark
    public Vector<Key> mRemoveFirst() {
        var l = listA;
        while (!l.isEmpty()) {
            l = l.tail();
        }
        return l;
    }

    @Benchmark
    public Vector<Key> mRemoveLast() {
        var l = listA;
        while (!l.isEmpty()) {
            l = l.init();
        }
        return l;
    }


    @Benchmark
    public Vector<Key> mRemoveAll() {
        Vector<Key> l = listA;
        return l.removeAll(data.setA);
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
    public Vector<Key> mSet() {
        int offset = data.nextIndexInA();
        Key key = data.nextKeyInB();
        return listA.update(offset, key);
    }

    @Benchmark
    public int mIndexOfLast() {
        return listA.indexOf(listA.last());
    }

}
