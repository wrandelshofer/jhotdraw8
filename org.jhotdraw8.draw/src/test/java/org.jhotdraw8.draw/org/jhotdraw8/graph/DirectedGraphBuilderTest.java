/*
 * @(#)DirectedGraphBuilderTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

public class DirectedGraphBuilderTest extends AbstractMutableDirectedGraphTest {
    @Override
    protected MutableDirectedGraph<Integer, Character> newInstance() {
        return new SimpleMutableDirectedGraph<>(0, 0);
    }

    @Override
    protected MutableDirectedGraph<Integer, Character> newInstance(DirectedGraph<Integer, Character> g) {
        return new SimpleMutableDirectedGraph<>(g);
    }
}