/*
 * @(#)AddToSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.util.function;

/**
 * A function that adds an element to a set if not already present.
 * <p>
 * The set can be implemented in various ways. For example:
 * <ul>
 *     <li>The set can be an implementation of one of the collection classes
 *     provided by the Java API.
 *          <pre>
 *         {@literal AddToSet<E> = new HashSet<>()::add;}
 *         </pre>
 *     </li>
 *     <li>The set can be a marker bit on an element object.
 *     <pre>
 *         class Element {
 *             private boolean marked;
 *             public boolean mark() {
 *                 boolean wasMarked = false;
 *                 marked = true;
 *                 return wasMarked;
 *             }
 *         }
 *        {@literal AddToSet<Element> = Element::mark;}
 *        </pre>
 *     </li>
 * </ul>
 *
 * @param <E> the element type of the set.
 */
@FunctionalInterface
public interface AddToSet<E> {
    /**
     * Adds the specified element to the set if it is not already present.
     *
     * @param e element to be added to the set
     * @return {@code true} if this set did not already contain the specified
     * element
     */
    boolean add(E e);
}
