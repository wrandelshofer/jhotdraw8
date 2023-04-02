package org.jhotdraw8.collection.primitive;

import org.jhotdraw8.base.function.ByteConsumer;

import java.util.Comparator;
import java.util.Spliterator;

public class ByteArraySpliterator implements SpliteratorOfByte {
    private final byte[] array;
    private int index;        // current index, modified on advance/split
    private final int fence;  // one past last index
    private final int characteristics;

    /**
     * Creates a spliterator covering all of the given array.
     *
     * @param array                     the array, assumed to be unmodified during use
     * @param additionalCharacteristics Additional spliterator characteristics
     *                                  of this spliterator's source or elements beyond {@code SIZED} and
     *                                  {@code SUBSIZED} which are always reported
     */
    public ByteArraySpliterator(byte[] array, int additionalCharacteristics) {
        this(array, 0, array.length, additionalCharacteristics);
    }

    /**
     * Creates a spliterator covering the given array and range
     *
     * @param array                     the array, assumed to be unmodified during use
     * @param origin                    the least index (inclusive) to cover
     * @param fence                     one past the greatest index to cover
     * @param additionalCharacteristics Additional spliterator characteristics
     *                                  of this spliterator's source or elements beyond {@code SIZED} and
     *                                  {@code SUBSIZED} which are always reported
     */
    public ByteArraySpliterator(byte[] array, int origin, int fence, int additionalCharacteristics) {
        this.array = array;
        this.index = origin;
        this.fence = fence;
        this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
    }

    @Override
    public SpliteratorOfByte trySplit() {
        int lo = index, mid = (lo + fence) >>> 1;
        return (lo >= mid)
                ? null
                : new ByteArraySpliterator(array, lo, index = mid, characteristics);
    }

    @Override
    public void forEachRemaining(ByteConsumer action) {
        byte[] a;
        int i, hi; // hoist accesses and checks from loop
        if (action == null)
            throw new NullPointerException();
        if ((a = array).length >= (hi = fence) &&
                (i = index) >= 0 && i < (index = hi)) {
            do {
                action.accept(a[i]);
            } while (++i < hi);
        }
    }

    @Override
    public boolean tryAdvance(ByteConsumer action) {
        if (action == null)
            throw new NullPointerException();
        if (index >= 0 && index < fence) {
            action.accept(array[index++]);
            return true;
        }
        return false;
    }

    @Override
    public long estimateSize() {
        return fence - index;
    }

    @Override
    public int characteristics() {
        return characteristics;
    }

    @Override
    public Comparator<? super Byte> getComparator() {
        if (hasCharacteristics(Spliterator.SORTED))
            return null;
        throw new IllegalStateException();
    }
}
