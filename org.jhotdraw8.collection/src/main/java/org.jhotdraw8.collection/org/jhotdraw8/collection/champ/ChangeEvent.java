/*
 * @(#)ChangeEvent.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

/**
 * This class is used to report a change (or no changes) of data in a CHAMP trie.
 *
 * @param <D> the data type
 */
class ChangeEvent<D> {
    enum Type {
        UNCHANGED,
        ADDED,
        REMOVED,
        REPLACED
    }

    private Type type = Type.UNCHANGED;
    private D data;

    public ChangeEvent() {
    }

    void found(D data) {
        this.data = data;
    }

    public D getData() {
        return data;
    }

    /**
     * Call this method to indicate that the value of an element has changed.
     *
     * @param oldData the old value of the element
     */
    void setReplaced(D oldData) {
        this.data = oldData;
        this.type = Type.REPLACED;
    }

    /**
     * Call this method to indicate that an element has been removed.
     *
     * @param oldData the value of the removed element
     */
    void setRemoved(D oldData) {
        this.data = oldData;
        this.type = Type.REMOVED;
    }

    /**
     * Call this method to indicate that a data element has been added.
     */
    void setAdded() {
        this.type = Type.ADDED;
    }

    /**
     * Returns true if the CHAMP trie has been modified.
     */
    boolean isModified() {
        return type != Type.UNCHANGED;
    }

    /**
     * Returns true if the data element has been replaced.
     */
    boolean isReplaced() {
        return type == Type.REPLACED;
    }
}
