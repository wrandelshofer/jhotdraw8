/*
 * @(#)ChangeEvent.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

public class ChangeEvent<V> {

    public boolean modified;
    private V oldValue;
    public boolean isUpdated;

    public ChangeEvent() {
    }

    void found(V oldValue) {
        this.oldValue = oldValue;
    }

    public V getOldValue() {
        return oldValue;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    /**
     * Returns true if a value has been inserted, replaced or removed.
     */
    public boolean isModified() {
        return modified;
    }

    void setValueUpdated(V oldValue) {
        this.oldValue = oldValue;
        this.isUpdated = true;
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
