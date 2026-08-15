package org.jhotdraw8.icollection.impl.fingertree;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class FingerTreeIterator<A> implements Iterator<A> {
    private Object[] arrays = new Object[12];
    private int[] dimensions = new int[12];
    private int[] indices = new int[12];
    int depth = -1;

    /// Current array of dimension 1
    private A[] a1;
    /// Index into a1
    int i1;
    /// Length of a1
    int a1len;

    public FingerTreeIterator(FingerTree<A> tree) {
        switch (tree) {
            case Tree0<A> n -> {

            }
            case Tree1<A> n -> {
                a1 = n.d1();
            }
            case Tree2<A> n -> {
                push(n.s1(), 1);
                push(n.d2(), 2);
                a1 = n.p1();
            }
            case Tree3<A> n -> {
                push(n.s1(), 1);
                push(n.s2(), 2);
                push(n.d3(), 3);
                push(n.p2(), 2);
                a1 = n.p1();
            }
            case Tree4<A> n -> {
                push(n.s1(), 1);
                push(n.s2(), 2);
                push(n.s3(), 3);
                push(n.d4(), 4);
                push(n.p3(), 3);
                push(n.p2(), 2);
                a1 = n.p1();
            }
            case Tree5<A> n -> {
                push(n.s1(), 1);
                push(n.s2(), 2);
                push(n.s3(), 3);
                push(n.s4(), 4);
                push(n.d5(), 5);
                push(n.p4(), 4);
                push(n.p3(), 3);
                push(n.p2(), 2);
                a1 = n.p1();
            }
            case Tree6<A> n -> {
                push(n.s1(), 1);
                push(n.s2(), 2);
                push(n.s3(), 3);
                push(n.s4(), 4);
                push(n.s5(), 5);
                push(n.d6(), 6);
                push(n.p5(), 5);
                push(n.p4(), 4);
                push(n.p3(), 3);
                push(n.p2(), 2);
                a1 = n.p1();
            }
        }
        a1len = a1 == null ? 0 : a1.length;
    }

    public FingerTreeIterator(FingerTree<A> tree, int fromIndex, int toIndex) {
        this(tree.slice(fromIndex, toIndex));
    }

    public boolean skip(int n) {
        while (n > 0) {
            if (i1 == a1len) moveToNextArray();
            if (i1 < a1len) {
                int step = Math.min(n, a1len - i1);
                i1 += step;
                n -= step;
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasNext() {
        if (i1 >= a1len) moveToNextArray();
        return i1 < a1len;
    }

    @Override
    @SuppressWarnings("unchecked")
    public A next() {
        if (i1 == a1len) moveToNextArray();
        if (i1 < a1len) {
            return a1[i1++];
        }
        throw new NoSuchElementException();
    }

    private void moveToNextArray() {
        while (depth >= 0) {
            A[] a = (A[]) arrays[depth];
            int dim = dimensions[depth];
            if (dim == 1) {
                pop();
                this.a1 = a;
                a1len = this.a1.length;
                i1 = 0;
                if (a1len > 0) {
                    return;
                }
            } else {
                int index = indices[depth]++;
                if (index < a.length) {
                    var subarray = a[index];
                    push(subarray, dim - 1);
                } else {
                    pop();
                }
            }
        }
        a1 = null;
        a1len = 0;
        i1 = 0;
    }

    private void push(Object array, int dimension) {
        depth++;
        arrays[depth] = array;
        dimensions[depth] = dimension;
        indices[depth] = 0;
    }

    private void pop() {
        arrays[depth] = null;
        dimensions[depth] = 0;
        indices[depth] = 0;
        depth--;
    }
}
