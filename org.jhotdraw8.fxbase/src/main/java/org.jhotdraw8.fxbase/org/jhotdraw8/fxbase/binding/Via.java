/*
 * @(#)Via.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.binding;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.lang.ref.WeakReference;
import java.util.function.Function;

/**
 * Builder for bindings that go via multiple properties.
 * <p>
 * Usage: Bind a text field to {@code person.address.city}.
 * <pre>{@literal
 *     TextField cityField;
 *     Property<Person> personProperty;
 *
 *     Via<Person> via = new Via<>(personProperty);
 *     cityField.textProperty().bindBidirectional(
 *         via.via(Person::addressProperty)
 *         .via(Address::cityProperty)
 *         .get()
 *     );
 * }</pre>
 * <p>
 * Creates change listeners that are strongly referenced from the root object.
 * <p>
 * When the root object is garbage collected, the change listeners unregister themselves.
 *
 * @param <T> the value type of the property
 */
public class Via<T> {
    private final WeakReference<Property<?>> weakReferenceToRoot;
    private final Property<T> intermediate;

    /**
     * Creates a new via builder.
     *
     * @param weakReferenceToRoot the root property
     */
    public Via(Property<T> weakReferenceToRoot) {
        this.weakReferenceToRoot = new WeakReference<>(weakReferenceToRoot);
        this.intermediate = weakReferenceToRoot;
    }

    private Via(WeakReference<Property<?>> weakReferenceToRoot, Property<T> intermediate) {
        this.weakReferenceToRoot = weakReferenceToRoot;
        this.intermediate = intermediate;
    }

    /**
     * Builds a new via step.
     *
     * @param viaFunction a function that returns the desired property
     * @param <U>         the type of the property
     * @return a via binding to the property
     */
    public <U> Via<U> via(Function<T, Property<U>> viaFunction) {
        // Create a change listener that has strong references to
        // - the 'viaFunction' function
        // - the intermediate 'next' property.
        ViaChangeListener<T, U> changeListener = new ViaChangeListener<>(weakReferenceToRoot, intermediate, viaFunction);
        changeListener.changed(intermediate, null, intermediate.getValue());

        // Calling addListener creates a strong reference to 'changeListener'.
        intermediate.addListener(changeListener);
        return new Via<>(weakReferenceToRoot, changeListener.next);
    }

    /**
     * Internally used change listener.
     *
     * @param <T>
     * @param <U>
     */
    private static class ViaChangeListener<T, U> implements ChangeListener<T> {
        private final ObjectProperty<U> next = new SimpleObjectProperty<>();
        private final Function<T, Property<U>> viaFunction;
        private final WeakReference<Property<?>> weakReferenceToRoot;
        final private Property<T> intermediate;

        private ViaChangeListener(WeakReference<Property<?>> weakReferenceToRoot, Property<T> intermediate, Function<T, Property<U>> viaFunction) {
            this.viaFunction = viaFunction;
            this.weakReferenceToRoot = weakReferenceToRoot;
            this.intermediate = intermediate;
        }

        @Override
        public void changed(ObservableValue<? extends T> o, T oldv, T newv) {
            if (oldv != null) {
                next.unbindBidirectional(viaFunction.apply(oldv));
            }
            if (weakReferenceToRoot.get() == null) {
                // Removes the strong reference to this listener
                intermediate.removeListener(this);
                return;
            }
            if (newv != null) {
                next.bindBidirectional(viaFunction.apply(newv));
            }
        }
    }

    /**
     * Gets the binding that was built.
     *
     * @return a binding
     */
    public Property<T> get() {
        return intermediate;
    }
}
