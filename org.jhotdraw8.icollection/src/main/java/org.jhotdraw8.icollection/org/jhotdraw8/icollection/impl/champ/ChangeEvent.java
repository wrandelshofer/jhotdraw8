/*
 * @(#)ChangeEvent.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class ChangeEvent {

    public boolean isUnchanged() {
        return type == ChangeEvent.Type.UNCHANGED;
    }

    enum Type {
        UNCHANGED,
        ADDED,
        REMOVED,
        REPLACED
    }

    private ChangeEvent.Type type = ChangeEvent.Type.UNCHANGED;
    private @Nullable Object[] oldData;
    private @Nullable Object[] newData;

    public ChangeEvent() {
    }

    void setFound(Object[] data) {
        this.oldData = data;
    }

    public @Nullable Object[] getOldData() {
        return oldData;
    }

    public @Nullable Object[] getNewData() {
        return newData;
    }

    public Object[] getOldDataNonNull() {
        return Objects.requireNonNull(oldData);
    }

    public Object[] getNewDataNonNull() {
        return Objects.requireNonNull(newData);
    }

    /// Call this method to indicate that the value of an element has changed.
    ///
    /// @param oldEntry the old value of the element
    /// @param newEntry the new value of the element
    void setReplaced(@Nullable Object[] oldEntry, @Nullable Object[] newEntry) {
        this.oldData = oldEntry;
        this.newData = newEntry;
        this.type = ChangeEvent.Type.REPLACED;
    }

    /// Call this method to indicate that an element has been removed.
    ///
    /// @param oldEntry the value of the removed element
    void setRemoved(@Nullable Object[] oldEntry) {
        this.oldData = oldEntry;
        this.type = ChangeEvent.Type.REMOVED;
    }

    /// Call this method to indicate that a data element has been added.
    void setAdded(@Nullable Object[] newEntry) {
        this.newData = newEntry;
        this.type = ChangeEvent.Type.ADDED;
    }

    /// Returns true if the CHAMP trie has been modified.
    public boolean isModified() {
        return type != ChangeEvent.Type.UNCHANGED;
    }

    /// Returns true if the data element has been replaced.
    public boolean isReplaced() {
        return type == ChangeEvent.Type.REPLACED;
    }

    void reset() {
        type = ChangeEvent.Type.UNCHANGED;
        oldData = null;
        newData = null;
    }
}
