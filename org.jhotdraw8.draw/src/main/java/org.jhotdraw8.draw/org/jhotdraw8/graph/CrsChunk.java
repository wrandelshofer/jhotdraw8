package org.jhotdraw8.graph;

interface CrsChunk {
    boolean addArrow(final int v, final int u, final int data, final boolean setIfPresent);

    int indexOf(final int v, final int u);

    int getSiblingsFromOffset(final int v);

    int getSiblingCount(final int v);

    int[] getSiblingsArray();

    boolean tryToRemoveArrow(final int v, final int u);

    void removeAllArrows(final int v);

    int removeArrowAt(final int v, final int removalIndex);

    int getSibling(final int v, final int k);

    int getArrow(final int v, final int k);

    int getVertexData(final int v);

    void setVertexData(final int v, final int data);
}
