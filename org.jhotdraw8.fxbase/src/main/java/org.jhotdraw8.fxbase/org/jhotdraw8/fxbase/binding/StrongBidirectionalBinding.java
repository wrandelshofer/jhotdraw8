/*
 * @(#)StrongBidirectionalBinding.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.binding;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import org.jhotdraw8.annotation.NonNull;

import java.util.Objects;

public class StrongBidirectionalBinding<T> implements InvalidationListener {
    private final @NonNull Property<T> propertyRef1;
    private final @NonNull Property<T> propertyRef2;
    private T oldValue;
    private boolean updating = false;

    private StrongBidirectionalBinding(Property<T> property1, Property<T> property2) {
        oldValue = property1.getValue();
        propertyRef1 = property1;
        propertyRef2 = property2;
    }


    protected Property<T> getProperty1() {
        return propertyRef1;
    }


    protected Property<T> getProperty2() {
        return propertyRef2;
    }

    @Override
    public void invalidated(Observable sourceProperty) {
        if (!updating) {
            final Property<T> property1 = propertyRef1;
            final Property<T> property2 = propertyRef2;
            if ((property1 == null) || (property2 == null)) {
                if (property1 != null) {
                    property1.removeListener(this);
                }
                if (property2 != null) {
                    property2.removeListener(this);
                }
            } else {
                try {
                    updating = true;
                    if (property1 == sourceProperty) {
                        T newValue = property1.getValue();
                        property2.setValue(newValue);
                        oldValue = newValue;
                    } else {
                        T newValue = property2.getValue();
                        property1.setValue(newValue);
                        oldValue = newValue;
                    }
                } catch (RuntimeException e) {
                    try {
                        if (property1 == sourceProperty) {
                            property1.setValue(oldValue);
                        } else {
                            property2.setValue(oldValue);
                        }
                    } catch (Exception e2) {
                        e2.addSuppressed(e);
                        unbind(property1, property2);
                        throw new RuntimeException(
                                "StrongBidirectionalBinding binding failed together with an attempt"
                                        + " to restore the source property to the previous value."
                                        + " Removing the bidirectional binding from properties " +
                                        property1 + " and " + property2, e2);
                    }
                    throw new RuntimeException(
                            "StrongBidirectionalBinding binding failed, setting to the previous value", e);
                } finally {
                    updating = false;
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StrongBidirectionalBinding<?> that = (StrongBidirectionalBinding<?>) o;
        return updating == that.updating && Objects.equals(propertyRef1, that.propertyRef1) && Objects.equals(propertyRef2, that.propertyRef2) && Objects.equals(oldValue, that.oldValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyRef1, propertyRef2, oldValue, updating);
    }

    public static <T> void unbind(Property<T> property1, Property<T> property2) {
        final @NonNull StrongBidirectionalBinding<T> binding = new StrongBidirectionalBinding<>(property1, property2);
        property1.removeListener(binding);
        property2.removeListener(binding);
    }

    public static <T> StrongBidirectionalBinding<T> bind(Property<T> property1, Property<T> property2) {
        final @NonNull StrongBidirectionalBinding<T> binding = new StrongBidirectionalBinding<>(property1, property2);
        property1.setValue(property2.getValue());
        property1.addListener(binding);
        property2.addListener(binding);
        return binding;
    }
}