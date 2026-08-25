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
import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.immutable.HashMap;
import scala.collection.immutable.Map;
import scala.collection.immutable.Vector;
import scala.collection.mutable.Builder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/// ```
/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro, 4.40 GHz
/// # org.scala-lang:scala-library:3.9.0-RC4
///
/// enchmark                      (mask)  (size)  Mode  Cnt         Score   Error  Units
/// ScalaHashMapJmh.mContainsKey      -65      10  avgt    2        16.292          ns/op
/// ScalaHashMapJmh.mContainsKey      -65    1000  avgt    2      4394.620          ns/op
/// ScalaHashMapJmh.mContainsKey      -65  100000  avgt    2   2516577.375          ns/op
/// ScalaHashMapJmh.mCopyOf           -65      10  avgt    2       145.083          ns/op
/// ScalaHashMapJmh.mCopyOf           -65    1000  avgt    2     34460.295          ns/op
/// ScalaHashMapJmh.mCopyOf           -65  100000  avgt    2  10811275.351          ns/op
/// ScalaHashMapJmh.mGetFirst         -65      10  avgt    2         1.130          ns/op
/// ScalaHashMapJmh.mGetFirst         -65    1000  avgt    2         1.546          ns/op
/// ScalaHashMapJmh.mGetFirst         -65  100000  avgt    2         6.707          ns/op
/// ScalaHashMapJmh.mIterateEntry     -65      10  avgt    2         4.155          ns/op
/// ScalaHashMapJmh.mIterateEntry     -65    1000  avgt    2      1465.277          ns/op
/// ScalaHashMapJmh.mIterateEntry     -65  100000  avgt    2    751109.245          ns/op
/// ScalaHashMapJmh.mIterateKey       -65      10  avgt    2         4.190          ns/op
/// ScalaHashMapJmh.mIterateKey       -65    1000  avgt    2      1406.465          ns/op
/// ScalaHashMapJmh.mIterateKey       -65  100000  avgt    2    797446.090          ns/op
/// ScalaHashMapJmh.mPut              -65      10  avgt    2       192.087          ns/op
/// ScalaHashMapJmh.mPut              -65    1000  avgt    2     39389.846          ns/op
/// ScalaHashMapJmh.mPut              -65  100000  avgt    2  13491331.405          ns/op
/// ScalaHashMapJmh.mRemove           -65      10  avgt    2       130.074          ns/op
/// ScalaHashMapJmh.mRemove           -65    1000  avgt    2     40048.029          ns/op
/// ScalaHashMapJmh.mRemove           -65  100000  avgt    2  16683743.050          ns/op
/// ScalaHashMapJmh.mRemoveAll        -65      10  avgt    2       138.127          ns/op
/// ScalaHashMapJmh.mRemoveAll        -65    1000  avgt    2     40875.102          ns/op
/// ScalaHashMapJmh.mRemoveAll        -65  100000  avgt    2  15019110.131          ns/op
/// ScalaHashMapJmh.mRemoveFirst      -65      10  avgt    2       148.939          ns/op
/// ScalaHashMapJmh.mRemoveFirst      -65    1000  avgt    2     52868.478          ns/op
/// ScalaHashMapJmh.mRemoveFirst      -65  100000  avgt    2  11112966.863          ns/op
/// ```
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@SuppressWarnings("unchecked")
public class ScalaHashMapJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private HashMap<Key, Boolean> setA;
    private Vector<Tuple2<Key, Boolean>> listA;
    private Vector<Key> listAKeys;
    private Method appended;


    @SuppressWarnings("unchecked")
    @Setup
    public void setup() throws InvocationTargetException, IllegalAccessException {
        try {
            appended = Vector.class.getDeclaredMethod("appended", Object.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        data = new BenchmarkData(size, mask);
        Builder<Tuple2<Key, Boolean>, HashMap<Key, Boolean>> b = HashMap.newBuilder();
        for (Key key : data.setA) {
            Tuple2<Key, Boolean> elem = new Tuple2<>(key, Boolean.TRUE);
            b.addOne(elem);
        }
        listA = Vector.<Tuple2<Key, Boolean>>newBuilder().result();
        listAKeys = Vector.<Key>newBuilder().result();
        for (Key key : data.listA) {
            Tuple2<Key, Boolean> elem = new Tuple2<>(key, Boolean.TRUE);
            listA = (Vector<Tuple2<Key, Boolean>>) appended.invoke(listA, elem);
            listAKeys = (Vector<Key>) appended.invoke(listAKeys, key);
        }
        setA = b.result();

    }


    @Benchmark
    public HashMap<Key, Boolean> mCopyOf() {
        return HashMap.from(listA);
    }


    @Benchmark
    public HashMap<Key, Boolean> mRemove() {
        HashMap<Key, Boolean> set = setA;
        for (Key key : data.listA) {
            set = set.removed(key);
        }
        return set;
    }

    @Benchmark
    public HashMap<Key, Boolean> mRemoveAll() {
        HashMap<Key, Boolean> set = setA;
        return set.removedAll(listAKeys);
    }

    @Benchmark
    public int mIterateKey() {
        int sum = 0;
        for (Iterator<Key> i = setA.keysIterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

    @Benchmark
    public int mIterateEntry() {
        int sum = 0;
        for (var i = setA.iterator(); i.hasNext(); ) {
            sum += i.next()._1.value;
        }
        return sum;
    }


    @Benchmark
    public Map<Key, Boolean> mPut() {
        Map<Key, Boolean> set = HashMap.<Key, Boolean>newBuilder().result();
        for (Key key : data.listA) {
            set = set.$plus(new Tuple2<>(key, Boolean.FALSE));
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public int mContainsKey() {
        int count = 0;
        for (Key k : data.listC) {
            if (setA.contains(k)) count++;
        }
        assert count == data.listC.size() / 2;
        return count;
    }

    @Benchmark
    public Key mGetFirst() {
        return setA.head()._1;
    }

    @Benchmark
    public HashMap<Key, Boolean> mRemoveFirst() {
        var set = setA;
        while (!set.isEmpty()) set = set.tail();
        return set;
    }

}
