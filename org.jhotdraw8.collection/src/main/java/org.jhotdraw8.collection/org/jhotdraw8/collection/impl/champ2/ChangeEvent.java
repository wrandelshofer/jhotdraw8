/*
 * @(#)ChangeEvent.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ2;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Objects;

/**
 * This class is used to report a change (or no changes) of data in a CHAMP trie.
 *
 * @param <D> the data type
 */
public class ChangeEvent<D> {
    enum Type {
        UNCHANGED,
        ADDED,
        REMOVED,
        REPLACED
    }

    private @NonNull Type type = Type.UNCHANGED;
    private @Nullable D oldData;
    private @Nullable D newData;

    public ChangeEvent() {
    }

    void found(D data) {
        this.oldData = data;
    }

    public @Nullable D getOldData() {
        return oldData;
    }

    public @Nullable D getNewData() {
        return newData;
    }

    public @NonNull D getOldDataNonNull() {
        return Objects.requireNonNull(oldData);
    }

    public @NonNull D getNewDataNonNull() {
        return Objects.requireNonNull(newData);
    }

    /**
     * Call this method to indicate that the value of an element has changed.
     *
     * @param oldData the old value of the element
     * @param newData the new value of the element
     */
    void setReplaced(@Nullable D oldData, @Nullable D newData) {
        this.oldData = oldData;
        this.newData = newData;
        this.type = Type.REPLACED;
    }

    /**
     * Call this method to indicate that an element has been removed.
     *
     * @param oldData the value of the removed element
     */
    void setRemoved(@Nullable D oldData) {
        this.oldData = oldData;
        this.type = Type.REMOVED;
    }

    /**
     * Call this method to indicate that a data element has been added.
     */
    void setAdded(@Nullable D newData) {
        this.newData = newData;
        this.type = Type.ADDED;
    }

    /**
     * Returns true if the CHAMP trie has been modified.
     */
    public boolean isModified() {
        return type != Type.UNCHANGED;
    }

    /**
     * Returns true if the data element has been replaced.
     */
    public boolean isReplaced() {
        return type == Type.REPLACED;
    }

    void reset() {
        type = Type.UNCHANGED;
        oldData = null;
        newData = null;
    }
}
