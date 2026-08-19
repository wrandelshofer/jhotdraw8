package org.jhotdraw8.icollection.impl.champset;

import org.jhotdraw8.icollection.impl.ArrayHelper;
import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;


/// This code has been derived from
/// [kotlix.collections.immutable, TrieNode.kt](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/core/commonMain/src/implementations/immutableSet/TrieNode.kt),
/// JetBrains s.r.o.
/// [Apache License 2.0](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/LICENSE.txt)
public class TrieNode<E> {
    int bitmap;
    public Object[] buffer;
    private @Nullable IdentityObject ownedBy;
    static final int MAX_BRANCHING_FACTOR = 32;
    public static final int LOG_MAX_BRANCHING_FACTOR = 5;
    private static final int MAX_BRANCHING_FACTOR_MINUS_ONE = MAX_BRANCHING_FACTOR - 1;
    private static final int MAX_SHIFT = 30;
    public static TrieNode<Object> EMPTY = new TrieNode<Object>(0, new Object[0], null);

    public static <T> TrieNode<T> empty() {
        //noinspection unchecked
        return (TrieNode<T>) EMPTY;
    }

    public TrieNode(int bitmap, Object[] buffer, @Nullable IdentityObject ownedBy) {
        this.bitmap = bitmap;
        this.buffer = buffer;
        this.ownedBy = ownedBy;
    }

    private static int indexSegment(int index, int shift) {
        return (index >> shift) & MAX_BRANCHING_FACTOR_MINUS_ONE;
    }

    private boolean hasNoCellAt(int positionMask) {
        return (bitmap & positionMask) == 0;
    }

    /// Computes (nodeMask << 32) | dataMask
    public long computeNodeAndDataMask(int shift) {
        if (shift == MAX_SHIFT) {
            return 0xffffffffL;
        }
        int nodeMask = 0;
        int dataMask = 0;
        for (ForEachOneBit iter = new ForEachOneBit(bitmap); iter.moveNext(); ) {
            int positionMask = iter.getPositionMask();
            int index = iter.getIndex();
            if (buffer[index] instanceof TrieNode) {
                nodeMask |= positionMask;
            } else {
                dataMask |= positionMask;
            }
        }
        return ((long) nodeMask << 32) | dataMask;
    }

    private int indexOfCellAt(int positionMask) {
        return Integer.bitCount(bitmap & (positionMask - 1));
    }

    @SuppressWarnings("unchecked")
    E elementAtIndex(int index) {
        return (E) buffer[index];
    }

    @SuppressWarnings("unchecked")
    TrieNode<E> nodeAtIndex(int index) {
        return (TrieNode<E>) buffer[index];
    }

    private TrieNode<E> addElementAt(int positionMask, E element, @Nullable IdentityObject owner) {
        assert hasNoCellAt(positionMask);

        var index = indexOfCellAt(positionMask);
        var newBitmap = bitmap | positionMask;
        var newBuffer = ArrayHelper.copyAdd(buffer, index, element);
        return setProperties(newBitmap, newBuffer, owner);
    }


    private TrieNode<E> setProperties(int newBitmap, Object[] newBuffer, @Nullable IdentityObject owner) {
        if (ownedBy != null && ownedBy == owner) {
            bitmap = newBitmap;
            buffer = newBuffer;
            return this;
        }
        return new TrieNode<>(newBitmap, newBuffer, owner);
    }

    private TrieNode<E> setCellAtIndex(int cellIndex, Object newCell, @Nullable IdentityObject owner) {
        if (ownedBy != null && ownedBy == owner) {
            buffer[cellIndex] = newCell;
            return this;
        }
        var newBuffer = buffer.clone();
        newBuffer[cellIndex] = newCell;
        return new TrieNode<>(bitmap, newBuffer, owner);
    }

    public TrieNode<E> add(int elementHash, E element, int shift) {
        var cellPositionMask = 1 << indexSegment(elementHash, shift);

        if (hasNoCellAt(cellPositionMask)) { // element is absent
            return addElementAt(cellPositionMask, element, null);
        }

        var cellIndex = indexOfCellAt(cellPositionMask);
        if (buffer[cellIndex] instanceof TrieNode<?>) { // element may be in node
            var targetNode = nodeAtIndex(cellIndex);
            var newNode = (shift == MAX_SHIFT) ?
                    targetNode.collisionAdd(element)
                    :
                    targetNode.add(elementHash, element, shift + LOG_MAX_BRANCHING_FACTOR);
            if (targetNode == newNode) return this;
            return setCellAtIndex(cellIndex, newNode, null);
        }
        // element is directly in buffer
        if (Objects.equals(element, buffer[cellIndex])) return this;
        return moveElementToNode(cellIndex, elementHash, element, shift, null);
    }

    private TrieNode<E> moveElementToNode(
            int elementIndex,
            int newElementHash, E newElement,
            int shift, @Nullable IdentityObject owner) {
        var node = makeNodeAtIndex(elementIndex, newElementHash, newElement, shift, owner);
        return setCellAtIndex(elementIndex, node, owner);
    }

    private TrieNode<E> makeNodeAtIndex(
            int elementIndex, int newElementHash, E newElement,
            int shift, @Nullable IdentityObject owner) {
        var storedElement = elementAtIndex(elementIndex);
        return makeNode(
                storedElement.hashCode(), storedElement,
                newElementHash, newElement, shift + LOG_MAX_BRANCHING_FACTOR, owner
        );
    }

    private TrieNode<E> makeNode(
            int elementHash1, E element1,
            int elementHash2, E element2,
            int shift, @Nullable IdentityObject owner
    ) {
        if (shift > MAX_SHIFT) {
            assert element1 != element2;
            // when two element hashes are entirely equal: the last level subtrie node stores them just as unordered list
            return new TrieNode<E>(0, new Object[]{element1, element2}, owner);
        }

        var setBit1 = indexSegment(elementHash1, shift);
        var setBit2 = indexSegment(elementHash2, shift);

        if (setBit1 != setBit2) {
            var nodeBuffer = (setBit1 < setBit2) ? new Object[]{element1, element2} : new Object[]{element2, element1};
            return new TrieNode<>((1 << setBit1) | (1 << setBit2), nodeBuffer, owner);
        }
        // hash segments at the given shift are equal: move these elements into the subtrie
        var node = makeNode(elementHash1, element1, elementHash2, element2, shift + LOG_MAX_BRANCHING_FACTOR, owner);
        return new TrieNode<E>(1 << setBit1, new Object[]{node}, owner);
    }

    public TrieNode<E> mutableAdd(int elementHash, E element, int shift, TrieBuilder<?> mutator) {
        var cellPosition = 1 << indexSegment(elementHash, shift);

        if (hasNoCellAt(cellPosition)) { // element is absent
            mutator.size++;
            return addElementAt(cellPosition, element, mutator.ownership);
        }

        var cellIndex = indexOfCellAt(cellPosition);
        if (buffer[cellIndex] instanceof TrieNode<?>) { // element may be in node
            var targetNode = nodeAtIndex(cellIndex);
            var newNode = (shift == MAX_SHIFT)
                    ? targetNode.mutableCollisionAdd(element, mutator)
                    : targetNode.mutableAdd(elementHash, element, shift + LOG_MAX_BRANCHING_FACTOR, mutator);
            if (targetNode == newNode) return this;
            return setCellAtIndex(cellIndex, newNode, mutator.ownership);
        }
        // element is directly in buffer
        if (Objects.equals(element, buffer[cellIndex])) return this;
        mutator.size++;
        return moveElementToNode(cellIndex, elementHash, element, shift, mutator.ownership);
    }

    public TrieNode<E> remove(int elementHash, E element, int shift) {
        var cellPositionMask = 1 << indexSegment(elementHash, shift);

        if (hasNoCellAt(cellPositionMask)) { // element is absent
            return this;
        }

        var cellIndex = indexOfCellAt(cellPositionMask);
        if (buffer[cellIndex] instanceof TrieNode<?>) { // element may be in node
            var targetNode = nodeAtIndex(cellIndex);
            var newNode = (shift == MAX_SHIFT)
                    ? targetNode.collisionRemove(element)
                    : targetNode.remove(elementHash, element, shift + LOG_MAX_BRANCHING_FACTOR);
            if (targetNode == newNode) return this;
            return canonicalizeNodeAtIndex(cellIndex, newNode, null);
        }
        // element is directly in buffer
        if (Objects.equals(element, buffer[cellIndex])) {
            assert shift == 0 || buffer.length > 1;
            return removeCellAtIndex(cellIndex, cellPositionMask, null);
        }
        return this;
    }

    private TrieNode<E> canonicalizeNodeAtIndex(
            int nodeIndex,
            TrieNode<E> newNode,
            @Nullable IdentityObject owner
    ) {
        Object cell;

        var newNodeBuffer = newNode.buffer;
        if (newNodeBuffer.length == 1 && !(newNodeBuffer[0] instanceof TrieNode<?>)) {
            if (buffer.length == 1) {
                newNode.bitmap = bitmap;
                return newNode;
            }
            cell = newNodeBuffer[0];
        } else {
            cell = newNode;
        }

        return setCellAtIndex(nodeIndex, cell, owner);
    }

    private TrieNode<E> removeCellAtIndex(int cellIndex, int positionMask, @Nullable IdentityObject owner) {
        assert !hasNoCellAt(positionMask);

        var newBitmap = bitmap ^ positionMask;
        var newBuffer = ArrayHelper.copyRemove(buffer, cellIndex);
        return setProperties(newBitmap, newBuffer, owner);
    }

    public TrieNode<E> mutableRemove(int elementHash, E element, int shift, TrieBuilder<?> mutator) {
        var cellPositionMask = 1 << indexSegment(elementHash, shift);

        if (hasNoCellAt(cellPositionMask)) { // element is absent
            return this;
        }

        var cellIndex = indexOfCellAt(cellPositionMask);
        if (buffer[cellIndex] instanceof TrieNode<?>) { // element may be in node
            var targetNode = nodeAtIndex(cellIndex);
            var newNode = (shift == MAX_SHIFT)
                    ? targetNode.mutableCollisionRemove(element, mutator)
                    : targetNode.mutableRemove(elementHash, element, shift + LOG_MAX_BRANCHING_FACTOR, mutator);
            // If newNode is a single-element node, it is newly created, or targetNode is owned by mutator and a cell was removed in-place.
            // Otherwise, the single element would have been lifted up.
            // If targetNode is owned by mutator, this node is also owned by mutator.
            // Thus, no new node will be created to replace this node.
            // If newNode !== targetNode, it is newly created.
            if (targetNode.ownedBy != mutator.ownership && targetNode == newNode) return this;
            return canonicalizeNodeAtIndex(cellIndex, newNode, mutator.ownership);
        }
        // element is directly in buffer
        if (Objects.equals(element, buffer[cellIndex])) {
            assert shift == 0 || buffer.length > 1;
            mutator.size--;
            return removeCellAtIndex(cellIndex, cellPositionMask, mutator.ownership);// check is empty
        }
        return this;
    }

    public boolean contains(int elementHash, E element, int shift) {
        var cellPositionMask = 1 << indexSegment(elementHash, shift);

        if (hasNoCellAt(cellPositionMask)) { // element is absent
            return false;
        }

        var cellIndex = indexOfCellAt(cellPositionMask);
        if (buffer[cellIndex] instanceof TrieNode<?>) { // element may be in node
            var targetNode = nodeAtIndex(cellIndex);
            if (shift == MAX_SHIFT) {
                return targetNode.collisionContainsElement(element);
            }
            return targetNode.contains(elementHash, element, shift + LOG_MAX_BRANCHING_FACTOR);
        }
        // element is directly in buffer
        return Objects.equals(element, buffer[cellIndex]);
    }

    private boolean collisionContainsElement(E element) {
        return ArrayHelper.contains(buffer, element);
    }

    //
    // val newSize = mySet.size + otherSet.size - deltaCounter.count
    @SuppressWarnings({"unchecked", "RedundantCast"})
    public TrieNode<E> mutableAddAll(
            TrieNode<E> otherNode,
            int shift,
            DeltaCounter intersectionSizeRef,
            TrieBuilder<?> mutator
    ) {
        if (this == otherNode) {
            intersectionSizeRef.count += this.calculateSize();
            return this;
        }
        if (shift > MAX_SHIFT) {
            return mutableCollisionAddAll(otherNode, intersectionSizeRef, mutator.ownership);
        }
        // union mask contains all the bits from input masks
        var newBitMap = bitmap | otherNode.bitmap;
        // first allocate the node and then fill it in
        // we are doing a union, so all the array elements are guaranteed to exist
        var mutableNode = (newBitMap == bitmap && ownedBy == mutator.ownership)
                ? this
                : new TrieNode<E>(newBitMap, new Object[Integer.bitCount(newBitMap)], mutator.ownership);

        // for each bit set in the resulting mask,
        // either left, right or both masks contain the same bit
        // we Note shouldn't overrun MAX_SHIFT because both sides are correct TrieNodes, right?
        for (ForEachOneBit iter = new ForEachOneBit(newBitMap); iter.moveNext(); ) {
            int positionMask = iter.getPositionMask();
            int newNodeIndex = iter.getIndex();
            var thisIndex = indexOfCellAt(positionMask);
            var otherNodeIndex = otherNode.indexOfCellAt(positionMask);

            if (hasNoCellAt(positionMask)) {
                // no element on left -> pick right
                mutableNode.buffer[newNodeIndex] = otherNode.buffer[otherNodeIndex];
            } else if (otherNode.hasNoCellAt(positionMask)) {
                // no element on right -> pick left
                mutableNode.buffer[newNodeIndex] = buffer[thisIndex];
            } else {
                // both nodes contain something at the masked bit
                var thisCell = buffer[thisIndex];
                var otherNodeCell = otherNode.buffer[otherNodeIndex];
                var thisIsNode = thisCell instanceof TrieNode<?>;
                var otherIsNode = otherNodeCell instanceof TrieNode<?>;

                if (thisIsNode && otherIsNode) {
                    // both are nodes -> merge them recursively
                    mutableNode.buffer[newNodeIndex] = ((TrieNode<E>) thisCell).mutableAddAll(
                            (TrieNode<E>) otherNodeCell, shift + LOG_MAX_BRANCHING_FACTOR,
                            intersectionSizeRef, mutator
                    );
                } else if (thisIsNode) {
                    // one of them is a node -> add the other one to it
                    var oldSize = mutator.size;
                    mutableNode.buffer[newNodeIndex] = ((TrieNode<E>) thisCell).mutableAdd(
                            Objects.hashCode((E) otherNodeCell), (E) otherNodeCell,
                            shift + LOG_MAX_BRANCHING_FACTOR, mutator);

                    if (mutator.size == oldSize) intersectionSizeRef.count++;
                } else if (otherIsNode) {
                    // same as last case, but reversed
                    var oldSize = mutator.size;
                    mutableNode.buffer[newNodeIndex] = ((TrieNode<E>) otherNodeCell).mutableAdd(
                            Objects.hashCode(thisCell), (E) thisCell,
                            shift + LOG_MAX_BRANCHING_FACTOR, mutator);

                    if (mutator.size == oldSize) intersectionSizeRef.count++;
                } else if (thisCell == otherNodeCell) {
                    // both are just E => compare them
                    intersectionSizeRef.count++;
                    mutableNode.buffer[newNodeIndex] = thisCell;
                } else {

                    // both are just E, but different => make a collision-ish node

                    mutableNode.buffer[newNodeIndex] = makeNode(
                            thisCell.hashCode(), (E) thisCell,
                            otherNodeCell.hashCode(), (E) otherNodeCell,
                            shift + LOG_MAX_BRANCHING_FACTOR, mutator.ownership);
                }
            }

        }
        return (this.elementsIdentityEquals(mutableNode))
                ? this
                : (otherNode.elementsIdentityEquals(mutableNode))
                ? otherNode
                : mutableNode;
    }

    private int calculateSize() {
        if (bitmap == 0) return buffer.length;
        var result = 0;
        for (Object e : buffer) {
            result += (e instanceof TrieNode<?>)
                    ? ((TrieNode<?>) e).calculateSize()
                    : 1;
        }
        return result;
    }


    private boolean elementsIdentityEquals(TrieNode<E> otherNode) {
        if (this == otherNode) return true;
        if (bitmap != otherNode.bitmap) return false;
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] != otherNode.buffer[i]) return false;
        }
        return true;
    }

    private TrieNode<E> collisionAdd(E element) {
        if (collisionContainsElement(element)) return this;
        var newBuffer = ArrayHelper.copyAdd(buffer, 0, element);
        return setProperties(0, newBuffer, null);
    }

    private TrieNode<E> mutableCollisionAdd(E element, TrieBuilder<?> mutator) {
        if (collisionContainsElement(element)) return this;
        mutator.size++;
        var newBuffer = ArrayHelper.copyAdd(buffer, 0, element);
        return setProperties(0, newBuffer, mutator.ownership);
    }

    private TrieNode<E> collisionRemove(E element) {
        var index = ArrayHelper.indexOf(buffer, element);
        if (index != -1) {
            return collisionRemoveElementAtIndex(index, null);
        }
        return this;
    }

    private TrieNode<E> collisionRemoveElementAtIndex(int i, @Nullable IdentityObject owner) {
        var newBuffer = ArrayHelper.copyRemove(buffer, i);
        return setProperties(0, newBuffer, owner);
    }

    private TrieNode<E> mutableCollisionRemove(E element, TrieBuilder<?> mutator) {
        var index = ArrayHelper.indexOf(buffer, element);
        if (index != -1) {
            mutator.size--;
            return collisionRemoveElementAtIndex(index, mutator.ownership);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    private TrieNode<E> mutableCollisionAddAll(
            TrieNode<E> otherNode,
            DeltaCounter intersectionSizeRef,
            IdentityObject owner
    ) {
        if (this == otherNode) {
            intersectionSizeRef.count += buffer.length;
            return this;
        }
        var tempBuffer = Arrays.copyOf(this.buffer, this.buffer.length + otherNode.buffer.length);
        var totalWritten = filterTo(otherNode.buffer, tempBuffer, this.buffer.length,
                (it) -> !this.collisionContainsElement((E) it));
        var totalSize = totalWritten + this.buffer.length;
        intersectionSizeRef.count += (tempBuffer.length - totalSize);
        if (totalSize == this.buffer.length) return this;
        if (totalSize == otherNode.buffer.length) return otherNode;

        var newBuffer = (totalSize == tempBuffer.length) ? tempBuffer : Arrays.copyOf(tempBuffer, totalSize);
        return setProperties(0, newBuffer, owner);
    }

    /// Writes all elements from `thisArray` to `newArray`, starting with `newArrayOffset`, filtering
    /// on the fly using `predicate`. By default, filters out [TrieNode#EMPTY] instances
    /// return number of elements written to `newArray`
    private int filterTo(Object[] thisArray, Object[] newArray, int newArrayOffset, Predicate<Object> predicate) {
        var i = 0;
        var j = 0;
        while (i < thisArray.length) {
            assert j <= i; // this is extremely important if newArray === this
            var e = thisArray[i];
            if (predicate.test(e)) {
                newArray[newArrayOffset + j] = thisArray[i];
                ++j;
                assert newArrayOffset + j <= newArray.length;
            }
            ++i;
        }
        return j;
    }

    ///  newSize = deltaCounter.count
    @SuppressWarnings("unchecked")
    public Object mutableRetainAll(
            TrieNode<E> otherNode,
            int shift,
            DeltaCounter intersectionSizeRef,
            TrieBuilder<?> mutator) {
        if (this == otherNode) {
            intersectionSizeRef.count += calculateSize();
            return this;
        }
        if (shift > MAX_SHIFT) {
            return mutableCollisionRetainAll(otherNode, intersectionSizeRef, mutator.ownership);
        }
        // intersection mask contains bits that are set in both inputs
        // this mask is not final because some children may have no intersection
        var newBitMap = bitmap & otherNode.bitmap;
        // zero means no nodes intersect
        if (newBitMap == 0) return EMPTY;
        var mutableNode =
                (ownedBy == mutator.ownership && newBitMap == bitmap) ? this
                        : new TrieNode<E>(newBitMap, new Object[Integer.bitCount(newBitMap)], mutator.ownership);
        // we need to keep track of the real mask because some of the children may intersect to nothing
        var realBitMap = 0;
        // for each bit in intersection mask, try to intersect children
        for (ForEachOneBit it = new ForEachOneBit(newBitMap); it.moveNext(); ) {
            int positionMask = it.getPositionMask();
            int newNodeIndex = it.getIndex();
            var thisIndex = indexOfCellAt(positionMask);
            var otherNodeIndex = otherNode.indexOfCellAt(positionMask);
            Object newValue;
            {
                var thisCell = buffer[thisIndex];
                var otherNodeCell = otherNode.buffer[otherNodeIndex];
                var thisIsNode = thisCell instanceof TrieNode<?>;
                var otherIsNode = otherNodeCell instanceof TrieNode<?>;
                if (thisIsNode && otherIsNode) {
                    // both are nodes -> merge them recursively
                    newValue = ((TrieNode<E>) thisCell).mutableRetainAll(
                            ((TrieNode<E>) otherNodeCell), shift + LOG_MAX_BRANCHING_FACTOR,
                            intersectionSizeRef, mutator
                    );
                } else if (thisIsNode) {
                    // one of them is a node -> check containment
                    if (((TrieNode<E>) thisCell).contains(
                            Objects.hashCode(otherNodeCell), (E) otherNodeCell,
                            shift + LOG_MAX_BRANCHING_FACTOR
                    )) {
                        intersectionSizeRef.count += 1;
                        newValue = otherNodeCell;
                    } else newValue = EMPTY;
                } else if (otherIsNode) {
                    // same as last case, but reversed
                    if (((TrieNode<E>) otherNodeCell).contains(
                            Objects.hashCode(thisCell), (E) thisCell, shift + LOG_MAX_BRANCHING_FACTOR)) {
                        intersectionSizeRef.count += 1;
                        newValue = thisCell;
                    } else newValue = EMPTY;
                } else if (Objects.equals(thisCell, otherNodeCell)) {
                    // both are just E => compare them
                    newValue = thisCell;
                    intersectionSizeRef.count += 1;
                    // both are just E, but different => return nothing
                } else {
                    newValue = EMPTY;
                }
            }
            if (newValue != EMPTY) {
                // elements that are not in realBitMap will be removed later
                realBitMap = realBitMap | positionMask;
            }
            mutableNode.buffer[newNodeIndex] = newValue;
        }
        // resulting array's size is the popcount of resulting mask
        var realSize = Integer.bitCount(realBitMap);
        if (realBitMap == 0) return EMPTY;
        // single values are kept only on root level
        if (realSize == 1 && shift != 0) {
            var single = mutableNode.buffer[mutableNode.indexOfCellAt(realBitMap)];
            if (single instanceof TrieNode<?>)
                return new TrieNode<E>(realBitMap, new Object[]{single}, mutator.ownership);
            return single;
        }
        if (realBitMap == newBitMap) {

            if (mutableNode.elementsIdentityEquals(this)) return this;
            if (mutableNode.elementsIdentityEquals(otherNode)) return otherNode;
            return mutableNode;

        } else {
            // clean up all the EMPTYs in the resulting buffer
            var realBuffer = new Object[realSize];
            filterTo(mutableNode.buffer, realBuffer, 0, o -> o != EMPTY);
            return new TrieNode<E>(realBitMap, realBuffer, mutator.ownership);
        }
    }


    @SuppressWarnings("unchecked")
    private TrieNode<E> mutableCollisionRetainAll(
            TrieNode<E> otherNode,
            DeltaCounter intersectionSizeRef,
            IdentityObject owner) {
        if (this == otherNode) {
            intersectionSizeRef.count += buffer.length;
            return this;
        }
        var tempBuffer =
                (owner == ownedBy) ? buffer
                        : new Object[Math.min(buffer.length, otherNode.buffer.length)];
        var totalWritten = filterTo(buffer, tempBuffer, 0, it ->
                otherNode.collisionContainsElement((E) it));
        intersectionSizeRef.count += totalWritten;

        if (totalWritten == 0) return (TrieNode<E>) EMPTY;
        if (totalWritten == 1) return (TrieNode<E>) tempBuffer[0];
        if (totalWritten == this.buffer.length) return this;
        if (totalWritten == otherNode.buffer.length) return otherNode;
        if (totalWritten == tempBuffer.length) return setProperties(0, tempBuffer, owner);
        return setProperties(0, Arrays.copyOf(tempBuffer, totalWritten), owner);
    }

    /// newSize = mySet.size - deltaCounter.count
    @SuppressWarnings("unchecked")
    public Object mutableRemoveAll(
            TrieNode<E> otherNode,
            int shift,
            DeltaCounter intersectionSizeRef,
            TrieBuilder<?> mutator) {
        if (this == otherNode) {
            intersectionSizeRef.count += calculateSize();
            return empty();
        }
        if (shift > MAX_SHIFT) {
            return mutableCollisionRemoveAll(otherNode, intersectionSizeRef, mutator.ownership);
        }
        // same as with intersection, only children of both nodes are considered
        // this mask is not final because some children may have no intersection
        var removalBitmap = bitmap & otherNode.bitmap;
        // zero means no intersection => nothing to remove
        if (removalBitmap == 0) return this;
        // node here is either us (if we are mutable) or a mutable copy
        TrieNode<E> mutableNode = (ownedBy == mutator.ownership) ? this
                : new TrieNode<>(bitmap, buffer.clone(), mutator.ownership);
        // keep track of the real mask
        var realBitMap = bitmap;
        for (ForEachOneBit it = new ForEachOneBit(removalBitmap); it.moveNext(); ) {
            int positionMask = it.getPositionMask();
            var thisIndex = indexOfCellAt(positionMask);
            var otherNodeIndex = otherNode.indexOfCellAt(positionMask);
            Object newValue;
            {
                var thisCell = buffer[thisIndex];
                var otherNodeCell = otherNode.buffer[otherNodeIndex];
                var thisIsNode = thisCell instanceof TrieNode<?>;
                var otherIsNode = otherNodeCell instanceof TrieNode<?>;
                if (thisIsNode && otherIsNode) {
                    // both are nodes -> merge them recursively
                    newValue = ((TrieNode<E>) thisCell).mutableRemoveAll(
                            ((TrieNode<E>) otherNodeCell), shift + LOG_MAX_BRANCHING_FACTOR,
                            intersectionSizeRef, mutator
                    );
                } else if (thisIsNode) {
                    // one of them is a node -> remove single element
                    var oldSize = mutator.size;
                    var removed = ((TrieNode<E>) thisCell).mutableRemove(
                            Objects.hashCode(otherNodeCell), (E) otherNodeCell,
                            shift + LOG_MAX_BRANCHING_FACTOR, mutator
                    );
                    // additional check needed for removal
                    if (oldSize != mutator.size) {
                        intersectionSizeRef.count += 1;
                        if (removed.buffer.length == 1 && !(removed.buffer[0] instanceof TrieNode<?>))
                            newValue = removed.buffer[0];
                        else newValue = removed;
                    } else newValue = thisCell;
                } else if (otherIsNode) {
                    // same as last case, but reversed
                    // "removing" a node from a value is basically checking if the value is contained in the node
                    if (((TrieNode<E>) otherNodeCell).contains(Objects.hashCode(thisCell), (E) thisCell, shift + LOG_MAX_BRANCHING_FACTOR)) {
                        intersectionSizeRef.count += 1;
                        newValue = EMPTY;
                    } else newValue = thisCell;
                } else if (Objects.equals(thisCell, otherNodeCell)) {
                    // both are just E => compare them
                    intersectionSizeRef.count += 1;
                    newValue = EMPTY;
                } else {
                    // both are just E, but different => nothing to remove, return left
                    newValue = thisCell;
                }
            }
            if (newValue == EMPTY) {
                // if we removed something, keep track
                realBitMap = realBitMap ^ positionMask;
            }
            mutableNode.buffer[thisIndex] = newValue;
        }
        // resulting size is popcount of the resulting mask
        var realSize = Integer.bitCount(realBitMap);

        if (realBitMap == 0) return empty();
        if (realSize == 1 && shift != 0) {
            // single values are kept only on root level
            var single = mutableNode.buffer[mutableNode.indexOfCellAt(realBitMap)];
            if (single instanceof TrieNode<?>)
                return new TrieNode<E>(realBitMap, new Object[]{single}, mutator.ownership);
            return single;
        }

        if (realBitMap == bitmap) {
            if (mutableNode.elementsIdentityEquals(this)) return this;
            return mutableNode;
        }


        // clean up all the EMPTYs in the resulting buffer
        var realBuffer = new Object[realSize];
        filterTo(mutableNode.buffer, realBuffer, 0, it -> it != EMPTY);
        return new TrieNode<E>(realBitMap, realBuffer, mutator.ownership);

    }

    @SuppressWarnings("unchecked")
    private Object mutableCollisionRemoveAll(
            TrieNode<E> otherNode,
            DeltaCounter intersectionSizeRef,
            IdentityObject owner
    ) {
        if (this == otherNode) {
            intersectionSizeRef.count += buffer.length;
            return EMPTY;
        }
        var tempBuffer = (owner == ownedBy) ? buffer : new Object[buffer.length];
        var totalWritten = filterTo(buffer, tempBuffer, 0, it ->
                !otherNode.collisionContainsElement((E) it));

        intersectionSizeRef.count += (buffer.length - totalWritten);

        if (totalWritten == 0) return EMPTY;
        if (totalWritten == 1) return tempBuffer[0];
        if (totalWritten == this.buffer.length) return this;
        if (totalWritten == tempBuffer.length) return setProperties(0, tempBuffer, owner);
        return setProperties(0, Arrays.copyOf(tempBuffer, totalWritten), owner);
    }


    @SuppressWarnings("unchecked")
    public boolean containsAll(TrieNode<E> otherNode, int shift) {
        if (this == otherNode) return true;
        if (shift > MAX_SHIFT) {
            // essentially `buffer.containsAll(otherNode.buffer)`
            for (var e : otherNode.buffer) {
                if (!ArrayHelper.contains(buffer, e)) return false;
            }
            return true;
        }

        // potential bitmap is an intersection of input bitmaps
        var potentialBitMap = bitmap & otherNode.bitmap;
        // left bitmap must contain right bitmap => right bitmap must be equal to intersection
        if (potentialBitMap != otherNode.bitmap) return false;
        // check each child, shortcut to false if any one isn't contained
        for (ForEachOneBit it = new ForEachOneBit(potentialBitMap); it.moveNext(); ) {
            int positionMask = it.getPositionMask();
            var thisIndex = indexOfCellAt(positionMask);
            var otherNodeIndex = otherNode.indexOfCellAt(positionMask);
            var thisCell = buffer[thisIndex];
            var otherNodeCell = otherNode.buffer[otherNodeIndex];
            var thisIsNode = thisCell instanceof TrieNode<?>;
            var otherIsNode = otherNodeCell instanceof TrieNode<?>;
            if (thisIsNode && otherIsNode) {
                // both are nodes => check recursively
                if (!((TrieNode<E>) thisCell).containsAll(((TrieNode<E>) otherNodeCell), shift + LOG_MAX_BRANCHING_FACTOR))
                    return false;
            } else if (thisIsNode) {
                // left is node, right is just E => check containment
                if (!((TrieNode<E>) thisCell).contains(
                        Objects.hashCode(otherNodeCell), (E) otherNodeCell,
                        shift + LOG_MAX_BRANCHING_FACTOR
                )) return false;
            } else if (otherIsNode) {
                // left is just E, right is node => not possible
                return false;
            } else {
                // both are just E => containment is just equality
                if (!Objects.equals(thisCell, otherNodeCell)) return false;

            }
        }
        return true;
    }
}
