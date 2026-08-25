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
/// Benchmark                                      (mask)   (size)  Mode  Cnt          Score   Error  Units
/// ScalaHashSetJmh.mAdd                              -65       10  avgt    2        229.303          ns/op
/// ScalaHashSetJmh.mAdd                              -65     1000  avgt    2      36157.912          ns/op
/// ScalaHashSetJmh.mAdd                              -65  1000000  avgt    2  266723904.605          ns/op
/// ScalaHashSetJmh.mContainsFound                    -65       10  avgt    2         19.958          ns/op
/// ScalaHashSetJmh.mContainsFound                    -65     1000  avgt    2       5458.831          ns/op
/// ScalaHashSetJmh.mContainsFound                    -65  1000000  avgt    2   82971810.179          ns/op
/// ScalaHashSetJmh.mContainsNotFound                 -65       10  avgt    2         12.048          ns/op
/// ScalaHashSetJmh.mContainsNotFound                 -65     1000  avgt    2       3384.010          ns/op
/// ScalaHashSetJmh.mContainsNotFound                 -65  1000000  avgt    2   37094318.220          ns/op
/// ScalaHashSetJmh.mCopyOf                           -65       10  avgt    2        129.081          ns/op
/// ScalaHashSetJmh.mCopyOf                           -65     1000  avgt    2      36934.436          ns/op
/// ScalaHashSetJmh.mCopyOf                           -65  1000000  avgt    2  233679802.909          ns/op
/// ScalaHashSetJmh.mGetFirst                         -65       10  avgt    2          1.136          ns/op
/// ScalaHashSetJmh.mGetFirst                         -65     1000  avgt    2          1.605          ns/op
/// ScalaHashSetJmh.mGetFirst                         -65  1000000  avgt    2          9.737          ns/op
/// ScalaHashSetJmh.mIterate                          -65       10  avgt    2          3.840          ns/op
/// ScalaHashSetJmh.mIterate                          -65     1000  avgt    2       2306.245          ns/op
/// ScalaHashSetJmh.mIterate                          -65  1000000  avgt    2    9216328.082          ns/op
/// ScalaHashSetJmh.mRemove                           -65       10  avgt    2        156.488          ns/op
/// ScalaHashSetJmh.mRemove                           -65     1000  avgt    2      42062.323          ns/op
/// ScalaHashSetJmh.mRemove                           -65  1000000  avgt    2  366234543.161          ns/op
/// ScalaHashSetJmh.mRemoveAllSameType                -65       10  avgt    2         22.618          ns/op
/// ScalaHashSetJmh.mRemoveAllSameType                -65     1000  avgt    2       5132.238          ns/op
/// ScalaHashSetJmh.mRemoveAllSameType                -65  1000000  avgt    2   11474329.272          ns/op
/// ScalaHashSetJmh.mRemoveFirst                      -65       10  avgt    2         13.116          ns/op
/// ScalaHashSetJmh.mRemoveFirst                      -65     1000  avgt    2         23.237          ns/op
/// ScalaHashSetJmh.mRemoveFirst                      -65  1000000  avgt    2         52.489          ns/op
/// ScalaHashSetJmh.mRetainAllNoneRetained            -65       10  avgt    2         21.504          ns/op
/// ScalaHashSetJmh.mRetainAllNoneRetained            -65     1000  avgt    2       6010.712          ns/op
/// ScalaHashSetJmh.mRetainAllNoneRetained            -65  1000000  avgt    2   44672602.697          ns/op
/// ScalaHashSetJmh.mRetainAllSameTypeAllRetained     -65       10  avgt    2         27.834          ns/op
/// ScalaHashSetJmh.mRetainAllSameTypeAllRetained     -65     1000  avgt    2      11263.946          ns/op
/// ScalaHashSetJmh.mRetainAllSameTypeAllRetained     -65  1000000  avgt    2   40427134.082          ns/op      ns/op
/// ```
@SuppressWarnings("unchecked")
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ScalaHashSetJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    @Param({"-65"})
    private int mask;
    private BenchmarkData data;
    private HashSet<Key> setA;
    private HashSet<Key> setAA;
    private HashSet<Key> setB;

    private Vector<Key> vectorA;

    @Benchmark
    public HashSet<Key> mAdd() {
        HashSet<Key> set = HashSet.<Key>newBuilder().result();
        for (Key key : data.listA) {
            set = (HashSet<Key>) set.$plus(key);
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public boolean mContainsFound() {
        boolean found = true;
        for (Key k : data.listA) {
            found = setA.contains(k) & found;//must be long-circuit and operator
        }
        return found;
    }

    @Benchmark
    public boolean mContainsNotFound() {
        boolean found = true;
        for (Key k : data.listB) {
            found = setA.contains(k) & found;//must be long-circuit and operator
        }
        return found;
    }

    @Benchmark
    public HashSet<Key> mCopyOf() {
        HashSet<Key> set = HashSet.<Key>newBuilder().result();
        set = set.concat(vectorA);
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public Key mGetFirst() {
        return setA.head();
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
    public HashSet<Key> mRemove() {
        HashSet<Key> set = setA;
        for (Key key : data.listA) {
            set = (HashSet<Key>) set.$minus(key);
        }
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public HashSet<Key> mRemoveAllSameType() {
        HashSet<Key> set = setA;
        HashSet<Key> updated = set.diff(setAA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public HashSet<Key> mRemoveFirst() {
        var s = setA;
        for (int i = 0, n = setA.size(); i < n; i++) {
            s = s.tail();
        }
        return s;
    }

    @Benchmark
    public HashSet<Key> mRetainAllNoneRetained() {
        HashSet<Key> set = setA;
        HashSet<Key> updated = set.intersect(setB);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public HashSet<Key> mRetainAllSameTypeAllRetained() {
        HashSet<Key> set = setA;
        HashSet<Key> updated = set.intersect(setAA);
        assert updated == setA;
        return updated;
    }

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

}
