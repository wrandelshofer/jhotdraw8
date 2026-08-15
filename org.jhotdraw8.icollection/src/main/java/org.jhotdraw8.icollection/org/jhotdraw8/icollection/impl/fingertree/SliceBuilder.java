package org.jhotdraw8.icollection.impl.fingertree;

import org.jspecify.annotations.NonNull;

import static java.util.Arrays.copyOfRange;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.concatArrays;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyOrUse;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty2;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty3;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty4;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty5;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty6;
import static org.jhotdraw8.icollection.impl.fingertree.FingerTree.BITS;
import static org.jhotdraw8.icollection.impl.fingertree.FingerTree.WIDTH;

/// Helper class for finger tree slicing.
///
/// It is initialized with the validated start and end offset.
/// Then the finger tree slices are added in succession with `consider`.
///
/// No matter what the dimension of the originating finger tree is or where the
/// cut is performed, this always results in a structure with the
/// highest-dimensional data in the middle and fingers of decreasing dimension
/// at both ends, which can be turned into a new finger tree with very little
/// rebalancing.
///
/// This code has been derived from
/// [Scala 3, Vector.scala](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/library/src/scala/collection/immutable/Vector.scala),
/// EPFL and Lightbend, Inc. dba Akka,
/// [Apache License 2.0](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/LICENSE)
public class SliceBuilder<A> {
    final int hi;
    private final int lo;
    private final Object[][] slices = new Object[11][];
    private int size, pos, maxDim = 0;

    public SliceBuilder(int lo, int hi) {
        this.lo = lo;
        this.hi = hi;
    }

    private <T> void add(int n, T[] a) {
        if (n <= maxDim) {
            slices[suffixIdx(n)] = a;
        } else {
            maxDim = n;
            slices[prefixIdx(n)] = a;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void addSlice(int n, T[] a, int lo, int hi) {
        if (n == 1) {
            add(1, copyOrUse(a, lo, hi));
            return;
        }
        var bitsN = BITS * (n - 1);
        var widthN = 1 << bitsN;
        var loN = lo >>> bitsN;
        var hiN = hi >>> bitsN;
        var loRest = lo & (widthN - 1);
        var hiRest = hi & (widthN - 1);
        if (loRest == 0) {
            if (hiRest == 0) {
                add(n, copyOrUse(a, loN, hiN));
            } else {
                if (hiN > loN) add(n, copyOrUse(a, loN, hiN));
                addSlice(n - 1, (T[]) a[hiN], 0, hiRest);
            }
            return;
        }
        if (hiN == loN) {
            addSlice(n - 1, (T[]) a[loN], loRest, hiRest);
        } else {
            addSlice(n - 1, (T[]) a[loN], loRest, widthN);
            if (hiRest == 0) {
                if (hiN > loN + 1) add(n, copyOrUse(a, loN + 1, hiN));
            } else {
                if (hiN > loN + 1) add(n, copyOrUse(a, loN + 1, hiN));
                addSlice(n - 1, (T[]) a[hiN], 0, hiRest);
            }
        }
    }

    /// Ensures prefix is not empty.
    ///
    /// @param n the dimension level (1 through \`maxDim\`) at which to ensure the prefix is non-empty
    private void balancePrefix(int n) {
        if (slices[prefixIdx(n)] != null) {
            return;
        }
        if (n == maxDim) {
            slices[prefixIdx(n)] = slices[suffixIdx(n)];
            slices[suffixIdx(n)] = null;
            return;
        }
        balancePrefix(n + 1);
        var preN1 = (A[][]) slices[prefixIdx(n + 1)];
        slices[prefixIdx(n)] = preN1[0];
        if (preN1.length == 1) {
            slices[prefixIdx(n + 1)] = null;
            if ((maxDim == n + 1) && (slices[suffixIdx(n + 1)] == null)) maxDim = n;
        } else {
            slices[prefixIdx(n + 1)] = copyOfRange(preN1, 1, preN1.length);
        }
    }

    /// Ensures suffix is not empty.
    ///
    /// @param n the dimension level (1 through \`maxDim\`) at which to ensure the suffix is non-empty
    private void balanceSuffix(int n) {
        if (slices[suffixIdx(n)] != null) {
            return;
        }
        if (n == maxDim) {
            slices[suffixIdx(n)] = slices[prefixIdx(n)];
            slices[prefixIdx(n)] = null;
            return;
        }
        balanceSuffix(n + 1);
        var sufN1 = (A[][]) slices[suffixIdx(n + 1)];
        slices[suffixIdx(n)] = sufN1[sufN1.length - 1];
        if (sufN1.length == 1) {
            slices[suffixIdx(n + 1)] = null;
            if ((maxDim == n + 1) && (slices[prefixIdx(n + 1)] == null)) maxDim = n;
        } else {
            slices[suffixIdx(n + 1)] = copyOfRange(sufN1, 0, sufN1.length - 1);
        }
    }

    public <T> void consider(int n, T[] a) {
        var count = a.length * (1 << (BITS * (n - 1)));
        var lo0 = Math.max(lo - pos, 0);
        var hi0 = Math.min(hi - pos, count);
        if (hi0 > lo0) {
            addSlice(n, a, lo0, hi0);
            size += (hi0 - lo0);
        }
        pos += count;
    }

    @SuppressWarnings("unchecked")
    private <T> T[] dataOr(int n, T[] a) {
        var p = slices[prefixIdx(n)];
        if (p != null) return (T[]) p;
        else {
            var s = slices[suffixIdx(n)];
            return (s != null) ? (T[]) s : a;
        }
    }

    private int prefixIdx(int n) {
        return n - 1;
    }

    @SuppressWarnings("unchecked")
    private <T> T[] prefixOr(int n, T[] a) {
        var p = slices[prefixIdx(n)];
        return (p != null) ? (T[]) p : a;
    }

    @SuppressWarnings("unchecked")
    public FingerTree<A> result() {
        if (size == 0) return resultEmpty();
        if (size <= 32) return resultSmall();
        return resultBig();
    }

    private @NonNull FingerTree<A> resultBig() {
        balancePrefix(1);
        balanceSuffix(1);
        var resultDim = maxDim;
        if (resultDim < 6) {
            var pre = slices[prefixIdx(maxDim)];
            var suf = slices[suffixIdx(maxDim)];
            if ((pre != null) && (suf != null)) {
                // The highest-dimensional data consists of two slices: concatenate if they fit into the main data array,
                // otherwise increase the dimension
                if (pre.length + suf.length <= WIDTH - 2) {
                    slices[prefixIdx(maxDim)] = concatArrays(pre, suf);
                    slices[suffixIdx(maxDim)] = null;
                } else resultDim += 1;
            } else {
                // A single highest-dimensional slice could have length WIDTH-1 if it came from a prefix or suffix but we
                // only allow WIDTH-2 for the main data, so increase the dimension in this case
                var one = (pre != null) ? pre : suf;
                if (one.length > WIDTH - 2) resultDim += 1;
            }
        }
        A[] p1 = (A[]) slices[prefixIdx(1)];
        A[] s1 = (A[]) slices[suffixIdx(1)];
        return switch (resultDim) {
            case 2 -> new Tree2<A>(size,
                    p1,
                    dataOr(2, empty2()),
                    s1);
            case 3 -> new Tree3<A>(size,
                    p1, prefixOr(2, empty2()),
                    dataOr(3, empty3()),
                    suffixOr(2, empty2()), s1);
            case 4 -> new Tree4<A>(size,
                    p1, prefixOr(2, empty2()), prefixOr(3, empty3()),
                    dataOr(4, empty4()),
                    suffixOr(3, empty3()), suffixOr(2, empty2()), s1);
            case 5 -> new Tree5<A>(size,
                    p1, prefixOr(2, empty2()), prefixOr(3, empty3()), prefixOr(4, empty4()),
                    dataOr(5, empty5()),
                    suffixOr(4, empty4()), suffixOr(3, empty3()), suffixOr(2, empty2()), s1);
            case 6 -> new Tree6<A>(size,
                    p1, prefixOr(2, empty2()), prefixOr(3, empty3()), prefixOr(4, empty4()), prefixOr(5, empty5()), dataOr(6, empty6()),
                    suffixOr(5, empty5()),
                    suffixOr(4, empty4()), suffixOr(3, empty3()), suffixOr(2, empty2()), s1);
            default -> throw new IllegalStateException("Unexpected value: " + resultDim);
        };
    }

    private FingerTree<A> resultEmpty() {
        return FingerTreeAPI.of();
    }

    private FingerTree<A> resultSmall() {
        A[] prefix1 = (A[]) slices[prefixIdx(1)];
        A[] suffix1 = (A[]) slices[suffixIdx(1)];
        A[] a;
        if (prefix1 != null) {
            if (suffix1 != null) a = concatArrays(prefix1, suffix1);
            else a = (A[]) prefix1;
        } else if (suffix1 != null) a = (A[]) suffix1;
        else {
            A[][] prefix2 = (A[][]) slices[prefixIdx(2)];
            if (prefix2 != null) a = prefix2[0];
            else {
                A[][] suffix2 = (A[][]) slices[suffixIdx(2)];
                a = suffix2[0];
            }
        }
        return new Tree1<>(a);
    }

    private int suffixIdx(int n) {
        return 11 - n;
    }

    @SuppressWarnings("unchecked")
    private <T> T[] suffixOr(int n, T[] a) {
        var s = slices[suffixIdx(n)];
        return (s != null) ? (T[]) s : a;
    }
}