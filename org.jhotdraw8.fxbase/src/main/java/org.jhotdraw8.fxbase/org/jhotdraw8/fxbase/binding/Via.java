/*
 * @(#)Via.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.binding;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Provides a binding via another binding.
 * <p>
 * Keeps hard references between the bindings.
 */
public class Via<T> {
    private final @NonNull Property<T> root;
    private @Nullable List<ChangeListener<T>> hardReferences;

    public Via(@NonNull Property<T> root) {
        this.root = Objects.requireNonNull(root, "mediator");

    }

    public <U> Via<U> via(Function<T, Property<U>> viaFunction) {
        ObjectProperty<U> next = new SimpleObjectProperty<>();
        final ChangeListener<T> changeListener = (o, oldv, newv) -> {
            if (oldv != null) {
                next.unbindBidirectional(viaFunction.apply(oldv));
            }
            if (newv != null) {
                next.bindBidirectional(viaFunction.apply(newv));
            }
        };
        if (hardReferences == null) {
            hardReferences = new ArrayList<>();
        }
        hardReferences.add(changeListener);
        changeListener.changed(root, null, root.getValue());
        root.addListener(changeListener);
        return new Via(next);
    }

    public Property<T> get() {
        return root;
    }
}
