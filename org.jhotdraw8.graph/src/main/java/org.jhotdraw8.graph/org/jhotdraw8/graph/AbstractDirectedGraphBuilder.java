/*
 * @(#)AbstractDirectedGraphBuilder.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.enumerator.AbstractIntEnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.IntEnumeratorSpliterator;

import java.util.Arrays;

import static java.lang.Math.max;

/**
 * AbstractDirectedGraphBuilder.
 * <p>
 * <b>Implementation:</b>
 * <p>
 * Example graph:
 * <pre>
 *     0 ──→ 1 ──→ 2
 *     │     │
 *     ↓     ↓
 *     3 ←── 4
 * </pre>
 * If the graph is inserted in the following sequence
 * into the builder:
 * <pre>
 *     buildAddVertex();
 *     buildAddVertex();
 *     buildAddVertex();
 *     buildAddVertex();
 *     buildAddVertex();
 *     buildAddVertex();
 *     build.addArrow(0, 1);
 *     build.addArrow(0, 3);
 *     build.addArrow(1, 2);
 *     build.addArrow(1, 4);
 *     build.addArrow(4, 3);
 * </pre>
 * Then the internal representation is as follows:
 * <ul>
 *     <li>For each vertex, there is an entry in table {@code lastArrows}.</li>
 *     <li>For each arrow, there is an entry in table {@code arrowHeads}.</li>
 *     <li>{@code arrowHeads} is a linked list. The linked list is ordered
 *     from the last arrow to the first. So we have to read it backwards!</li>
 *     <li>Each entry in {@code lastArrows} contains two fields:
 *     <ol>
 *         <li>A pointer to an entry in {@code arrowHeads}.</li>
 *         <li>The arrow count for this vertex.</li>
 *     </ol>
 *     <li>Each entry in {@code arrowHeads} contains two fields:
 *     <ol>
 *         <li>A vertex.
 *         <br>A tombstone={@value #TOMBSTONE} marks a deleted arrow head.</li>
 *         <li>A pointer to the next entry in {@code arrowHeads}.
 *         <br>A sentinel={@value #SENTINEL} marks the end of a linked list.</li>
 *     </ol>
 * </ul>
 * <pre>
 * vertexCount: 5
 * arrowCountInclusiveDeleted: 5
 * deletedArrowCount: 0
 * lastDeletedArrow: SENT
 *
 *  vertex#    lastArrow             arrow#    arrowHeads
 *           pointer,count                    vertex, next
 *    0     [  1  ][  2  ] ─────┐       0    [  1  ][SENT ] ←┐
 *    1     [  2  ][  2  ] ───┐ └─────→ 1    [  3  ][  0  ] ─┘
 *    2     [  0  ][  0  ] X  │         2    [  2  ][SENT ] ←┐
 *    3     [  0  ][  0  ] X  └───────→ 3    [  4  ][  2  ] ─┘
 *    4     [  4  ][  1  ] ───────────→ 4    [  3  ][SENT ] X
 * </pre>
 * If the arrow 1 → 3 is deleted, it is removed from the linked
 * list of vertex 1. The arrow head is marked with a tombstone.
 * <pre>
 * vertexCount: 5
 * arrowCountInclusiveDeleted: 5
 * deletedArrowCount: 1
 * lastDeletedArrow: 1
 *
 *  vertex#    lastArrow             arrow#    arrowHeads
 *           pointer,count                    vertex, next
 *    0     [  1  ][  2  ] ───────────→ 0    [  1  ][SENT ]
 *    1     [  2  ][  2  ] ───┐         1    [TOMB ][SENT ]
 *    2     [  0  ][  0  ] X  │         2    [  2  ][SENT ] ←┐
 *    3     [  0  ][  0  ] X  └───────→ 3    [  4  ][  2 ] ─┘
 *    4     [  4  ][  1  ] ───────────→ 4    [  3  ][SENT ] X
 * </pre>
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractDirectedGraphBuilder implements IndexedDirectedGraph {

    protected static final int ARROWS_NEXT_FIELD = 1;
    protected static final int ARROWS_NUM_FIELDS = 2;
    protected static final int ARROWS_VERTEX_FIELD = 0;
    protected static final int LASTARROW_COUNT_FIELD = 0;
    protected static final int LASTARROW_NUM_FIELDS = 2;
    protected static final int LASTARROW_POINTER_FIELD = 1;
    protected static final int SENTINEL = -1;
    private static final int TOMBSTONE = -2;

    /**
     * This is a linked list of deleted arrowHeads.
     * The pointer points to the first deleted element in arrowHeads.
     */
    private int pointerToLastDeletedArrow = -1;
    /**
     * The number of deleted arrowHeads.
     */
    private int deletedArrowCount;
    /**
     * The number of used arrowHeads.
     */
    protected int arrowCountIncludingDeletedArrows;
    /**
     * Table of arrow heads.
     * <p>
     * {@code arrows[i * ARROWS_NUM_FIELDS+ARROWS_VERTEX_FIELD} contains the
     * index of the vertex of the i-th arrow.
     * <p>
     * {@code arrows[i * ARROWS_NUM_FIELDS+ARROWS_NEXT_FIELD} contains the index
     * of the next arrow.
     */
    private int[] nextArrowHeads;

    /**
     * Table of last arrows.
     * <p>
     * {@code lastArrow[i * ARROWS_NUM_FIELDS+LASTARROW_POINTER_FIELD} contains
     * the index of the last arrow of the i-th vertex in table {@link #nextArrowHeads}.
     * <p>
     * {@code lastArrow[i * ARROWS_NUM_FIELDS+LASTARROW_COUNT_FIELD} contains
     * the number of arrows of the i-th vertex.
     */
    private int[] nextLastArrow;

    /**
     * The vertex count.
     */
    private int vertexCount;

    public AbstractDirectedGraphBuilder() {
        this(16, 16);
    }

    public AbstractDirectedGraphBuilder(int vertexCapacity, int arrowCapacity) {
        if (vertexCapacity < 0) {
            throw new IllegalArgumentException("vertexCapacity: " + vertexCapacity);
        }
        if (arrowCapacity < 0) {
            throw new IllegalArgumentException("arrowCapacity: " + arrowCapacity);
        }
        this.nextArrowHeads = new int[arrowCapacity * ARROWS_NUM_FIELDS];
        this.nextLastArrow = new int[vertexCapacity * LASTARROW_NUM_FIELDS];
    }

    /**
     * Builder-method: adds a directed arrow from 'a' to 'b'.
     *
     * @param a vertex a
     * @param b vertex b
     * @return index of the arrow
     */
    protected int buildAddArrow(int a, int b) {
        if (nextArrowHeads.length <= arrowCountIncludingDeletedArrows * ARROWS_NUM_FIELDS) {
            nextArrowHeads = Arrays.copyOf(nextArrowHeads, max(1, arrowCountIncludingDeletedArrows) * ARROWS_NUM_FIELDS * 2);
        }
        return doAddArrow(a, b, nextArrowHeads, nextLastArrow);
    }

    /**
     * Builder-method: adds a directed arrow from 'a' to 'b'.
     *
     * @param a          vertex a
     * @param b          vertex b
     * @param lastArrow  the array of last arrows
     * @param arrowHeads the array of arrow heads
     */
    protected int doAddArrow(int a, int b, int @NonNull [] arrowHeads, int @NonNull [] lastArrow) {
        int arrowCountOfA = lastArrow_getCount(lastArrow, a);
        int lastArrowPointer = arrowCountOfA == 0 ? SENTINEL : lastArrow[a * LASTARROW_NUM_FIELDS + LASTARROW_POINTER_FIELD];

        final int newArrowPointer;
        if (deletedArrowCount > 0) {
            newArrowPointer = pointerToLastDeletedArrow;
            pointerToLastDeletedArrow = arrowHead_getNext(arrowHeads, pointerToLastDeletedArrow);
            deletedArrowCount--;
        } else {
            newArrowPointer = arrowCountIncludingDeletedArrows;
            arrowCountIncludingDeletedArrows++;
        }

        arrowHead_setNext(arrowHeads, newArrowPointer, lastArrowPointer);
        arrowHead_setVertex(arrowHeads, newArrowPointer, b);
        lastArrow_setLast(lastArrow, a, newArrowPointer);
        lastArrow_setCount(lastArrow, a, arrowCountOfA + 1);
        return newArrowPointer;
    }

    private void arrowHead_setVertex(int[] arrowHeads, int pointerToArrow, int vidx) {
        arrowHeads[pointerToArrow * ARROWS_NUM_FIELDS + ARROWS_VERTEX_FIELD] = vidx;
    }

    private int arrowHead_getVertex(int[] arrowHeads, int pointerToArrow) {
        return arrowHeads[pointerToArrow * ARROWS_NUM_FIELDS + ARROWS_VERTEX_FIELD];
    }


    private int lastArrow_getCount(int[] lastArrow, int vidx) {
        return lastArrow[vidx * LASTARROW_NUM_FIELDS + LASTARROW_COUNT_FIELD];
    }

    private void lastArrow_setCount(int[] lastArrow, int vidx, int count) {
        lastArrow[vidx * LASTARROW_NUM_FIELDS + LASTARROW_COUNT_FIELD] = count;
    }

    /**
     * Builder-method: adds a vertex.
     */
    protected void buildAddVertex() {
        vertexCount++;
        if (nextLastArrow.length < vertexCount * LASTARROW_NUM_FIELDS) {
            nextLastArrow = Arrays.copyOf(nextLastArrow, vertexCount * LASTARROW_NUM_FIELDS * 2);
        }
    }

    @Override
    public int getArrowCount() {
        return arrowCountIncludingDeletedArrows - deletedArrowCount;
    }

    /**
     * Removes the i-th arrow of vertex vi.
     *
     * @param a a vertex
     * @param i the i-th arrow of vertex vi
     */
    protected int buildRemoveArrowAt(int a, int i) {
        return buildRemoveArrowAt(a, i, nextLastArrow, nextArrowHeads, arrowCountIncludingDeletedArrows);
    }

    /**
     * Removes the i-th arrow of vertex v.
     *
     * @param vidx       the index of the vertex v
     * @param i          the i-th arrow of vertex v
     * @param lastArrow  the array of last arrows
     * @param arrowHeads the array of arrow heads
     * @param arrowCount the number of arrows
     */
    protected int buildRemoveArrowAt(int vidx, int i, int @NonNull [] lastArrow, int @NonNull [] arrowHeads, int arrowCount) {
        if (vidx < 0 || vidx >= getVertexCount()) {
            throw new IllegalArgumentException("vidx:" + i);
        }
        int nextCount = getNextCount(vidx);
        if (i < 0 || i >= nextCount) {
            throw new IllegalArgumentException("i:" + i);
        }

        // find the arrow pointer and the previous arrow pointer
        int prevArrowPtr = SENTINEL;
        int arrowPtr = lastArrow_getLast(lastArrow, vidx);
        for (int j = nextCount - 1; j > i; j--) {
            prevArrowPtr = arrowPtr;
            arrowPtr = arrowHead_getNext(arrowHeads, arrowPtr);
        }

        // place tombstone
        arrowHead_setVertex(arrowHeads, arrowPtr, TOMBSTONE);

        if (prevArrowPtr == SENTINEL) {
            // if there is no previous arrowId => make the pointer from lastArrow point to the arrow after arrowId.
            lastArrow_setLast(lastArrow, vidx, arrowHead_getNext(arrowHeads, arrowPtr));
        } else {
            // if there is a previous arrowId => make the pointer from prevArrowId point to the arrow after arrowId.
            arrowHead_setNext(arrowHeads, prevArrowPtr, arrowHead_getNext(arrowHeads, arrowPtr));
        }
        // Decrease number of arrows for vertex vi
        lastArrow_setCount(lastArrow, vidx, lastArrow_getCount(lastArrow, vidx) - 1);

        // Add the deleted arrowHead to the list of deleted arrows.
        deletedArrowCount++;
        arrowHead_setNext(arrowHeads, arrowPtr, pointerToLastDeletedArrow);
        pointerToLastDeletedArrow = arrowPtr;

        return arrowPtr;
    }

    protected void buildRemoveVertex(int vidx) {
        // Remove all outgoing arrows
        for (int i = getNextCount(vidx) - 1; i >= 0; i--) {
            buildRemoveArrowAt(vidx, i);
        }
        buildRemoveVertexAfterArrowsHaveBeenRemoved(vidx);
    }

    protected void buildRemoveVertexAfterArrowsHaveBeenRemoved(int vidx) {
        // Remove vertex from nextLastArrow array
        if (vidx < vertexCount - 1) {
            System.arraycopy(nextLastArrow, (vidx + 1) * LASTARROW_NUM_FIELDS, nextLastArrow, vidx * LASTARROW_NUM_FIELDS,
                    (vertexCount - vidx - 1) * LASTARROW_NUM_FIELDS);
        }
        lastArrow_setLast(nextLastArrow, vertexCount - 1, 0);
        lastArrow_setCount(nextLastArrow, vertexCount - 1, 0);

        // Change vertex indices of all vertices after vidx
        for (int arrowPtr = 0; arrowPtr < arrowCountIncludingDeletedArrows; arrowPtr++) {
            int uidx = arrowHead_getVertex(nextArrowHeads, arrowPtr);
            if (uidx > vidx) {
                arrowHead_setVertex(nextArrowHeads, arrowPtr, uidx - 1);
            }
        }

        vertexCount--;
    }

    protected void buildInsertVertexAt(int vidx) {
        if (nextLastArrow.length < (vertexCount + 1) * LASTARROW_NUM_FIELDS) {
            nextLastArrow = Arrays.copyOf(nextLastArrow, (vertexCount + 1) * LASTARROW_NUM_FIELDS * 2);
        }


        // Insert vertex in nextLastArrow array
        if (vidx < vertexCount) {
            System.arraycopy(nextLastArrow,
                    (vidx) * LASTARROW_NUM_FIELDS, nextLastArrow,
                    (vidx + 1) * LASTARROW_NUM_FIELDS,
                    (vertexCount - vidx) * LASTARROW_NUM_FIELDS);
        }
        lastArrow_setLast(nextLastArrow, vidx, 0);
        lastArrow_setCount(nextLastArrow, vidx, 0);

        // Change vertex indices of all vertices after vidx
        for (int arrowPtr = 0; arrowPtr < arrowCountIncludingDeletedArrows; arrowPtr++) {
            int uidx = arrowHead_getVertex(nextArrowHeads, arrowPtr);
            if (uidx >= vidx) {
                arrowHead_setVertex(nextArrowHeads, arrowPtr, uidx + 1);
            }
        }

        vertexCount++;
    }


    private void arrowHead_setNext(int[] arrowHeads, int arrowPointer, int pointerToNextArrow) {
        arrowHeads[arrowPointer * ARROWS_NUM_FIELDS + ARROWS_NEXT_FIELD] = pointerToNextArrow;
    }

    private void lastArrow_setLast(int[] lastArrow, int vidx, int arrowPointer) {
        lastArrow[vidx * LASTARROW_NUM_FIELDS + LASTARROW_POINTER_FIELD] = arrowPointer;
    }

    private int lastArrow_getLast(int[] lastArrow, int vidx) {
        return lastArrow[vidx * LASTARROW_NUM_FIELDS + LASTARROW_POINTER_FIELD];
    }

    private int arrowHead_getNext(int[] arrowHeads, int arrowPointer) {
        return arrowHeads[arrowPointer * ARROWS_NUM_FIELDS + ARROWS_NEXT_FIELD];
    }

    protected int getNextArrowIndex(int vi, int i) {
        return getArrowIndex(vi, i, nextLastArrow, nextArrowHeads);
    }

    protected int getArrowIndex(int vi, int i, int @NonNull [] lastArrow, int @NonNull [] arrowHeads) {
        int arrowPointer = lastArrow_getLast(lastArrow, vi);
        int nextCount = lastArrow_getCount(lastArrow, vi);
        for (int j = nextCount - 1; j > i; j--) {
            arrowPointer = arrowHead_getNext(arrowHeads, arrowPointer);
        }
        return arrowPointer;
    }

    @Override
    public int getNextAsInt(int v, int i) {
        int arrowId = getNextArrowIndex(v, i);
        return arrowHead_getVertex(nextArrowHeads, arrowId);
    }

    @Override
    public int getNextCount(int v) {
        return lastArrow_getCount(nextLastArrow, v);
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    public void clear() {
        arrowCountIncludingDeletedArrows = 0;
        vertexCount = 0;
        deletedArrowCount = 0;
        pointerToLastDeletedArrow = -1;
        Arrays.fill(nextArrowHeads, 0);
        Arrays.fill(nextLastArrow, 0);
    }

    private boolean ordered = true;

    public boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    @Override
    public @NonNull IntEnumeratorSpliterator nextVerticesEnumerator(int v) {
        if (ordered) {
            return getNextVerticesOrdered(v);
        } else {
            return getNextVerticesUnordered(v);
        }
    }

    public @NonNull IntEnumeratorSpliterator getNextVerticesUnordered(int vidx) {
        class MySpliterator extends AbstractIntEnumeratorSpliterator {
            private int arrowPtr;

            public MySpliterator(int vidx, int lo, int hi) {
                super(hi - lo, ORDERED | NONNULL | SIZED);

                arrowPtr = lastArrow_getCount(nextLastArrow, vidx) == 0 ? -1 : lastArrow_getLast(nextLastArrow, vidx);
            }

            @Override
            public boolean moveNext() {
                if (arrowPtr != -1) {
                    current = arrowHead_getVertex(nextArrowHeads, arrowPtr);
                    arrowPtr = arrowHead_getNext(nextArrowHeads, arrowPtr);
                    return true;
                }
                return false;
            }


        }
        return new MySpliterator(vidx, 0, getNextCount(vidx));
    }

    public @NonNull IntEnumeratorSpliterator getNextVerticesOrdered(int vidx) {
        class MySpliterator extends AbstractIntEnumeratorSpliterator {
            private int index;
            private final int limit;
            private final int vidx;
            private final int @NonNull [] arrows;

            public MySpliterator(int vidx, int lo, int hi) {
                super(hi - lo, ORDERED | NONNULL | SIZED | SUBSIZED);
                limit = hi;
                index = lo;
                this.vidx = vidx;
                int nextCount = getNextCount(vidx);
                if (nextCount > 0) {
                    arrows = new int[nextCount];
                    int arrowPtr = lastArrow_getLast(nextLastArrow, vidx);
                    arrows[nextCount - 1] = arrowHead_getVertex(nextArrowHeads, arrowPtr);
                    for (int j = nextCount - 1; j > lo; j--) {
                        arrowPtr = arrowHead_getNext(nextArrowHeads, arrowPtr);
                        arrows[j - 1] = arrowHead_getVertex(nextArrowHeads, arrowPtr);
                    }
                } else {
                    arrows = new int[0];
                }
            }

            private MySpliterator(int vidx, int lo, int hi, int @NonNull [] arrows) {
                super(hi - lo, ORDERED | NONNULL | SIZED | SUBSIZED);
                this.vidx = vidx;
                this.index = lo;
                this.limit = hi;
                this.arrows = arrows;
            }

            @Override
            public boolean moveNext() {
                if (index < limit) {
                    int i = index++;
                    current = arrows[i];
                    return true;
                }
                return false;
            }

            @Override
            public @Nullable MySpliterator trySplit() {
                int hi = limit, lo = index, mid = (lo + hi) >>> 1;
                return (lo >= mid) ? null : // divide range in half unless too small
                        new MySpliterator(vidx, lo, index = mid, arrows);
            }

        }
        return new MySpliterator(vidx, 0, getNextCount(vidx));
    }
}
