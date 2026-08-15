package org.jhotdraw8.icollection.impl.fingertree;

import org.jspecify.annotations.Nullable;

/// Represents a Fingertree.
///
/// ```
/// <A>           = parameterized element type of Fingertree
///
/// Tree<A>        = Tree0<A> | Tree1<A> | Tree2<A> | ... | Tree6<A>
/// Tree0<A>       = ()
/// Tree1<A>.      = (Arr1)
/// Tree2<A>       = (int,int, Arr1, Arr2, Arr1)
/// Tree3<A>       = (int,int,int, Arr1, Arr2, Arr3, Arr2, Arr1)
/// Tree4<A>       = (int,int,int,int, Arr1, Arr2, Arr3, Arr4, Arr3, Arr2, Arr1)
/// Tree5<A>       = (int,int,int,int,int, Arr1, Arr2, Arr3, Arr4, Arr5, Arr4, Arr3, Arr2, Arr1)
/// Tree6<A>       = (int,int,int,int,int,int, Arr1, Arr2, Arr3, Arr4, Arr5, Arr6, Arr5, Arr4, Arr3, Arr2, Arr1)
///
/// Arr1          = A[]
/// Arr2          = A[][]
/// Arr3          = A[][][]
/// Arr4          = A[][][][]
/// Arr5          = A[][][][][]
/// Arr6          = A[][][][][]
/// ```
///
/// |Tree | min|     max size|                prefix|  data  |suffix                |
/// |-----|---:|------------:|---------------------:|:------:|----------------------|
/// |Tree0|   0|            0|                      |        |                      |
/// |Tree1|   1|           32|                      |   32   |                      |
/// |Tree2|  32|        1,088|                    32|  32^2  |32                    |
/// |Tree3|32^2|       34,880|               32,32^2|  32^3  |32^2,32               |
/// |Tree4|32^3|    1,116,224|          32,32^2,32^3|  32^4  |32^3,32^2,32          |
/// |Tree5|32^4|   35,719,232|     32,32^2,32^3,32^4|  32^5  |32^4,42^3,32^2,32     |
/// |Tree6|32^5|2,216,757,312|32,32^2,32^3,32^4,32^5| 32^5*64|32^5,32^4,32^3,32^2,32|
///
/// A Fingertree is a general-purpose, immutable data structure.  It provides random access and updates
/// in O(log n) time, as well as very fast append/prepend/tail/init (amortized O(1), worst case O(log n)).
///
/// The finger trees are radix-balanced trees of width 32. There is a separate subclass
/// for each level (0 to 6, with 0 being the empty vector and 6 a tree with a maximum width of 64 at the
/// top level).
///
/// Tree balancing:
///  - Only the tree dimension of an array may have a size < WIDTH
///  - In a \`data\` (central) array the tree dimension may be up to WIDTH-2 long, in \`prefix1\` and \`suffix1\` up
///   to WIDTH, and in other \`prefix\` and \`suffix\` arrays up to WIDTH-1
///  - \`prefix1\` and \`suffix1\` are never empty
///  - Balancing does not cross the main data array (i.e. prepending never touches the suffix and appending never touches
///   the prefix). The level is increased/decreased when the affected side plus main data is already full/empty
///  - All arrays are left-aligned and truncated
///  In addition to the data slices (\`prefix1\`, \`prefix2\`, ..., \`dataN\`, ..., \`suffix2\`, \`suffix1\`) we store a running
///  count of elements after each prefix for more efficient indexing without having to dereference all prefix arrays.
///
///
/// This code has been derived from
/// [Scala 3, Vector.scala](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/library/src/scala/collection/immutable/Vector.scala),
/// EPFL and Lightbend, Inc. dba Akka,
/// [Apache License 2.0](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/LICENSE)
///
/// @tparam A the element type of the vector
public sealed interface FingerTree<A> permits Tree0, Tree1, Tree2, Tree3, Tree4, Tree5, Tree6 {
    int BITS = 5;
    int BITS5 = BITS * 5;
    int WIDTH5 = 1 << BITS5;
    int BITS4 = BITS * 4;
    int WIDTH4 = 1 << BITS4;
    int BITS3 = BITS * 3;
    int WIDTH3 = 1 << BITS3;
    int BITS2 = BITS * 2;
    int WIDTH2 = 1 << BITS2;
    int WIDTH = 1 << BITS;
    int LASTWIDTH = WIDTH << 1; // 1 extra bit in the last level to go up to Int.MaxValue (2^31-1) instead of 2^30
    int MASK = WIDTH - 1;

    /// Adds `element` as the tree element and returns the updated fingertree
    FingerTree<A> addFirst(@Nullable A x);

    /// Adds `element` as the last element and returns the updated fingertree
    FingerTree<A> addLast(@Nullable A x);

    /// Gets the element at the specified `offset`
    @Nullable A get(int index);

    @Nullable A getFirst();

    @Nullable A getLast();

    default FingerTreeAPI.Result<A> removeAt(int index) {
        if (index == 0) return removeFirst();
        if (index == size() - 1) return removeLast();
        var removed = get(index);
        var a = slice(0, index);
        var b = slice(index + 1, size());
        var builder = new FingerTreeBuilder<A>();
        builder.addVector(a);
        builder.addVector(b);
        var c = builder.build();
        return new FingerTreeAPI.Result<>(c, removed);
    }

    /// Removes the tree element, and returns the updated fingertree and the removed value
    FingerTreeAPI.Result<A> removeFirst();

    /// Removes the last element, and returns the updated fingertree and the removed value
    FingerTreeAPI.Result<A> removeLast();

    /// Sets the element at the specified `offset` and returns the updated fingertree and the previous value
    FingerTreeAPI.Result<A> set(int index, A x);

    int size();

    /// Returns a fingertree that contains the elements from `from` (inclusive) to `to` (exclusive).
    FingerTree<A> slice(int from, int to);

    /// Gets the slice at offset.
    ///
    /// @param idx the zero-based slice offset
    /// @return the underlying data array (of dimension matching the slice level) for the slice at position \`idx\`
    A[] getSliceAt(int idx);

    /// Number of slices.
    int getSliceCount();
}
