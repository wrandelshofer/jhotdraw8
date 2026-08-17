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
import scala.collection.IterableOnce;
import scala.collection.immutable.Vector;
import scala.collection.mutable.ReusableBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro, 4.40 GHz
/// # org.scala-lang:scala-library:3.9.0-RC4
///
/// Benchmark                          (size)  Mode  Cnt         Score   Error  Units
/// ScalaVectorJmh.mAddAllList             10  avgt    2        26.237          ns/op
/// ScalaVectorJmh.mAddAllList           1000  avgt    2      2539.792          ns/op
/// ScalaVectorJmh.mAddAllList         100000  avgt    2    245912.538          ns/op
/// ScalaVectorJmh.mAddAllSameType         10  avgt    2        12.580          ns/op
/// ScalaVectorJmh.mAddAllSameType       1000  avgt    2       504.331          ns/op
/// ScalaVectorJmh.mAddAllSameType     100000  avgt    2      2184.817          ns/op
/// ScalaVectorJmh.mCopyOf                 10  avgt    2        14.371          ns/op
/// ScalaVectorJmh.mCopyOf               1000  avgt    2      2817.540          ns/op
/// ScalaVectorJmh.mCopyOf             100000  avgt    2    254299.698          ns/op
/// ScalaVectorJmh.mAddFirst               10  avgt    2         4.781          ns/op
/// ScalaVectorJmh.mAddFirst             1000  avgt    2         7.352          ns/op
/// ScalaVectorJmh.mAddFirst          1000000  avgt    2        55.746          ns/op
/// ScalaVectorJmh.mAddLast                10  avgt    2         6.011          ns/op
/// ScalaVectorJmh.mAddLast              1000  avgt    2         5.880          ns/op
/// ScalaVectorJmh.mAddLast           1000000  avgt    2        22.715          ns/op
/// ScalaVectorJmh.mContainsNotFound       10  avgt    2         4.344          ns/op
/// ScalaVectorJmh.mContainsNotFound     1000  avgt    2       554.177          ns/op
/// ScalaVectorJmh.mContainsNotFound  1000000  avgt    2   4137814.057          ns/op
/// ScalaVectorJmh.mGet                    10  avgt    2         1.632          ns/op
/// ScalaVectorJmh.mGet                  1000  avgt    2         2.317          ns/op
/// ScalaVectorJmh.mGet               1000000  avgt    2        26.716          ns/op
/// ScalaVectorJmh.mHead                   10  avgt    2         0.691          ns/op
/// ScalaVectorJmh.mHead                 1000  avgt    2         0.721          ns/op
/// ScalaVectorJmh.mHead              1000000  avgt    2         0.690          ns/op
/// ScalaVectorJmh.mIterate                10  avgt    2         3.928          ns/op
/// ScalaVectorJmh.mIterate              1000  avgt    2       877.779          ns/op
/// ScalaVectorJmh.mIterate           1000000  avgt    2   6207636.812          ns/op
/// ScalaVectorJmh.mRemoveAtIndex          10  avgt    2        14.427          ns/op
/// ScalaVectorJmh.mRemoveAtIndex        1000  avgt    2        87.478          ns/op
/// ScalaVectorJmh.mRemoveAtIndex     1000000  avgt    2       240.039          ns/op
/// ScalaVectorJmh.mRemoveLast             10  avgt    2         3.427          ns/op
/// ScalaVectorJmh.mRemoveLast           1000  avgt    2        13.242          ns/op
/// ScalaVectorJmh.mRemoveLast        1000000  avgt    2        26.207          ns/op
/// ScalaVectorJmh.mReversedIterate        10  avgt    2         3.591          ns/op
/// ScalaVectorJmh.mReversedIterate      1000  avgt    2       894.541          ns/op
/// ScalaVectorJmh.mReversedIterate   1000000  avgt    2   8808471.098          ns/op
/// ScalaVectorJmh.mSet                    10  avgt    2         9.144          ns/op
/// ScalaVectorJmh.mSet                  1000  avgt    2        17.514          ns/op
/// ScalaVectorJmh.mSet               1000000  avgt    2        83.646          ns/op
/// ScalaVectorJmh.mTail                   10  avgt    2         3.363          ns/op
/// ScalaVectorJmh.mTail                 1000  avgt    2         4.802          ns/op
/// ScalaVectorJmh.mTail              1000000  avgt    2         7.231          ns/op
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@SuppressWarnings("unchecked")
public class ScalaVectorJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private Vector<Key> listA;
    private Vector<Key> listB;


    private Method updated;
    private int index;


    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);

        ReusableBuilder<Key, Vector<Key>> bA = Vector.newBuilder();
        for (Key key : data.setA) {
            bA.addOne(key);
        }
        listA = bA.result();

        ReusableBuilder<Key, Vector<Key>> bB = Vector.newBuilder();
        for (Key key : data.setB) {
            bB.addOne(key);
        }
        listB = bB.result();

        data.nextKeyInA();
        try {
            updated = Vector.class.getDeclaredMethod("updated", Integer.TYPE, Object.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        index = Math.min(listA.length() - 1, BigInteger.valueOf(listA.length() / 2).nextProbablePrime().intValue());
    }

    private Method appendedMethod;
    private Method appendedAllMethod;

    {
        try {
            appendedMethod = Vector.class.getDeclaredMethod("appended", Object.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    {
        try {
            appendedAllMethod = Vector.class.getDeclaredMethod("appendedAll", IterableOnce.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
/*
    @Benchmark
    public Vector<Key> mCopyOf() {
        ReusableBuilder<Key, Vector<Key>> builder = Vector.<Key>newBuilder();
        builder.addAll(CollectionConverters.asScala(data.listA));
        return builder.result();
    }

    @Benchmark
    public Vector<Key> mAddAllSameType() throws InvocationTargetException, IllegalAccessException {
        return (Vector<Key>) appendedAllMethod.invoke(listA, listB);
    }

    @Benchmark
    public Vector<Key> mAddAllList() throws InvocationTargetException, IllegalAccessException {
        return (Vector<Key>) appendedAllMethod.invoke(listA, CollectionConverters.asScala(data.listB));
    }

    @Benchmark
    public Vector<Key> mAddAllSet() throws InvocationTargetException, IllegalAccessException {
        return (Vector<Key>) appendedAllMethod.invoke(listA, data.setB);
    }
*/

    @Benchmark
    public Vector<Key> mCopyOfOneByOne() throws InvocationTargetException, IllegalAccessException {
        Vector<Key> l = Vector.<Key>newBuilder().result();
        for (Key key : data.listA) {
            l = (Vector<Key>) appendedMethod.invoke(l, key);
        }
        return l;
    }
/*
    //   @Benchmark
    //   public Vector<Key> mRemoveOneByOne() {
    //       var l = listA;
    //       for (var e : data.listA) {
    //           l = l.removed(e);
    //       }
    //       if (!l.isEmpty()) throw new AssertionError("map: " + l);
    //       return l;
    //   }
//
    //   @Benchmark
    //   public Vector<Key> mRemoveAll() {
    //       Vector<Key> set = listA;
    //       return set.filter(data.listA);
    //   }
//
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
        for (Iterator<Key> i = listA.reverseIterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

    @Benchmark
    public Vector<Key> mTail() {
        return listA.tail();
    }

    @Benchmark
    public Vector<Key> mAddLast() {
        Key key = data.nextKeyInB();
        return (Vector<Key>) (listA).$colon$plus(key);
    }

    @Benchmark
    public Vector<Key> mAddFirst() {
        Key key = data.nextKeyInB();
        return (Vector<Key>) (listA).$plus$colon(key);
    }

    @Benchmark
    public Vector<Key> mRemoveLast() {
        return listA.init();
    }

    @Benchmark
    public Vector<Key> mRemoveAtIndex() throws InvocationTargetException, IllegalAccessException {
        return (Vector<Key>) appendedAllMethod.invoke(listA.take(index), listA.drop(index));
    }

    @Benchmark
    public Key mGet() {
        int index = data.nextIndexInA();
        return listA.apply(index);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return listA.contains(key);
    }

    @Benchmark
    public Key mHead() {
        return listA.head();
    }

    @Benchmark
    public Vector<Key> mSet() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int index = data.nextIndexInA();
        Key key = data.nextKeyInB();

        return (Vector<Key>) updated.invoke(listA, index, key);
    }
*/
}
