/*
 * @(#)DirectedGraphBuilderTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * Tests {@link SimpleMutableDirectedGraph}.
 */
public class SimpleMutableDirectedGraphTest extends AbstractMutableDirectedGraphTest<Integer, Character> {
    @Override
    protected MutableDirectedGraph<Integer, Character> newInstance() {
        return new SimpleMutableDirectedGraph<>(0, 0);
    }

    @Override
    protected MutableDirectedGraph<Integer, Character> newInstance(DirectedGraph<Integer, Character> g) {
        return new SimpleMutableDirectedGraph<>(g);
    }

    @Override
    protected @NonNull Integer newVertex(int id) {
        return id;
    }

    @Override
    protected @NonNull Character newArrow(@NonNull Integer from, @NonNull Integer to, char id) {
        return id;
    }

    @Override
    protected @NonNull char getArrowId(@Nullable Character character) {
        return character == null ? '\u0000' : character;
    }

    @Override
    protected @NonNull int getVertexId(@NonNull Integer integer) {
        return integer;
    }
}