
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.champmap.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champmap.ChangeEvent;

/// This Builder allows to efficiently build a [PersistentHashMap] without
/// generating intermediate editions.
public class PersistentHashMapBuilder<K, V> implements MapBuilder<K, V, PersistentHashMap<K, V>> {
    private BitmapIndexedNode<K, V> hashMap = BitmapIndexedNode.emptyNode();
    private IdentityObject owner = new IdentityObject();
    private int size;

    public PersistentHashMapBuilder() {
    }

    @Override
    public PersistentHashMapBuilder<K, V> add(K key, V value) {
        var details = new ChangeEvent<V>();
        var newMap = hashMap.put(owner, key, value,
                PersistentHashMap.keyHash(key), 0, details,
                PersistentHashMap::keyHash);
        hashMap = newMap;
        size++;
        return this;
    }


    @Override
    public PersistentHashMap<K, V> build() {
        owner = new IdentityObject();
        return new PersistentHashMap<>(hashMap, size);
    }
}
