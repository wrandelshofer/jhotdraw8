package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

class VertexBackLinkOld<VV> {
    private final int remainingLength;
    private final @NonNull VV vertex;
    private final @Nullable VertexBackLinkOld<VV> parent;
    private final int depth;

    public VertexBackLinkOld(@NonNull VV node, @Nullable VertexBackLinkOld<VV> parent, int remainingLength) {
        this.vertex = node;
        this.parent = parent;
        this.remainingLength = remainingLength;
        this.depth = parent == null ? 0 : parent.depth + 1;
    }


    /**
     * Return the path length up to this back link.
     */
    protected int getRemainingLength() {
        return remainingLength;
    }

    public @Nullable VertexBackLinkOld<VV> getParent() {
        return parent;
    }

    public @NonNull VV getVertex() {
        return vertex;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return "BackLink{" +
                "v=" + vertex +
                ", c=" + remainingLength +
                ", parent=" + parent +
                '}';
    }
}
