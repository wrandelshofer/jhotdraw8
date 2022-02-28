/*
 * @(#)AddToIntSet.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.util.function;

import org.jhotdraw8.annotation.NonNull;

import java.util.BitSet;

/**
 * Represents a function that adds an element to a set if not already present.
 * <p>
 * The set can be implemented in various ways. For example:
 * <ul>
 *     <li>The set can be an implementation of one of the collection classes
 *     provided by the Java API.
 *          <pre>
 *         {@literal AddToIntSet=new HashSet<Integer>()::add;}
 *         </pre>
 *     </li>
 *     <li>The set can be a marker bit in a {@link BitSet}.
 *     <pre>
 *        {@literal AddToIntSet=AddToIntSet.addToBitSet(new BitSet());}
 *     </li>
 * </ul>
 * @author Werner Randelshofer
 */
@FunctionalInterface
public interface AddToIntSet extends AddToSet<Integer> {
    @Override
    default boolean add(Integer integer) {
        return addAsInt(integer);
    }

    /**
     * Adds the specified element to the set if it is not already present.
     *
     * @param e element to be added to the set
     * @return {@code true} if this set did not already contain the specified
     * element
     */
    boolean addAsInt(int e);


    /**
     * Creates an instance that adds to a bit set.
     *
     * @param bitSet a bit set
     * @return a new instance
     */
    @NonNull
    static AddToIntSet addToBitSet(@NonNull BitSet bitSet) {
        return i -> {
            boolean b = bitSet.get(i);
            if (!b) {
                bitSet.set(i);
            }
            return !b;
        };
    }
}
