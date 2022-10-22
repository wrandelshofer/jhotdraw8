/*
 * @(#)PushBackEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.enumerator;

/**
 * An enumerator that can push back the current element.
 *
 * @param <E> the element type
 */
public interface PushBackEnumeratorSpliterator<E> extends EnumeratorSpliterator<E> {
    /**
     * Pushes the current element back into the enumeration.
     * <p>
     * So that a subsequent call to {@link EnumeratorSpliterator#moveNext()}
     * will have the same effect as the call to
     * {@link EnumeratorSpliterator#moveNext()} that was done before this
     * method has been called.
     */
    void pushBack();
}
