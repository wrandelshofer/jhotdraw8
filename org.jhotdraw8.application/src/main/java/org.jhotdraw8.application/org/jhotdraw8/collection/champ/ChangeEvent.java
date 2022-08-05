/*
 * @(#)ChangeEvent.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

class ChangeEvent<V> {

    public boolean modified;
    private V oldValue;
    public boolean valueUpdated;
    public int numInBothCollections;

    public ChangeEvent() {
    }

    void reset() {
        modified = valueUpdated = false;
    }

    void found(V oldValue) {
        this.oldValue = oldValue;
    }

    public V getOldValue() {
        return oldValue;
    }

    public boolean isValueUpdated() {
        return valueUpdated;
    }

    /**
     * Returns true if an entry has been inserted, updated or removed.
     */
    public boolean isModified() {
        return modified;
    }

    void setValueUpdated(V oldValue) {
        this.oldValue = oldValue;
        this.valueUpdated = true;
        this.modified = true;
    }

    void setValueRemoved(V oldValue) {
        this.oldValue = oldValue;
        this.modified = true;
    }

    void setValueAdded() {
        this.modified = true;
    }
}
