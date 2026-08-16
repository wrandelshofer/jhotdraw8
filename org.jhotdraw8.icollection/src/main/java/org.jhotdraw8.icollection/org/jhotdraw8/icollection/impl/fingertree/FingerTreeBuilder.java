package org.jhotdraw8.icollection.impl.fingertree;

import java.util.Arrays;
import java.util.function.Consumer;

import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyIfDifferentSize;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyOrUse;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyPrepend;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyTail;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.new1;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.new2;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.new3;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.new4;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.new5;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.new6;
import static org.jhotdraw8.icollection.impl.fingertree.FingerTree.BITS;
import static org.jhotdraw8.icollection.impl.fingertree.FingerTree.BITS2;
import static org.jhotdraw8.icollection.impl.fingertree.FingerTree.BITS3;
import static org.jhotdraw8.icollection.impl.fingertree.FingerTree.BITS4;
import static org.jhotdraw8.icollection.impl.fingertree.FingerTree.BITS5;
import static org.jhotdraw8.icollection.impl.fingertree.FingerTree.LASTWIDTH;
import static org.jhotdraw8.icollection.impl.fingertree.FingerTree.MASK;
import static org.jhotdraw8.icollection.impl.fingertree.FingerTree.WIDTH;
import static org.jhotdraw8.icollection.impl.fingertree.FingerTree.WIDTH2;
import static org.jhotdraw8.icollection.impl.fingertree.FingerTree.WIDTH3;
import static org.jhotdraw8.icollection.impl.fingertree.FingerTree.WIDTH4;
import static org.jhotdraw8.icollection.impl.fingertree.FingerTree.WIDTH5;

/// This code has been derived from
/// [Scala 3, Vector.scala](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/library/src/scala/collection/immutable/Vector.scala),
/// EPFL and Lightbend, Inc. dba Akka,
/// [Apache License 2.0](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/LICENSE)
public class FingerTreeBuilder<A> {


    private A[][][][][][] a6;
    private A[][][][][] a5;
    private A[][][][] a4;
    private A[][][] a3;
    private A[][] a2;
    private A[] a1 = new1(WIDTH);
    private int len1, lenRest, offset = 0;
    private boolean prefixIsRightAligned = false;
    private int depth = 1;

    private void setLen(int i) {
        len1 = i & MASK;
        lenRest = i - len1;
    }

    private int knownSize() {
        return len1 + lenRest - offset;
    }

    private int size() {
        return knownSize();
    }


    public boolean isEmpty() {
        return knownSize() == 0;
    }

    private boolean nonEmpty() {
        return knownSize() != 0;
    }

    @SuppressWarnings("unchecked")
    void clear() {
        a6 = null;
        a5 = null;
        a4 = null;
        a3 = null;
        a2 = null;
        a1 = (A[]) new Object[WIDTH];
        len1 = 0;
        lenRest = 0;
        offset = 0;
        prefixIsRightAligned = false;
        depth = 1;
    }

    public FingerTreeBuilder<A> initSparse(int size, A elem) {
        if (!isEmpty()) throw new IllegalStateException("initSparse can only be called when empty");
        setLen(size);
        Arrays.fill(a1, elem);
        if (size > WIDTH) {
            a2 = (A[][]) new Object[WIDTH][];
            Arrays.fill(a2, a1);
            if (size > WIDTH2) {
                a3 = (A[][][]) new Object[WIDTH][][];
                Arrays.fill(a3, a2);
                if (size > WIDTH3) {
                    a4 = (A[][][][]) new Object[WIDTH][][][];
                    Arrays.fill(a4, a3);
                    if (size > WIDTH4) {
                        a5 = (A[][][][][]) new Object[WIDTH][][][][];
                        Arrays.fill(a5, a4);
                        if (size > WIDTH5) {
                            a6 = (A[][][][][][]) new Object[LASTWIDTH][][][][][];
                            Arrays.fill(a6, a5);
                            depth = 6;
                        } else depth = 5;
                    } else depth = 4;
                } else depth = 3;
            } else depth = 2;
        } else depth = 1;
        return this;
    }

    private void initFrom(A[] p1) {
        depth = 1;
        setLen(p1.length);
        a1 = copyOrUse(p1, 0, WIDTH);
        if (len1 == 0 && lenRest > 0) {
// force advance() on next addition:
            len1 = WIDTH;
            lenRest -= WIDTH;
        }
    }

    @SuppressWarnings("unchecked")
    public FingerTreeBuilder<A> initFrom(FingerTree<?> v) {
        if (!isEmpty()) throw new IllegalStateException("initFrom can only be called when empty");
        switch (v.getSliceCount()) {
            case 0 -> {
            }
            case 1 -> {
                Tree1<A> v1 = (Tree1<A>) v;
                depth = 1;
                setLen(v1.size());
                a1 = copyOrUse(v1.d1(), 0, WIDTH);
            }
            case 3 -> {
                Tree2<A> v2 = (Tree2<A>) v;
                var d2 = v2.d2();
                a1 = copyOrUse(v2.s1(), 0, WIDTH);
                depth = 2;
                offset = WIDTH - v2.len1();
                setLen(v2.size() + offset);
                a2 = new2(WIDTH);
                a2[0] = v2.p1();
                System.arraycopy(d2, 0, a2, 1, d2.length);
                a2[d2.length + 1] = a1;
            }
            case 5 -> {
                Tree3<A> v3 = (Tree3<A>) v;
                var d3 = v3.d3();
                var s2 = v3.s2();
                a1 = copyOrUse(v3.s1(), 0, WIDTH);
                depth = 3;
                offset = WIDTH2 - v3.len12();
                setLen(v3.size() + offset);
                a3 = new3(WIDTH);
                a3[0] = copyPrepend(v3.p1(), v3.p2());
                System.arraycopy(d3, 0, a3, 1, d3.length);
                a2 = copyOf(s2, WIDTH);
                a3[d3.length + 1] = a2;
                a2[s2.length] = a1;
            }
            case 7 -> {
                Tree4<A> v4 = (Tree4<A>) v;
                var d4 = v4.d4();
                var s3 = v4.s3();
                var s2 = v4.s2();
                a1 = copyOrUse(v4.s1(), 0, WIDTH);
                depth = 4;
                offset = WIDTH3 - v4.len123();
                setLen(v4.size() + offset);
                a4 = new4(WIDTH);
                a4[0] = copyPrepend(copyPrepend(v4.p1(), v4.p2()), v4.p3());
                System.arraycopy(d4, 0, a4, 1, d4.length);
                a3 = copyOf(s3, WIDTH);
                a2 = copyOf(s2, WIDTH);
                a4[d4.length + 1] = a3;
                a3[s3.length] = a2;
                a2[s2.length] = a1;
            }
            case 9 -> {
                Tree5<A> v5 = (Tree5<A>) v;
                var d5 = v5.d5();
                var s4 = v5.s4();
                var s3 = v5.s3();
                var s2 = v5.s2();
                a1 = copyOrUse(v5.s1(), 0, WIDTH);
                depth = 5;
                offset = WIDTH4 - v5.len1234();
                setLen(v5.size() + offset);
                a5 = new5(WIDTH);
                a5[0] = copyPrepend(copyPrepend(copyPrepend(v5.p1(), v5.p2()), v5.p3()), v5.p4());
                System.arraycopy(d5, 0, a5, 1, d5.length);
                a4 = copyOf(s4, WIDTH);
                a3 = copyOf(s3, WIDTH);
                a2 = copyOf(s2, WIDTH);
                a5[d5.length + 1] = a4;
                a4[s4.length] = a3;
                a3[s3.length] = a2;
                a2[s2.length] = a1;
            }
            case 11 -> {
                Tree6<A> v6 = (Tree6<A>) v;
                var d6 = v6.d6();
                var s5 = v6.s5();
                var s4 = v6.s4();
                var s3 = v6.s3();
                var s2 = v6.s2();
                a1 = copyOrUse(v6.s1(), 0, WIDTH);
                depth = 6;
                offset = WIDTH5 - v6.len12345();
                setLen(v6.size() + offset);
                a6 = new6(LASTWIDTH);
                a6[0] = copyPrepend(copyPrepend(copyPrepend(copyPrepend(v6.p1(), v6.p2()), v6.p3()), v6.p4()), v6.p5());
                System.arraycopy(d6, 0, a6, 1, d6.length);
                a5 = copyOf(s5, WIDTH);
                a4 = copyOf(s4, WIDTH);
                a3 = copyOf(s3, WIDTH);
                a2 = copyOf(s2, WIDTH);
                a6[d6.length + 1] = a5;
                a5[s5.length] = a4;
                a4[s4.length] = a3;
                a3[s3.length] = a2;
                a2[s2.length] = a1;
            }
        }
        if (len1 == 0 && lenRest > 0) {
// force advance() on next addition:
            len1 = WIDTH;
            lenRest -= WIDTH;
        }
        return this;
    }


    private FingerTreeBuilder<A> alignTo(int before, FingerTree<A> bigVector) {
        if (len1 != 0 || lenRest != 0)
            throw new UnsupportedOperationException("A non-empty VectorBuilder cannot be aligned retrospectively. Please call .reset() or use a new VectorBuilder.");
        int prefixLength, maxPrefixLength;
        switch (bigVector) {
            case Tree0<A> _ -> {
                prefixLength = 0;
                maxPrefixLength = 1;
            }
            case Tree1<A> v1 -> {
                prefixLength = 0;
                maxPrefixLength = 1;
            }
            case Tree2<A> v2 -> {
                prefixLength = v2.len1();
                maxPrefixLength = WIDTH;
            }
            case Tree3<A> v3 -> {
                prefixLength = v3.len12();
                maxPrefixLength = WIDTH2;
            }
            case Tree4<A> v4 -> {
                prefixLength = v4.len123();
                maxPrefixLength = WIDTH3;
            }
            case Tree5<A> v5 -> {
                prefixLength = v5.len1234();
                maxPrefixLength = WIDTH4;
            }
            case Tree6<A> v6 -> {
                prefixLength = v6.len12345();
                maxPrefixLength = WIDTH5;
            }
        }
        if (maxPrefixLength == 1) return this; // does not really make sense to align for <= 32 element-vector
        var overallPrefixLength = (before + prefixLength) % maxPrefixLength;
        offset = (maxPrefixLength - overallPrefixLength) % maxPrefixLength;
        // pretend there are already `offset` elements added
        advanceN(offset & ~MASK);
        len1 = offset & MASK;
        prefixIsRightAligned = true;
        return this;
    }

    private void shrinkOffsetIfToLarge(int width) {
        var newOffset = offset % width;
        lenRest -= offset - newOffset;
        offset = newOffset;
    }

    /**
     * Removes `offset` leading `null`s in the prefix.
     * This is needed after calling `alignTo` and subsequent additions,
     * directly before the result is used for creating a new Vector.
     * Note that the outermost array keeps its length to keep the
     * Builder re-usable.
     * <p>
     * example:
     * a2 = Array(null, ..., null, Array(null, .., null, 0, 1, .., element), Array(element+1, .., element+32), ...)
     * becomes
     * a2 = Array(Array(0, 1, .., element), Array(element+1, .., element+32), ..., ?, ..., ?)
     */
    @SuppressWarnings("unchecked")
    private void leftAlignPrefix() {
        A[] a = null; // the array we modify
        A[] aParent = null; // a's parent, so aParent[0] == a
        if (depth >= 6) {
            a = (A[]) a6;
            var i = offset >>> BITS5;
            if (i > 0) System.arraycopy(a, i, a, 0, LASTWIDTH - i);
            shrinkOffsetIfToLarge(WIDTH5);
            if ((lenRest >>> BITS5) == 0) depth = 5;
            aParent = a;
            a = (A[]) a[0];
        }
        if (depth >= 5) {
            if (a == null) a = (A[]) a5;
            var i = (offset >>> BITS4) & MASK;
            if (depth == 5) {
                if (i > 0) System.arraycopy(a, i, a, 0, WIDTH - i);
                a5 = (A[][][][][]) a;
                shrinkOffsetIfToLarge(WIDTH4);
                if ((lenRest >>> BITS4) == 0) depth = 4;
            } else {
                if (i > 0) a = copyOfRange(a, i, WIDTH);
                aParent[0] = (A) a;
            }
            aParent = a;
            a = (A[]) a[0];
        }
        if (depth >= 4) {
            if (a == null) a = (A[]) a4;
            var i = (offset >>> BITS3) & MASK;
            if (depth == 4) {
                if (i > 0) System.arraycopy(a, i, a, 0, WIDTH - i);
                a4 = (A[][][][]) a;
                shrinkOffsetIfToLarge(WIDTH3);
                if ((lenRest >>> BITS3) == 0) depth = 3;
            } else {
                if (i > 0) a = copyOfRange(a, i, WIDTH);
                aParent[0] = (A) a;
            }
            aParent = a;
            a = (A[]) a[0];
        }
        if (depth >= 3) {
            if (a == null) a = (A[]) a3;
            var i = (offset >>> BITS2) & MASK;
            if (depth == 3) {
                if (i > 0) System.arraycopy(a, i, a, 0, WIDTH - i);
                a3 = (A[][][]) a;
                shrinkOffsetIfToLarge(WIDTH2);
                if ((lenRest >>> BITS2) == 0) depth = 2;
            } else {
                if (i > 0) a = copyOfRange(a, i, WIDTH);
                aParent[0] = (A) a;
            }
            aParent = a;
            a = (A[]) a[0];
        }
        if (depth >= 2) {
            if (a == null) a = (A[]) a2;
            var i = (offset >>> BITS) & MASK;
            if (depth == 2) {
                if (i > 0) System.arraycopy(a, i, a, 0, WIDTH - i);
                a2 = (A[][]) a;
                shrinkOffsetIfToLarge(WIDTH);
                if ((lenRest >>> BITS) == 0) depth = 1;
            } else {
                if (i > 0) a = copyOfRange(a, i, WIDTH);
                aParent[0] = (A) a;
            }
            aParent = a;
            a = (A[]) a[0];
        }
        if (depth >= 1) {
            if (a == null) a = a1;
            var i = offset & MASK;
            if (depth == 1) {
                if (i > 0) System.arraycopy(a, i, a, 0, WIDTH - i);
                a1 = a;
                len1 -= offset;
                offset = 0;
            } else {
                if (i > 0) a = copyOfRange(a, i, WIDTH);
                aParent[0] = (A) a;
            }
        }
        prefixIsRightAligned = false;
    }

    public void addOne(A elem) {
        if (len1 == WIDTH) advance();
        a1[len1] = elem;
        len1 += 1;
    }

    private void addArr1(A[] data) {
        if (data.length > WIDTH) throw new IllegalArgumentException("array size too large");
        var dl = data.length;
        if (dl > 0) {
            if (len1 == WIDTH) advance();
            var copy1 = Math.min(WIDTH - len1, dl);
            var copy2 = dl - copy1;
            System.arraycopy(data, 0, a1, len1, copy1);
            len1 += copy1;
            if (copy2 > 0) {
                advance();
                System.arraycopy(data, copy1, a1, 0, copy2);
                len1 += copy2;
            }
        }
    }

    private void addArrN(A[] slice, int dim) {
//    assert(dim >= 2)
//    assert(lenRest % WIDTH == 0)
//    assert(len1 == 0 || len1 == WIDTH)
        if (slice.length == 0) return;
        if (len1 == WIDTH) advance();
        var sl = slice.length;
        switch (dim) {
            case 2 -> {
                // lenRest is always a multiple of WIDTH
                var copy1 = Math.min(((WIDTH2 - lenRest) >>> BITS) & MASK, sl);
                var copy2 = sl - copy1;
                var destPos = (lenRest >>> BITS) & MASK;
                System.arraycopy(slice, 0, a2, destPos, copy1);
                advanceN(WIDTH * copy1);
                if (copy2 > 0) {
                    System.arraycopy(slice, copy1, a2, 0, copy2);
                    advanceN(WIDTH * copy2);
                }
            }
            case 3 -> {
                if (lenRest % WIDTH2 != 0) {
                    // lenRest is not multiple of WIDTH2, so this slice does not align, need to try lower dimension
                    for (var e : slice) {
                        addArrN((A[]) e, 2);
                    }
                    return;
                }
                var copy1 = Math.min(((WIDTH3 - lenRest) >>> BITS2) & MASK, sl);
                var copy2 = sl - copy1;
                var destPos = (lenRest >>> BITS2) & MASK;
                System.arraycopy(slice, 0, a3, destPos, copy1);
                advanceN(WIDTH2 * copy1);
                if (copy2 > 0) {
                    System.arraycopy(slice, copy1, a3, 0, copy2);
                    advanceN(WIDTH2 * copy2);
                }
            }
            case 4 -> {
                if (lenRest % WIDTH3 != 0) {
                    // lenRest is not multiple of WIDTH3, so this slice does not align, need to try lower dimensions
                    for (var e : slice) {
                        addArrN((A[]) e, 3);
                    }
                    return;
                }
                var copy1 = Math.min(((WIDTH4 - lenRest) >>> BITS3) & MASK, sl);
                var copy2 = sl - copy1;
                var destPos = (lenRest >>> BITS3) & MASK;
                System.arraycopy(slice, 0, a4, destPos, copy1);
                advanceN(WIDTH3 * copy1);
                if (copy2 > 0) {
                    System.arraycopy(slice, copy1, a4, 0, copy2);
                    advanceN(WIDTH3 * copy2);
                }
            }
            case 5 -> {
                if (lenRest % WIDTH4 != 0) {
                    // lenRest is not multiple of WIDTH4, so this slice does not align, need to try lower dimensions
                    for (var e : slice) {
                        addArrN((A[]) e, 4);
                    }
                    return;
                }
                var copy1 = Math.min(((WIDTH5 - lenRest) >>> BITS4) & MASK, sl);
                var copy2 = sl - copy1;
                var destPos = (lenRest >>> BITS4) & MASK;
                System.arraycopy(slice, 0, a5, destPos, copy1);
                advanceN(WIDTH4 * copy1);
                if (copy2 > 0) {
                    System.arraycopy(slice, copy1, a5, 0, copy2);
                    advanceN(WIDTH4 * copy2);
                }
            }
            case 6 -> { // note width is now LASTWIDTH
                if (lenRest % WIDTH5 != 0) {
                    // lenRest is not multiple of WIDTH5, so this slice does not align, need to try lower dimensions
                    for (var e : slice) {
                        addArrN((A[]) e, 5);
                    }
                    return;
                }
                var copy1 = sl;
                // there is no copy2 because there can't be another a6 to copy to
                var destPos = lenRest >>> BITS5;
                if (destPos + copy1 > LASTWIDTH)
                    throw new IllegalArgumentException("exceeding 2^31 elements");
                System.arraycopy(slice, 0, a6, destPos, copy1);
                advanceN(WIDTH5 * copy1);
            }
        }
    }

    private <T, K> void foreachRec(int level, T[] a, Consumer<K> f) {
        var i = 0;
        var len = a.length;
        if (level == 0) {
            while (i < len) {
                f.accept((K) a[i]);
                i += 1;
            }
        } else {
            var l = level - 1;
            while (i < len) {
                foreachRec(l, (T[]) a[i], f);
                i += 1;
            }
        }
    }

    /// Dimension of the slice at offset.
    ///
    /// @param count the total number of slices
    /// @param idx   the zero-based slice offset
    /// @return the dimension (1-based level) of the slice at position \`idx\`: rises from 1 at the outermost prefix slice up to \`count/2 + 1\` at the central data array, then falls back to 1 at the outermost suffix slice
    private int vectorSliceDim(int count, int idx) {
        var c = count / 2;
        return c + 1 - Math.abs(idx - c);
    }

    public FingerTreeBuilder<A> addAll(Iterable<? extends A> elems) {
        var it = elems.iterator();
        while (it.hasNext()) {
            addOne(it.next());
        }
        return this;
    }

    public FingerTreeBuilder<A> addVector(FingerTree<? extends A> xs) {
        var sliceCount = xs.getSliceCount();
        var sliceIdx = 0;
        while (sliceIdx < sliceCount) {
            var slice = xs.getSliceAt(sliceIdx);
            var vsdim = vectorSliceDim(sliceCount, sliceIdx);
            if (vsdim == 1) {
                addArr1(slice);
            } else if (len1 == WIDTH || len1 == 0) {
                addArrN(slice, vsdim);
            } else {
                foreachRec(vsdim - 2, slice, this::addArr1);
            }
            sliceIdx += 1;
        }
        return this;
    }


    private void advance() {
        var idx = lenRest + WIDTH;
        var xor = idx ^ lenRest;
        lenRest = idx;
        len1 = 0;
        advance1(idx, xor);
    }

    private void advanceN(int n) {
        if (n > 0) {
// assert(n % 32 == 0)
            var idx = lenRest + n;
            var xor = idx ^ lenRest;
            lenRest = idx;
            len1 = 0;
            advance1(idx, xor);
        }
    }

    private void advance1(int idx, int xor) {
        if (xor <= 0) {            // level = 6 or something very unexpected happened
            throw new IllegalArgumentException("advance1(" + idx + ", " + xor + "): a1=" + a1 + ", a2=" + a2 + ", a3=" + a3 + ", a4=" + a4 + ", a5=" + a5 + ", a6=" + a6 + ", depth=" + depth);
        } else if (xor < WIDTH2) { // level = 1
            if (depth <= 1) {
                a2 = new2(WIDTH);
                a2[0] = a1;
                depth = 2;
            }
            a1 = new1(WIDTH);
            a2[(idx >>> BITS) & MASK] = a1;
        } else if (xor < WIDTH3) { // level = 2
            if (depth <= 2) {
                a3 = new3(WIDTH);
                a3[0] = a2;
                depth = 3;
            }
            a1 = new1(WIDTH);
            a2 = new2(WIDTH);
            a2[(idx >>> BITS) & MASK] = a1;
            a3[(idx >>> BITS2) & MASK] = a2;
        } else if (xor < WIDTH4) { // level = 3
            if (depth <= 3) {
                a4 = new4(WIDTH);
                a4[0] = a3;
                depth = 4;
            }
            a1 = new1(WIDTH);
            a2 = new2(WIDTH);
            a3 = new3(WIDTH);
            a2[(idx >>> BITS) & MASK] = a1;
            a3[(idx >>> BITS2) & MASK] = a2;
            a4[(idx >>> BITS3) & MASK] = a3;
        } else if (xor < WIDTH5) { // level = 4
            if (depth <= 4) {
                a5 = new5(WIDTH);
                a5[0] = a4;
                depth = 5;
            }
            a1 = new1(WIDTH);
            a2 = new2(WIDTH);
            a3 = new3(WIDTH);
            a4 = new4(WIDTH);
            a2[(idx >>> BITS) & MASK] = a1;
            a3[(idx >>> BITS2) & MASK] = a2;
            a4[(idx >>> BITS3) & MASK] = a3;
            a5[(idx >>> BITS4) & MASK] = a4;
        } else {                   // level = 5
            if (depth <= 5) {
                a6 = new6(LASTWIDTH);
                a6[0] = a5;
                depth = 6;
            }
            a1 = new1(WIDTH);
            a2 = new2(WIDTH);
            a3 = new3(WIDTH);
            a4 = new4(WIDTH);
            a5 = new5(WIDTH);
            a2[(idx >>> BITS) & MASK] = a1;
            a3[(idx >>> BITS2) & MASK] = a2;
            a4[(idx >>> BITS3) & MASK] = a3;
            a5[(idx >>> BITS4) & MASK] = a4;
            a6[idx >>> BITS5] = a5;
        }
    }

    public FingerTree<A> build() {
        if (prefixIsRightAligned) leftAlignPrefix();
        var len = len1 + lenRest;
        var size = len - offset;
        if (size == 0) return FingerTreeAPI.<A>of();
        else if (len < 0) throw new IndexOutOfBoundsException("Vector cannot have negative size $len");
        else if (len <= WIDTH) {
            return new Tree1<>(copyIfDifferentSize(a1, size));
        } else if (len <= WIDTH2) {
            var i1 = (len - 1) & MASK;
            var i2 = (len - 1) >>> BITS;
            var d2 = copyOfRange(a2, 1, i2);
            var p1 = a2[0];
            var s1 = copyIfDifferentSize(a2[i2], i1 + 1);
            return new Tree2<>(size, p1, d2, s1);
        } else if (len <= WIDTH3) {
            var i1 = (len - 1) & MASK;
            var i2 = ((len - 1) >>> BITS) & MASK;
            var i3 = ((len - 1) >>> BITS2);
            var d3 = copyOfRange(a3, 1, i3);
            var p2 = copyTail(a3[0]);
            var p1 = a3[0][0];
            var s2 = copyOf(a3[i3], i2);
            var s1 = copyIfDifferentSize(a3[i3][i2], i1 + 1);
            return new Tree3<>(size, p1, p2, d3, s2, s1);
        } else if (len <= WIDTH4) {
            var i1 = (len - 1) & MASK;
            var i2 = ((len - 1) >>> BITS) & MASK;
            var i3 = ((len - 1) >>> BITS2) & MASK;
            var i4 = ((len - 1) >>> BITS3);
            var d4 = copyOfRange(a4, 1, i4);
            var p3 = copyTail(a4[0]);
            var p2 = copyTail(a4[0][0]);
            var p1 = a4[0][0][0];
            var s3 = copyOf(a4[i4], i3);
            var s2 = copyOf(a4[i4][i3], i2);
            var s1 = copyIfDifferentSize(a4[i4][i3][i2], i1 + 1);
            return new Tree4<>(size, p1, p2, p3, d4, s3, s2, s1);
        } else if (len <= WIDTH5) {
            var i1 = (len - 1) & MASK;
            var i2 = ((len - 1) >>> BITS) & MASK;
            var i3 = ((len - 1) >>> BITS2) & MASK;
            var i4 = ((len - 1) >>> BITS3) & MASK;
            var i5 = ((len - 1) >>> BITS4);
            var d5 = copyOfRange(a5, 1, i5);
            var p4 = copyTail(a5[0]);
            var p3 = copyTail(a5[0][0]);
            var p2 = copyTail(a5[0][0][0]);
            var p1 = a5[0][0][0][0];
            var s4 = copyOf(a5[i5], i4);
            var s3 = copyOf(a5[i5][i4], i3);
            var s2 = copyOf(a5[i5][i4][i3], i2);
            var s1 = copyIfDifferentSize(a5[i5][i4][i3][i2], i1 + 1);
            return new Tree5<>(size, p1, p2, p3, p4, d5, s4, s3, s2, s1);
        } else {
            var i1 = (len - 1) & MASK;
            var i2 = ((len - 1) >>> BITS) & MASK;
            var i3 = ((len - 1) >>> BITS2) & MASK;
            var i4 = ((len - 1) >>> BITS3) & MASK;
            var i5 = ((len - 1) >>> BITS4) & MASK;
            var i6 = ((len - 1) >>> BITS5);
            var d6 = copyOfRange(a6, 1, i6);
            var p5 = copyTail(a6[0]);
            var p4 = copyTail(a6[0][0]);
            var p3 = copyTail(a6[0][0][0]);
            var p2 = copyTail(a6[0][0][0][0]);
            var p1 = a6[0][0][0][0][0];
            var s5 = copyOf(a6[i6], i5);
            var s4 = copyOf(a6[i6][i5], i4);
            var s3 = copyOf(a6[i6][i5][i4], i3);
            var s2 = copyOf(a6[i6][i5][i4][i3], i2);
            var s1 = copyIfDifferentSize(a6[i6][i5][i4][i3][i2], i1 + 1);
            return new Tree6<>(size, p1, p2, p3, p4, p5, d6, s5, s4, s3, s2, s1);
        }

    }
}
