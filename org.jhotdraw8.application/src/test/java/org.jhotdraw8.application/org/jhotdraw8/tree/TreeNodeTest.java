/*
 * @(#)TreeNodeTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.tree;

import org.jhotdraw8.fxbase.tree.TreeNode;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreeNodeTest {
    static class Node implements TreeNode<Node> {
        private final List<Node> children = new ArrayList<>();
        private @Nullable Node parent;
        private final int value;

        public Node(int value) {
            this.value = value;
        }

        public void add(Node node) {
            if (node.getParent() != null) {
                throw new IllegalStateException();
            }
            node.setParent(this);
            children.add(node);
        }

        @Override
        public List<Node> getChildren() {
            return children;
        }

        @Override
        public @Nullable Node getParent() {
            return parent;
        }

        @Override
        public void setParent(@Nullable Node newValue) {
            this.parent = newValue;
        }

        @Override
        public String toString() {
            return "Node{" + value + '}';
        }
    }

    @Test
    public void testGetHeight() {
        // depth 1:
        Node root = new Node(1);

        // depth 2:
        root.add(new Node(21));
        root.add(new Node(22));

        // depth 3:
        root.getChild(1).add(new Node(31));
        root.getChild(1).add(new Node(32));

        // depth 4:
        root.getChild(1).getChild(0).add(new Node(41));
        root.getChild(1).getChild(1).add(new Node(42));
        root.getChild(1).getChild(1).add(new Node(43));
        root.getChild(1).getChild(1).add(new Node(44));

        assertEquals(4, root.getMaxDepth());
        assertEquals(1, root.getChild(0).getMaxDepth());
        assertEquals(3, root.getChild(1).getMaxDepth());
        assertEquals(2, root.getChild(1).getChild(0).getMaxDepth());
        assertEquals(1, root.getChild(1).getChild(0).getChild(0).getMaxDepth());
    }
}