/*
 * @(#)SimpleMutableDirectedGraphTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jspecify.annotations.Nullable;

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
    protected Integer newVertex(int id) {
        return id;
    }

    @Override
    protected Character newArrow(Integer start, Integer end, char id) {
        return id;
    }

    @Override
    protected char getArrowId(@Nullable Character character) {
        return character == null ? '\u0000' : character;
    }

    @Override
    protected int getVertexId(Integer integer) {
        return integer;
    }
}