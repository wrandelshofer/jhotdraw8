/*
 * @(#)UndoableEditHelper.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.undo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import javax.swing.event.UndoableEditEvent;
import java.util.function.Consumer;

/**
 * Helper for firing {@link CompositeEdit} events.
 */
public class UndoableEditHelper {
    private @Nullable CompositeEdit edit;
    private final @NonNull Consumer<UndoableEditEvent> handler;
    private final @NonNull Object source;

    /**
     * Creates a new instance.
     *
     * @param source  the event source
     * @param handler the event handler
     */
    public UndoableEditHelper(@NonNull Object source, @NonNull Consumer<UndoableEditEvent> handler) {
        this.handler = handler;
        this.source = source;
    }

    /**
     * Starts composing edits.
     */
    public void startCompositeEdit(@Nullable String localizedName) {
        if (edit == null) {
            edit = new CompositeEdit(localizedName);
            fire(edit);
        }
    }

    private void fire(CompositeEdit edit) {
        new UndoableEditEvent(source, edit);
    }

    /**
     * Stops composing edits.
     */
    public void stopCompositeEdit() {
        if (edit != null) {
            fire(edit);
            edit = null;
        }
    }

}
