/*
 * @(#)ChangeEvent.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champmap;

public class ChangeEvent<V> {

    public boolean isModified;
    private V oldValue;
    private boolean isReplaced;

    public ChangeEvent() {
    }

    void found(V oldValue) {
        this.oldValue = oldValue;
    }

    public V getOldValue() {
        return oldValue;
    }

    public boolean hasReplacedValue() {
        return isReplaced;
    }

    public boolean isModified() {
        return isModified;
    }

    // update: inserted/removed single element, element count changed
    void modified() {
        this.isModified = true;
    }

    void updated(V oldValue) {
        this.oldValue = oldValue;
        this.isModified = true;
        this.isReplaced = true;
    }
}
