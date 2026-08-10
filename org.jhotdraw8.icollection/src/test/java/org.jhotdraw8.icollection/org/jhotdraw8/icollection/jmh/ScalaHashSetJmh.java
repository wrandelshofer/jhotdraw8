package org.jhotdraw8.icollection.jmh;

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
import scala.collection.Iterator;
import scala.collection.immutable.HashSet;
import scala.collection.immutable.Vector;
import scala.collection.mutable.ReusableBuilder;

import java.util.concurrent.TimeUnit;

/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro, 4.40 GHz
/// # org.scala-lang:scala-library:3.9.0-RC4
///
/// Benchmark                                           (mask)   (size)  Mode  Cnt          Score   Error  Units
/// ScalaHashSetJmh.mContainsFound                         -65       10  avgt    2          2.590          ns/op
/// ScalaHashSetJmh.mContainsFound                         -65     1000  avgt    2          6.226          ns/op
/// ScalaHashSetJmh.mContainsFound                         -65  1000000  avgt    2         74.249          ns/op
/// ScalaHashSetJmh.mContainsNotFound                      -65       10  avgt    2          2.577          ns/op
/// ScalaHashSetJmh.mContainsNotFound                      -65     1000  avgt    2          6.121          ns/op
/// ScalaHashSetJmh.mContainsNotFound                      -65  1000000  avgt    2         70.332          ns/op
/// ScalaHashSetJmh.mCopyOf                                -65       10  avgt    2        129.882          ns/op
/// ScalaHashSetJmh.mCopyOf                                -65     1000  avgt    2      37467.436          ns/op
/// ScalaHashSetJmh.mCopyOf                                -65  1000000  avgt    2  220793679.804          ns/op
/// ScalaHashSetJmh.mCopyOnyByOne                          -65       10  avgt    2        228.293          ns/op
/// ScalaHashSetJmh.mCopyOnyByOne                          -65     1000  avgt    2      36711.522          ns/op
/// ScalaHashSetJmh.mCopyOnyByOne                          -65  1000000  avgt    2  271402385.135          ns/op
/// ScalaHashSetJmh.mHead                                  -65       10  avgt    2          1.112          ns/op
/// ScalaHashSetJmh.mHead                                  -65     1000  avgt    2          1.589          ns/op
/// ScalaHashSetJmh.mHead                                  -65  1000000  avgt    2          9.730          ns/op
/// ScalaHashSetJmh.mIterate                               -65       10  avgt    2          3.829          ns/op
/// ScalaHashSetJmh.mIterate                               -65     1000  avgt    2       1399.518          ns/op
/// ScalaHashSetJmh.mIterate                               -65  1000000  avgt    2    9109568.426          ns/op
/// ScalaHashSetJmh.mRemoveAllFromSameType                 -65       10  avgt    2         23.106          ns/op
/// ScalaHashSetJmh.mRemoveAllFromSameType                 -65     1000  avgt    2       5200.002          ns/op
/// ScalaHashSetJmh.mRemoveAllFromSameType                 -65  1000000  avgt    2   11175346.758          ns/op
/// ScalaHashSetJmh.mRemoveOneByOne                        -65       10  avgt    2        143.467          ns/op
/// ScalaHashSetJmh.mRemoveOneByOne                        -65     1000  avgt    2      44132.586          ns/op
/// ScalaHashSetJmh.mRemoveOneByOne                        -65  1000000  avgt    2  346660932.466          ns/op
/// ScalaHashSetJmh.mRemoveThenAdd                         -65       10  avgt    2         33.488          ns/op
/// ScalaHashSetJmh.mRemoveThenAdd                         -65     1000  avgt    2         81.460          ns/op
/// ScalaHashSetJmh.mRemoveThenAdd                         -65  1000000  avgt    2        386.668          ns/op
/// ScalaHashSetJmh.mRetainAllFromSameTypeAllRetained      -65       10  avgt    2         25.241          ns/op
/// ScalaHashSetJmh.mRetainAllFromSameTypeAllRetained      -65     1000  avgt    2      11088.832          ns/op
/// ScalaHashSetJmh.mRetainAllFromSameTypeAllRetained      -65  1000000  avgt    2   30986386.935          ns/op
/// ScalaHashSetJmh.mRetainAllFromSameTypeNoneRetained     -65       10  avgt    2         21.346          ns/op
/// ScalaHashSetJmh.mRetainAllFromSameTypeNoneRetained     -65     1000  avgt    2       6002.350          ns/op
/// ScalaHashSetJmh.mRetainAllFromSameTypeNoneRetained     -65  1000000  avgt    2   35631559.609          ns/op
/// ScalaHashSetJmh.mTail                                  -65       10  avgt    2         12.754          ns/op
/// ScalaHashSetJmh.mTail                                  -65     1000  avgt    2         22.612          ns/op
/// ScalaHashSetJmh.mTail                                  -65  1000000  avgt    2         51.526          ns/op
/// <pre>
/// # JMH version: 1.36
/// # VM version: JDK 1.8.0_345, OpenJDK 64-Bit Server VM, 25.345-b01
/// # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
/// # org.scala-lang:scala-library:2.13.10
///
/// Benchmark                                           (mask)    (size)  Mode  Cnt           Score   Error  Units
/// ScalaHashSetJmh.mContainsFound                         -65        10  avgt                6.725          ns/op
/// ScalaHashSetJmh.mContainsFound                         -65      1000  avgt               20.507          ns/op
/// ScalaHashSetJmh.mContainsFound                         -65    100000  avgt               55.253          ns/op
/// ScalaHashSetJmh.mContainsFound                         -65  10000000  avgt              405.071          ns/op
/// ScalaHashSetJmh.mContainsNotFound                      -65        10  avgt                6.473          ns/op
/// ScalaHashSetJmh.mContainsNotFound                      -65      1000  avgt               19.743          ns/op
/// ScalaHashSetJmh.mContainsNotFound                      -65    100000  avgt               62.095          ns/op
/// ScalaHashSetJmh.mContainsNotFound                      -65  10000000  avgt              392.801          ns/op
/// ScalaHashSetJmh.mCopyOf                                -65        10  avgt              353.895          ns/op
/// ScalaHashSetJmh.mCopyOf                                -65      1000  avgt            88596.545          ns/op
/// ScalaHashSetJmh.mCopyOf                                -65    100000  avgt         17829710.321          ns/op
/// ScalaHashSetJmh.mCopyOf                                -65  10000000  avgt       5661884708.500          ns/op
/// ScalaHashSetJmh.mCopyOnyByOne                          -65        10  avgt              388.675          ns/op
/// ScalaHashSetJmh.mCopyOnyByOne                          -65      1000  avgt           111281.696          ns/op
/// ScalaHashSetJmh.mCopyOnyByOne                          -65    100000  avgt         25852027.394          ns/op
/// ScalaHashSetJmh.mCopyOnyByOne                          -65  10000000  avgt       6265015677.000          ns/op
/// ScalaHashSetJmh.mHead                                  -65        10  avgt                1.700          ns/op
/// ScalaHashSetJmh.mHead                                  -65      1000  avgt               12.121          ns/op
/// ScalaHashSetJmh.mHead                                  -65    100000  avgt               19.224          ns/op
/// ScalaHashSetJmh.mHead                                  -65  10000000  avgt               32.005          ns/op
/// ScalaHashSetJmh.mIterate                               -65        10  avgt                9.398          ns/op
/// ScalaHashSetJmh.mIterate                               -65      1000  avgt             2966.436          ns/op
/// ScalaHashSetJmh.mIterate                               -65    100000  avgt           841856.560          ns/op
/// ScalaHashSetJmh.mIterate                               -65  10000000  avgt        383086628.111          ns/op
/// ScalaHashSetJmh.mRemoveAllFromSameType                 -65        10  avgt               76.545          ns/op
/// ScalaHashSetJmh.mRemoveAllFromSameType                 -65      1000  avgt            14703.175          ns/op
/// ScalaHashSetJmh.mRemoveAllFromSameType                 -65    100000  avgt          2533578.884          ns/op
/// ScalaHashSetJmh.mRemoveAllFromSameType                 -65  10000000  avgt        348406140.724          ns/op
/// ScalaHashSetJmh.mRemoveOneByOne                        -65        10  avgt              375.492          ns/op
/// ScalaHashSetJmh.mRemoveOneByOne                        -65      1000  avgt           111162.056          ns/op
/// ScalaHashSetJmh.mRemoveOneByOne                        -65    100000  avgt         28575799.701          ns/op
/// ScalaHashSetJmh.mRemoveOneByOne                        -65  10000000  avgt       6993188211.500          ns/op
/// ScalaHashSetJmh.mRemoveThenAdd                         -65        10  avgt               84.743          ns/op
/// ScalaHashSetJmh.mRemoveThenAdd                         -65      1000  avgt              247.513          ns/op
/// ScalaHashSetJmh.mRemoveThenAdd                         -65    100000  avgt              343.133          ns/op
/// ScalaHashSetJmh.mRemoveThenAdd                         -65  10000000  avgt              805.154          ns/op
/// ScalaHashSetJmh.mRetainAllFromSameTypeAllRetained      -65        10  avgt               75.121          ns/op
/// ScalaHashSetJmh.mRetainAllFromSameTypeAllRetained      -65      1000  avgt            29126.588          ns/op
/// ScalaHashSetJmh.mRetainAllFromSameTypeAllRetained      -65    100000  avgt          5777778.110          ns/op
/// ScalaHashSetJmh.mRetainAllFromSameTypeAllRetained      -65  10000000  avgt       1466537553.143          ns/op
/// ScalaHashSetJmh.mRetainAllFromSameTypeNoneRetained     -65        10  avgt               60.991          ns/op
/// ScalaHashSetJmh.mRetainAllFromSameTypeNoneRetained     -65      1000  avgt            20926.892          ns/op
/// ScalaHashSetJmh.mRetainAllFromSameTypeNoneRetained     -65    100000  avgt          5980150.953          ns/op
/// ScalaHashSetJmh.mRetainAllFromSameTypeNoneRetained     -65  10000000  avgt       1452928071.143          ns/op
/// ScalaHashSetJmh.mTail                                  -65        10  avgt               38.216          ns/op
/// ScalaHashSetJmh.mTail                                  -65      1000  avgt               70.390          ns/op
/// ScalaHashSetJmh.mTail                                  -65    100000  avgt               95.061          ns/op
/// ScalaHashSetJmh.mTail                                  -65  10000000  avgt              139.069          ns/op
/// </pre>
@SuppressWarnings("unchecked")
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ScalaHashSetJmh {
    @Param({"10", "1000", "1000000"})
    private int size;

    @Param({"-65"})
    private int mask;
    private BenchmarkData data;
    private HashSet<Key> setA;
    private HashSet<Key> setAA;
    private HashSet<Key> setB;

    private Vector<Key> vectorA;

    @Setup
    public void setup() {

        data = new BenchmarkData(size, mask);

        ReusableBuilder<Key, HashSet<Key>> bA = HashSet.newBuilder();
        ReusableBuilder<Key, HashSet<Key>> bB = HashSet.newBuilder();
        ReusableBuilder<Key, HashSet<Key>> bAA = HashSet.newBuilder();
        ReusableBuilder<Key, Vector<Key>> bvA = Vector.newBuilder();
        for (Key key : data.setA) {
            bA.addOne(key);
            bAA.addOne(key);
            bvA.addOne(key);
        }
        for (Key key : data.setB) {
            bB.addOne(key);
        }
        setA = bA.result();
        setAA = bAA.result();
        setB = bB.result();
        vectorA = bvA.result();
    }


    @Benchmark
    public HashSet<Key> mCopyOf() {
        HashSet<Key> set = HashSet.<Key>newBuilder().result();
        set = set.concat(vectorA);
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public HashSet<Key> mCopyOnyByOne() {
        HashSet<Key> set = HashSet.<Key>newBuilder().result();
        for (Key key : data.listA) {
            set = (HashSet<Key>) set.$plus(key);
        }
        assert set.size() == data.listA.size();
        return set;
    }


    @Benchmark
    public HashSet<Key> mRemoveOneByOne() {
        HashSet<Key> set = setA;
        for (Key key : data.listA) {
            set = (HashSet<Key>) set.$minus(key);
        }
        assert set.isEmpty();
        return set;
    }


    @Benchmark
    public HashSet<Key> mRemoveAllFromSameType() {
        HashSet<Key> set = setA;
        HashSet<Key> updated = set.diff(setAA);
        assert updated.isEmpty();
        return updated;
    }


    @Benchmark
    public HashSet<Key> mRetainAllFromSameTypeAllRetained() {
        HashSet<Key> set = setA;
        HashSet<Key> updated = set.intersect(setAA);
        assert updated == setA;
        return updated;
    }


    @Benchmark
    public HashSet<Key> mRetainAllFromSameTypeNoneRetained() {
        HashSet<Key> set = setA;
        HashSet<Key> updated = set.intersect(setB);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Iterator<Key> i = setA.iterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

    @Benchmark
    public HashSet<Key> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return (HashSet<Key>) setA.$minus(key).$plus(key);
    }

    @Benchmark
    public Key mHead() {
        return setA.iterator().next();
    }

    @Benchmark
    public HashSet<Key> mTail() {
        return setA.tail();
    }

    @Benchmark
    public boolean mContainsFound() {
        Key key = data.nextKeyInA();
        return setA.contains(key);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return setA.contains(key);
    }

}
