/*
 * @(#)SimpleWeakListener.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.event;

import javafx.beans.WeakListener;
import org.jhotdraw8.base.event.Listener;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.EventObject;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * SimpleWeakListener.
 *
 * @param <E> the type of the event
 */
public final class SimpleWeakListener<E extends EventObject> implements Listener<E>, WeakListener {

    private final WeakReference<Listener<E>> ref;
    private final Consumer<Listener<E>> removeListener;

    public SimpleWeakListener(@Nullable Listener<E> listener, Consumer<Listener<E>> removeListener) {
        Objects.requireNonNull(listener, "listener");
        this.ref = new WeakReference<>(listener);
        this.removeListener = removeListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean wasGarbageCollected() {
        return (ref.get() == null);
    }

    @Override
    public void handle(E event) {
        Listener<E> listener = ref.get();
        if (listener != null) {
            listener.handle(event);
        } else {
            // The weakly reference listener has been garbage collected,
            // so this WeakListener will now unhook itself from the
            // source bean
            removeListener.accept(this);
        }
    }

}
