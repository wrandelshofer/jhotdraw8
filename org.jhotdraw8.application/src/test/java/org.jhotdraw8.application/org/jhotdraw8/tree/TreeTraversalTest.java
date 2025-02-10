/*
 * @(#)TreeTraversalTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.tree;

import org.jhotdraw8.fxbase.tree.PostorderSpliterator;
import org.jhotdraw8.fxbase.tree.PreorderSpliterator;
import org.jhotdraw8.fxbase.tree.SimpleTreeNode;
import org.jhotdraw8.fxbase.tree.TreeBreadthFirstSpliterator;
import org.jhotdraw8.fxbase.tree.TreeDepthFirstSpliterator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TreeTraversalTest.
 *
  */
public class TreeTraversalTest {

    private static SimpleTreeNode<String> createTree() {
        //       F
        //     ↙︎  ↘︎
        //    B     G
        //  ↙︎  ↘︎      ↘
        // A    D       I
        //    ↙︎  ↘︎     ↙
        //   C    E   H

        SimpleTreeNode<String> a = new SimpleTreeNode<String>("A");
        SimpleTreeNode<String> b = new SimpleTreeNode<String>("B");
        SimpleTreeNode<String> c = new SimpleTreeNode<String>("C");
        SimpleTreeNode<String> d = new SimpleTreeNode<String>("D");
        SimpleTreeNode<String> e = new SimpleTreeNode<String>("E");
        SimpleTreeNode<String> f = new SimpleTreeNode<String>("F");
        SimpleTreeNode<String> g = new SimpleTreeNode<String>("G");
        SimpleTreeNode<String> h = new SimpleTreeNode<String>("H");
        SimpleTreeNode<String> i = new SimpleTreeNode<String>("I");

        b.addChild(a);
        b.addChild(d);
        d.addChild(c);
        d.addChild(e);
        f.addChild(b);
        f.addChild(g);
        g.addChild(i);
        i.addChild(h);

        return f;
    }

    @Test
    public void testPreorderTraversal() throws Exception {
        SimpleTreeNode<String> root = createTree();
        PreorderSpliterator<SimpleTreeNode<String>> instance = new PreorderSpliterator<>(SimpleTreeNode::getChildren, root);

        StringBuilder buf = new StringBuilder();
        instance.forEachRemaining(node -> buf.append(node.getValue()));

        String expected = "FBADCEGIH";
        String actual = buf.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void testPostorderTraversal() throws Exception {
        SimpleTreeNode<String> root = createTree();
        PostorderSpliterator<SimpleTreeNode<String>> instance = new PostorderSpliterator<>(SimpleTreeNode::getChildren, root);

        StringBuilder buf = new StringBuilder();
        instance.forEachRemaining(node -> buf.append(node.getValue()));

        String expected = "ACEDBHIGF";
        String actual = buf.toString();
        assertEquals(expected, actual);
    }


    @Test
    public void testBreadthFirstTraversal() throws Exception {
        SimpleTreeNode<String> root = createTree();
        TreeBreadthFirstSpliterator<SimpleTreeNode<String>> instance = new TreeBreadthFirstSpliterator<>(SimpleTreeNode::getChildren, root);

        StringBuilder buf = new StringBuilder();
        instance.forEachRemaining(node -> buf.append(node.getValue()));

        String expected = "FBGADICEH";
        String actual = buf.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void testDepthFirstTraversal() throws Exception {
        SimpleTreeNode<String> root = createTree();
        TreeDepthFirstSpliterator<SimpleTreeNode<String>> instance = new TreeDepthFirstSpliterator<>(SimpleTreeNode::getChildren, root);

        StringBuilder buf = new StringBuilder();
        instance.forEachRemaining(node -> buf.append(node.getValue()));

        String expected = "FGIHBDECA";
        String actual = buf.toString();
        assertEquals(expected, actual);
    }

}
