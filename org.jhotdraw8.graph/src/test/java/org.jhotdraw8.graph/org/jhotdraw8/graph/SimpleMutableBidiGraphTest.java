/*
 * @(#)SimpleMutableBidiGraphTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jspecify.annotations.Nullable;

/**
 * Tests {@link SimpleMutableBidiGraph}.
 */
public class SimpleMutableBidiGraphTest extends AbstractMutableBidiGraphTest<Integer, Character> {
    @Override
    protected MutableBidiGraph<Integer, Character> newInstance() {
        return new SimpleMutableBidiGraph<>();
    }

    @Override
    protected MutableBidiGraph<Integer, Character> newInstance(DirectedGraph<Integer, Character> g) {
        return new SimpleMutableBidiGraph<>(g);
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
