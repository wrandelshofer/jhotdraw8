/*
 * @(#)Tombstone.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.icollection.ChampVectorSet;

/**
 * A tombstone is used by {@link ChampVectorSet} to mark a deleted slot in its Vector.
 * <p>
 * A tombstone stores the minimal number of neighbors 'before' and 'after' it in the
 * Vector.
 * <p>
 * When we insert a new tombstone, we update 'before' and 'after' values only on
 * the first and last tombstone of a sequence of tombstones. Therefore, a delete
 * operation requires reading of up to 3 neighboring elements in the vector, and
 * updates of up to 3 elements.
 * <p>
 * There are no tombstones at the first and last element of the vector. When we
 * remove the first or last element of the vector, we remove the tombstones.
 * <p>
 * Example: Tombstones are shown as <i>before</i>.<i>after</i>.
 * <pre>
 *
 *
 *                              Indices:  0   1   2   3   4   5   6   7   8   9
 * Initial situation:           Values:  'a' 'b' 'c' 'd' 'e' 'f' 'g' 'h' 'i' 'j'
 *
 * Deletion of element 5:
 * - read elements at indices 4, 5, 6                    'e' 'f' 'g'
 * - notice that none of them are tombstones
 * - put tombstone 0.0 at index 5                            0.0
 *
 * After deletion of element 5:          'a' 'b' 'c' 'd' 'e' 0.0 'g' 'h' 'i' 'j'
 *
 * After deletion of element 7:          'a' 'b' 'c' 'd' 'e' 0.0 'g' 0.0 'i' 'j'
 *
 * Deletion of element 8:
 * - read elements at indices 7, 8, 9                                0.0 'i' 'j'
 * - notice that 7 is a tombstone 0.0
 * - put tombstones 0.1, 1.0 at indices 7 and 8
 *
 * After deletion of element 8:          'a' 'b' 'c' 'd' 'e' 0.0 'g' 0.1 1.0 'j'
 *
 * Deletion of element 6:
 * - read elements at indices 5, 6, 7                        0.0 'g' 0.1
 * - notice that two of them are tombstones
 * - put tombstones 0.3, 0.0, 3.0 at indices 5, 6 and 8
 *
 * After deletion of element 6:          'a' 'b' 'c' 'd' 'e' 0.3 0.0 0.1 3.0 'j'
 *
 * Deletion of the last element 9:
 * - read elements at index 8                                            3.0
 * - notice that it is a tombstone
 * - remove the last element and the neighboring tombstone sequence
 *
 * After deletion of element 9:          'a' 'b' 'c' 'd' 'e'
 * </pre>
 * References:
 * <p>
 * The design of this class is inspired by 'SimplePersistentSequencedMap.scala'.
 * <dl>
 *      <dt>SimplePersistentSequencedMap.scala
 *      <br>The Scala library. Copyright EPFL and Lightbend, Inc. Apache License 2.0.</dt>
 *      <dd><a href="https://github.com/scala/scala/blob/28eef15f3cc46f6d3dd1884e94329d7601dc20ee/src/library/scala/collection/persistent/VectorMap.scala">github.com</a>
 *      </dd>
 * </dl>
 *
 * @param skip if negative: minimal number of neighboring tombstones before this one<br>
 *             if positive: minimal number of neighboring tombstones after this one
 */
public record Tombstone(int skip) {
    /**
     * We allocate the most common tomb-stones only once in memory.
     * <p>
     * If we get lucky, and the tombstones do not cluster too much,
     * we can save lots of memory by this.
     */
    private static final Tombstone[] TOMBS = {
            new Tombstone(0),
            new Tombstone(1),
            new Tombstone(2),
            new Tombstone(3),
    };

    static Tombstone create(int skip) {
        assert skip >= 0;
        return skip < TOMBS.length ? TOMBS[skip] : new Tombstone(skip);
    }
}
