package org.jhotdraw8.collection;

/**
 * Extension of {@link IndexedBooleanSet} that can be used, when
 * the size of the set is not known.
 */
public class GrowableIndexedBooleanSet extends IndexedBooleanSet {
    @Override
    public boolean add(int index) {
        ensureCapacity(index);
        return super.add(index);
    }

    @Override
    public boolean remove(int index) {
        ensureCapacity(index);
        return super.remove(index);
    }

    @Override
    public boolean get(int index) {
        ensureCapacity(index);
        return super.get(index);
    }

    private void ensureCapacity(int index) {
        if (size() < index) {
            setSize(Integer.highestOneBit(index + index - 1));
        }
    }


}
