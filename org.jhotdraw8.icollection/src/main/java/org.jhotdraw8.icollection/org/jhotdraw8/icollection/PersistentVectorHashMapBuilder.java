
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jhotdraw8.icollection.impl.champ.SequencedEntry;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeBuilder;

/// This Builder allows to efficiently build a [PersistentVectorHashMap] without
/// generating intermediate editions.
public class PersistentVectorHashMapBuilder<K, V> implements MapBuilder<K, V, PersistentVectorHashMap<K, V>> {
    private final FingerTreeBuilder<Object> vector = new FingerTreeBuilder<>();
    private BitmapIndexedNode<SequencedEntry<K, V>> hashMap = BitmapIndexedNode.emptyNode();
    private IdentityObject owner = new IdentityObject();
    private int size;
    private final int offset;

    public PersistentVectorHashMapBuilder() {
        this(Integer.MIN_VALUE / 4);
    }

    PersistentVectorHashMapBuilder(int offset) {
        this.offset = offset;
    }

    @Override
    public PersistentVectorHashMapBuilder<K, V> add(K key, V value) {
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        var newEntry = new SequencedEntry<>(key, value, size + offset);
        hashMap = hashMap.put(owner, newEntry,
                SequencedEntry.keyHash(key), 0, details,
                SequencedEntry::failIfKeyExists,
                SequencedEntry::keyEquals, SequencedEntry::entryKeyHash);
        vector.addOne(newEntry);
        size++;
        return this;
    }


    @Override
    public PersistentVectorHashMap<K, V> build() {
        owner = new IdentityObject();
        return new PersistentVectorHashMap<>(
                new PrivateData(
                        new PersistentVectorHashMap.OpaqueRecord<>(hashMap,
                                vector.build(), size, offset)));
    }
}
