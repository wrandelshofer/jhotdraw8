package org.jhotdraw8.icollection.impl.champlinked;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;
import org.jhotdraw8.icollection.impl.champset.ForEachOneBit;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/// This code has been derived from
/// [kotlix.collections.immutable, TrieNode.kt](https://github.com/Kotlin/kotlinx.collections.immutable/blob/1d00eeaf6f4559a7953332cb6171c04b886d9a17/core/commonMain/src/implementations/immutableMap/TrieNode.kt),
/// JetBrains s.r.o.
/// [Apache License 2.0](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/LICENSE.txt)

@SuppressWarnings("FinalClass")
public final class TrieNode<K> {
    public static final int LOG_MAX_BRANCHING_FACTOR = 5;
    public static final Object NO_DATA = new Object();
    static final int MAX_BRANCHING_FACTOR = 32;
    private static final int MAX_BRANCHING_FACTOR_MINUS_ONE = MAX_BRANCHING_FACTOR - 1;
    private static final int MAX_SHIFT = 30;
    public static TrieNode<Object> EMPTY = new TrieNode<Object>(0, 0, new Object[0], null);
    public Object[] buffer;
    int dataMap;
    int nodeMap;
    private final @Nullable MutabilityOwnership ownedBy;

    TrieNode(int dataMap, int nodeMap, Object[] buffer, @Nullable MutabilityOwnership ownedBy) {
        this.dataMap = dataMap;
        this.nodeMap = nodeMap;
        this.buffer = buffer;
        this.ownedBy = ownedBy;
    }

    public static <T, U> TrieNode<T> empty() {
        //noinspection unchecked
        return (TrieNode<T>) EMPTY;
    }

    private static <K, V> Object[] insertEntryAtIndex(Object[] array, int keyIndex, K key, V value, int ENTRY_SIZE) {
        var newBuffer = new Object[array.length + ENTRY_SIZE];
        System.arraycopy(array, 0, newBuffer, 0, keyIndex);
        System.arraycopy(array, keyIndex, newBuffer, keyIndex + ENTRY_SIZE, array.length - keyIndex);
        newBuffer[keyIndex] = key;
        newBuffer[keyIndex + 1] = value;
        return newBuffer;
    }

    private static <K, V> Object[] insertArrayEntryAtIndex(Object[] array, int keyIndex, Object[] entry, int ENTRY_SIZE) {
        var newBuffer = new Object[array.length + ENTRY_SIZE];
        System.arraycopy(array, 0, newBuffer, 0, keyIndex);
        System.arraycopy(array, keyIndex, newBuffer, keyIndex + ENTRY_SIZE, array.length - keyIndex);
        System.arraycopy(entry, 0, newBuffer, keyIndex, entry.length);
        return newBuffer;
    }

    public static <K, V> TrieNode<K> newTrieNode(int dataMap, int nodeMap, Object[] buffer, @Nullable MutabilityOwnership ownedBy) {
        return new TrieNode<K>(dataMap, nodeMap, buffer, ownedBy);
    }

    public static <VV> VV noData() {
        //noinspection unchecked
        return (VV) NO_DATA;
    }

    private static Object[] removeEntryAtIndex(Object[] array, int keyIndex, int ENTRY_SIZE) {
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

    private static Object[] replaceEntryWithNode(Object[] array, int keyIndex, int nodeIndex, TrieNode<?> newNode, int ENTRY_SIZE) {
        var newNodeIndex = nodeIndex - ENTRY_SIZE; // place where to insert new node in the new buffer
        var newBuffer = new Object[array.length - ENTRY_SIZE + 1];
        System.arraycopy(array, 0, newBuffer, 0, keyIndex);
        System.arraycopy(array, keyIndex + ENTRY_SIZE, newBuffer, keyIndex, nodeIndex - keyIndex - ENTRY_SIZE);
        newBuffer[newNodeIndex] = newNode;
        System.arraycopy(array, nodeIndex, newBuffer, newNodeIndex + 1, array.length - nodeIndex);
        return newBuffer;
    }

    private static <K, V> Object[] replaceNodeWithEntry(Object[] array, int nodeIndex, int keyIndex, Object[] entry, int ENTRY_SIZE) {
        var newBuffer = Arrays.copyOf(array, array.length + ENTRY_SIZE - 1);
        System.arraycopy(newBuffer, nodeIndex + 1, newBuffer, nodeIndex + ENTRY_SIZE, array.length - nodeIndex - 1);
        System.arraycopy(newBuffer, keyIndex, newBuffer, keyIndex + ENTRY_SIZE, nodeIndex - keyIndex);
        System.arraycopy(entry, 0, newBuffer, keyIndex, ENTRY_SIZE);
        return newBuffer;
    }

    private ModificationResult<K, Object> asInsertResult() {
        return new ModificationResult<>(this, 1, null);
    }

    private ModificationResult<K, Object> asUpdateResult(Object oldValue) {
        return new ModificationResult<>(this, 0, oldValue);
    }

    private Object[] bufferMoveEntryToNode(
            int keyIndex,
            int positionMask,
            int newKeyHash,
            K newKey,
            Object newValue,
            int shift,
            @Nullable MutabilityOwnership owner,
            int ENTRY_SIZE) {
        var storedKey = keyAtIndex(keyIndex);
        var storedKeyHash = storedKey.hashCode();
        Object storedValue = valueAtKeyIndex(keyIndex);
        var newNode = makeNode(
                storedKeyHash, storedKey, storedValue,
                newKeyHash, newKey, newValue, shift + LOG_MAX_BRANCHING_FACTOR, owner,
                ENTRY_SIZE);

        var nodeIndex = nodeIndex(positionMask) + 1; // place where to insert new node in the current buffer

        return replaceEntryWithNode(buffer, keyIndex, nodeIndex, newNode, ENTRY_SIZE);
    }

    private Object[] bufferMoveArrayEntryToNode(
            int keyIndex,
            int positionMask,
            int newKeyHash,
            K newKey,
            Object[] newEntry,
            int shift,
            @Nullable MutabilityOwnership owner,
            int ENTRY_SIZE) {
        var storedKey = keyAtIndex(keyIndex);
        var storedKeyHash = storedKey.hashCode();
        var newNode = makeNode(
                storedKeyHash, storedKey, keyIndex, buffer,
                newKeyHash, newKey, newEntry, shift + LOG_MAX_BRANCHING_FACTOR, owner,
                ENTRY_SIZE);

        var nodeIndex = nodeIndex(positionMask) + 1; // place where to insert new node in the current buffer

        return replaceEntryWithNode(buffer, keyIndex, nodeIndex, newNode, ENTRY_SIZE);
    }

    private int calculateSize(int ENTRY_SIZE) {
        if (nodeMap == 0) return buffer.length / ENTRY_SIZE;
        var numValues = Integer.bitCount(dataMap);
        var result = numValues;
        for (int i = (numValues * ENTRY_SIZE); i < buffer.length; i++) {
            result += nodeAtIndex(i).calculateSize(ENTRY_SIZE);
        }
        return result;
    }

    private boolean collisionContainsKey(K key, int ENTRY_SIZE) {
        return collisionKeyIndex(key, ENTRY_SIZE) != -1;
    }

    /**
     * Returns number of entries, works regardless if node is a collision node
     * or a regular node.
     *
     * @return entry count
     */
    public int collisionEntryCount(int ENTRY_SIZE) {
        return isCollisionNode() ? buffer.length / ENTRY_SIZE : Integer.bitCount(dataMap);
    }

    private Object collisionGet(K key, int ENTRY_SIZE) {
        var keyIndex = collisionKeyIndex(key, ENTRY_SIZE);
        return (keyIndex != -1) ? valueAtKeyIndex(keyIndex) : null;
    }

    // here and later:
    // positionMask — an int in form 2^n, i.e. having the single bit set, whose ordinal is a logical position in buffer

    private <VV> @Nullable VV collisionGet(K key, int ENTRY_SIZE, int valueIndex) {
        var keyIndex = collisionKeyIndex(key, ENTRY_SIZE);
        return (keyIndex != -1) ? valueAtKeyIndex(keyIndex, valueIndex) : null;
    }

    private boolean collisionGetArray(K key, Object[] outputArray, int ENTRY_SIZE) {
        var keyIndex = collisionKeyIndex(key, ENTRY_SIZE);
        if (keyIndex == -1) return false;
        System.arraycopy(buffer, keyIndex, outputArray, 0, ENTRY_SIZE);
        return true;
    }

    private int collisionKeyIndex(Object key, int ENTRY_SIZE) {
        for (int i = 0; i < buffer.length; i += ENTRY_SIZE) {
            if (Objects.equals(key, keyAtIndex(i))) return i;
        }
        return -1;
    }

    private @Nullable ModificationResult<K, Object> collisionPut(K key, Object value, int ENTRY_SIZE) {
        var keyIndex = collisionKeyIndex(key, ENTRY_SIZE);
        if (keyIndex != -1) {
            Object oldValue = valueAtKeyIndex(keyIndex);
            if (Objects.equals(value, oldValue)) {
                return null;
            }
            var newBuffer = buffer.clone();
            newBuffer[keyIndex + 1] = value;
            return TrieNode.<K, Object>newTrieNode(0, 0, newBuffer, null).asUpdateResult(oldValue);
        }
        var newBuffer = insertEntryAtIndex(buffer, 0, key, value, ENTRY_SIZE);
        return TrieNode.<K, Object>newTrieNode(0, 0, newBuffer, null).asInsertResult();
    }

    private TrieNode<K> collisionRemove(K key, int ENTRY_SIZE) {
        var keyIndex = collisionKeyIndex(key, ENTRY_SIZE);
        if (keyIndex != -1) {
            return collisionRemoveEntryAtIndex(keyIndex, ENTRY_SIZE);
        }
        return this;
    }

    private TrieNode<K> collisionRemove(K key, Object value, int ENTRY_SIZE) {
        var keyIndex = collisionKeyIndex(key, ENTRY_SIZE);
        if (keyIndex != -1 && Objects.equals(value, valueAtKeyIndex(keyIndex))) {
            return collisionRemoveEntryAtIndex(keyIndex, ENTRY_SIZE);
        }
        return this;
    }

    private TrieNode<K> collisionRemoveEntryAtIndex(int i, int ENTRY_SIZE) {
        if (buffer.length == ENTRY_SIZE) return empty();
        var newBuffer = removeEntryAtIndex(buffer, i, ENTRY_SIZE);
        return newTrieNode(0, 0, newBuffer, null);
    }

    public boolean containsKey(int keyHash, @Nullable K key, int shift, int ENTRY_SIZE) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            return Objects.equals(key, keyAtIndex(entryKeyIndex(keyPositionMask, ENTRY_SIZE)));
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var targetNode = nodeAtIndex(nodeIndex(keyPositionMask));
            if (shift == MAX_SHIFT) {
                return targetNode.collisionContainsKey(key, ENTRY_SIZE);
            }
            return targetNode.containsKey(keyHash, key, shift + LOG_MAX_BRANCHING_FACTOR, ENTRY_SIZE);
        }

        // key is absent
        return false;
    }

    private boolean elementsIdentityEquals(TrieNode<K> otherNode) {
        if (this == otherNode) return true;
        if (nodeMap != otherNode.nodeMap) return false;
        if (dataMap != otherNode.dataMap) return false;
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] != otherNode.buffer[i]) return false;
        }
        return true;
    }

    /**
     * Returns number of entries stored in this trie node (not counting subnodes)
     */
    public int entryCount() {
        return Integer.bitCount(dataMap);
    }

    /**
     * Gets the index in buffer of the data entry key corresponding to the position specified by [positionMask].
     */
    private int entryKeyIndex(int positionMask, int ENTRY_SIZE) {
        return ENTRY_SIZE * Integer.bitCount(dataMap & (positionMask - 1));
    }

    private <K1, V1> boolean equalsWith(TrieNode<K1> that, BiPredicate<Object, V1> equalityComparator, int ENTRY_SIZE) {
        if (this == that) return true;
        if (dataMap != that.dataMap || nodeMap != that.nodeMap) return false;
        if (dataMap == 0 && nodeMap == 0) { // collision node
            if (buffer.length != that.buffer.length) return false;

            for (int i = 0; i < buffer.length; i += ENTRY_SIZE) {
                var thatKey = that.keyAtIndex(i);
                V1 thatValue = that.valueAtKeyIndex(i);
                var keyIndex = collisionKeyIndex(thatKey, ENTRY_SIZE);
                if (keyIndex != -1) {
                    Object value = valueAtKeyIndex(keyIndex);
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
            if (!nodeAtIndex(i).equalsWith(that.nodeAtIndex(i), equalityComparator, ENTRY_SIZE)) return false;
        }
        return true;
    }

    public boolean getArrayEntry(K key, Object[] outputArray, int ENTRY_SIZE) {
        return getArrayEntry(Objects.hashCode(key), key, 0, outputArray, ENTRY_SIZE);
    }

    /// Fills the value into the provided array, returns true if the value is present
    public boolean getArrayEntry(int keyHash, K key, int shift, Object[] outputArray, int ENTRY_SIZE) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask, ENTRY_SIZE);

            if (Objects.equals(key, keyAtIndex(keyIndex))) {
                System.arraycopy(buffer, keyIndex, outputArray, 0, ENTRY_SIZE);
                return true;
            }
            return false;
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var targetNode = nodeAtIndex(nodeIndex(keyPositionMask));
            if (shift == MAX_SHIFT) {
                return targetNode.collisionGetArray(key, outputArray, ENTRY_SIZE);
            }
            return targetNode.getArrayEntry(keyHash, key, shift + LOG_MAX_BRANCHING_FACTOR, outputArray, ENTRY_SIZE);
        }

        // key is absent
        return false;
    }

    /// Returns the default value if the key is not present
    public <VV> @Nullable VV getOrDefault(int keyHash, K key, int shift, @Nullable VV defaultValue, int ENTRY_SIZE, int valueIndex) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask, ENTRY_SIZE);

            if (Objects.equals(key, keyAtIndex(keyIndex))) {
                return valueAtKeyIndex(keyIndex, valueIndex);
            }
            return defaultValue;
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var targetNode = nodeAtIndex(nodeIndex(keyPositionMask));
            if (shift == MAX_SHIFT) {
                return targetNode.collisionGet(key, ENTRY_SIZE, valueIndex);
            }
            return targetNode.getOrDefault(keyHash, key, shift + LOG_MAX_BRANCHING_FACTOR, defaultValue, ENTRY_SIZE, valueIndex);
        }

        // key is absent
        return defaultValue;
    }

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
     * Returns `true` if this node contains exactly one entry and no subtrie nodes,
     * meaning the parent should replace the node with the entry it contains.
     */
    private boolean hasSingleEntry(int ENTRY_SIZE) {
        return buffer.length == ENTRY_SIZE && nodeMap == 0;
    }

    int indexSegment(int index, int shift) {
        return (index >> shift) & MAX_BRANCHING_FACTOR_MINUS_ONE;
    }

    private TrieNode<K> insertEntryAt(int positionMask, K key, Object value, int ENTRY_SIZE) {
        assert !hasEntryAt(positionMask);

        var keyIndex = entryKeyIndex(positionMask, ENTRY_SIZE);
        var newBuffer = insertEntryAtIndex(buffer, keyIndex, key, value, ENTRY_SIZE);
        return newTrieNode(dataMap | positionMask, nodeMap, newBuffer, null);
    }

    public boolean isCollisionNode() {
        return buffer.length > 0 && dataMap == 0 && nodeMap == 0;
    }

    public boolean isEmpty() {
        return buffer.length == 0;
    }

    /**
     * Retrieves the buffer element at the given [keyIndex] as key of a data entry.
     */
    @SuppressWarnings("unchecked")
    private K keyAtIndex(int keyIndex) {
        return (K) buffer[keyIndex];
    }

    /**
     * Creates a newTrieNode for holding two given key value entries
     */
    private TrieNode<K> makeNode(
            int keyHash1,
            K key1,
            Object value1,
            int keyHash2,
            K key2,
            Object value2,
            int shift,
            @Nullable MutabilityOwnership owner,
            int ENTRY_SIZE) {
        Object[] newBuffer = new Object[ENTRY_SIZE * 2];
        if (shift > MAX_SHIFT) {
            assert key1 != key2;
            // when two key hashes are entirely equal: the last level subtrie node stores them just as unordered list
            newBuffer[0] = key1;
            newBuffer[1] = value1;
            newBuffer[ENTRY_SIZE + 0] = key2;
            newBuffer[ENTRY_SIZE + 1] = value2;
            return newTrieNode(0, 0, newBuffer, owner);
        }

        var setBit1 = indexSegment(keyHash1, shift);
        var setBit2 = indexSegment(keyHash2, shift);
        if (setBit1 != setBit2) {
            if (setBit1 < setBit2) {
                newBuffer[0] = key1;
                newBuffer[1] = value1;
                newBuffer[ENTRY_SIZE + 0] = key2;
                newBuffer[ENTRY_SIZE + 1] = value2;
            } else {
                newBuffer[0] = key2;
                newBuffer[1] = value2;
                newBuffer[ENTRY_SIZE + 0] = key1;
                newBuffer[ENTRY_SIZE + 1] = value1;

            }
            return newTrieNode((1 << setBit1) | (1 << setBit2), 0, newBuffer, owner);
        }
        // hash segments at the given shift are equal: move these entries into the subtrie
        var node = makeNode(keyHash1, key1, value1, keyHash2, key2, value2, shift + LOG_MAX_BRANCHING_FACTOR, owner, ENTRY_SIZE);
        return newTrieNode(0, 1 << setBit1, new Object[]{node}, owner);
    }

    /**
     * Creates a newTrieNode for holding two given key value entries
     */
    private TrieNode<K> makeNode(
            int keyHash1,
            K key1,
            int key1Index,
            Object[] key1Buffer,
            int keyHash2,
            K key2,
            Object[] entry2,
            int shift,
            @Nullable MutabilityOwnership owner,
            int ENTRY_SIZE) {
        Object[] newBuffer = new Object[ENTRY_SIZE * 2];
        if (shift > MAX_SHIFT) {
            assert key1 != key2;
            // when two key hashes are entirely equal: the last level subtrie node stores them just as unordered list
            System.arraycopy(key1Buffer, key1Index, newBuffer, 0, ENTRY_SIZE);
            System.arraycopy(entry2, 0, newBuffer, ENTRY_SIZE, ENTRY_SIZE);
            return newTrieNode(0, 0, newBuffer, owner);
        }

        var setBit1 = indexSegment(keyHash1, shift);
        var setBit2 = indexSegment(keyHash2, shift);
        if (setBit1 != setBit2) {
            if (setBit1 < setBit2) {
                System.arraycopy(key1Buffer, key1Index, newBuffer, 0, ENTRY_SIZE);
                System.arraycopy(entry2, 0, newBuffer, ENTRY_SIZE, ENTRY_SIZE);
            } else {
                System.arraycopy(key1Buffer, key1Index, newBuffer, ENTRY_SIZE, ENTRY_SIZE);
                System.arraycopy(entry2, 0, newBuffer, 0, ENTRY_SIZE);

            }
            return newTrieNode((1 << setBit1) | (1 << setBit2), 0, newBuffer, owner);
        }
        // hash segments at the given shift are equal: move these entries into the subtrie
        var node = makeNode(keyHash1, key1, key1Index, key1Buffer, keyHash2, key2, entry2,
                shift + LOG_MAX_BRANCHING_FACTOR, owner, ENTRY_SIZE);
        return newTrieNode(0, 1 << setBit1, new Object[]{node}, owner);
    }

    private TrieNode<K> moveEntryToNode(
            int keyIndex,
            int positionMask,
            int newKeyHash,
            K newKey,
            Object newValue,
            int shift,
            int ENTRY_SIZE) {
        assert hasEntryAt(positionMask);
        assert !hasNodeAt(positionMask);

        var newBuffer = bufferMoveEntryToNode(keyIndex, positionMask, newKeyHash, newKey, newValue, shift, null, ENTRY_SIZE);
        return newTrieNode(dataMap ^ positionMask, nodeMap | positionMask, newBuffer, null);
    }

    private TrieNode<K> mutableCollisionPut(K key, Object[] entry, TrieBuilder<K, Object> mutator, BiFunction<Object[], Object[], Object[]> updateFunction, int ENTRY_SIZE) {
        // Check if there is an entry with the specified key.
        var keyIndex = collisionKeyIndex(key, ENTRY_SIZE);
        if (keyIndex != -1) { // found entry with the specified key
            Object[] oldEntry = entryAtKeyIndex(keyIndex, ENTRY_SIZE);
            Object[] newEntry = updateFunction.apply(oldEntry, entry);
            if (newEntry == oldEntry) {
                return this;
            }
            mutator.entry = oldEntry;
            mutator.modCount++;

            // If the [mutator] is exclusive owner of this node, update value of the entry in-place.
            if (ownedBy == mutator.ownership) {
                System.arraycopy(newEntry, 0, buffer, keyIndex, ENTRY_SIZE);
                return this;
            }

            // Create new node with updated entry value.
            var newBuffer = buffer.clone();
            System.arraycopy(newEntry, 0, newBuffer, keyIndex, ENTRY_SIZE);
            return newTrieNode(0, 0, newBuffer, mutator.ownership);
        }
        // Create new collision node with the specified entry added to it.
        mutator.size++;
        var newBuffer = insertArrayEntryAtIndex(buffer, 0, entry, ENTRY_SIZE);
        return newTrieNode(0, 0, newBuffer, mutator.ownership);
    }

    @SuppressWarnings("unchecked")
    private TrieNode<K> mutableCollisionPutAll(
            TrieNode<K> otherNode,
            DeltaCounter intersectionCounter,
            MutabilityOwnership owner,
            int ENTRY_SIZE, TrieBuilder<K, Object> mutator) {
        assert nodeMap == 0;
        assert dataMap == 0;
        assert otherNode.nodeMap == 0;
        assert otherNode.dataMap == 0;
        var tempBuffer = Arrays.copyOf(this.buffer, this.buffer.length + otherNode.buffer.length);
        var i = this.buffer.length;
        var replaced = false;
        var sharedKeys = true;
        for (int j = 0; j < otherNode.buffer.length; j += ENTRY_SIZE) {
            var keyIndex = this.collisionKeyIndex(otherNode.buffer[j], ENTRY_SIZE);
            if (keyIndex == -1) {
                tempBuffer[i] = otherNode.buffer[j];
                tempBuffer[i + 1] = otherNode.buffer[j + 1];
                i += ENTRY_SIZE;
            } else {
                intersectionCounter.count++;
                if (!Objects.equals(tempBuffer[keyIndex], otherNode.buffer[j])) sharedKeys = false;
                if (!Arrays.equals(tempBuffer, keyIndex + 1, keyIndex - 1 + ENTRY_SIZE,
                        otherNode.buffer, j + 1, j - 1 + ENTRY_SIZE)) {
                    System.arraycopy(otherNode.buffer, j + 1, tempBuffer, keyIndex + 1, ENTRY_SIZE - 1);
                    replaced = true;
                }
            }
        }
        if (replaced) mutator.modCount++;
        int newSize = i;
        if (newSize == this.buffer.length && !replaced) return this;
        if (newSize == otherNode.buffer.length && sharedKeys) return otherNode;
        if (newSize == tempBuffer.length) return newTrieNode(0, 0, tempBuffer, mutator.ownership);
        return newTrieNode(0, 0, Arrays.copyOf(tempBuffer, newSize), mutator.ownership);
    }

    private TrieNode<K> mutableCollisionRemove(K key, TrieBuilder<K, Object> mutator, int ENTRY_SIZE) {
        var keyIndex = collisionKeyIndex(key, ENTRY_SIZE);
        if (keyIndex != -1) {
            return mutableCollisionRemoveEntryAtIndex(keyIndex, mutator, ENTRY_SIZE);
        }
        return this;
    }

    private TrieNode<K> mutableCollisionRemove(K key, Object value, TrieBuilder<K, Object> mutator, int ENTRY_SIZE) {
        var keyIndex = collisionKeyIndex(key, ENTRY_SIZE);
        if (keyIndex != -1 && Objects.equals(value, valueAtKeyIndex(keyIndex))) {
            return mutableCollisionRemoveEntryAtIndex(keyIndex, mutator, ENTRY_SIZE);
        }
        return this;
    }

    private TrieNode<K> mutableCollisionRemoveEntryAtIndex(int i, TrieBuilder<K, Object> mutator, int ENTRY_SIZE) {
        mutator.size--;
        mutator.entry = (mutator.entry == null) ? new Object[ENTRY_SIZE] : mutator.entry;
        mutator.value = valueAtKeyIndex(i);
        System.arraycopy(buffer, i, mutator.entry, 0, ENTRY_SIZE);
        if (buffer.length == ENTRY_SIZE) return empty();

        if (ownedBy == mutator.ownership) {
            buffer = removeEntryAtIndex(buffer, i, ENTRY_SIZE);
            return this;
        }
        var newBuffer = removeEntryAtIndex(buffer, i, ENTRY_SIZE);
        return newTrieNode(0, 0, newBuffer, mutator.ownership);
    }

    private TrieNode<K> mutableInsertEntryAt(int positionMask, K key, Object[] entry, MutabilityOwnership owner, int ENTRY_SIZE) {
        assert !hasEntryAt(positionMask);

        var keyIndex = entryKeyIndex(positionMask, ENTRY_SIZE);
        if (ownedBy == owner) {
            buffer = insertArrayEntryAtIndex(buffer, keyIndex, entry, ENTRY_SIZE);
            dataMap = dataMap | positionMask;
            return this;
        }
        var newBuffer = insertArrayEntryAtIndex(buffer, keyIndex, entry, ENTRY_SIZE);
        return newTrieNode(dataMap | positionMask, nodeMap, newBuffer, owner);
    }

    private TrieNode<K> mutableMoveEntryToNode(
            int keyIndex,
            int positionMask,
            int newKeyHash,
            K newKey,
            Object[] newValue,
            int shift,
            MutabilityOwnership owner,
            int ENTRY_SIZE) {
        assert hasEntryAt(positionMask);
        assert !hasNodeAt(positionMask);

        if (ownedBy == owner) {
            buffer = bufferMoveArrayEntryToNode(keyIndex, positionMask, newKeyHash, newKey, newValue, shift, owner, ENTRY_SIZE);
            dataMap = dataMap ^ positionMask;
            nodeMap = nodeMap | positionMask;
            return this;
        }
        var newBuffer = bufferMoveArrayEntryToNode(keyIndex, positionMask, newKeyHash, newKey, newValue, shift, owner, ENTRY_SIZE);
        return newTrieNode(dataMap ^ positionMask, nodeMap | positionMask, newBuffer, owner);
    }

    public TrieNode<K> mutablePut(
            @Nullable Object[] entry,
            TrieBuilder<K, Object> mutator,
            BiFunction<@Nullable Object[], @Nullable Object[], @Nullable Object[]> updateFunction, int ENTRY_SIZE) {
        return mutablePut(Objects.hashCode(entry[0]), (K) entry[0], entry, 0, mutator, updateFunction, ENTRY_SIZE);
    }

    public TrieNode<K> mutablePut(
            int keyHash,
            K key,
            @Nullable Object[] entry,
            int shift,
            TrieBuilder<K, Object> mutator,
            BiFunction<@Nullable Object[], @Nullable Object[], @Nullable Object[]> updateFunction, int ENTRY_SIZE) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);
        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask, ENTRY_SIZE);
            if (Objects.equals(key, keyAtIndex(keyIndex))) {
                Object[] oldEntry = entryAtKeyIndex(keyIndex, ENTRY_SIZE);
                Object[] updatedEntry = updateFunction.apply(oldEntry, entry);
                if (oldEntry == updatedEntry) {
                    return this;
                }
                mutator.entry = oldEntry;
                mutator.modCount++;
                return mutableUpdateEntryAtIndex(keyIndex, updatedEntry, mutator, ENTRY_SIZE);
            }
            mutator.size++;
            return mutableMoveEntryToNode(keyIndex, keyPositionMask, keyHash, key, entry, shift, mutator.ownership, ENTRY_SIZE);
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var nodeIndex = nodeIndex(keyPositionMask);
            var targetNode = nodeAtIndex(nodeIndex);
            var newNode = (shift == MAX_SHIFT) ?
                    targetNode.mutableCollisionPut(key, entry, mutator, updateFunction, ENTRY_SIZE)
                    :
                    targetNode.mutablePut(keyHash, key, entry, shift + LOG_MAX_BRANCHING_FACTOR, mutator, updateFunction, ENTRY_SIZE);
            if (targetNode == newNode) {
                return this;
            }
            return updateNodeAtIndex(nodeIndex, keyPositionMask, newNode, mutator.ownership, ENTRY_SIZE);
        }

        // key is absent
        mutator.size++;
        return mutableInsertEntryAt(keyPositionMask, key, entry, mutator.ownership, ENTRY_SIZE);
    }

    // int newSize = mySet.size + otherSet.size - deltaCounter.count
    public TrieNode<K> mutablePutAll(
            TrieNode<K> otherNode,
            int shift,
            DeltaCounter intersectionCounter,
            TrieBuilder<K, Object> mutator,
            BiFunction<Object[], Object[], Object[]> updateFunction, int ENTRY_SIZE) {
        if (this == otherNode) {
            intersectionCounter.count += calculateSize(ENTRY_SIZE);
            return this;
        }
        // the collision case
        if (shift > MAX_SHIFT) {
            return mutableCollisionPutAll(otherNode, intersectionCounter, mutator.ownership, ENTRY_SIZE, mutator);
        }

        // new nodes are where either of the old ones were
        var newNodeMap = nodeMap | otherNode.nodeMap;
        // entries stay being entries only if one bits were in exactly one of input nodes
        // but not in the new data nodes
        var newDataMap = dataMap ^ otherNode.dataMap & ~newNodeMap;
        // (**) now, this is tricky: we have a number of entry-entry pairs, and we don't know yet whether
        // they result in an entry (if keys are equal) or a new node (if they are not)
        // but we want to keep it to single allocation, so we check and mark equal ones here
        for (ForEachOneBit iter = new ForEachOneBit(dataMap & otherNode.dataMap); iter.moveNext(); ) {
            int positionMask = iter.currentPositionMask();
            var leftKey = this.keyAtIndex(this.entryKeyIndex(positionMask, ENTRY_SIZE));
            var rightKey = otherNode.keyAtIndex(otherNode.entryKeyIndex(positionMask, ENTRY_SIZE));
            // if they are equal, put them in the data map
            if (Objects.equals(leftKey, rightKey)) newDataMap = newDataMap | positionMask;
                // if they are not, put them in the node map
            else newNodeMap = newNodeMap | positionMask;
            // we can use this later to skip calling equals() again
        }
        assert (newNodeMap & newDataMap) == 0;
        TrieNode<K> mutableNode;
        if (this.ownedBy == mutator.ownership && this.dataMap == newDataMap && this.nodeMap == newNodeMap) {
            mutableNode = this;
        } else {
            var newBuffer = new Object[Integer.bitCount(newDataMap) * ENTRY_SIZE + Integer.bitCount(newNodeMap)];
            mutableNode = newTrieNode(newDataMap, newNodeMap, newBuffer, null);
        }

        for (ForEachOneBit it = new ForEachOneBit(newNodeMap); it.moveNext(); ) {
            int positionMask = it.currentPositionMask();
            int index = it.currentIndex();
            var newNodeIndex = mutableNode.buffer.length - 1 - index;
            mutableNode.buffer[newNodeIndex] =
                    mutablePutAllFromOtherNodeCell(otherNode, positionMask, shift, intersectionCounter, mutator, updateFunction, ENTRY_SIZE);
        }
        for (ForEachOneBit it = new ForEachOneBit(newDataMap); it.moveNext(); ) {
            int positionMask = it.currentPositionMask();
            int index = it.currentIndex();
            var newKeyIndex = index * ENTRY_SIZE;
            if (!otherNode.hasEntryAt(positionMask)) {
                var oldKeyIndex = this.entryKeyIndex(positionMask, ENTRY_SIZE);
                mutableNode.buffer[newKeyIndex] = this.keyAtIndex(oldKeyIndex);
                mutableNode.buffer[newKeyIndex + 1] = this.valueAtKeyIndex(oldKeyIndex);
            }
            // there is either only one entry in otherNode, or
            // both entries are here => they are equal, see ** above
            // so keep this node's key if both are here, and take the argument's value
            else {
                var otherKeyIndex = otherNode.entryKeyIndex(positionMask, ENTRY_SIZE);
                var otherValue = otherNode.valueAtKeyIndex(otherKeyIndex);
                if (this.hasEntryAt(positionMask)) {
                    var thisKeyIndex = this.entryKeyIndex(positionMask, ENTRY_SIZE);
                    intersectionCounter.count++;
                    if (mutableNode != this) {
                        var thisValue = this.valueAtKeyIndex(thisKeyIndex);
                        if (thisValue != otherValue) mutator.modCount++;
                    }
                    mutableNode.buffer[newKeyIndex] = this.keyAtIndex(thisKeyIndex);
                } else {
                    mutableNode.buffer[newKeyIndex] = otherNode.keyAtIndex(otherKeyIndex);
                }
                mutableNode.buffer[newKeyIndex + 1] = otherValue;
            }
        }

        return (this.elementsIdentityEquals(mutableNode)) ? this
                : (otherNode.elementsIdentityEquals(mutableNode)) ? otherNode
                : mutableNode;
    }

    /**
     * Updates the cell of this node at [positionMask] with entries from the cell of [otherNode] at [positionMask].
     */
    private TrieNode<K> mutablePutAllFromOtherNodeCell(
            TrieNode<K> otherNode,
            int positionMask,
            int shift,
            DeltaCounter intersectionCounter,
            TrieBuilder<K, Object> mutator,
            BiFunction<Object[], Object[], Object[]> updateFunction, int ENTRY_SIZE) {
        if (this.hasNodeAt(positionMask)) {
            var targetNode = this.nodeAtIndex(nodeIndex(positionMask));
            if (otherNode.hasNodeAt(positionMask)) {
                var otherTargetNode = otherNode.nodeAtIndex(otherNode.nodeIndex(positionMask));
                return targetNode.mutablePutAll(
                        otherTargetNode,
                        shift + LOG_MAX_BRANCHING_FACTOR,
                        intersectionCounter,
                        mutator,
                        updateFunction, ENTRY_SIZE);
            } else if (otherNode.hasEntryAt(positionMask)) {
                var keyIndex = otherNode.entryKeyIndex(positionMask, ENTRY_SIZE);
                var key = otherNode.keyAtIndex(keyIndex);
                var value = otherNode.entryAtKeyIndex(keyIndex, ENTRY_SIZE);
                var oldSize = mutator.size;
                TrieNode<K> result;
                if (shift == MAX_SHIFT) {
                    result = targetNode.mutableCollisionPut(key, value, mutator, updateFunction, ENTRY_SIZE);
                } else {
                    result = targetNode.mutablePut(key.hashCode(), key, value, shift + LOG_MAX_BRANCHING_FACTOR, mutator, updateFunction, ENTRY_SIZE);
                }
                if (mutator.size == oldSize) intersectionCounter.count++;
                return result;
            } else {
                return targetNode;
            }

        } else if (otherNode.hasNodeAt(positionMask)) {
            var otherTargetNode = otherNode.nodeAtIndex(otherNode.nodeIndex(positionMask));
            if (this.hasEntryAt(positionMask)) {
                // if otherTargetNode already has a value associated with the key, do not put this entry
                var keyIndex = this.entryKeyIndex(positionMask, ENTRY_SIZE);
                var key = this.keyAtIndex(keyIndex);
                boolean hasKey = (shift == MAX_SHIFT)
                        ? otherTargetNode.collisionContainsKey(key, ENTRY_SIZE)
                        : otherTargetNode.containsKey(key.hashCode(), key, shift + LOG_MAX_BRANCHING_FACTOR, ENTRY_SIZE);
                if (hasKey) {
                    intersectionCounter.count++;
                    return otherTargetNode;
                } else {
                    Object[] value = this.entryAtKeyIndex(keyIndex, ENTRY_SIZE);
                    return (shift == MAX_SHIFT)
                            ? otherTargetNode.mutableCollisionPut(key, value, mutator, updateFunction, ENTRY_SIZE)
                            : otherTargetNode.mutablePut(
                            key.hashCode(), key, value,
                            shift + LOG_MAX_BRANCHING_FACTOR, mutator,
                            updateFunction, ENTRY_SIZE);
                }
            } else return otherTargetNode;
        } else { // two entries, and they are not equal by key. See (**) in mutablePutAll
            var thisKeyIndex = this.entryKeyIndex(positionMask, ENTRY_SIZE);
            var thisKey = this.keyAtIndex(thisKeyIndex);
            Object thisValue = this.entryAtKeyIndex(thisKeyIndex, ENTRY_SIZE);
            var otherKeyIndex = otherNode.entryKeyIndex(positionMask, ENTRY_SIZE);
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
                    mutator.ownership,
                    ENTRY_SIZE);
        }
    }

    /// int newSize = size + mutator.size;
    public TrieNode<K> mutableRemove(int keyHash, K key, int shift, TrieBuilder<K, Object> mutator, int ENTRY_SIZE) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask, ENTRY_SIZE);

            if (Objects.equals(key, keyAtIndex(keyIndex))) {
                return mutableRemoveEntryAtIndex(keyIndex, keyPositionMask, mutator, ENTRY_SIZE);
            }
            return this;
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var nodeIndex = nodeIndex(keyPositionMask);

            var targetNode = nodeAtIndex(nodeIndex);
            var newNode = (shift == MAX_SHIFT) ?
                    targetNode.mutableCollisionRemove(key, mutator, ENTRY_SIZE)
                    :
                    targetNode.mutableRemove(keyHash, key, shift + LOG_MAX_BRANCHING_FACTOR, mutator, ENTRY_SIZE);
            return mutableReplaceNode(targetNode, newNode, nodeIndex, keyPositionMask, mutator.ownership, ENTRY_SIZE);
        }

        // key is absent
        return this;
    }

    TrieNode<K> mutableRemove(
            int keyHash,
            K key,
            Object value,
            int shift,
            TrieBuilder<K, Object> mutator,
            int ENTRY_SIZE) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask, ENTRY_SIZE);

            if (Objects.equals(key, keyAtIndex(keyIndex)) && Objects.equals(value, valueAtKeyIndex(keyIndex))) {
                return mutableRemoveEntryAtIndex(keyIndex, keyPositionMask, mutator, ENTRY_SIZE);
            }
            return this;
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var nodeIndex = nodeIndex(keyPositionMask);

            var targetNode = nodeAtIndex(nodeIndex);
            var newNode = (shift == MAX_SHIFT) ?
                    targetNode.mutableCollisionRemove(key, value, mutator, ENTRY_SIZE)
                    :
                    targetNode.mutableRemove(keyHash, key, value, shift + LOG_MAX_BRANCHING_FACTOR, mutator, ENTRY_SIZE);
            return mutableReplaceNode(targetNode, newNode, nodeIndex, keyPositionMask, mutator.ownership, ENTRY_SIZE);
        }

        // key is absent
        return this;
    }

    private TrieNode<K> mutableRemoveEntryAtIndex(
            int keyIndex,
            int positionMask,
            TrieBuilder<K, Object> mutator,
            int ENTRY_SIZE) {
        assert hasEntryAt(positionMask);
        mutator.size--;
        mutator.value = valueAtKeyIndex(keyIndex);
        mutator.entry = new Object[ENTRY_SIZE];
        System.arraycopy(buffer, keyIndex, mutator.entry, 0, ENTRY_SIZE);
        if (buffer.length == ENTRY_SIZE) return empty();

        if (ownedBy == mutator.ownership) {
            buffer = removeEntryAtIndex(buffer, keyIndex, ENTRY_SIZE);
            dataMap = dataMap ^ positionMask;
            return this;
        }
        var newBuffer = removeEntryAtIndex(buffer, keyIndex, ENTRY_SIZE);
        return newTrieNode(dataMap ^ positionMask, nodeMap, newBuffer, mutator.ownership);
    }

    ///
    /// @param nodeIndex    node index
    /// @param positionMask position mask
    /// @param owner        owner
    /// @return updated node, the node can be empty!
    private TrieNode<K> mutableRemoveNodeAtIndex(
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
        return newTrieNode(dataMap, nodeMap ^ positionMask, newBuffer, owner);
    }

    private TrieNode<K> mutableReplaceNode(
            TrieNode<K> targetNode,
            TrieNode<K> newNode,
            int nodeIndex,
            int positionMask,
            MutabilityOwnership owner,
            int ENTRY_SIZE) {
        return (newNode.isEmpty()) ? mutableRemoveNodeAtIndex(nodeIndex, positionMask, owner)
                // `newNode` === `targetNode` means the child returned itself (a no-op, or an owned in-place removal),
                // so this node's buffer already points to it. Keep this node unchanged to avoid spuriously
                // clearing `PersistentHashMapBuilder.builtMap` on no-ops. The `hasSingleEntry` exclusion still routes
                // a child that shrank to one entry to `updateNodeAtIndex`, which promotes it.
                : (newNode == targetNode && !newNode.hasSingleEntry(ENTRY_SIZE)) ? this
                : updateNodeAtIndex(nodeIndex, positionMask, newNode, owner, ENTRY_SIZE);

    }

    private TrieNode<K> mutableUpdateEntryAtIndex(
            int keyIndex,
            Object[] entry,
            TrieBuilder<K, Object> mutator,
            int ENTRY_SIZE) {

        // If the [mutator] is exclusive owner of this node, update value at specified index in-place.
        if (ownedBy == mutator.ownership) {
            System.arraycopy(entry, 0, buffer, keyIndex, ENTRY_SIZE);
            return this;
        }
        // Structural change due to node replacement.
        mutator.modCount++;
        // Create new node with updated value at specified index.
        var newBuffer = buffer.clone();
        System.arraycopy(entry, 0, newBuffer, keyIndex, ENTRY_SIZE);
        return newTrieNode(dataMap, nodeMap, newBuffer, mutator.ownership);
    }

    @SuppressWarnings("unchecked")
    public Object noDataValue() {
        return (Object) NO_DATA;
    }

    /**
     * Retrieves the buffer element at the given [nodeIndex] as subtrie node.
     */
    @SuppressWarnings("unchecked")
    private TrieNode<K> nodeAtIndex(int nodeIndex) {
        return (TrieNode<K>) buffer[nodeIndex];
    }

    public int nodeCount() {
        return Integer.bitCount(nodeMap);
    }

    /**
     * Gets the index in buffer of the subtrie node entry corresponding to the position specified by [positionMask].
     */
    private int nodeIndex(int positionMask) {
        return buffer.length - 1 - Integer.bitCount(nodeMap & (positionMask - 1));
    }

    /// Returns null if an entry with the same key and value is already in the trie
    public @Nullable ModificationResult<K, Object> put(int keyHash, @Nullable K key, @Nullable Object value, int shift, int ENTRY_SIZE) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask, ENTRY_SIZE);
            if (Objects.equals(key, keyAtIndex(keyIndex))) {
                Object oldValue = valueAtKeyIndex(keyIndex);
                if (Objects.equals(oldValue, value)) return null;
                return updateValueAtIndex(keyIndex, value).asUpdateResult(oldValue);
            }
            return moveEntryToNode(keyIndex, keyPositionMask, keyHash, key, value, shift, ENTRY_SIZE).asInsertResult();
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var nodeIndex = nodeIndex(keyPositionMask);
            var targetNode = nodeAtIndex(nodeIndex);
            ModificationResult<K, Object> putResult;
            if (shift == MAX_SHIFT) {
                putResult = targetNode.collisionPut(key, value, ENTRY_SIZE);
                if (putResult == null) return null;
            } else {
                putResult = targetNode.put(keyHash, key, value, shift + LOG_MAX_BRANCHING_FACTOR, ENTRY_SIZE);
                if (putResult == null) return null;
            }
            return putResult.replaceNode(node -> updateNodeAtIndex(nodeIndex, keyPositionMask, node, null, ENTRY_SIZE));
        }

        // no entry at this key hash segment
        return insertEntryAt(keyPositionMask, key, value, ENTRY_SIZE).asInsertResult();
    }

    public TrieNode<K> remove(int keyHash, K key, int shift, int ENTRY_SIZE) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask, ENTRY_SIZE);

            if (Objects.equals(key, keyAtIndex(keyIndex))) {
                return removeEntryAtIndex(keyIndex, keyPositionMask, ENTRY_SIZE);
            }
            return this;
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var nodeIndex = nodeIndex(keyPositionMask);

            var targetNode = nodeAtIndex(nodeIndex);
            var newNode = (shift == MAX_SHIFT) ?
                    targetNode.collisionRemove(key, ENTRY_SIZE)
                    :
                    targetNode.remove(keyHash, key, shift + LOG_MAX_BRANCHING_FACTOR, ENTRY_SIZE);
            return replaceNode(targetNode, newNode, nodeIndex, keyPositionMask, ENTRY_SIZE);
        }

        // key is absent
        return this;
    }

    TrieNode<K> remove(int keyHash, K key, Object value, int shift, int ENTRY_SIZE) {
        var keyPositionMask = 1 << indexSegment(keyHash, shift);

        if (hasEntryAt(keyPositionMask)) { // key is directly in buffer
            var keyIndex = entryKeyIndex(keyPositionMask, ENTRY_SIZE);

            if (Objects.equals(key, keyAtIndex(keyIndex)) && Objects.equals(value, valueAtKeyIndex(keyIndex))) {
                return removeEntryAtIndex(keyIndex, keyPositionMask, ENTRY_SIZE);
            }
            return this;
        }
        if (hasNodeAt(keyPositionMask)) { // key is in node
            var nodeIndex = nodeIndex(keyPositionMask);

            var targetNode = nodeAtIndex(nodeIndex);
            var newNode = (shift == MAX_SHIFT) ?
                    targetNode.collisionRemove(key, value, ENTRY_SIZE)
                    :
                    targetNode.remove(keyHash, key, value, shift + LOG_MAX_BRANCHING_FACTOR, ENTRY_SIZE);
            return replaceNode(targetNode, newNode, nodeIndex, keyPositionMask, ENTRY_SIZE);
        }

        // key is absent
        return this;
    }

    private TrieNode<K> removeEntryAtIndex(int keyIndex, int positionMask, int ENTRY_SIZE) {
        assert hasEntryAt(positionMask);
        if (buffer.length == ENTRY_SIZE) return empty();
        var newBuffer = removeEntryAtIndex(buffer, keyIndex, ENTRY_SIZE);
        return newTrieNode(dataMap ^ positionMask, nodeMap, newBuffer, null);
    }

    private TrieNode<K> removeNodeAtIndex(int nodeIndex, int positionMask) {
        assert hasNodeAt(positionMask);
        if (buffer.length == 1) return empty();

        var newBuffer = removeNodeAtIndex(buffer, nodeIndex);
        return newTrieNode(dataMap, nodeMap ^ positionMask, newBuffer, null);
    }

    private TrieNode<K> replaceNode(TrieNode<K> targetNode, TrieNode<K> newNode, int nodeIndex, int positionMask, int ENTRY_SIZE) {
        return (newNode.isEmpty()) ? removeNodeAtIndex(nodeIndex, positionMask)
                : (targetNode != newNode) ? updateNodeAtIndex(nodeIndex, positionMask, newNode, null, ENTRY_SIZE)
                : this;
    }

    /**
     * The given [newNode] must not be a part of any persistent map instance.
     */
    private TrieNode<K> updateNodeAtIndex(
            int nodeIndex,
            int positionMask,
            TrieNode<K> newNode,
            @Nullable MutabilityOwnership owner,
            int ENTRY_SIZE) {
        if (newNode.hasSingleEntry(ENTRY_SIZE)) {
            if (buffer.length == 1) {
                assert dataMap == 0 && (nodeMap ^ positionMask) == 0;
                newNode.dataMap = nodeMap;
                return newNode;
            }

            var keyIndex = entryKeyIndex(positionMask, ENTRY_SIZE);
            var newBuffer = replaceNodeWithEntry(buffer, nodeIndex, keyIndex, newNode.buffer, ENTRY_SIZE);
            return newTrieNode(dataMap ^ positionMask, nodeMap ^ positionMask, newBuffer, owner);
        }

        if (owner != null && ownedBy == owner) {
            buffer[nodeIndex] = newNode;
            return this;
        }

        var newBuffer = buffer.clone();
        newBuffer[nodeIndex] = newNode;
        return newTrieNode(dataMap, nodeMap, newBuffer, owner);
    }

    private TrieNode<K> updateValueAtIndex(int keyIndex, Object value) {
        assert buffer[keyIndex + 1] != value;
        var newBuffer = buffer.clone();
        newBuffer[keyIndex + 1] = value;
        return newTrieNode(dataMap, nodeMap, newBuffer, null);
    }

    /**
     * Retrieves the buffer element next to the given [keyIndex] as value of a data entry.
     */
    @SuppressWarnings("unchecked")
    private <V> V valueAtKeyIndex(int keyIndex) {
        return (V) buffer[keyIndex + 1];
    }

    private Object[] entryAtKeyIndex(int keyIndex, int ENTRY_SIZE) {
        return Arrays.copyOfRange(buffer, keyIndex, keyIndex + ENTRY_SIZE);
    }

    private <V> V valueAtKeyIndex(int keyIndex, int valueIndex) {
        return (V) buffer[keyIndex + valueIndex];
    }


}
