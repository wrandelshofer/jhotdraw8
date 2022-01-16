package org.jhotdraw8.tree;

import org.jhotdraw8.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

/**
 * Computes the maximal depth of a tree.
 */
public class TreeMaxDepthCalculator {
    /**
     * Computes the maximal depth of the sub-tree starting at this tree node.
     * <p>
     * References:
     * <dl>
     *     <dt>Binary Tree: Maximum Depth/Height Of Deepest Node using recursive and iterative way </dt>
     *     <dd><a href="https://dev.to/ashutosh049/binary-tree-maximum-depthheight-of-deepest-node-using-recursive-and-iterative-way-hpp">dev.to</a></dd>
     * </dl>
     *
     * @return the maximal depth
     */
    public <T> int getMaxDepth(@NonNull T root, @NonNull Function<T, Iterable<T>> getChildren) {
        Deque<T> q = new ArrayDeque<>();
        q.add(root);
        int maxDepth = 0;
        while (!q.isEmpty()) {
            maxDepth++;
            for (int i = q.size(); i > 0; i--) {
                T curr = q.remove();
                for (T child : getChildren.apply(curr)) {
                    q.add(child);
                }
            }
        }
        return maxDepth;
    }
}
