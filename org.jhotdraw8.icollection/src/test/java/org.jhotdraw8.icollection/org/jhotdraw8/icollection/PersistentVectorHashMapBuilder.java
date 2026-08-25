
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.alt.impl.champset.BitmapIndexedNode;
import org.jhotdraw8.icollection.alt.impl.champset.ChangeEvent;
import org.jhotdraw8.icollection.alt.impl.champset.SequencedEntry;
import org.jhotdraw8.icollection.impl.MutabilityOwnership;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeBuilder;

/// This Builder allows to efficiently build a [PersistentVectorHashMap] without
/// generating intermediate editions.
public class PersistentVectorHashMapBuilder<K, V> implements MapBuilder<K, V, PersistentVectorHashMap<K, V>> {
    private final FingerTreeBuilder<Object> vector = new FingerTreeBuilder<>();
    private BitmapIndexedNode<SequencedEntry<K, V>> hashMap = BitmapIndexedNode.emptyNode();
    private MutabilityOwnership owner;
    private int size;
    private final int offset;

    public PersistentVectorHashMapBuilder() {
        this(new MutabilityOwnership(), Integer.MIN_VALUE / 4);
    }

    PersistentVectorHashMapBuilder(MutabilityOwnership owner, int offset) {
        this.offset = offset;
        this.owner = owner;
    }

    @Override
    public PersistentVectorHashMapBuilder<K, V> put(K key, V value) {
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        var newEntry = new SequencedEntry<>(key, value, size + offset);
        var newHashMap = hashMap.put(owner, newEntry,
                SequencedEntry.keyHash(key), 0, details,
                SequencedEntry::failIfKeyExists,
                SequencedEntry::keyEquals, SequencedEntry::entryKeyHash);
        if (details.isModified()) {
            hashMap = newHashMap;
            vector.addOne(newEntry);
            size++;
        }
        return this;
    }


    @Override
    public PersistentVectorHashMap<K, V> build() {
        owner = new MutabilityOwnership();
        return new PersistentVectorHashMap<>(hashMap, vector.build(), size, offset);
    }
}
