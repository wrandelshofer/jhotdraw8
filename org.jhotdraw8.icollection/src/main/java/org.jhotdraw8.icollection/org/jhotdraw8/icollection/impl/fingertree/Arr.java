package org.jhotdraw8.icollection.impl.fingertree;

import java.lang.reflect.Array;
import java.util.Arrays;

import static java.util.Arrays.copyOf;

/// Provides helper methods for arrays
///
/// This code has been derived from
/// [Scala 3, Vector.scala](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/library/src/scala/collection/immutable/Vector.scala),
/// EPFL and Lightbend, Inc. dba Akka,
/// [Apache License 2.0](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/LICENSE)
public interface Arr<A> {
    @SuppressWarnings("unchecked")
    static <A> A[] new1(int size) {
        return (A[]) new Object[size];
    }

    static <A> A[][] new2(int size) {
        //noinspection unchecked
        return (A[][]) new Object[size][];
    }

    static <A> A[][][] new3(int size) {
        //noinspection unchecked
        return (A[][][]) new Object[size][][];
    }

    static <A> A[][][][] new4(int size) {
        //noinspection unchecked
        return (A[][][][]) new Object[size][][][];
    }

    static <A> A[][][][][] new5(int size) {
        //noinspection unchecked
        return (A[][][][][]) new Object[size][][][][];
    }

    static <A> A[][][][][][] new6(int size) {
        //noinspection unchecked
        return (A[][][][][][]) new Object[size][][][][][];
    }

    static <A> A[] wrap1(A x) {
        var a = Arr.<A>new1(1);
        a[0] = x;
        return a;
    }

    static <A> A[][] wrap2(A[] x) {
        var a = Arr.<A>new2(1);
        a[0] = x;
        return a;
    }

    static <A> A[][][] wrap3(A[][] x) {
        var a = Arr.<A>new3(1);
        a[0] = x;
        return a;
    }

    static <A> A[][][][] wrap4(A[][][] x) {
        var a = Arr.<A>new4(1);
        a[0] = x;
        return a;
    }

    static <A> A[][][][][] wrap5(A[][][][] x) {
        var a = Arr.<A>new5(1);
        a[0] = x;
        return a;
    }

    static <A> A[][][][][][] wrap6(A[][][][][] x) {
        var a = Arr.<A>new6(1);
        a[0] = x;
        return a;
    }

    static <A> A[] copyIfDifferentSize(A[] a, int len) {
        return (a.length == len) ? a : copyOf(a, len);
    }

    static <A> A[] copyAppend(A[] as, A a) {
        var alen = as.length;
        var ac = copyOf(as, alen + 1);
        ac[alen] = a;
        return ac;
    }

    static <T> T[] concatArrays(T[] a, T[] b) {
        var dest = copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, dest, a.length, b.length);
        return dest;
    }

    static <A> A[] copyTail(A[] as) {
        return Arrays.copyOfRange(as, 1, as.length);
    }

    static <A> A[] copySlice(A[] as, int from, int to) {
        return Arrays.copyOfRange(as, from, to);
    }

    static <A> A[] copyInit(A[] as) {
        return copyOf(as, as.length - 1);
    }

    static <A> A[] copyPrepend(A a, A[] as) {
        var alen = as.length;
        @SuppressWarnings("unchecked")
        var ac = (A[]) Array.newInstance(as.getClass().getComponentType(), alen + 1);
        System.arraycopy(as, 0, ac, 1, alen);
        ac[0] = a;
        return ac;
    }

    static <T> T[] copyOrUse(T[] a, int start, int end) {
        if (start == 0 && end == a.length) return a;
        else return Arrays.copyOfRange(a, start, end);
    }

    static <A> A[] copyUpdate(A[] as, int idx1, A a) {
        var c = as.clone();
        c[idx1] = a;
        return c;
    }

    static <A> A[] copyRemove(A[] as, int idx) {
        var c = (A[]) new Object[as.length - 1];
        System.arraycopy(as, 0, c, 0, idx);
        System.arraycopy(as, idx + 1, c, idx, as.length - idx - 1);
        return c;
    }

    static <A> A[][] copyUpdate(A[][] as, int idx2, int idx1, A a) {
        var c = as.clone();
        c[idx2] = copyUpdate(c[idx2], idx1, a);
        return c;
    }

    static <A> A[][][] copyUpdate(A[][][] as, int idx3, int idx2, int idx1, A a) {
        var c = as.clone();
        c[idx3] = copyUpdate(c[idx3], idx2, idx1, a);
        return c;
    }

    static <A> A[][][][] copyUpdate(A[][][][] as, int idx4, int idx3, int idx2, int idx1, A a) {
        var c = as.clone();
        c[idx4] = copyUpdate(c[idx4], idx3, idx2, idx1, a);
        return c;
    }

    static <A> A[][][][][] copyUpdate(A[][][][][] as, int idx5, int idx4, int idx3, int idx2, int idx1, A a) {
        var c = as.clone();
        c[idx5] = copyUpdate(c[idx5], idx4, idx3, idx2, idx1, a);
        return c;
    }

    static <A> A[][][][][][] copyUpdate(A[][][][][][] array, int idx6, int idx5, int idx4, int idx3, int idx2, int idx1, A a) {
        var c = array.clone();
        c[idx6] = copyUpdate(c[idx6], idx5, idx4, idx3, idx2, idx1, a);
        return c;
    }


    final Object[] EMPTY1 = new Object[0];
    final Object[][] EMPTY2 = new Object[0][];
    final Object[][][] EMPTY3 = new Object[0][][];
    final Object[][][][] EMPTY4 = new Object[0][][][];
    final Object[][][][][] EMPTY5 = new Object[0][][][][];
    final Object[][][][][][] EMPTY6 = new Object[0][][][][][];

    @SuppressWarnings("unchecked")
    static <A> A[] empty1() {
        return (A[]) EMPTY1;
    }

    @SuppressWarnings("unchecked")
    static <A> A[][] empty2() {
        return (A[][]) EMPTY2;
    }

    @SuppressWarnings("unchecked")
    static <A> A[][][] empty3() {
        return (A[][][]) EMPTY3;
    }

    @SuppressWarnings("unchecked")
    static <A> A[][][][] empty4() {
        return (A[][][][]) EMPTY4;
    }

    @SuppressWarnings("unchecked")
    static <A> A[][][][][] empty5() {
        return (A[][][][][]) EMPTY5;
    }

    @SuppressWarnings("unchecked")
    static <A> A[][][][][][] empty6() {
        return (A[][][][][][]) EMPTY6;
    }
}
