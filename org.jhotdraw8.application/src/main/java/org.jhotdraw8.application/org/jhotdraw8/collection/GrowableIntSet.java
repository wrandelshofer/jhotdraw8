package org.jhotdraw8.collection;

/**
 * Extension of {@link DenseIntSet} that can be used, when
 * the size of the set is not known.
 */
public class GrowableIntSet extends DenseIntSet {
    @Override
    public boolean addAsInt(int e) {
        ensureCapacity(e);
        return super.addAsInt(e);
    }

    @Override
    public boolean removeAsInt(int e) {
        ensureCapacity(e);
        return super.removeAsInt(e);
    }

    @Override
    public boolean containsAsInt(int index) {
        ensureCapacity(index);
        return super.containsAsInt(index);
    }

    private void ensureCapacity(int index) {
        if (capacity() < index) {
            setCapacity(Integer.highestOneBit(index + index - 1));
        }
    }


}
