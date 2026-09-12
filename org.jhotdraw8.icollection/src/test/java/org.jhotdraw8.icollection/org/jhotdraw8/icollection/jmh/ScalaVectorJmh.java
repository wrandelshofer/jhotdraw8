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
import scala.collection.Seq;
import scala.collection.immutable.Vector;
import scala.collection.mutable.Builder;
import scala.collection.mutable.ReusableBuilder;
import scala.jdk.javaapi.CollectionConverters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro, 4.40 GHz
/// # org.scala-lang:scala-library:3.9.0-RC4
///
/// Benchmark                         (size)  Mode  Cnt           Score   Error  Units
/// ScalaVectorJmh.mAdd                   11  avgt    2          68.253          ns/op
/// ScalaVectorJmh.mAdd                 1009  avgt    2        8608.271          ns/op
/// ScalaVectorJmh.mAdd               100003  avgt    2      983544.441          ns/op
/// ScalaVectorJmh.mAddAll                11  avgt    2          23.260          ns/op
/// ScalaVectorJmh.mAddAll              1009  avgt    2        2814.382          ns/op
/// ScalaVectorJmh.mAddAll            100003  avgt    2      258147.300          ns/op
/// ScalaVectorJmh.mAddAllSameType        11  avgt    2          12.480          ns/op
/// ScalaVectorJmh.mAddAllSameType      1009  avgt    2         459.825          ns/op
/// ScalaVectorJmh.mAddAllSameType    100003  avgt    2       45728.012          ns/op
/// ScalaVectorJmh.mAddAt                 11  avgt    2         305.446          ns/op
/// ScalaVectorJmh.mAddAt               1009  avgt    2      225903.701          ns/op
/// ScalaVectorJmh.mAddAt             100003  avgt    2  1191169254.667          ns/op
/// ScalaVectorJmh.mAddFirst              11  avgt    2          60.705          ns/op
/// ScalaVectorJmh.mAddFirst            1009  avgt    2        8607.193          ns/op
/// ScalaVectorJmh.mAddFirst          100003  avgt    2      969064.695          ns/op
/// ScalaVectorJmh.mAddLast               11  avgt    2          67.182          ns/op
/// ScalaVectorJmh.mAddLast             1009  avgt    2        8559.349          ns/op
/// ScalaVectorJmh.mAddLast           100003  avgt    2      978752.803          ns/op
/// ScalaVectorJmh.mContainsNotFound      11  avgt    2           4.633          ns/op
/// ScalaVectorJmh.mContainsNotFound    1009  avgt    2         555.586          ns/op
/// ScalaVectorJmh.mContainsNotFound  100003  avgt    2       72069.018          ns/op
/// ScalaVectorJmh.mCopyOf                11  avgt    2          15.478          ns/op
/// ScalaVectorJmh.mCopyOf              1009  avgt    2        2844.742          ns/op
/// ScalaVectorJmh.mCopyOf            100003  avgt    2      252407.407          ns/op
/// ScalaVectorJmh.mGet                   11  avgt    2           1.664          ns/op
/// ScalaVectorJmh.mGet                 1009  avgt    2           2.453          ns/op
/// ScalaVectorJmh.mGet               100003  avgt    2           5.516          ns/op
/// ScalaVectorJmh.mGetFirst              11  avgt    2           0.701          ns/op
/// ScalaVectorJmh.mGetFirst            1009  avgt    2           0.689          ns/op
/// ScalaVectorJmh.mGetFirst          100003  avgt    2           0.689          ns/op
/// ScalaVectorJmh.mIndexOfLast           11  avgt    2           5.926          ns/op
/// ScalaVectorJmh.mIndexOfLast         1009  avgt    2        1021.404          ns/op
/// ScalaVectorJmh.mIndexOfLast       100003  avgt    2      128951.412          ns/op
/// ScalaVectorJmh.mIterate               11  avgt    2           4.255          ns/op
/// ScalaVectorJmh.mIterate             1009  avgt    2         894.754          ns/op
/// ScalaVectorJmh.mIterate           100003  avgt    2      121046.961          ns/op
/// ScalaVectorJmh.mListIterate           11  avgt    2           4.264          ns/op
/// ScalaVectorJmh.mListIterate         1009  avgt    2         889.831          ns/op
/// ScalaVectorJmh.mListIterate       100003  avgt    2      121809.737          ns/op
/// ScalaVectorJmh.mOfArray               11  avgt    2          26.888          ns/op
/// ScalaVectorJmh.mOfArray             1009  avgt    2        2532.173          ns/op
/// ScalaVectorJmh.mOfArray           100003  avgt    2      248672.373          ns/op
/// ScalaVectorJmh.mRemoveAll             11  avgt    2         154.427          ns/op
/// ScalaVectorJmh.mRemoveAll           1009  avgt    2       24214.366          ns/op
/// ScalaVectorJmh.mRemoveAll         100003  avgt    2     5774368.093          ns/op
/// ScalaVectorJmh.mRemoveFirst           11  avgt    2          39.564          ns/op
/// ScalaVectorJmh.mRemoveFirst         1009  avgt    2        6723.405          ns/op
/// ScalaVectorJmh.mRemoveFirst       100003  avgt    2      774806.507          ns/op
/// ScalaVectorJmh.mRemoveLast            11  avgt    2          42.535          ns/op
/// ScalaVectorJmh.mRemoveLast          1009  avgt    2        6917.833          ns/op
/// ScalaVectorJmh.mRemoveLast        100003  avgt    2      782409.165          ns/op
/// ScalaVectorJmh.mReversedIterate       11  avgt    2           3.608          ns/op
/// ScalaVectorJmh.mReversedIterate     1009  avgt    2         807.612          ns/op
/// ScalaVectorJmh.mReversedIterate   100003  avgt    2      187295.925          ns/op
/// ScalaVectorJmh.mSet                   11  avgt    2          24.175          ns/op
/// ScalaVectorJmh.mSet                 1009  avgt    2        1577.373          ns/op
/// ScalaVectorJmh.mSet               100003  avgt    2      155558.263          ns/op
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@SuppressWarnings("unchecked")
public class ScalaVectorJmh {
    @Param({"11", "1009", "100003"})
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


    @Benchmark
    public Vector<Key> mCopyOf() {
        ReusableBuilder<Key, Vector<Key>> builder = Vector.<Key>newBuilder();
        builder.addAll(CollectionConverters.asScala(data.listA));
        return builder.result();
    }

    @Benchmark
    public Vector<Key> mAddAll() {
        ReusableBuilder<Key, Vector<Key>> builder = Vector.<Key>newBuilder();
        builder.addAll(listA);
        builder.addAll(CollectionConverters.asScala(data.listB));
        return builder.result();
    }

    @Benchmark
    public Vector<Key> mAddAllSameType() throws InvocationTargetException, IllegalAccessException {
        return (Vector<Key>) appendedAllMethod.invoke(listA, listB);
    }

    @Benchmark
    public Vector<Key> mOfArray() throws InvocationTargetException, IllegalAccessException {
        return (Vector<Key>) appendedAllMethod.invoke(listA, CollectionConverters.asScala(data.listB));
    }

    @Benchmark
    public Vector<Key> mAdd() {
        Vector<Key> l = Vector.<Key>newBuilder().result();
        for (Key key : data.listA) {
            l = (Vector<Key>) l.$colon$plus(key);
        }
        return l;
    }

    @Benchmark
    public Vector<Key> mAddLast() {
        Vector<Key> l = Vector.<Key>newBuilder().result();
        for (Key key : data.listA) {
            l = (Vector<Key>) l.$colon$plus(key);
        }
        return l;
    }

    @Benchmark
    public Vector<Key> mAddFirst() {
        Vector<Key> l = Vector.<Key>newBuilder().result();
        for (Key key : data.listA) {
            l = (Vector<Key>) l.$plus$colon(key);
        }
        return l;
    }

    @Benchmark
    public Vector<Key> mAddAt() {
        Vector<Key> l = Vector.<Key>newBuilder().result();
        for (Key key : data.listA) {
            int index = l.size() / 2;
            l = (Vector<Key>) ((Vector<Key>) l.take(index).$colon$plus(key)).$colon$plus$plus((IterableOnce<Key>) l.drop(index));
        }
        return l;
    }

    @Benchmark
    public Vector<Key> mRemove() {
        var l = listA;
        for (var e : data.listA) {
            int indexToRemove = l.indexOf(e);
            l = ((Vector<Key>) l.patch(indexToRemove, null, 1));
        }
        if (!l.isEmpty()) throw new AssertionError("map: " + l);
        return l;
    }

    @Benchmark
    public Vector<Key> mRemoveAt() {
        var l = listA;
        for (var e : data.listA) {
            int indexToRemove = l.size() / 2;
            l = ((Vector<Key>) l.patch(indexToRemove, null, 1));
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
        l = (Vector<Key>) l.diff(CollectionConverters.asScala(data.setA).toSeq());
        return l;
    }


    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (var i = listA.iterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

    @Benchmark
    public int mListIterate() {
        int sum = 0;
        for (var i = listA.iterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

    @Benchmark
    public int mReversedIterate() {
        int sum = 0;
        for (int i = listA.size() - 1; i >= 0; i--) {
            sum += listA.apply(i).value;
        }
        return sum;
    }


    @Benchmark
    public Key mGet() {
        int offset = data.nextIndexInA();
        return listA.apply(offset);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return listA.contains(key);
    }

    @Benchmark
    public Key mGetFirst() {
        return listA.head();
    }

    @Benchmark
    public Vector<Key> mSet() {
        int offset = data.nextIndexInA();
        Key key = data.nextKeyInB();
        return (Vector<Key>) listA.patch(offset, ((Builder<Key, Seq<Key>>) Seq.<Key>newBuilder().addOne(key)).result(), 1);
    }

    @Benchmark
    public int mIndexOfLast() {
        return listA.indexOf(listA.last());
    }


}
