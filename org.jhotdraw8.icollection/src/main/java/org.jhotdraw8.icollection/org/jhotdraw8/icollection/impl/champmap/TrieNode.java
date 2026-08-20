package org.jhotdraw8.icollection.impl.champmap;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;
import org.jhotdraw8.icollection.impl.champset.ForEachOneBit;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;

/// This code has been derived from
/// [kotlix.collections.immutable, TrieNode.kt](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/core/commonMain/src/implementations/immutableMap/TrieNode.kt),
/// JetBrains s.r.o.
/// [Apache License 2.0](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/LICENSE.txt)

public class TrieNode<K, V> {
    int dataMap;
    int nodeMap;
    public Object[] buffer;
    private @Nullable MutabilityOwnership ownedBy;
    static final int MAX_BRANCHING_FACTOR = 32;
    public static final int LOG_MAX_BRANCHING_FACTOR = 5;
    private static final int MAX_BRANCHING_FACTOR_MINUS_ONE = MAX_BRANCHING_FACTOR - 1;
    private static final int MAX_SHIFT = 30;
    public static final int ENTRY_SIZE = 2;
    public static TrieNode<Object, Object> EMPTY = new TrieNode<Object, Object>(0, 0, new Object[0], null);

    public static <T, U> TrieNode<T, U> empty() {
        //noinspection unchecked
        return (TrieNode<T, U>) EMPTY;
    }

    TrieNode(int dataMap, int nodeMap, Object[] buffer) {
        this(dataMap, nodeMap, buffer, null);
    }

    TrieNode(int dataMap, int nodeMap, Object[] buffer, @Nullable MutabilityOwnership ownedBy) {
        this.dataMap = dataMap;
        this.nodeMap = nodeMap;
        this.buffer = buffer;
        this.ownedBy = ownedBy;
    }

    private static <K, V> Object[] insertEntryAtIndex(Object[] array, int keyIndex, K key, V value) {
        var newBuffer = new Object[array.length + ENTRY_SIZE];
        System.arraycopy(array, 0, newBuffer, 0, keyIndex);
        System.arraycopy(array, keyIndex, newBuffer, keyIndex + ENTRY_SIZE, array.length - keyIndex);
        newBuffer[keyIndex] = key;
        newBuffer[keyIndex + 1] = value;
        return newBuffer;
    }

    private static Object[] replaceEntryWithNode(Object[] array, int keyIndex, int nodeIndex, TrieNode<?, ?> newNode) {
        var newNodeIndex = nodeIndex - ENTRY_SIZE; // place where to insert new node in the new buffer
        var newBuffer = new Object[array.length - ENTRY_SIZE + 1];
        System.arraycopy(array, 0, newBuffer, 0, keyIndex);
        System.arraycopy(array, keyIndex + ENTRY_SIZE, newBuffer, keyIndex, nodeIndex - keyIndex - ENTRY_SIZE);
        newBuffer[newNodeIndex] = newNode;
        System.arraycopy(array, nodeIndex, newBuffer, newNodeIndex + 1, array.length - nodeIndex);
        return newBuffer;
    }

    private static <K, V> Object[] replaceNodeWithEntry(Object[] array, int nodeIndex, int keyIndex, K key, V value) {
        var newBuffer = Arrays.copyOf(array, array.length + 1);
        System.arraycopy(newBuffer, nodeIndex + 1, newBuffer, nodeIndex + 2, array.length - nodeIndex - 1);
        System.arraycopy(newBuffer, keyIndex, newBuffer, keyIndex + 2, nodeIndex - keyIndex);
        newBuffer[keyIndex] = key;
        newBuffer[keyIndex + 1] = value;
        return newBuffer;
    }

    private static Object[] removeEntryAtIndex(Object[] array, int keyIndex) {
        var newBuffer = new Object[array.length - ENTRY_SIZE];
        System.arraycopy(array, 0, newBuffer, 0, keyIndex);
        System.arraycopy(array, keyIndex + ENTRY_SIZE, newBuffer, keyIndex, array.length - keyIndex - ENTRY_SIZE);
        return newBuffer;
    }

    private static Object[] removeNodeAtIndex(Object[] array, int nodeIndex) {
        var newBuffer = new Object[array.length - 1];
        System.arraycopy(array, 0, newBuffer, 0, nodeIndex);
        System.arraycopy(array, nodeIndex + 1, newBuffer, nodeIndex, array.length - nodeIndex - 1);
        return newBuffer;
    }

    private ModificationResult<K, V> asInsertResult() {
        return new ModificationResult<>(this, 1, null);
    }

    private ModificationResult<K, V> asUpdateResult(@Nullable V oldValue) {
        return new ModificationResult<>(this, 0, oldValue);
    }

    /**
     * Returns number of entries stored in this trie node (not counting subnodes)
     */
    public int entryCount() {
        return Integer.bitCount(dataMap);
    }

    /**
     * Returns number of entries, works regardless if node is a collision node
     * or a regular node.
     *
     * @return entry count
     */
    public int collisionEntryCount() {
        return isCollisionNode() ? buffer.length / ENTRY_SIZE : Integer.bitCount(dataMap);
    }

    public boolean isEmpty() {
        return buffer.length == 0;
    }

    public int nodeCount() {
        return Integer.bitCount(nodeMap);
    }

    /**
     * Returns `true` if this node contains exactly one entry and no subtrie nodes,
     * meaning the parent should replace the node with the entry it contains.
     */
    private boolean hasSingleEntry() {
        return buffer.length == ENTRY_SIZE && nodeMap == 0;
    }

    // here and later:
    // positionMask — an int in form 2^n, i.e. having the single bit set, whose ordinal is a logical position in buffer


    /**
     * Returns true if the data bit map has the bit specified by [positionMask] set, indicating there's a data entry in the buffer at that position.
     */
    private boolean hasEntryAt(int positionMask) {
        return (dataMap & positionMask) != 0;
    }

    /**
     * Returns true if the node bit map has the bit specified by [positionMask] set, indicating there's a subtrie node in the buffer at that position.
     */
    private boolean hasNodeAt(int positionMask) {
        return (nodeMap & positionMask) != 0;
    }

    /**
     * Gets the index in buffer of the data entry key corresponding to the position specified by [positionMask].
     */
    private int entryKeyIndex(int positionMask) {
        return ENTRY_SIZE * Integer.bitCount(dataMap & (positionMask - 1));
    }

    public boolean isCollisionNode() {
        return buffer.length > 0 && dataMap == 0 && nodeMap == 0;
    }

    /**
     * Gets the index in buffer of the subtrie node entry corresponding to the position specified by [positionMask].
     */
    private int nodeIndex(int positionMask) {
        return buffer.length - 1 - Integer.bitCount(nodeMap & (positionMask - 1));
    }

    /**
     * Retrieves the buffer element at the given [keyIndex] as key of a data entry.
     */
    @SuppressWarnings("unchecked")
    private K keyAtIndex(int keyIndex) {
        return (K) buffer[keyIndex];
    }

    /**
     * Retrieves the buffer element next to the given [keyIndex] as value of a data entry.
     */
    @SuppressWarnings("unchecked")
    private V valueAtKeyIndex(int keyIndex) {
        return (V) buffer[keyIndex + 1];
    }

    /**
     * Retrieves the buffer element at the given [nodeIndex] as subtrie node.
     */
    @SuppressWarnings("unchecked")
    private TrieNode<K, V> nodeAtIndex(int nodeIndex) {
        return (TrieNode<K, V>) buffer[nodeIndex];
    }

    private TrieNode<K, V> insertEntryAt(int positionMask, K key, V value) {
        assert !hasEntryAt(positionMask);

        var keyIndex = entryKeyIndex(positionMask);
        var newBuffer = insertEntryAtIndex(buffer, keyIndex, key, value);
        return new TrieNode<>(dataMap | positionMask, nodeMap, newBuffer);
    }

    private TrieNode<K, V> mutableInsertEntryAt(int positionMask, K key, V value, MutabilityOwnership owner) {
        assert !hasEntryAt(positionMask);

        var keyIndex = entryKeyIndex(positionMask);
        if (ownedBy == owner) {
            buffer = insertEntryAtIndex(buffer, keyIndex, key, value);
            dataMap = dataMap | positionMask;
            return this;
        }
        var newBuffer = insertEntryAtIndex(buffer, keyIndex, key, value);
        return new TrieNode<>(dataMap | positionMask, nodeMap, newBuffer, owner);
    }

    private TrieNode<K, V> updateValueAtIndex(int keyIndex, V value) {
        assert buffer[keyIndex + 1] != value;
        var newBuffer = buffer.clone();
        newBuffer[keyIndex + 1] = value;
        return new TrieNode<>(dataMap, nodeMap, newBuffer);
    }

    private TrieNode<K, V> mutableUpdateValueAtIndex(
            int keyIndex,
            V value,
            TrieBuilder<K, V> mutator
    ) {
        assert buffer[keyIndex + 1] != value;

        // If the [mutator] is exclusive owner of this node, update value at specified index in-place.
        if (ownedBy == mutator.ownership) {
            buffer[keyIndex + 1] = value;
            return this;
        }
        // Structural change due to node replacement.
        mutator.modCount++;
        // Create new node with updated value at specified index.
        var newBuffer = buffer.clone();
        newBuffer[keyIndex + 1] = value;
        return new TrieNode<>(dataMap, nodeMap, newBuffer, mutator.ownership);
    }

    /**
     * The given [newNode] must not be a part of any persistent map instance.
     */
    private TrieNode<K, V> updateNodeAtIndex(
            int nodeIndex,
            int positionMask,
            TrieNode<K, V> newNode,
            @Nullable MutabilityOwnership owner
    ) {
        if (newNode.hasSingleEntry()) {
            if (buffer.length == 1) {
                assert dataMap == 0 && (nodeMap ^ positionMask) == 0;
                newNode.dataMap = nodeMap;
                return newNode;
            }

            var keyIndex = entryKeyIndex(positionMask);
            var newBuffer = replaceNodeWithEntry(buffer, nodeIndex, keyIndex, newNode.buffer[0], newNode.buffer[1]);
            return new TrieNode<>(dataMap ^ positionMask, nodeMap ^ positionMask, newBuffer, owner);
        }

        if (owner != null && ownedBy == owner) {
            buffer[nodeIndex] = newNode;
            return this;
        }

        var newBuffer = buffer.clone();
        newBuffer[nodeIndex] = newNode;
        return new TrieNode<>(dataMap, nodeMap, newBuffer, owner);
    }

    private TrieNode<K, V> removeNodeAtIndex(int nodeIndex, int positionMask) {
        assert hasNodeAt(positionMask);
        if (buffer.length == 1) return empty();

        var newBuffer = removeNodeAtIndex(buffer, nodeIndex);
        return new TrieNode<>(dataMap, nodeMap ^ positionMask, newBuffer);
    }

    ///
    /// @param nodeIndex    node index
    /// @param positionMask position mask
    /// @param owner        owner
    /// @return updated node, the node can be empty!
    private TrieNode<K, V> mutableRemoveNodeAtIndex(
            int nodeIndex,
            int positionMask,
            MutabilityOwnership owner
    ) {
        assert hasNodeAt(positionMask);
        if (buffer.length == 1) return empty();

        if (ownedBy == owner) {
            buffer = removeNodeAtIndex(buffer, nodeIndex);
            nodeMap = nodeMap ^ positionMask;
            return this;
        }
        var newBuffer = removeNodeAtIndex(buffer, nodeIndex);
        return new TrieNode<>(dataMap, nodeMap ^ positionMask, newBuffer, owner);
    }


    private Object[] bufferMoveEntryToNode(
            int keyIndex,
            int positionMask,
            int newKeyHash,
            K newKey,
            V newValue,
            int shift,
            @Nullable MutabilityOwnership owner
    ) {
        var storedKey = keyAtIndex(keyIndex);
        var storedKeyHash = storedKey.hashCode();
        var storedValue = valueAtKeyIndex(keyIndex);
        var newNode = makeNode(
                storedKeyHash, storedKey, storedValue,
                newKeyHash, newKey, newValue, shift + LOG_MAX_BRANCHING_FACTOR, owner
        );

        var nodeIndex = nodeIndex(positionMask) + 1; // place where to insert new node in the current buffer

        return replaceEntryWithNode(buffer, keyIndex, nodeIndex, newNode);
    }


    private TrieNode<K, V> moveEntryToNode(
            int keyIndex,
            int positionMask,
            int newKeyHash,
            K newKey,
            V newValue,
            int shift
    ) {
        assert hasEntryAt(positionMask);
        assert !hasNodeAt(positionMask);

        var newBuffer = bufferMoveEntryToNode(keyIndex, positionMask, newKeyHash, newKey, newValue, shift, null);
        return new TrieNode<>(dataMap ^ positionMask, nodeMap | positionMask, newBuffer);
    }

    private TrieNode<K, V> mutableMoveEntryToNode(
            int keyIndex,
            int positionMask,
            int newKeyHash,
            K newKey,
            V newValue,
            int shift,
            MutabilityOwnership owner
    ) {
        assert hasEntryAt(positionMask);
        assert !hasNodeAt(positionMask);

        if (ownedBy == owner) {
            buffer = bufferMoveEntryToNode(keyIndex, positionMask, newKeyHash, newKey, newValue, shift, owner);
            dataMap = dataMap ^ positionMask;
            nodeMap = nodeMap | positionMask;
            return this;
        }
        var newBuffer = bufferMoveEntryToNode(keyIndex, positionMask, newKeyHash, newKey, newValue, shift, owner);
        return new TrieNode<>(dataMap ^ positionMask, nodeMap | positionMask, newBuffer, owner);
    }

    int indexSegment(int index, int shift) {
        return (index >> shift) & MAX_BRANCHING_FACTOR_MINUS_ONE;
    }

    /**
     * Creates a new TrieNode for holding two given key value entries
     */
    private TrieNode<K, V> makeNode(
            int keyHash1,
            K key1,
            V value1,
            int keyHash2,
            K key2,
            V value2,
            int shift,
            @Nullable MutabilityOwnership owner
    ) {
        if (shift > MAX_SHIFT) {
            assert key1 != key2;
            // when two key hashes are entirely equal: the last level subtrie node stores them just as unordered list
            return new TrieNode<>(0, 0, new Object[]{key1, value1, key2, value2}, owner);
        }

        var setBit1 = indexSegment(keyHash1, shift);
        var setBit2 = indexSegment(keyHash2, shift);

        if (setBit1 != setBit2) {
            var nodeBuffer = (setBit1 < setBit2) ?
                    new Object[]{key1, value1, key2, value2}
                    :
                    new Object[]{key2, value2, key1, value1};
            return new TrieNode<>((1 << setBit1) | (1 << setBit2), 0, nodeBuffer, owner);
        }
        // hash segments at the given shift are equal: move these entries into the subtrie
        var node = makeNode(keyHash1, key1, value1, keyHash2, key2, value2, shift + LOG_MAX_BRANCHING_FACTOR, owner);
        return new TrieNode<>(0, 1 << setBit1, new Object[]{node}, owner);
    }

    private TrieNode<K, V> removeEntryAtIndex(int keyIndex, int positionMask) {
        assert hasEntryAt(positionMask);
        if (buffer.length == ENTRY_SIZE) return empty();
        var newBuffer = removeEntryAtIndex(buffer, keyIndex);
        return new TrieNode<>(dataMap ^ positionMask, nodeMap, newBuffer);
    }

    private TrieNode<K, V> mutableRemoveEntryAtIndex(
            int keyIndex,
            int positionMask,
            TrieBuilder<K, V> mutator
    ) {
        assert hasEntryAt(positionMask);
        mutator.size--;
        mutator.operationResult = valueAtKeyIndex(keyIndex);
        if (buffer.length == ENTRY_SIZE) return empty();

        if (ownedBy == mutator.ownership) {
            buffer = removeEntryAtIndex(buffer, keyIndex);
            dataMap = dataMap ^ positionMask;
            return this;
        }
        var newBuffer = removeEntryAtIndex(buffer, keyIndex);
        return new TrieNode<>(dataMap ^ positionMask, nodeMap, newBuffer, mutator.ownership);
    }

    private TrieNode<K, V> collisionRemoveEntryAtIndex(int i) {
        if (buffer.length == ENTRY_SIZE) return empty();
        var newBuffer = removeEntryAtIndex(buffer, i);
        return new TrieNode<>(0, 0, newBuffer);
    }

    private TrieNode<K, V> mutableCollisionRemoveEntryAtIndex(int i, TrieBuilder<K, V> mutator) {
        mutator.size--;
        mutator.operationResult = valueAtKeyIndex(i);
        if (buffer.length == ENTRY_SIZE) return empty();

        if (ownedBy == mutator.ownership) {
            buffer = removeEntryAtIndex(buffer, i);
            return this;
        }
        var newBuffer = removeEntryAtIndex(buffer, i);
        return new TrieNode<>(0, 0, newBuffer, mutator.ownership);
    }

    private int collisionKeyIndex(Object key) {
        for (int i = 0; i < buffer.length; i += ENTRY_SIZE) {
            if (Objects.equals(key, keyAtIndex(i))) return i;
        }
        return -1;
    }

    private boolean collisionContainsKey(K key) {
        return collisionKeyIndex(key) != -1;
    }

    private @Nullable V collisionGet(K key) {
        var keyIndex = collisionKeyIndex(key);
        return (keyIndex != -1) ? valueAtKeyIndex(keyIndex) : null;
    }

    private @Nullable ModificationResult<K, V> collisionPut(K key, V value) {
        var keyIndex = collisionKeyIndex(key);
        if (keyIndex != -1) {
            V oldValue = valueAtKeyIndex(keyIndex);
            if (Objects.equals(value, oldValue)) {
                return null;
            }
            var newBuffer = buffer.clone();
            newBuffer[keyIndex + 1] = value;
            return new TrieNode<K, V>(0, 0, newBuffer).asUpdateResult(oldValue);
        }
        var newBuffer = insertEntryAtIndex(buffer, 0, key, value);
        return new TrieNode<K, V>(0, 0, newBuffer).asInsertResult();
    }

    private TrieNode<K, V> mutableCollisionPut(K key, V value, TrieBuilder<K, V> mutator) {
        // Check if there is an entry with the specified key.
        var keyIndex = collisionKeyIndex(key);
        if (keyIndex != -1) { // found entry with the specified key
            V oldValue = valueAtKeyIndex(keyIndex);
            if (Objects.equals(value, oldValue)) {
                return this;
            }
            mutator.operationResult = oldValue;
            mutator.modCount++;

            // If the [mutator] is exclusive owner of this node, update value of the entry in-place.
            if (ownedBy == mutator.ownership) {
                buffer[keyIndex + 1] = value;
                return this;
            }

            // Create new node with updated entry value.
            var newBuffer = buffer.clone();
            newBuffer[keyIndex + 1] = value;
            return new TrieNode<>(0, 0, newBuffer, mutator.ownership);
        }
        // Create new collision node with the specified entry added to it.
        mutator.size++;
        var newBuffer = insertEntryAtIndex(buffer, 0, key, value);
        return new TrieNode<>(0, 0, newBuffer, mutator.ownership);
    }

    private TrieNode<K, V> collisionRemove(K key) {
        var keyIndex = collisionKeyIndex(key);
        if (keyIndex != -1) {
            return collisionRemoveEntryAtIndex(keyIndex);
        }
        return this;
    }

    private TrieNode<K, V> mutableCollisionRemove(K key, TrieBuilder<K, V> mutator) {
        var keyIndex = collisionKeyIndex(key);
        if (keyIndex != -1) {
            return mutableCollisionRemoveEntryAtIndex(keyIndex, mutator);
        }
        return this;
    }

    private TrieNode<K, V> collisionRemove(K key, V value) {
        var keyIndex = collisionKeyIndex(key);
        if (keyIndex != -1 && Objects.equals(value, valueAtKeyIndex(keyIndex))) {
            return collisionRemoveEntryAtIndex(keyIndex);
        }
        return this;
    }

    private TrieNode<K, V> mutableCollisionRemove(K key, V value, TrieBuilder<K, V> mutator) {
        var keyIndex = collisionKeyIndex(key);
        if (keyIndex != -1 && Objects.equals(value, valueAtKeyIndex(keyIndex))) {
            return mutableCollisionRemoveEntryAtIndex(keyIndex, mutator);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    private TrieNode<K, V> mutableCollisionPutAll(
            TrieNode<K, V> otherNode,
            DeltaCounter intersectionCounter,
            MutabilityOwnership owner
    ) {
        assert nodeMap == 0;
        assert dataMap == 0;
        assert otherNode.nodeMap == 0;
        assert otherNode.dataMap == 0;
        var tempBuffer = Arrays.copyOf(this.buffer, this.buffer.length + otherNode.buffer.length);
        var i = this.buffer.length;
        for (int j = 0; j < otherNode.buffer.length; j += ENTRY_SIZE) {

            if (!this.collisionContainsKey((K) otherNode.buffer[j])) {
                tempBuffer[i] = otherNode.buffer[j];
                tempBuffer[i + 1] = otherNode.buffer[j + 1];
                i += ENTRY_SIZE;
            } else intersectionCounter.count++;
        }
        int newSize = i;
        if (newSize == this.buffer.length) return this;
        if (newSize == otherNode.buffer.length) return otherNode;
        if (newSize == tempBuffer.length) return new TrieNode<>(0, 0, tempBuffer, owner);
        return new TrieNode<>(0, 0, Arrays.copyOf(tempBuffer, newSize), owner);
    }

    /**
     * Updates the cell of this node at [positionMask] with entries from the cell of [otherNode] at [positionMask].
     */
    private TrieNode<K, V> mutablePutAllFromOtherNodeCell(
            TrieNode<K, V> otherNode,
            int positionMask,
            int shift,
            DeltaCounter intersectionCounter,
            TrieBuilder<K, V> mutator
    ) {
        if (this.hasNodeAt(positionMask)) {
            var targetNode = this.nodeAtIndex(nodeIndex(positionMask));
            if (otherNode.hasNodeAt(positionMask)) {
                var otherTargetNode = otherNode.nodeAtIndex(otherNode.nodeIndex(positionMask));
                return targetNode.mutablePutAll(
                        otherTargetNode,
                        shift + LOG_MAX_BRANCHING_FACTOR,
                        intersectionCounter,
                        mutator
                );
            } else if (otherNode.hasEntryAt(positionMask)) {
                var keyIndex = otherNode.entryKeyIndex(positionMask);
                var key = otherNode.keyAtIndex(keyIndex);
                var value = otherNode.valueAtKeyIndex(keyIndex);
                var oldSize = mutator.size;
                var result = targetNode.mutablePut(key.hashCode(), key, value, shift + LOG_MAX_BRANCHING_FACTOR, mutator);
                if (mutator.size == oldSize) intersectionCounter.count++;
                return result;
            } else {
                return targetNode;
            }

        } else if (otherNode.hasNodeAt(positionMask)) {
            var otherTargetNode = otherNode.nodeAtIndex(otherNode.nodeIndex(positionMask));
            if (this.hasEntryAt(positionMask)) {
                // if otherTargetNode already has a value associated with the key, do not put this entry
                var keyIndex = this.entryKeyIndex(positionMask);
                var key = this.keyAtIndex(keyIndex);
                if (otherTargetNode.containsKey(key.hashCode(), key, shift + LOG_MAX_BRANCHING_FACTOR)) {
                    intersectionCounter.count++;
                    return otherTargetNode;
                } else {
                    var value = this.valueAtKeyIndex(keyIndex);
                    return otherTargetNode.mutablePut(
                            key.hashCode(), key, value,
                            shift + LOG_MAX_BRANCHING_FACTOR, mutator
                    );
                }
            } else return otherTargetNode;
        } else { // two entries, and they are not equal by key. See (**) in mutablePutAll
            var thisKeyIndex = this.entryKeyIndex(positionMask);
            var thisKey = this.keyAtIndex(thisKeyIndex);
            var thisValue = this.valueAtKeyIndex(thisKeyIndex);
            var otherKeyIndex = otherNode.entryKeyIndex(positionMask);
            var otherKey = otherNode.keyAtIndex(otherKeyIndex);
            var otherValue = otherNode.valueAtKeyIndex(otherKeyIndex);
            return makeNode(
                    thisKey.hashCode(),
                    thisKey,
                    thisValue,
                    otherKey.hashCode(),
                    otherKey,
                    otherValue,
                    shift + LOG_MAX_BRANCHING_FACTOR,
                    mutator.ownership
            );
        }
    }

    private int calculateSize() {
        if (nodeMap == 0) return buffer.length / ENTRY_SIZE;
        var numValues = Integer.bitCount(dataMap);
        var result = numValues;
        for (int i = (numValues * ENTRY_SIZE); i < buffer.length; i++) {
            result += nodeAtIndex(i).calculateSize();
        }
        return result;
    }

    private boolean elementsIdentityEquals(TrieNode<K, V> otherNode) {
        if (this == otherNode) return true;
        if (nodeMap != otherNode.nodeMap) return false;
        if (dataMap != otherNode.dataMap) return false;
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] != otherNode.buffer[i]) return false;
        }
        return true;
    }

    public boolean containsKey(int keyHash, @Nullable K key, int shift) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            return Objects.equals(key, keyAtIndex(entryKeyIndex(keyPositionMask)));
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var targetNode = nodeAtIndex(nodeIndex(keyPositionMask));
            if (shift == MAX_SHIFT) {
                return targetNode.collisionContainsKey(key);
            }
            return targetNode.containsKey(keyHash, key, shift + LOG_MAX_BRANCHING_FACTOR);
        }

        // key is absent
        return false;
    }

    public static final Object NO_DATA = new Object();

    @SuppressWarnings("unchecked")
    public V noDataValue() {
        return (V) NO_DATA;
    }

    /// Returns the default value if the key is not present
    public @Nullable V getOrDefault(int keyHash, K key, int shift, @Nullable V defaultValue) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask);

            if (Objects.equals(key, keyAtIndex(keyIndex))) {
                return valueAtKeyIndex(keyIndex);
            }
            return defaultValue;
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var targetNode = nodeAtIndex(nodeIndex(keyPositionMask));
            if (shift == MAX_SHIFT) {
                return targetNode.collisionGet(key);
            }
            return targetNode.getOrDefault(keyHash, key, shift + LOG_MAX_BRANCHING_FACTOR, defaultValue);
        }

        // key is absent
        return defaultValue;
    }

    // int newSize = mySet.size + otherSet.size - deltaCounter.count
    public TrieNode<K, V> mutablePutAll(
            TrieNode<K, V> otherNode,
            int shift,
            DeltaCounter intersectionCounter,
            TrieBuilder<K, V> mutator
    ) {
        if (this == otherNode) {
            intersectionCounter.count += calculateSize();
            return this;
        }
        // the collision case
        if (shift > MAX_SHIFT) {
            return mutableCollisionPutAll(otherNode, intersectionCounter, mutator.ownership);
        }

        // new nodes are where either of the old ones were
        var newNodeMap = nodeMap | otherNode.nodeMap;
        // entries stay being entries only if one bits were in exactly one of input nodes
        // but not in the new data nodes
        var newDataMap = dataMap ^ otherNode.dataMap & ~newNodeMap;
        // (**) now, this is tricky: we have a number of entry-entry pairs and we don't know yet whether
        // they result in an entry (if keys are equal) or a new node (if they are not)
        // but we want to keep it to single allocation, so we check and mark equal ones here
        for (ForEachOneBit iter = new ForEachOneBit(dataMap & otherNode.dataMap); iter.moveNext(); ) {
            int positionMask = iter.currentPositionMask();
            var leftKey = this.keyAtIndex(this.entryKeyIndex(positionMask));
            var rightKey = otherNode.keyAtIndex(otherNode.entryKeyIndex(positionMask));
            // if they are equal, put them in the data map
            if (Objects.equals(leftKey, rightKey)) newDataMap = newDataMap | positionMask;
                // if they are not, put them in the node map
            else newNodeMap = newNodeMap | positionMask;
            // we can use this later to skip calling equals() again
        }
        assert (newNodeMap & newDataMap) == 0;
        TrieNode<K, V> mutableNode;
        if (this.ownedBy == mutator.ownership && this.dataMap == newDataMap && this.nodeMap == newNodeMap) {
            mutableNode = this;
        } else {
            var newBuffer = new Object[Integer.bitCount(newDataMap) * ENTRY_SIZE + Integer.bitCount(newNodeMap)];
            mutableNode = new TrieNode<>(newDataMap, newNodeMap, newBuffer);
        }

        for (ForEachOneBit it = new ForEachOneBit(newNodeMap); it.moveNext(); ) {
            int positionMask = it.currentPositionMask();
            int index = it.currentIndex();
            var newNodeIndex = mutableNode.buffer.length - 1 - index;
            mutableNode.buffer[newNodeIndex] =
                    mutablePutAllFromOtherNodeCell(otherNode, positionMask, shift, intersectionCounter, mutator);
        }
        for (ForEachOneBit it = new ForEachOneBit(newDataMap); it.moveNext(); ) {
            int positionMask = it.currentPositionMask();
            int index = it.currentIndex();
            var newKeyIndex = index * ENTRY_SIZE;
            if (!otherNode.hasEntryAt(positionMask)) {
                var oldKeyIndex = this.entryKeyIndex(positionMask);
                mutableNode.buffer[newKeyIndex] = this.keyAtIndex(oldKeyIndex);
                mutableNode.buffer[newKeyIndex + 1] = this.valueAtKeyIndex(oldKeyIndex);
            }
            // there is either only one entry in otherNode, or
            // both entries are here => they are equal, see ** above
            // so just overwrite that
            else {
                var oldKeyIndex = otherNode.entryKeyIndex(positionMask);
                mutableNode.buffer[newKeyIndex] = otherNode.keyAtIndex(oldKeyIndex);
                mutableNode.buffer[newKeyIndex + 1] = otherNode.valueAtKeyIndex(oldKeyIndex);
                if (this.hasEntryAt(positionMask)) intersectionCounter.count++;
            }
        }

        return (this.elementsIdentityEquals(mutableNode)) ? this
                : (otherNode.elementsIdentityEquals(mutableNode)) ? otherNode
                : mutableNode;
    }

    /// Returns null if an entry with the same key and value is already in the trie
    public @Nullable ModificationResult<K, V> put(int keyHash, K key, V value, int shift) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask);
            if (Objects.equals(key, keyAtIndex(keyIndex))) {
                V oldValue = valueAtKeyIndex(keyIndex);
                if (Objects.equals(oldValue, value)) return null;
                return updateValueAtIndex(keyIndex, value).asUpdateResult(oldValue);
            }
            return moveEntryToNode(keyIndex, keyPositionMask, keyHash, key, value, shift).asInsertResult();
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var nodeIndex = nodeIndex(keyPositionMask);
            var targetNode = nodeAtIndex(nodeIndex);
            ModificationResult<K, V> putResult;
            if (shift == MAX_SHIFT) {
                putResult = targetNode.collisionPut(key, value);
                if (putResult == null) return null;
            } else {
                putResult = targetNode.put(keyHash, key, value, shift + LOG_MAX_BRANCHING_FACTOR);
                if (putResult == null) return null;
            }
            return putResult.replaceNode(node -> updateNodeAtIndex(nodeIndex, keyPositionMask, node, null));
        }

        // no entry at this key hash segment
        return insertEntryAt(keyPositionMask, key, value).asInsertResult();
    }

    public TrieNode<K, V> mutablePut(
            int keyHash,
            K key,
            V value,
            int shift,
            TrieBuilder<K, V> mutator
    ) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask);

            if (Objects.equals(key, keyAtIndex(keyIndex))) {
                V oldValue = valueAtKeyIndex(keyIndex);
                if (Objects.equals(oldValue, value)) {
                    return this;
                }
                mutator.operationResult = oldValue;
                mutator.modCount++;
                return mutableUpdateValueAtIndex(keyIndex, value, mutator);
            }
            mutator.size++;
            return mutableMoveEntryToNode(keyIndex, keyPositionMask, keyHash, key, value, shift, mutator.ownership);
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var nodeIndex = nodeIndex(keyPositionMask);

            var targetNode = nodeAtIndex(nodeIndex);
            var newNode = (shift == MAX_SHIFT) ?
                    targetNode.mutableCollisionPut(key, value, mutator)
                    :
                    targetNode.mutablePut(keyHash, key, value, shift + LOG_MAX_BRANCHING_FACTOR, mutator);
            if (targetNode == newNode) {
                return this;
            }
            return updateNodeAtIndex(nodeIndex, keyPositionMask, newNode, mutator.ownership);
        }

        // key is absent
        mutator.size++;
        return mutableInsertEntryAt(keyPositionMask, key, value, mutator.ownership);
    }

    public TrieNode<K, V> remove(int keyHash, K key, int shift) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask);

            if (Objects.equals(key, keyAtIndex(keyIndex))) {
                return removeEntryAtIndex(keyIndex, keyPositionMask);
            }
            return this;
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var nodeIndex = nodeIndex(keyPositionMask);

            var targetNode = nodeAtIndex(nodeIndex);
            var newNode = (shift == MAX_SHIFT) ?
                    targetNode.collisionRemove(key)
                    :
                    targetNode.remove(keyHash, key, shift + LOG_MAX_BRANCHING_FACTOR);
            return replaceNode(targetNode, newNode, nodeIndex, keyPositionMask);
        }

        // key is absent
        return this;
    }

    private TrieNode<K, V> replaceNode(TrieNode<K, V> targetNode, TrieNode<K, V> newNode, int nodeIndex, int positionMask) {
        return (newNode.isEmpty()) ? removeNodeAtIndex(nodeIndex, positionMask)
                : (targetNode != newNode) ? updateNodeAtIndex(nodeIndex, positionMask, newNode, null)
                : this;
    }

    public TrieNode<K, V> mutableRemove(int keyHash, K key, int shift, TrieBuilder<K, V> mutator) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask);

            if (Objects.equals(key, keyAtIndex(keyIndex))) {
                return mutableRemoveEntryAtIndex(keyIndex, keyPositionMask, mutator);
            }
            return this;
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var nodeIndex = nodeIndex(keyPositionMask);

            var targetNode = nodeAtIndex(nodeIndex);
            var newNode = (shift == MAX_SHIFT) ?
                    targetNode.mutableCollisionRemove(key, mutator)
                    :
                    targetNode.mutableRemove(keyHash, key, shift + LOG_MAX_BRANCHING_FACTOR, mutator);
            return mutableReplaceNode(targetNode, newNode, nodeIndex, keyPositionMask, mutator.ownership);
        }

        // key is absent
        return this;
    }

    ///
    /// @param targetNode
    /// @param newNode      newNode can be empty to indicate that
    /// @param nodeIndex
    /// @param positionMask
    /// @param owner
    /// @return updated node, the node can be empty!
    private TrieNode<K, V> mutableReplaceNode(
            TrieNode<K, V> targetNode,
            TrieNode<K, V> newNode,
            int nodeIndex,
            int positionMask,
            MutabilityOwnership owner
    ) {
        return (newNode.isEmpty()) ? mutableRemoveNodeAtIndex(nodeIndex, positionMask, owner)
                // `newNode` === `targetNode` means the child returned itself (a no-op, or an owned in-place removal),
                // so this node's buffer already points to it. Keep this node unchanged to avoid spuriously
                // clearing `PersistentHashMapBuilder.builtMap` on no-ops. The `hasSingleEntry` exclusion still routes
                // a child that shrank to one entry to `updateNodeAtIndex`, which promotes it.
                : (newNode == targetNode && !newNode.hasSingleEntry()) ? this
                : updateNodeAtIndex(nodeIndex, positionMask, newNode, owner);

    }

    TrieNode<K, V> remove(int keyHash, K key, V value, int shift) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask);

            if (Objects.equals(key, keyAtIndex(keyIndex)) && Objects.equals(value, valueAtKeyIndex(keyIndex))) {
                return removeEntryAtIndex(keyIndex, keyPositionMask);
            }
            return this;
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var nodeIndex = nodeIndex(keyPositionMask);

            var targetNode = nodeAtIndex(nodeIndex);
            var newNode = (shift == MAX_SHIFT) ?
                    targetNode.collisionRemove(key, value)
                    :
                    targetNode.remove(keyHash, key, value, shift + LOG_MAX_BRANCHING_FACTOR);
            return replaceNode(targetNode, newNode, nodeIndex, keyPositionMask);
        }

        // key is absent
        return this;
    }

    TrieNode<K, V> mutableRemove(
            int keyHash,
            K key,
            V value,
            int shift,
            TrieBuilder<K, V> mutator
    ) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask);

            if (Objects.equals(key, keyAtIndex(keyIndex)) && Objects.equals(value, valueAtKeyIndex(keyIndex))) {
                return mutableRemoveEntryAtIndex(keyIndex, keyPositionMask, mutator);
            }
            return this;
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var nodeIndex = nodeIndex(keyPositionMask);

            var targetNode = nodeAtIndex(nodeIndex);
            var newNode = (shift == MAX_SHIFT) ?
                    targetNode.mutableCollisionRemove(key, value, mutator)
                    :
                    targetNode.mutableRemove(keyHash, key, value, shift + LOG_MAX_BRANCHING_FACTOR, mutator);
            return mutableReplaceNode(targetNode, newNode, nodeIndex, keyPositionMask, mutator.ownership);
        }

        // key is absent
        return this;
    }

    private <K1, V1> boolean equalsWith(TrieNode<K1, V1> that, BiPredicate<V, V1> equalityComparator) {
        if (this == that) return true;
        if (dataMap != that.dataMap || nodeMap != that.nodeMap) return false;
        if (dataMap == 0 && nodeMap == 0) { // collision node
            if (buffer.length != that.buffer.length) return false;

            for (int i = 0; i < buffer.length; i += ENTRY_SIZE) {
                var thatKey = that.keyAtIndex(i);
                var thatValue = that.valueAtKeyIndex(i);
                var keyIndex = collisionKeyIndex(thatKey);
                if (keyIndex != -1) {
                    var value = valueAtKeyIndex(keyIndex);
                    if (!equalityComparator.test(value, thatValue)) return false;
                } else {
                    return false;
                }
            }
            return true;
        }

        var valueSize = Integer.bitCount(dataMap) * ENTRY_SIZE;
        for (int i = 0; i < valueSize; i += ENTRY_SIZE) {
            if (keyAtIndex(i) != that.keyAtIndex(i)) return false;
            if (!equalityComparator.test(valueAtKeyIndex(i), that.valueAtKeyIndex(i))) return false;
        }
        for (int i = valueSize; i < buffer.length; i++) {
            if (!nodeAtIndex(i).equalsWith(that.nodeAtIndex(i), equalityComparator)) return false;
        }
        return true;
    }


}
