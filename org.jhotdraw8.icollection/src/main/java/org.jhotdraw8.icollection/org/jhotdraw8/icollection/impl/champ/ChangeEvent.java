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
    private @Nullable Object[] oldEntry;
    private @Nullable Object[] newEntry;

    public ChangeEvent() {
    }

    void setFound(Object[] data) {
        this.oldEntry = data;
    }

    public @Nullable Object[] getOldEntry() {
        return oldEntry;
    }

    public @Nullable Object[] getNewEntry() {
        return newEntry;
    }

    public Object[] getOldDataNonNull() {
        return Objects.requireNonNull(oldEntry);
    }

    public Object[] getNewDataNonNull() {
        return Objects.requireNonNull(newEntry);
    }

    /// Call this method to indicate that the value of an element has changed.
    ///
    /// @param oldEntry the old value of the element
    /// @param newEntry the new value of the element
    void setReplaced(@Nullable Object[] oldEntry, @Nullable Object[] newEntry) {
        this.oldEntry = oldEntry;
        this.newEntry = newEntry;
        this.type = ChangeEvent.Type.REPLACED;
    }

    /// Call this method to indicate that an element has been removed.
    ///
    /// @param oldEntry the value of the removed element
    void setRemoved(@Nullable Object[] oldEntry) {
        this.oldEntry = oldEntry;
        this.type = ChangeEvent.Type.REMOVED;
    }

    /// Call this method to indicate that a data element has been added.
    void setAdded(@Nullable Object[] newEntry) {
        this.newEntry = newEntry;
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
        oldEntry = null;
        newEntry = null;
    }
}
